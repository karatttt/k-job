package org.kjob.nameserver.core.distro;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.common.utils.net.MyNetUtil;
import org.kjob.nameserver.config.KJobNameServerConfig;
import org.kjob.nameserver.core.GrpcClient;
import org.kjob.nameserver.module.SyncInfo;
import org.kjob.remote.protos.DistroCausa;
import org.springframework.stereotype.Component;

import javax.imageio.spi.ServiceRegistry;
import java.rmi.Remote;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
@Component

public class DistroClientDataProcessor {
    private final String curServerIp;
    private final List<String> clusterNodes;
    private final GrpcClient grpcClient;

    public DistroClientDataProcessor(KJobNameServerConfig kJobNameServerConfig, GrpcClient grpcClient) {
        this.curServerIp = MyNetUtil.address;
        this.clusterNodes = kJobNameServerConfig.getServerAddressList();
        this.grpcClient = grpcClient;
    }

    // 判断是否为责任节点（基于服务ip的哈希取模）
    private boolean isResponsibleNode(String serviceName) {
        int hash = Math.abs(serviceName.hashCode());
        int index = hash % clusterNodes.size();
        return clusterNodes.get(index).equals(curServerIp); // 假设 nodeId 是地址的一部分
    }

    /**
     *
     * @param syncInfo scheduleServer or worker syncInfo, bind to a nameServer
     * @param operation
     */
    public void handleSync(SyncInfo syncInfo, String operation) {
        if (isResponsibleNode(syncInfo.getClientIp())) {
            syncNodeInfoToOthers(syncInfo, operation);
        } else {
            // 同步的工作转发到责任节点
            String targetNode = clusterNodes.get(Math.abs(syncInfo.getClientIp().hashCode()) % clusterNodes.size());
            grpcClient.redirectSyncInfo(syncInfo, targetNode, operation);
        }
    }

    /**
     * send info to other nodes
     * @param syncInfo
     * @param operation
     */
    private void syncNodeInfoToOthers(SyncInfo syncInfo, String operation) {
        for (String target : clusterNodes) {
            if (!target.equals(curServerIp)) { // 不发给自身
                grpcClient.sendSyncInfo(syncInfo, target, operation);
            }
        }
    }
    public boolean syncNodeInfoToOthers(DistroCausa.SyncNodeInfoReq syncInfo) {
        try {
            for (String target : clusterNodes) {
                if (!target.equals(curServerIp)) { // 不发给自身
                    grpcClient.sendSyncInfo(syncInfo, target);
                }
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }

    // 接收其他节点的同步请求
//    @PostMapping("/sync")
//    public void handleSync(@RequestBody Map<String, String> payload) {
//        String serviceName = payload.get("serviceName");
//        String ip = payload.get("ip");
//        String operation = payload.get("operation");
//
//        if ("register".equals(operation)) {
//            registry.register(serviceName, ip);
//        } else if ("unregister".equals(operation)) {
//            registry.unregister(serviceName, ip);
//        }
//    }
}
