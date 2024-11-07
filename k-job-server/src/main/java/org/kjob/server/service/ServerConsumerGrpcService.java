package org.kjob.server.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.kjob.remote.api.MqGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.MqCausa;
import org.kjob.server.service.handler.ConsumerHandler;
import org.springframework.beans.factory.annotation.Autowired;
@GrpcService
public class ServerConsumerGrpcService extends MqGrpc.MqImplBase {
    @Autowired
    ConsumerHandler consumerHandler;
    @Override
    public void createJob(MqCausa.CreateJobReq request, StreamObserver<CommonCausa.Response> responseObserver) {
        consumerHandler.handle(request, responseObserver);
    }
}
