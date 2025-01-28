package org.kjob.nameserver.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerRegisterSyncInfo extends SyncInfo{
    public String scheduleServerIp;
}
