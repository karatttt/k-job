package org.kjob.nameserver.core.distro;

import org.kjob.common.constant.RemoteConstant;
import org.kjob.common.utils.net.MyNetUtil;
import org.kjob.nameserver.config.KJobNameServerConfig;
import org.kjob.nameserver.core.GrpcClient;
import org.kjob.nameserver.core.ServerIpAddressManager;
import org.kjob.nameserver.module.sync.FullSyncInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

/**
 * dataCheck
 */
@Component
public class ClusterDataCheckService {
    @Value("${grpc.server.port}")
    private String port;
    private final List<String> clusterNodes;
    private final String curServerIp;
    private final ServerIpAddressManager serverIpAddressManager;
    private final GrpcClient grpcClient;
    private final Executor executor = Executors.newFixedThreadPool(10);

    public ClusterDataCheckService(ServerIpAddressManager serverIpAddressManager,
                                   KJobNameServerConfig kJobNameServerConfig,
                                   GrpcClient grpcClient) {
        this.curServerIp = MyNetUtil.address;
        this.grpcClient = grpcClient;
        this.clusterNodes = kJobNameServerConfig.getServerAddressList();
        this.serverIpAddressManager = serverIpAddressManager;

        // 启动定时心跳任务
        ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeat, 5, 10, TimeUnit.SECONDS);
    }
    // 发送心跳
    private void sendHeartbeat() {
        String checksum = serverIpAddressManager.calculateChecksum();
        FullSyncInfo info = serverIpAddressManager.getClientAllInfo();
        for (String node : clusterNodes) {
            if (!node.contains(curServerIp) && !node.equals(RemoteConstant.LOOPBACKIP + ":"+ port)) { // 不发给自身
                grpcClient.dataCheck(checksum, node, info, executor);
            }
        }
    }
}
