package org.kjob.nameserver.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.kjob.nameserver.balance.ServerIpAddressManagerService;
import org.kjob.nameserver.module.ReBalanceInfo;
import org.kjob.remote.api.RegisterToNameServerGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.RegisterCausa;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@GrpcService
public class RegisterGrpcService extends RegisterToNameServerGrpc.RegisterToNameServerImplBase {
    @Autowired
    ServerIpAddressManagerService service;
    @Override
    public void serverRegister(RegisterCausa.ServerRegisterReporter request, StreamObserver<CommonCausa.Response> responseObserver) {
        if(!service.getServerIpAddressSet().contains(request.getServerIpAddress())){
            service.add2ServerIpAddressSet(request);
        }
        CommonCausa.Response build = CommonCausa.Response.newBuilder().build();
        responseObserver.onNext(build);
        responseObserver.onCompleted();
    }

    @Override
    public void workerSubscribe(RegisterCausa.WorkerSubscribeReq request, StreamObserver<CommonCausa.Response> responseObserver) {
        service.addAppName2WorkerNumMap(request.getWorkerIpAddress(),request.getAppName());
        ReBalanceInfo info = service.getServerIpAddressReBalanceList(request.getAppName());

        RegisterCausa.WorkerSubscribeResponse build = RegisterCausa.WorkerSubscribeResponse.newBuilder()
                .addAllServerAddressIpLists(info.getServerIpList())
                .setIsSplit(info.isSplit())
                .setSubAppName(info.getSubAppName()).build();
        CommonCausa.Response build1 = CommonCausa.Response.newBuilder()
                .setWorkerSubscribeResponse(build)
                .build();
        responseObserver.onNext(build1);
        responseObserver.onCompleted();
    }
}
