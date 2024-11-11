package org.kjob.server.consumer;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.checkerframework.checker.units.qual.A;
import org.kjob.remote.api.MqGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.MqCausa;
import org.kjob.server.consumer.entity.Response;
import org.kjob.server.consumer.entity.ResponseEnum;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@GrpcService
@Slf4j
public class ServerConsumerGrpcService extends MqGrpc.MqImplBase {
    DefaultMessageStore defaultMessageStore = new DefaultMessageStore();
    ServerConsumerGrpcService(Consumer consumer){
        defaultMessageStore.startWatcher(consumer);
        DelayedQueueManager.init(consumer);
    }
    @Override
    public void send(MqCausa.Message request, StreamObserver<CommonCausa.Response> responseObserver) {
        defaultMessageStore.writeToCommitLog(request, new RemotingResponseCallback() {
            @Override
            public void callback(Response response) {
                if(response.getRes().equals(ResponseEnum.SUCCESS)){
                    CommonCausa.Response build = CommonCausa.Response.newBuilder().setCode(200).build();
                    responseObserver.onNext(build);
                    responseObserver.onCompleted();
                } else {
                    log.error(response.getRes().getV());
                    CommonCausa.Response build = CommonCausa.Response.newBuilder().setCode(500).build();
                    responseObserver.onNext(build);
                    responseObserver.onCompleted();
                }
            }
        });



    }


}
