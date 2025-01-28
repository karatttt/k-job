package org.kjob.nameserver.service;

import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.devh.boot.grpc.server.service.GrpcService;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.nameserver.core.ServerIpAddressManager;
import org.kjob.nameserver.core.distro.DistroClientDataProcessor;
import org.kjob.remote.api.DistroGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.DistroCausa;
import org.kjob.remote.protos.RegisterCausa;

import java.util.HashSet;
import java.util.Set;

@GrpcService
@AllArgsConstructor
public class DistroGrpcService extends DistroGrpc.DistroImplBase {
    private final ServerIpAddressManager serverIpAddressManager;

    private final DistroClientDataProcessor process;
    @Override
    public void syncNodeInfo(DistroCausa.SyncNodeInfoReq request, StreamObserver<CommonCausa.Response> responseObserver) {
        syncCurNode(request);
        CommonCausa.Response build = CommonCausa.Response.newBuilder().build();
        responseObserver.onNext(build);
        responseObserver.onCompleted();
    }
    @Override
    public void redirectSyncInfo(DistroCausa.SyncNodeInfoReq request, StreamObserver<CommonCausa.Response> responseObserver) {
        boolean b1 = syncCurNode(request);
        boolean b2 = process.syncNodeInfoToOthers(request);
        if(b1 && b2){
            responseObserver.onNext(CommonCausa.Response.newBuilder().setCode(RemoteConstant.SUCCESS).build());
        } else {
            responseObserver.onNext(CommonCausa.Response.newBuilder().setCode(RemoteConstant.FAULT).build());
        }
        responseObserver.onCompleted();
    }
    private boolean syncCurNode(DistroCausa.SyncNodeInfoReq request){
        try {
            switch (request.getOperation()) {
                case RemoteConstant.INCREMENTAL_ADD_SERVER:
                    String ip = request.getScheduleServerRegisterInfo().getServerIpAddress();
                    serverIpAddressManager.add2ServerAddressSet(ip);
                    break;

                case RemoteConstant.INCREMENTAL_ADD_WORKER:
                    RegisterCausa.WorkerSubscribeReq info = request.getWorkerSubscribeInfo();
                    serverIpAddressManager.addAppName2WorkerNumMap(info.getWorkerIpAddress(), info.getAppName());
                    serverIpAddressManager.addScheduleTimes(info.getServerIpAddress(), info.getScheduleTime());
                    break;

                case RemoteConstant.INCREMENTAL_REMOVE_SERVER:
                    String address = request.getScheduleServerRegisterInfo().getServerIpAddress();
                    serverIpAddressManager.removeServerAddress(address);
                    break;

                case RemoteConstant.INCREMENTAL_REMOVE_WORKER:
                    RegisterCausa.WorkerSubscribeReq syncInfo = request.getWorkerSubscribeInfo();
                    serverIpAddressManager.cleanAppName2WorkerNumMap(syncInfo.getAppName());
                    break;

                case RemoteConstant.FULL_SYNC:
                    HashSet<String> workerIpAddressSet = new HashSet<>(request.getFullSyncInfo().getWorkerIpAddressSetList());
                    HashSet<String> serverAddressSet = new HashSet<>(request.getFullSyncInfo().getServerAddressSetList());
                    serverIpAddressManager.resetInfo(serverAddressSet, workerIpAddressSet,
                            request.getFullSyncInfo().getAppName2WorkerNumMapMap(),
                            request.getFullSyncInfo().getServerAddress2ScheduleTimesMapMap());
                    break;
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }
}
