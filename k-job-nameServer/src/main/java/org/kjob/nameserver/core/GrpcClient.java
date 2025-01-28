package org.kjob.nameserver.core;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.common.exception.KJobException;
import org.kjob.nameserver.config.KJobNameServerConfig;
import org.kjob.nameserver.core.distro.DistroClientDataProcessor;
import org.kjob.nameserver.module.*;
import org.kjob.remote.api.DistroGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.DistroCausa;
import org.kjob.remote.protos.RegisterCausa;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class GrpcClient {
    private final Map<String, DistroGrpc.DistroFutureStub> clusterFutureStubMap = new HashMap<>();
    private final Map<String, DistroGrpc.DistroBlockingStub> clusterBlockingStubMap = new HashMap<>();

    private final int RETRY_TIMES = 3;

    public GrpcClient(KJobNameServerConfig kJobNameServerConfig){
        for (String ip : kJobNameServerConfig.getServerAddressList()) {
            String[] split = ip.split(":");
            ManagedChannel channel = ManagedChannelBuilder.forAddress(split[0], Integer.parseInt(split[1])).build();
            DistroGrpc.DistroFutureStub distroFutureStub = DistroGrpc.newFutureStub(channel);
            DistroGrpc.DistroBlockingStub distroBlockingStub = DistroGrpc.newBlockingStub(channel);
            clusterFutureStubMap.put(ip, distroFutureStub);
            clusterBlockingStubMap.put(ip, distroBlockingStub);
        }
    }

    /**
     * send async
     * @param syncInfo
     * @param target
     * @param operation
     */
    public void sendSyncInfo(SyncInfo syncInfo, String target, String operation) {
        DistroCausa.SyncNodeInfoReq req= buildReq(syncInfo, operation);
        DistroGrpc.DistroFutureStub distroFutureStub = clusterFutureStubMap.get(target);
        distroFutureStub.syncNodeInfo(req);
    }
    public void sendSyncInfo(DistroCausa.SyncNodeInfoReq syncInfo, String target) {
        DistroGrpc.DistroFutureStub distroFutureStub = clusterFutureStubMap.get(target);
        distroFutureStub.syncNodeInfo(syncInfo);
    }

    /**
     * send sync and retry
     * @param syncInfo
     * @param targetNode
     * @param operation
     */
    public void redirectSyncInfo(SyncInfo syncInfo, String targetNode, String operation) {
        try {
            DistroCausa.SyncNodeInfoReq req = buildReq(syncInfo, operation);
            DistroGrpc.DistroBlockingStub distroFutureStub = clusterBlockingStubMap.get(targetNode);
            CommonCausa.Response response = distroFutureStub.syncNodeInfo(req);
            if (response.getCode() == RemoteConstant.SUCCESS) {
                return;
            }

            int retryTimes = RETRY_TIMES;
            while (retryTimes-- > 0) {
                if (response.getCode() != RemoteConstant.SUCCESS) {
                    redirectSyncInfo(syncInfo, targetNode, operation);
                }
            }
        } catch (Exception e){
           log.error("redirect syncInfo error");
        }
    }

    private DistroCausa.SyncNodeInfoReq buildReq(SyncInfo syncInfo, String operation) {
        DistroCausa.SyncNodeInfoReq.Builder builder = DistroCausa.SyncNodeInfoReq.newBuilder();
        switch (operation){
            case RemoteConstant.INCREMENTAL_ADD_SERVER:
                return builder.setScheduleServerRegisterInfo(RegisterCausa.ServerRegisterReporter.newBuilder()
                        .setServerIpAddress(((ServerRegisterSyncInfo) syncInfo).getScheduleServerIp()).build())
                        .setOperation(operation).build();

            case RemoteConstant.INCREMENTAL_ADD_WORKER:
                WorkerSubscribeSyncInfo syncInfo1 = (WorkerSubscribeSyncInfo) syncInfo;
                RegisterCausa.WorkerSubscribeReq build = RegisterCausa.WorkerSubscribeReq.newBuilder().setServerIpAddress(syncInfo1.getServerIpAddress())
                        .setWorkerIpAddress(syncInfo1.getWorkerIpAddress())
                        .setAppName(syncInfo1.getAppName())
                        .setScheduleTime(syncInfo1.getScheduleTime()).build();
                return builder.setWorkerSubscribeInfo(build)
                        .setOperation(operation).build();

            case RemoteConstant.INCREMENTAL_REMOVE_SERVER:
                return builder.setScheduleServerRemoveInfo(DistroCausa.ServerRemoveInfo.newBuilder()
                        .setServerAddress(((ServerRemoveSyncInfo) syncInfo).getServerIpAddress()).build())
                        .setOperation(operation).build();

            case RemoteConstant.INCREMENTAL_REMOVE_WORKER:
                return builder.setWorkerRemoveInfo(DistroCausa.WorkerRemoveInfo.newBuilder()
                        .setAppName(((WorkerRemoveSyncInfo) syncInfo).getAppName()).build())
                        .setOperation(operation).build();

            case RemoteConstant.FULL_SYNC:
                FullSyncInfo syncInfo2 = (FullSyncInfo) syncInfo;
                DistroCausa.FullSyncInfo builder1 = DistroCausa.FullSyncInfo.newBuilder()
                        .addAllServerAddressSet(syncInfo2.getServerAddressSet())
                        .addAllWorkerIpAddressSet(syncInfo2.getWorkerIpAddressSet())
                        .putAllServerAddress2ScheduleTimesMap(syncInfo2.getServerAddress2ScheduleTimesMap())
                        .putAllAppName2WorkerNumMap(syncInfo2.getAppName2WorkerNumMap()).build();
                return builder.setFullSyncInfo(builder1)
                        .setOperation(operation).build();
        }
        return null;
    }



}
