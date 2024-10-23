package org.kjob.worker.service.handler;

import io.grpc.stub.StreamObserver;
import org.kjob.remote.protos.CommonCausa;

public interface RpcHandler {
     void handle(Object req,StreamObserver<CommonCausa.Response> responseObserver);
}
