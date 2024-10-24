package org.kjob.worker.core.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kjob.common.domain.WorkerHeartbeat;
import org.kjob.common.enhance.SafeRunnable;
import org.kjob.common.module.SystemMetrics;
import org.kjob.common.utils.net.MyNetUtil;
import org.kjob.remote.protos.ScheduleCausa;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.strategies.StrategyCaller;
import org.kjob.worker.common.utils.SystemInfoUtils;
import org.kjob.worker.core.discover.ServerDiscoverService;


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
        heartbeat.setServerIpAddress(currentServer);
        heartbeat.setSystemMetrics(systemMetrics);
        heartbeat.setAppName(config.getAppName());
        heartbeat.setAppId(serverDiscoverService.getCurrentAppId());
        heartbeat.setHeartbeatTime(System.currentTimeMillis());
        heartbeat.setWorkerAddress(MyNetUtil.address);
        heartbeat.setClient("KingPenguin");
//        heartbeat.setTag(config.getWorkerConfig().getTag());

        // 上报 Tracker 数量
//        heartbeat.setLightTaskTrackerNum(LightTaskTrackerManager.currentTaskTrackerSize());
////        heartbeat.setHeavyTaskTrackerNum(HeavyTaskTrackerManager.currentTaskTrackerSize());
//        // 是否超载
//        if (config.getMaxLightweightTaskNum() <= LightTaskTrackerManager.currentTaskTrackerSize() || config.getMaxHeavyweightTaskNum() <= HeavyTaskTrackerManager.currentTaskTrackerSize()){
//            heartbeat.setOverload(true);
//        }

        // 发送请求
        if (StringUtils.isEmpty(currentServer)) {
            return;
        }
        // log
        log.info("[WorkerHealthReporter] report health status,appId:{},appName:{},isOverload:{},maxLightweightTaskNum:{},currentLightweightTaskNum:{},maxHeavyweightTaskNum:{}",
                heartbeat.getAppId(),
                heartbeat.getAppName(),
                heartbeat.isOverload(),
                config.getMaxLightweightTaskNum(),
                heartbeat.getLightTaskTrackerNum(),
                config.getMaxHeavyweightTaskNum()
        );

        ScheduleCausa.SystemMetrics builder0 = ScheduleCausa.SystemMetrics.newBuilder()
                .setCpuLoad(heartbeat.getSystemMetrics().getCpuLoad())
                .setCpuProcessors(heartbeat.getSystemMetrics().getCpuProcessors())
                .setDiskTotal(heartbeat.getSystemMetrics().getDiskTotal())
                .setDiskUsage(heartbeat.getSystemMetrics().getDiskUsage())
                .setScore(heartbeat.getSystemMetrics().getScore())
                .setJvmMaxMemory(heartbeat.getSystemMetrics().getJvmMaxMemory())
                .setJvmUsedMemory(heartbeat.getSystemMetrics().getJvmUsedMemory())
                .setJvmMemoryUsage(heartbeat.getSystemMetrics().getJvmMemoryUsage()).build();
        ScheduleCausa.WorkerHeartbeat builder = ScheduleCausa.WorkerHeartbeat.newBuilder()
                .setServerIpAddress(currentServer)
                .setAppId(heartbeat.getAppId())
                .setAppName(heartbeat.getAppName())
                .setHeartbeatTime(heartbeat.getHeartbeatTime())
                .setClient(heartbeat.getClient())
                .setIsOverload(heartbeat.isOverload())
                .setWorkerAddress(heartbeat.getWorkerAddress())
                .setSystemMetrics(builder0)
                .build();

        StrategyCaller.call(TransportTypeEnum.HEARTBEAT_HEALTH_REPORT, builder);
    }
}
