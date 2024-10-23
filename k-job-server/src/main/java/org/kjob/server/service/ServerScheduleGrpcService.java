package org.kjob.server.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.kjob.remote.api.ScheduleGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.ScheduleCausa;
import org.kjob.server.service.handler.HeartHealthReportHandler;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class ServerScheduleGrpcService extends ScheduleGrpc.ScheduleImplBase {
    @Autowired
    HeartHealthReportHandler heartHealthReportHandler;
    @Override
    public void reportWorkerHeartbeat(ScheduleCausa.WorkerHeartbeat request, StreamObserver<CommonCausa.Response> responseObserver) {
        heartHealthReportHandler.handle(request, responseObserver);
    }
}
