package org.kjob.nameserver.core;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.nameserver.config.KJobNameServerConfig;
import org.kjob.nameserver.module.sync.*;
import org.kjob.remote.api.DistroGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.DistroCausa;
import org.kjob.remote.protos.RegisterCausa;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class GrpcClient {
    private final Map<String, DistroGrpc.DistroFutureStub> clusterFutureStubMap = new HashMap<>();
    private final Map<String, DistroGrpc.DistroBlockingStub> clusterBlockingStubMap = new HashMap<>();
    private final Executor syncInfoexecutor = Executors.newFixedThreadPool(5);

    private final int RETRY_TIMES = 3;

    public GrpcClient(KJobNameServerConfig kJobNameServerConfig){
        for (String ip : kJobNameServerConfig.getServerAddressList()) {
            String[] split = ip.split(":");
            ManagedChannel channel = ManagedChannelBuilder.
                    forAddress(split[0], Integer.parseInt(split[1])).
                    usePlaintext().build();
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
        ListenableFuture<CommonCausa.Response> future = distroFutureStub.syncNodeInfo(req);
    }

    public void sendSyncInfo(DistroCausa.SyncNodeInfoReq syncInfo, String target) {
        DistroGrpc.DistroFutureStub distroFutureStub = clusterFutureStubMap.get(target);
        ListenableFuture<CommonCausa.Response> future = distroFutureStub.syncNodeInfo(syncInfo);
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
            DistroGrpc.DistroBlockingStub distroBlockingStub = clusterBlockingStubMap.get(targetNode);
            CommonCausa.Response response = distroBlockingStub.syncNodeInfo(req);
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

    public void dataCheck(String checkSum, String target, FullSyncInfo info, Executor executor){
        DistroGrpc.DistroFutureStub distroFutureStub = clusterFutureStubMap.get(target);

        ListenableFuture<CommonCausa.Response> future = distroFutureStub.clusterDataCheck(DistroCausa.DataCheckReq.newBuilder().setCheckSum(checkSum).build());
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    if(future.get().getCode() == RemoteConstant.NO_MATCH) {
                        DistroCausa.SyncNodeInfoReq req = buildReq(info, RemoteConstant.FULL_SYNC);
                        DistroGrpc.DistroBlockingStub distroBlockingStub = clusterBlockingStubMap.get(target);

                        log.info("dataCheck no match, send full sync info to :{}", target);
                        distroBlockingStub.syncNodeInfo(req);
                    } else {
                        log.info("dataCheck match with:{} ", target);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("dataCheck error,target:{}", target);
                }
            }
        }, executor);
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
