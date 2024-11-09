package org.kjob.server.consumer;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.checkerframework.checker.units.qual.A;
import org.kjob.remote.api.MqGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.MqCausa;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@GrpcService
public class ServerConsumerGrpcService extends MqGrpc.MqImplBase {
    DefaultMessageStore defaultMessageStore = new DefaultMessageStore();
    ServerConsumerGrpcService(Consumer consumer){
        defaultMessageStore.startWatcher(consumer);
        DelayedQueueManager.init(consumer);
    }
    @Override
    public void send(MqCausa.Message request, StreamObserver<CommonCausa.Response> responseObserver) {
        defaultMessageStore.writeToCommitLog(request);
        CommonCausa.Response build = CommonCausa.Response.newBuilder().setCode(200).build();
        responseObserver.onNext(build);
        responseObserver.onCompleted();
    }


}
