package org.kjob.worker.core.discover;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kjob.common.domain.WorkerAppInfo;

import org.kjob.common.exception.KJobException;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.strategies.StrategyCaller;
import org.kjob.worker.subscribe.WorkerSubscribeManager;
import org.springframework.beans.BeanUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
public class KJobServerDiscoverService implements ServerDiscoverService{

    private final KJobWorkerConfig config;

    // only ip address ,no port
    private String currentIpAddress;

    private static int FAILED_COUNT = 0;

    private static final int MAX_FAILED_COUNT = 3;

    private Long appId;


    public KJobServerDiscoverService(KJobWorkerConfig config) {
        this.config = config;
    }


    @Override
    public WorkerAppInfo assertApp() {
        ServerDiscoverCausa.AppName builder = ServerDiscoverCausa.AppName.newBuilder()
                .setAppName(config.getAppName())
                .build();

        ServerDiscoverCausa.WorkInfo workInfo = (ServerDiscoverCausa.WorkInfo) StrategyCaller.call(TransportTypeEnum.ASSERT_APP, builder);
        WorkerAppInfo workerAppInfo = new WorkerAppInfo();
        BeanUtils.copyProperties(workInfo, workerAppInfo);
        appId = workInfo.getAppId();
        return workerAppInfo;
    }

    @Override
    public String getCurrentServerAddress() {
        return currentIpAddress;
    }

    @Override
    public Long getCurrentAppId() {
        return appId;
    }

    @Override
    public void heartbeatCheck(ScheduledExecutorService heartbeatCheckExecutor) {
        // each discovery connect no more than MAX_FAILED_COUNT
        setCurrentIpAddress(discovery());
        if (StringUtils.isEmpty(this.currentIpAddress)) {
            throw new KJobException("can't find any available server, this worker has been quarantined.");
        }
        // check server and update currentIpAddress, asserting already success , so scheduleAtFixedRate here
        heartbeatCheckExecutor.scheduleAtFixedRate(() -> {
                    try {
                        setCurrentIpAddress(discovery());
                        log.info("[KJObServerDiscovery] jump ip :{}", currentIpAddress);
                    } catch (Exception e) {
                        log.error("[KJObServerDiscovery] fail to discovery server!", e);
                    }
                }
                , 5, 5, TimeUnit.SECONDS);
    }

    private void setCurrentIpAddress(String serverIpAddress) {
        this.currentIpAddress = serverIpAddress;
        WorkerSubscribeManager.setCurrentServerIp(currentIpAddress);
    }

    private String discovery() {
        String result = null;

        // ask currentIpAddress
        if (!StringUtils.isEmpty(currentIpAddress)) {
            result = acquire(currentIpAddress);
        }
        // ask other server
        for (String httpServerAddress : config.getServerAddress()) {
            if (StringUtils.isEmpty(result)) {
                result = acquire(httpServerAddress);
            }else {
                break;
            }
        }
        if (StringUtils.isEmpty(result)) {
            log.warn("[KJObServerDiscovery] can't find any available server, this worker has been quarantined.");

            if (FAILED_COUNT++ > MAX_FAILED_COUNT) {
                // todo frequent job
                FAILED_COUNT = 0;
            }
            return null;
        } else {
            // 重置失败次数
            FAILED_COUNT = 0;
            log.debug("[KJObServerDiscovery] current server is {}.", result);
            return result;
        }
    }

    private String acquire(String currentIpAddress) {

        ServerDiscoverCausa.HeartbeatCheck build = ServerDiscoverCausa.HeartbeatCheck.newBuilder()
                .setCurrentServer(currentIpAddress)
                .setAppId(appId).build();
        ServerDiscoverCausa.AvailableServer availableServer = (ServerDiscoverCausa.AvailableServer) StrategyCaller.call(TransportTypeEnum.HEARTBEAT_CHECK, build);
        if(availableServer == null){
            return null;
        } else {
            return availableServer.getAvailableServer();
        }
    }
}
