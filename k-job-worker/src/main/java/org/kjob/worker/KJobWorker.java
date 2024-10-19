package org.kjob.worker;

import lombok.extern.slf4j.Slf4j;
import org.kjob.common.domain.WorkerAppInfo;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.common.grpc.RpcInitializer;
import org.kjob.worker.core.executor.ExecutorManager;
import org.kjob.worker.transport.KJobServerDiscoverService;
import org.kjob.worker.transport.ServerDiscoverService;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

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
            kJobServerDiscoverService.heartbeatCheck(executorManager.getHeartbeatCheckExecutor());



        } catch (Exception e){
            log.error("[kJob] start error");
        }


    }

    public void destroy() {
    }
}
