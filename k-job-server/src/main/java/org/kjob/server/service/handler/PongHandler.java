package org.kjob.server.service.handler;

import io.grpc.stub.StreamObserver;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.springframework.stereotype.Component;

@Component
public class PongHandler implements RpcHandler{
    @Override
    public void handle(Object req, StreamObserver<ServerDiscoverCausa.Response> responseObserver) {
        ServerDiscoverCausa.Response build = ServerDiscoverCausa.Response.newBuilder().setCode(RemoteConstant.SUCCESS).build();
        responseObserver.onNext(build);
        responseObserver.onCompleted();
    }
}
