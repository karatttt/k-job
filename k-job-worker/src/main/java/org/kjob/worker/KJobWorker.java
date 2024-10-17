package org.kjob.worker;

import lombok.extern.slf4j.Slf4j;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.transport.KJobServerDiscoverService;
import org.kjob.worker.transport.ServerDiscoverService;

@Slf4j
public class KJobWorker {

    KJobWorkerConfig config;
    public KJobWorker(KJobWorkerConfig config) {
        this.config = config;
    }

    public void init() {

        log.info("[KJob] starting ...");
        // 第一次通讯，通过appName获取appId，即当前的分组
        KJobServerDiscoverService kJobServerDiscoverService = new KJobServerDiscoverService(config);
        //


    }

    public void destroy() {
    }
}
