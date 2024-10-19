package org.kjob.server.service.handler;

import io.grpc.stub.StreamObserver;
import org.kjob.remote.protos.ServerDiscoverCausa;

public interface RpcHandler {
     void handle(Object req,StreamObserver<ServerDiscoverCausa.Response> responseObserver);
}
