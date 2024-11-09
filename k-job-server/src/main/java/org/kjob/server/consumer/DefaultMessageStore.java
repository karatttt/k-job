package org.kjob.server.consumer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.kjob.remote.protos.MqCausa;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
/**
 * CONSUMER_QUEUE_FILE在消息队列中的设计是为了避免所有的消费者访问同一个commitLog
 * 同时也为了Topic的隔离
 * 本项目借鉴其设计，实际上可以用一个COMMIT_LOG_FILE也可以完成
 * question1：内存加载是一块整块的内存吗？
 * question2：所有的消费者访问同一个commitLog效率会低吗？
 */
@Slf4j
public class DefaultMessageStore {
    private static final String COMMIT_LOG_FILE;

    private static final String CONSUMER_QUEUE_FILE;

    static {
        try {
            String commitLogPath = Objects.requireNonNull(DefaultMessageStore.class.getClassLoader().getResource("message/commitlog.dat")).toURI().getPath();
            String consumerQueuePath = Objects.requireNonNull(DefaultMessageStore.class.getClassLoader().getResource("message/consumerQueue.dat")).toURI().getPath();
            // 修复路径格式
            COMMIT_LOG_FILE = commitLogPath.startsWith("/") ? commitLogPath.substring(1) : commitLogPath;
            CONSUMER_QUEUE_FILE = consumerQueuePath.startsWith("/") ? consumerQueuePath.substring(1) : consumerQueuePath;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    private static final AtomicLong commitLogCurPosition = new AtomicLong(0);
    private static final AtomicLong currentConsumerQueuePosition = new AtomicLong(0);
    private final AtomicLong consumerPosition = new AtomicLong(0); // 记录消费者在consumerQueue中的消费位置
    private static final long POLL_INTERVAL_MS = 10;
    private final AtomicLong lastProcessedOffset = new AtomicLong(0);
    private MappedByteBuffer commitLogBuffer;  // 映射到内存的commitlog文件
    private MappedByteBuffer consumerQueueBuffer; // 映射到内存的consumerQueue文件
    private Consumer consumer;
    ThreadPoolExecutor consumerthreadPoolExecutor;

    // 启动线程监视commitlog并写入consumerQueue
    public void startWatcher(Consumer consumer) {
        // 在启动时，将整个文件映射到内存中
        try {
            mapFilesToMemory();
        } catch (IOException e) {
            log.error("[DefaultMessageStore] mapFilesToMemory error");
            return;
        }
        // 从commitLog中拉取数据到ConsumerQueue
        Thread watcherThread = new Thread(() -> {
            while (true) {
                try {
                    checkAndWriteMessages();
                    Thread.sleep(POLL_INTERVAL_MS); // 轮询间隔
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        // 守护线程
        watcherThread.setDaemon(true);
        watcherThread.start();

        // 分派消息给消费者
        this.consumer = consumer;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                consumeMessages();
            }
        }, 3000L, 3000L);

        // 消费者线程池
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        ThreadFactory consumerThreadPoolFactory = new ThreadFactoryBuilder().setNameFormat("kjob-consumer-%d").build();
        consumerthreadPoolExecutor = new ThreadPoolExecutor(availableProcessors * 10, availableProcessors * 10, 120L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>((1024 * 2), true), consumerThreadPoolFactory, new ThreadPoolExecutor.AbortPolicy());

    }

    // 映射文件到内存
    private void mapFilesToMemory() throws IOException {
//        long commitLogSize = 1024L * 1024L * 1024L; // 1GB
//        long consumerQueueSize = 1024L * 1024L * 512L; // 0.5GB
        long commitLogSize = 1024L * 1024L; // 1M
        long consumerQueueSize = 1024L * 512L; // 0.5M

        try (FileChannel commitLogChannel = FileChannel.open(Paths.get(COMMIT_LOG_FILE), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            // 如果文件小于 1GB，则调整大小
            if (commitLogChannel.size() < commitLogSize) {
                commitLogChannel.truncate(commitLogSize); // 扩展文件至 1GB
            }
            commitLogBuffer = commitLogChannel.map(FileChannel.MapMode.READ_WRITE, 0, commitLogSize);
        }
        try (FileChannel consumerQueueChannel = FileChannel.open(Paths.get(CONSUMER_QUEUE_FILE), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            if (consumerQueueChannel.size() < consumerQueueSize) {
                consumerQueueChannel.truncate(consumerQueueSize); // 扩展文件至 1GB
            }
            consumerQueueBuffer = consumerQueueChannel.map(FileChannel.MapMode.READ_WRITE, 0, consumerQueueSize);
        }
    }
    // 检查commitlog中的新消息并写入consumerQueue
    private void checkAndWriteMessages() {
        long startOffset = lastProcessedOffset.get();
        long commitLogSize = commitLogBuffer.limit();
        if (startOffset >= commitLogSize) {
            return; // 没有新数据可读
        }
        // 从commitlog中读取消息
        while (startOffset < commitLogCurPosition.get()) {
            int messageSize = commitLogBuffer.getInt((int) startOffset);  // 前4个字节是消息大小
            byte[] messageBytes = new byte[messageSize];
            commitLogBuffer.position((int) startOffset + 4); // 跳过消息大小部分
            commitLogBuffer.get(messageBytes); // 读取消息内容
            // 将消息的大小和偏移量写入consumerQueue
            writeToConsumerQueue(messageSize, startOffset);
            // 更新最后处理的偏移量
            startOffset += 4 + messageSize; // 4字节为消息大小 + 实际消息大小
        }
        // 读取完毕后更新最后处理的偏移量
        lastProcessedOffset.set(startOffset);
    }

    // 将消息的大小和偏移量写入consumerQueue
    private void writeToConsumerQueue(int messageSize, long offset) {
        // 确保有足够的空间来写入消息的大小和偏移量
        consumerQueueBuffer.putInt(messageSize); // 4字节表示消息大小
        consumerQueueBuffer.putLong(offset);      // 8字节表示消息的偏移量
        consumerQueueBuffer.force();
        currentConsumerQueuePosition.set(consumerQueueBuffer.position());
    }

    // 向commitLog文件写入消息
    public void writeToCommitLog(MqCausa.Message message) {
        byte[] messageBytes = message.toByteArray();
        int messageSize = messageBytes.length;
        // 将消息的大小（4字节）和消息内容（messageBytes）写入commitLogBuffer
        commitLogBuffer.putInt(messageSize);  // 4字节表示消息大小
        commitLogBuffer.put(messageBytes);    // 消息内容
        commitLogCurPosition.addAndGet(4 + messageSize);
        // 刷新到磁盘
        commitLogBuffer.force();
    }

    // 消费者读取consumerQueue中的消息
    public void consumeMessages() {
        long messageNum = 0L;
        long currentConsumerPosition = consumerPosition.get();
        while (currentConsumerPosition < currentConsumerQueuePosition.get()) {
            // 如果剩余数据不足以表示完整的消息条目（4字节大小 + 8字节偏移量），则退出循环
            if (currentConsumerQueuePosition.get() - currentConsumerPosition < 12) break;
            int messageSize = consumerQueueBuffer.getInt((int) currentConsumerPosition);
            long messageOffset = consumerQueueBuffer.getLong((int) currentConsumerPosition + 4);
            System.out.println("Consuming message of size: " + messageSize + ", at offset: " + messageOffset);

            // 根据 offset 和 size 从 commitLog 中读取消息内容
            byte[] messageBytes = new byte[messageSize];
            commitLogBuffer.position((int) (messageOffset + 4)); // 设置位置到指定偏移量
            commitLogBuffer.get(messageBytes, 0, messageSize); // 读取消息内容

            // 处理消息
            try {
                MqCausa.Message message1 = MqCausa.Message.parseFrom(messageBytes);
                System.out.println("Message content: " + message1.getCreateJobReq().getJobId());
                consumerthreadPoolExecutor.execute(() -> consumer.consume(message1));
            } catch (InvalidProtocolBufferException e) {
                log.error("[DefaultMessageStore] invalid msg");
            }
            // 更新消费者位置
            currentConsumerPosition += 12;

            // 防止线程池OOM
            if(messageNum++ > 32){
                break;
            }
        }
        consumerPosition.set(currentConsumerPosition);
    }
//    public static void main(String[] args) throws InterruptedException {
//        DefaultMessageStore watcher = new DefaultMessageStore();
//        watcher.startWatcher(); // 启动监视commitlog的线程
//
//        // 模拟向commitlog中添加一些消息（这通常由生产者完成）
//        for (int i = 0; i < 5; i++) {
//            // 向commitlog中写入消息
//            MqCausa.CreateJobReq build = MqCausa.CreateJobReq.newBuilder().setJobId(12).setJobName("dsdsd").build();
//            MqCausa.Message build1 = MqCausa.Message.newBuilder().setMessageType(MqCausa.MessageType.JOB_CREATE)
//                    .setCreateJobReq(build).build();
//            watcher.writeToCommitLog(build1);
//
//            try {
//                Thread.sleep(100); // 模拟消息添加之间的延迟
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        // 保持主线程运行，以便观察commitlog的监视过程
//        Thread.sleep(5000);
//        watcher.consumeMessages();
//    }
}
