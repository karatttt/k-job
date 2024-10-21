package org.kjob.worker.remote.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kjob.common.domain.WorkerHeartbeat;
import org.kjob.common.enhance.SafeRunnable;
import org.kjob.common.module.SystemMetrics;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.common.utils.SystemInfoUtils;
import org.kjob.worker.remote.discover.KJobServerDiscoverService;
import org.kjob.worker.remote.discover.ServerDiscoverService;


/**
 * Worker健康度定时上报
 *
 * @author tjq
 * @since 2020/3/25
 */
@Slf4j
@RequiredArgsConstructor
public class WorkerHealthReporter extends SafeRunnable {

    private final ServerDiscoverService serverDiscoverService;
    private final KJobWorkerConfig config;
    @Override
    public void run0() {

        // 没有可用Server，无法上报
        String currentServer = serverDiscoverService.getCurrentServerAddress();
        if (StringUtils.isEmpty(currentServer)) {
            log.warn("[WorkerHealthReporter] no available server,fail to report health info!");
            return;
        }

        SystemMetrics systemMetrics;
        systemMetrics = SystemInfoUtils.getSystemMetrics();


        WorkerHeartbeat heartbeat = new WorkerHeartbeat();

        heartbeat.setSystemMetrics(systemMetrics);

        heartbeat.setAppName(config.getAppName());
        heartbeat.setAppId(serverDiscoverService.getCurrentAppId());
        heartbeat.setHeartbeatTime(System.currentTimeMillis());
//        heartbeat.setVersion(PowerJobWorkerVersion.getVersion());
//        heartbeat.setProtocol(config.getWorkerConfig().getProtocol().name());
        heartbeat.setClient("KingPenguin");
//        heartbeat.setTag(config.getWorkerConfig().getTag());

        // 上报 Tracker 数量
        heartbeat.setLightTaskTrackerNum(LightTaskTrackerManager.currentTaskTrackerSize());
//        heartbeat.setHeavyTaskTrackerNum(HeavyTaskTrackerManager.currentTaskTrackerSize());
        // 是否超载
        if (config.getMaxLightweightTaskNum() <= LightTaskTrackerManager.currentTaskTrackerSize() || config.getMaxHeavyweightTaskNum() <= HeavyTaskTrackerManager.currentTaskTrackerSize()){
            heartbeat.setOverload(true);
        }

        // 发送请求
        if (StringUtils.isEmpty(currentServer)) {
            return;
        }
        // log
        log.info("[WorkerHealthReporter] report health status,appId:{},appName:{},isOverload:{},maxLightweightTaskNum:{},currentLightweightTaskNum:{},maxHeavyweightTaskNum:{},currentHeavyweightTaskNum:{}" ,
                heartbeat.getAppId(),
                heartbeat.getAppName(),
                heartbeat.isOverload(),
                config.getMaxLightweightTaskNum(),
                heartbeat.getLightTaskTrackerNum(),
                config.getMaxHeavyweightTaskNum(),
                heartbeat.getHeavyTaskTrackerNum()
        );

        TransportUtils.reportWorkerHeartbeat(heartbeat, currentServer, config.getTransporter());
    }
}
