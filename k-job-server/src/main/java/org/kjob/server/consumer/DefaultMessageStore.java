package org.kjob.server.consumer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
/**
 * CONSUMER_QUEUE_FILE在消息队列中的设计是为了避免所有的消费者访问同一个commitLog
 * 同时也为了Topic的隔离
 * 本项目借鉴其设计，实际上可以用一个COMMIT_LOG_FILE也可以完成
 * question1：内存加载是一块整块的内存吗？
 * question2：所有的消费者访问同一个commitLog效率会低吗？
 */

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
            System.out.println(COMMIT_LOG_FILE);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    private static final AtomicLong commitLogCurPosition = new AtomicLong(0);

    private static final long POLL_INTERVAL_MS = 1000; // 每秒轮询一次
    private static final int BUFFER_SIZE = 1024; // 示例缓冲区大小，针对commitlog

    private AtomicLong lastProcessedOffset = new AtomicLong(0);
    private MappedByteBuffer commitLogBuffer;  // 映射到内存的commitlog文件
    private MappedByteBuffer consumerQueueBuffer; // 映射到内存的consumerQueue文件

    // 启动线程监视commitlog并写入consumerQueue
    public void startWatcher() {
        // 在启动时，将整个文件映射到内存中
        try {
            mapFilesToMemory();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

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
        watcherThread.setDaemon(true);
        watcherThread.start();
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
        int currentPosition = consumerQueueBuffer.position();

        // 确保有足够的空间来写入消息的大小和偏移量
        consumerQueueBuffer.putInt(messageSize); // 4字节表示消息大小
        consumerQueueBuffer.putLong(offset);      // 8字节表示消息的偏移量
//        consumerQueueBuffer.position(currentPosition); // 恢复原始位置以便继续操作
        consumerQueueBuffer.force();
    }

    // 向commitlog文件写入消息
    public void writeToCommitLog(String message) {
        byte[] messageBytes = message.getBytes();
        int messageSize = messageBytes.length;

        // 将消息的大小（4字节）和消息内容（messageBytes）写入commitLogBuffer
        commitLogBuffer.putInt(messageSize);  // 4字节表示消息大小
        commitLogBuffer.put(messageBytes);    // 消息内容
        commitLogCurPosition.addAndGet(4 + messageSize);
        // 刷新到磁盘
        commitLogBuffer.force();
    }

    public static void main(String[] args) {
        DefaultMessageStore watcher = new DefaultMessageStore();
        watcher.startWatcher(); // 启动监视commitlog的线程

        // 模拟向commitlog中添加一些消息（这通常由生产者完成）
        for (int i = 0; i < 5; i++) {
            // 向commitlog中写入消息
            watcher.writeToCommitLog("Message " + i);

            try {
                Thread.sleep(100); // 模拟消息添加之间的延迟
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 保持主线程运行，以便观察commitlog的监视过程
        try {
            Thread.sleep(100); // 监视commitlog 10秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
