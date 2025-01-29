package org.kjob.nameserver.core.distro;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.domain.WorkerHeartbeat;
import org.kjob.nameserver.module.ClientHeartbeat;
import org.kjob.nameserver.module.ClientNodeInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * hold client status through heartbeat between server and client
 */
@Component
@Slf4j
public class ClientStatusManager {
    ClientStatusManager(){
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(this::cleanClientNode, 1000, 5000, TimeUnit.MILLISECONDS);
    }
    Map<String, ClientNodeInfo> address2ClientNodeMap = Maps.newConcurrentMap();

    public void updateStatus(ClientHeartbeat heartbeat) {
        String clientIp = heartbeat.getIp();
        String type = heartbeat.getClientType();
        long heartbeatTime = heartbeat.getHeartbeatTime();

        ClientNodeInfo c = address2ClientNodeMap.computeIfAbsent(clientIp + ":" + type, ignore -> {
            ClientNodeInfo info = new ClientNodeInfo();
            info.refresh(heartbeat);
            return info;
        });
        long oldTime = c.getLastActiveTime();
        if (heartbeatTime < oldTime) {
            log.warn("[ClientStatusManager] receive the expired heartbeat from {}, serverTime: {}, heartTime: {}", clientIp, System.currentTimeMillis(), heartbeat.getHeartbeatTime());
            return;
        }
        c.refresh(heartbeat);
    }

    private void cleanClientNode(){
        List<String> timeoutAddress = Lists.newLinkedList();
        address2ClientNodeMap.forEach((addr, clientInfo) -> {
            if (clientInfo.timeout()) {
                timeoutAddress.add(addr);
            }
        });
        if (!timeoutAddress.isEmpty()) {
            log.info("[ClientStatusManager] detective timeout client({}), try to release their infos.", timeoutAddress);
            timeoutAddress.forEach(address2ClientNodeMap::remove);
        }
    }
}
