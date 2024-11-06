package org.kjob.nameserver.balance;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.kjob.nameserver.module.ReBalanceInfo;
import org.kjob.remote.protos.RegisterCausa;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServerIpAddressManagerService {
    @Value("${kjob.name-server.max-worker-num}")
    private int maxWorkerNum;
    @Getter
    private Set<String> serverIpAddressSet = new HashSet<>();
    private Set<String> workerIpAddressSet = new HashSet<>();

    // for split group
    private final Map<String, Integer> appName2WorkerNumMap = Maps.newHashMap();

//    private final Map<String, Integer> serverIp2ConnectNumMap = Maps.newHashMap();

    // for dynamic change group
    private final Map<String, Long> serverIp2ScheduleTimesMap = Maps.newHashMap();
    private  int serverIpCount = 0;


    public  void add2ServerIpAddressSet(RegisterCausa.ServerRegisterReporter req) {
        serverIpAddressSet.add(req.getServerIpAddress());
//        serverIp2ConnectNumMap.put(req.getServerIpAddress(), 0);
        serverIpCount++;
    }
    public void addScheduleTimes(String serverIpAddress, long scheduleTime) {
        if(!serverIpAddress.isEmpty()) {
            serverIp2ScheduleTimesMap.put(serverIpAddress, serverIp2ScheduleTimesMap.getOrDefault(serverIpAddress, 0L) + scheduleTime);
        }
    }
    public void addAppName2WorkerNumMap(String workerIpAddress, String appName){
        if(!workerIpAddressSet.contains(workerIpAddress)) {
            workerIpAddressSet.add(workerIpAddress);
            appName2WorkerNumMap.put(appName, appName2WorkerNumMap.getOrDefault(appName, 0) + 1);
        }
    }

    public ReBalanceInfo getServerIpAddressReBalanceList(String serverAddress, String appName) {
        ReBalanceInfo reBalanceInfo = new ReBalanceInfo();
        // get sorted scheduleTimes serverList
        List<String> newServerIpList = serverIp2ScheduleTimesMap.keySet().stream().sorted(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (int) (serverIp2ScheduleTimesMap.get(o1) - serverIp2ScheduleTimesMap.get(o2));
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
        Long lestScheduleTimes = serverIp2ScheduleTimesMap.get(newServerIpList.get(newServerIpList.size() - 1));
        Long comparedScheduleTimes = lestScheduleTimes == 0 ? 1 : lestScheduleTimes;
        if(serverIp2ScheduleTimesMap.get(serverAddress) / comparedScheduleTimes == 1){
            reBalanceInfo.setSplit(false);
            reBalanceInfo.setChangeServer(true);
            // first server is target lest scheduleTimes server
            reBalanceInfo.setServerIpList(newServerIpList);
            reBalanceInfo.setSubAppName("");
            return reBalanceInfo;
        }
        // return default list
        reBalanceInfo.setSplit(false);
        reBalanceInfo.setServerIpList(new ArrayList<String>(serverIpAddressSet));
        reBalanceInfo.setSubAppName("");
        return reBalanceInfo;

    }


}
