package org.kjob.worker;

import lombok.extern.slf4j.Slf4j;
import org.kjob.common.domain.WorkerAppInfo;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.common.grpc.RpcInitializer;
import org.kjob.worker.core.executor.ExecutorManager;
import org.kjob.worker.remote.discover.KJobServerDiscoverService;
import org.kjob.worker.remote.schedule.WorkerHealthReporter;

import java.util.concurrent.TimeUnit;

@Slf4j
public class KJobWorker {

    KJobWorkerConfig config;
    public KJobWorker(KJobWorkerConfig config) {
        this.config = config;
    }

    public void init() {


        log.info("[KJob] starting ...");

        // init rpc
        RpcInitializer rpcInitializer = new RpcInitializer(config.getPort(),config.getServerAddress());
        rpcInitializer.initRpcStrategies();

        KJobServerDiscoverService kJobServerDiscoverService = new KJobServerDiscoverService(config);


        try{
            // get appId
            WorkerAppInfo workerAppInfo = kJobServerDiscoverService.assertApp();
            System.out.println(workerAppInfo.getAppId());

            // init ThreadPool
            final ExecutorManager executorManager = new ExecutorManager(config);

            // connect server
            kJobServerDiscoverService.heartbeatCheck(executorManager.getHeartbeatExecutor());

            // init health reporter
            executorManager.getHeartbeatExecutor().scheduleAtFixedRate(new WorkerHealthReporter(kJobServerDiscoverService, config), 0, config.getHealthReportInterval(), TimeUnit.SECONDS);




        } catch (Exception e){
            log.error("[kJob] start error");
        }


    }

    public void destroy() {
    }
}
