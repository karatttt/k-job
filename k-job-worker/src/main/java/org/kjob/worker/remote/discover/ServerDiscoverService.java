package org.kjob.worker.remote.discover;

import org.kjob.common.domain.WorkerAppInfo;

import java.util.concurrent.ScheduledExecutorService;

public interface ServerDiscoverService {
    /**
     * 鉴权 & 附带信息下发
     * @return appInfo
     */
    WorkerAppInfo assertApp();

    /**
     * 获取当前的 server 地址
     * @return server 地址
     */
    String getCurrentServerAddress();
    Long getCurrentAppId();

    void heartbeatCheck(ScheduledExecutorService heartbeatCheckExecutor);
}
