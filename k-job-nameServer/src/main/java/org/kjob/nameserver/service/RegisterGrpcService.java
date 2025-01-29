package org.kjob.nameserver.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.nameserver.core.ServerIpAddressManager;
import org.kjob.nameserver.core.distro.ClientStatusManager;
import org.kjob.nameserver.core.distro.DistroClientDataProcessor;
import org.kjob.nameserver.module.ClientHeartbeat;
import org.kjob.nameserver.module.ReBalanceInfo;
import org.kjob.nameserver.module.sync.ServerRegisterSyncInfo;
import org.kjob.nameserver.module.sync.WorkerSubscribeSyncInfo;
import org.kjob.remote.api.RegisterToNameServerGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.RegisterCausa;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

/**
 *
 * handle schedule and worker register/subscribe
 * register/subscribe also a heartbeat
 */
@GrpcService
public class RegisterGrpcService extends RegisterToNameServerGrpc.RegisterToNameServerImplBase {
    @Autowired
    ServerIpAddressManager service;
    @Autowired
    DistroClientDataProcessor processor;
    @Autowired
    ClientStatusManager clientStatusManager;
    /**
     * register when ScheduleServer
     * also schedule heartbeat
     * @param request
     * @param responseObserver
     */
    @Override
    public void serverRegister(RegisterCausa.ServerRegisterReporter request, StreamObserver<CommonCausa.Response> responseObserver) {
        service.add2ServerAddressSet(request.getServerIpAddress());

        // heartbeat
        clientStatusManager.updateStatus(new ClientHeartbeat(request.getServerIpAddress(),
                RemoteConstant.SERVER, request.getRegisterTimestamp()));
        // sync
        processor.handleSync(new ServerRegisterSyncInfo(request.getServerIpAddress()), RemoteConstant.INCREMENTAL_ADD_SERVER);
        CommonCausa.Response build = CommonCausa.Response.newBuilder().build();
        responseObserver.onNext(build);
        responseObserver.onCompleted();
    }

    /**
     * worker subscribe at fixed rate,and update scheduleTimes
     * also worker heartbeat
     * @param request
     * @param responseObserver
     */
    @Override
    public void workerSubscribe(RegisterCausa.WorkerSubscribeReq request, StreamObserver<CommonCausa.Response> responseObserver) {
        service.addAppName2WorkerNumMap(request.getWorkerIpAddress(),request.getAppName());
        service.addScheduleTimes(request.getServerIpAddress(),request.getScheduleTime());
        // heartbeat
        clientStatusManager.updateStatus(new ClientHeartbeat(request.getWorkerIpAddress(),
                RemoteConstant.WORKER, request.getSubscribeTimestamp()));
        // sync
        processor.handleSync(new WorkerSubscribeSyncInfo(request.getWorkerIpAddress(),
                request.getAppName(),
                request.getScheduleTime(),
                request.getServerIpAddress()), RemoteConstant.INCREMENTAL_ADD_WORKER);

        ReBalanceInfo info = service.getServerAddressReBalanceList(request.getServerIpAddress(), request.getAppName());

        RegisterCausa.WorkerSubscribeResponse build = RegisterCausa.WorkerSubscribeResponse.newBuilder()
                .addAllServerAddressIpLists(info.getServerIpList())
                .setIsSplit(info.isSplit())
                .setIsChangeServer(info.isChangeServer())
                .setSubAppName(info.getSubAppName()).build();
        CommonCausa.Response build1 = CommonCausa.Response.newBuilder()
                .setWorkerSubscribeResponse(build)
                .build();
        responseObserver.onNext(build1);
        responseObserver.onCompleted();
    }

    /**
     * producer get serverList at fixed rate
     * @param request
     * @param responseObserver
     */
    @Override
    public void fetchServerList(RegisterCausa.FetchServerAddressListReq request, StreamObserver<CommonCausa.Response> responseObserver) {
        ArrayList<String> serverAddressList = new ArrayList<>(service.getServerAddressSet());
        RegisterCausa.ServerAddressList builder = RegisterCausa.ServerAddressList.newBuilder().addAllServerAddressList(serverAddressList).build();
        CommonCausa.Response build1 = CommonCausa.Response.newBuilder()
                .setServerAddressList(builder)
                .build();
        responseObserver.onNext(build1);
        responseObserver.onCompleted();
    }
}
