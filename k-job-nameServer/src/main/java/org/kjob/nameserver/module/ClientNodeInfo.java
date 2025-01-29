package org.kjob.nameserver.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kjob.common.domain.WorkerHeartbeat;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientNodeInfo {
    private String type;
    private long lastActiveTime;
    private static final long CLIENT_TIMEOUT_MS = 60000;

    public void refresh(ClientHeartbeat clientHeartbeat) {
        lastActiveTime = clientHeartbeat.getHeartbeatTime();
    }

    public boolean timeout() {
        long timeout = System.currentTimeMillis() - lastActiveTime;
        return timeout > CLIENT_TIMEOUT_MS;
    }

}
