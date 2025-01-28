package org.kjob.nameserver.module;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;

@Getter
@AllArgsConstructor
public class WorkerSubscribeSyncInfo extends SyncInfo{
    String workerIpAddress;
    String appName;
    long scheduleTime;
    String serverIpAddress;
}
