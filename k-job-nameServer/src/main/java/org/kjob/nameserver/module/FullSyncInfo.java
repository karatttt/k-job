package org.kjob.nameserver.module;

import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
@Getter
public class FullSyncInfo extends SyncInfo{
    private Set<String> serverAddressSet;
    private Set<String> workerIpAddressSet;
    /**
     * for split group
     */
    private Map<String, Integer> appName2WorkerNumMap;
    /**
     * for dynamic change group
     */
    private  Map<String, Long> serverAddress2ScheduleTimesMap;
}
