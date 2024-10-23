package org.kjob.worker.common.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import org.kjob.worker.common.KJobWorkerConfig;

import java.util.concurrent.*;

public class ExecutorManager
{

    @Getter
    private static  ScheduledExecutorService heartbeatExecutor = null;
    @Getter
    private static  ScheduledExecutorService lightweightTaskStatusCheckExecutor = null;
    @Getter
    private static  ExecutorService lightweightTaskExecutorService = null;
    public static void initExecutorManager(){

        final int availableProcessors = Runtime.getRuntime().availableProcessors();

        ThreadFactory coreThreadFactory = new ThreadFactoryBuilder().setNameFormat("kjob-worker-core-%d").build();
        heartbeatExecutor =  new ScheduledThreadPoolExecutor(3, coreThreadFactory);



        ThreadFactory lightTaskReportFactory = new ThreadFactoryBuilder().setNameFormat("powerjob-worker-light-task-status-check-%d").build();
        // 都是 io 密集型任务
        lightweightTaskStatusCheckExecutor =  new ScheduledThreadPoolExecutor(availableProcessors * 10, lightTaskReportFactory);

        ThreadFactory lightTaskExecuteFactory = new ThreadFactoryBuilder().setNameFormat("powerjob-worker-light-task-execute-%d").build();
        // 大部分任务都是 io 密集型
        lightweightTaskExecutorService = new ThreadPoolExecutor(availableProcessors * 10,availableProcessors * 10, 120L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>((1024 * 2),true), lightTaskExecuteFactory, new ThreadPoolExecutor.AbortPolicy());

    }

    public static void shutdown(){
        heartbeatExecutor.shutdownNow();
        lightweightTaskExecutorService.shutdown();
        lightweightTaskStatusCheckExecutor.shutdown();
    }


}
