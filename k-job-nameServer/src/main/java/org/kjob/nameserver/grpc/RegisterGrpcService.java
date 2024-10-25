package org.kjob.nameserver.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.kjob.nameserver.balance.ServerIpAddressManager;
import org.kjob.remote.api.RegisterToNameServerGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.RegisterCausa;

@GrpcService
public class RegisterGrpcService extends RegisterToNameServerGrpc.RegisterToNameServerImplBase {
    @Override
    public void serverRegister(RegisterCausa.serverRegisterReporter request, StreamObserver<CommonCausa.Response> responseObserver) {
        if(!ServerIpAddressManager.getServerIpAddressSet().contains(request.getServerIpAddress())){
            ServerIpAddressManager.add2ServerIpAddressSet(request.getServerIpAddress());
        }
        responseObserver.onNext(null);
        responseObserver.onCompleted();
    }
}
