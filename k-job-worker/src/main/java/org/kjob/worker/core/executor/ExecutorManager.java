package org.kjob.worker.core.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.kjob.worker.common.KJobWorkerConfig;

import java.util.concurrent.*;

public class ExecutorManager
{

    private final ScheduledExecutorService heartbeatExecutor;
    public ExecutorManager(KJobWorkerConfig workerConfig){



        ThreadFactory coreThreadFactory = new ThreadFactoryBuilder().setNameFormat("kjob-worker-core-%d").build();
        heartbeatExecutor =  new ScheduledThreadPoolExecutor(3, coreThreadFactory);



//        ThreadFactory lightTaskExecuteFactory = new ThreadFactoryBuilder().setNameFormat("powerjob-worker-light-task-execute-%d").build();
//        // 大部分任务都是 io 密集型
//        lightweightTaskExecutorService = new ThreadPoolExecutor(availableProcessors * 10,availableProcessors * 10, 120L, TimeUnit.SECONDS,
//                new ArrayBlockingQueue<>((workerConfig.getMaxLightweightTaskNum() * 2),true), lightTaskExecuteFactory, new ThreadPoolExecutor.AbortPolicy());

    }

    public ScheduledExecutorService getHeartbeatExecutor(){
        return heartbeatExecutor;
    }
    public void shutdown(){
        heartbeatExecutor.shutdownNow();
    }




}
