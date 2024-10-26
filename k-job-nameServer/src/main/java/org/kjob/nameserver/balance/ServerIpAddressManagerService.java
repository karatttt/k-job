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
    private final Map<String, Integer> appName2WorkerNumMap = Maps.newHashMap();
    private final Map<String, Integer> serverIp2ConnectNumMap = Maps.newHashMap();
    private  int serverIpCount = 0;


    public  void add2ServerIpAddressSet(RegisterCausa.ServerRegisterReporter req) {
        serverIpAddressSet.add(req.getServerIpAddress());
        serverIp2ConnectNumMap.put(req.getServerIpAddress(), 0);
        serverIpCount++;
    }

    public void addAppName2WorkerNumMap(String workerIpAddress, String appName){
        if(!workerIpAddressSet.contains(workerIpAddress)) {
            workerIpAddressSet.add(workerIpAddress);
            appName2WorkerNumMap.put(appName, appName2WorkerNumMap.getOrDefault(appName, 0) + 1);
        }
    }

    public ReBalanceInfo getServerIpAddressReBalanceList(String appName) {
        ReBalanceInfo reBalanceInfo = new ReBalanceInfo();
        if(!appName2WorkerNumMap.isEmpty() && appName2WorkerNumMap.get(appName) == 1){
            // return new serverIpList
            List<String> newServerIpList;
            newServerIpList = serverIp2ConnectNumMap.keySet().stream().sorted(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return serverIp2ConnectNumMap.get(o1) - serverIp2ConnectNumMap.get(o2);
                }
            }).collect(Collectors.toList());
            serverIp2ConnectNumMap.put(newServerIpList.get(0), serverIp2ConnectNumMap.get(newServerIpList.get(0) + maxWorkerNum));
            reBalanceInfo.setSplit(true);
            reBalanceInfo.setServerIpList(newServerIpList);
//            reBalanceInfo.setSubAppName(appName + ":" + appName2WorkerNumMap.size() / maxWorkerNum);
            reBalanceInfo.setSubAppName(appName + ":" + appName2WorkerNumMap.size());

            return reBalanceInfo;
        }
         // return default list
        reBalanceInfo.setSplit(false);
        reBalanceInfo.setServerIpList(new ArrayList<String>(serverIpAddressSet));
        reBalanceInfo.setSubAppName("");
        return reBalanceInfo;

    }
}
