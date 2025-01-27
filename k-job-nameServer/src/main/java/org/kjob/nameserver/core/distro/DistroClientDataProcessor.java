package org.kjob.nameserver.core.distro;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.common.utils.net.MyNetUtil;
import org.kjob.nameserver.config.KJobNameServerConfig;
import org.kjob.nameserver.core.GrpcClient;
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

    // 处理注册请求（外部调用）
    public void handleRegister(String ip) {
        if (isResponsibleNode(ip)) {
            HashMap<String, Long> map = new HashMap<>();
            map.put(ip, 0L);
            syncNodeInfo(map, RemoteConstant.INCREMENTAL_ADD_SYNC);
        } else {
            // 转发到责任节点
            String targetNode = clusterNodes.get(Math.abs(ip.hashCode()) % clusterNodes.size());
            //restTemplate.postForObject(targetNode + "/register", Map.of("serviceName", serviceName, "ip", ip), Void.class);
        }
    }

    // 同步增量或者全量的客户端Ip的数据到其他节点
    private void syncNodeInfo(HashMap<String, Long> map, String operation) {
        for (String node : clusterNodes) {
            if (!node.equals(curServerIp)) { // 不发给自身
                grpcClient.SyncNodeInfo(map, node, operation);
            }
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
