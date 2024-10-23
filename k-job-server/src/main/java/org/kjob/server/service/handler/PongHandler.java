package org.kjob.server.service.handler;

import io.grpc.stub.StreamObserver;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.springframework.stereotype.Component;

@Component
public class PongHandler implements RpcHandler{
    @Override
    public void handle(Object req, StreamObserver<CommonCausa.Response> responseObserver) {
        CommonCausa.Response build = CommonCausa.Response.newBuilder().setCode(RemoteConstant.SUCCESS).build();
        responseObserver.onNext(build);
        responseObserver.onCompleted();
    }
}
