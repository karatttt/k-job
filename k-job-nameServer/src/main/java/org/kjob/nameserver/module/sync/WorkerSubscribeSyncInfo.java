package org.kjob.nameserver.module.sync;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkerSubscribeSyncInfo extends SyncInfo{
    String workerIpAddress;
    String appName;
    long scheduleTime;
    String serverIpAddress;
}
