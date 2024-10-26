package org.kjob.worker;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.domain.WorkerAppInfo;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.common.grpc.RpcInitializer;
import org.kjob.worker.common.executor.ExecutorManager;
import org.kjob.worker.core.discover.KJobServerDiscoverService;
import org.kjob.worker.core.schedule.WorkerHealthReporter;
import org.kjob.worker.processor.KJobProcessorLoader;
import org.kjob.worker.processor.ProcessorLoader;
import org.kjob.worker.processor.factory.BuiltInDefaultProcessorFactory;
import org.kjob.worker.processor.factory.ProcessorFactory;
import org.kjob.worker.subscribe.WorkerSubscribeStarter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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
        RpcInitializer rpcInitializer = new RpcInitializer(config.getServerPort(),config.getPort(),config.getServerAddress(),config.getNameServerAddress());
        rpcInitializer.initRpcStrategies();
        rpcInitializer.initRpcServer(config);

        KJobServerDiscoverService kJobServerDiscoverService = new KJobServerDiscoverService(config);

        try{
            // subscribe to nameServer
            WorkerSubscribeStarter.start(config.getAppName());

            // get appId
            kJobServerDiscoverService.assertApp();

            // init ThreadPool
            ExecutorManager.initExecutorManager();

            // init processorLoader for handler task
            ProcessorLoader processorLoader = buildProcessorLoader();
            KJobWorkerConfig.setProcessorLoader(processorLoader);

            // connect server
            kJobServerDiscoverService.heartbeatCheck(ExecutorManager.getHeartbeatExecutor());

            // init health reporter
            ExecutorManager.getHealthReportExecutor().scheduleAtFixedRate(new WorkerHealthReporter(kJobServerDiscoverService, config), 0, config.getHealthReportInterval(), TimeUnit.SECONDS);

        } catch (Exception e){
            log.error("[kJob] start error");
        }


    }
    private ProcessorLoader buildProcessorLoader() {
        List<ProcessorFactory> customPF = Optional.ofNullable(config.getProcessorFactoryList()).orElse(Collections.emptyList());
        List<ProcessorFactory> finalPF = Lists.newArrayList(customPF);

        finalPF.add(new BuiltInDefaultProcessorFactory());

        return new KJobProcessorLoader(finalPF);
    }

    public void destroy() {
        ExecutorManager.shutdown();
    }
}
