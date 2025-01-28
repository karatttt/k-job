package org.kjob.nameserver.core;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.kjob.nameserver.module.ReBalanceInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServerIpAddressManager {
    @Value("${kjob.name-server.max-worker-num}")
    private int maxWorkerNum;
    @Getter
    private  Set<String> serverAddressSet = new HashSet<>();
    private  Set<String> workerIpAddressSet = new HashSet<>();
    /**
     * for split group
     */
    private  Map<String, Integer> appName2WorkerNumMap = Maps.newHashMap();
    /**
     * for dynamic change group
     */
    private  Map<String, Long> serverAddress2ScheduleTimesMap = Maps.newHashMap();

    public void add2ServerAddressSet(String serverIpAddress) {
        serverAddressSet.add(serverIpAddress);
    }
    public void removeServerAddress(String serverIpAddress) {
        serverAddressSet.remove(serverIpAddress);
    }
    public void addScheduleTimes(String serverIpAddress, long scheduleTime) {
        if(!serverIpAddress.isEmpty()) {
            serverAddress2ScheduleTimesMap.put(serverIpAddress, serverAddress2ScheduleTimesMap.getOrDefault(serverIpAddress, 0L) + scheduleTime);
        }
    }
    public void addAppName2WorkerNumMap(String workerIpAddress, String appName){
        if(!workerIpAddressSet.contains(workerIpAddress)) {
            workerIpAddressSet.add(workerIpAddress);
            appName2WorkerNumMap.put(appName, appName2WorkerNumMap.getOrDefault(appName, 0) + 1);
        }
    }
    public void cleanAppName2WorkerNumMap(String appName){
        if(appName2WorkerNumMap.containsKey(appName)){
            appName2WorkerNumMap.put(appName, appName2WorkerNumMap.get(appName) - 1);
        }
    }

    public void resetInfo(Set<String> serverAddressSet,
                          Set<String> workerIpAddressSet,
                          Map<String, Integer> appName2WorkerNumMap,
                          Map<String, Long> serverAddress2ScheduleTimesMap){
        this.serverAddressSet = serverAddressSet;
        this.workerIpAddressSet = workerIpAddressSet;
        this.appName2WorkerNumMap = appName2WorkerNumMap;
        this.serverAddress2ScheduleTimesMap = serverAddress2ScheduleTimesMap;
    }
    public ReBalanceInfo getServerAddressReBalanceList(String serverAddress, String appName) {
        // first req, serverAddress is empty
        if(serverAddress.isEmpty()){
            ReBalanceInfo reBalanceInfo = new ReBalanceInfo();
            reBalanceInfo.setSplit(false);
            reBalanceInfo.setServerIpList(new ArrayList<String>(serverAddressSet));
            reBalanceInfo.setSubAppName("");
            return reBalanceInfo;
        }
        ReBalanceInfo reBalanceInfo = new ReBalanceInfo();
        // get sorted scheduleTimes serverList
        List<String> newServerIpList = serverAddress2ScheduleTimesMap.keySet().stream().sorted(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (int) (serverAddress2ScheduleTimesMap.get(o1) - serverAddress2ScheduleTimesMap.get(o2));
            }
        }).collect(Collectors.toList());

        // see if split
        if(!appName2WorkerNumMap.isEmpty() && appName2WorkerNumMap.get(appName) > maxWorkerNum && appName2WorkerNumMap.get(appName) % maxWorkerNum == 1){
            // return new serverIpList
            reBalanceInfo.setSplit(true);
            reBalanceInfo.setChangeServer(false);
            reBalanceInfo.setServerIpList(newServerIpList);
            reBalanceInfo.setSubAppName(appName + ":" + appName2WorkerNumMap.size());
            return reBalanceInfo;
        }
        // see if need change server
        Long lestScheduleTimes = serverAddress2ScheduleTimesMap.get(newServerIpList.get(newServerIpList.size() - 1));
        Long comparedScheduleTimes = lestScheduleTimes == 0 ? 1 : lestScheduleTimes;
        if(serverAddress2ScheduleTimesMap.get(serverAddress) / comparedScheduleTimes > 2){
            reBalanceInfo.setSplit(false);
            reBalanceInfo.setChangeServer(true);
            // first server is target lest scheduleTimes server
            reBalanceInfo.setServerIpList(newServerIpList);
            reBalanceInfo.setSubAppName("");
            return reBalanceInfo;
        }
        // return default list
        reBalanceInfo.setSplit(false);
        reBalanceInfo.setServerIpList(new ArrayList<String>(serverAddressSet));
        reBalanceInfo.setSubAppName("");
        return reBalanceInfo;

    }
}
