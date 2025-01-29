package org.kjob.nameserver.module.sync;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerRegisterSyncInfo extends SyncInfo{
    public String scheduleServerIp;
}
