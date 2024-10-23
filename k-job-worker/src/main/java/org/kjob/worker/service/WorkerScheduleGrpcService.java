package org.kjob.worker.service;

import io.grpc.stub.StreamObserver;
import org.kjob.remote.api.ScheduleGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.ScheduleCausa;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.service.handler.ScheduleJobHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkerScheduleGrpcService extends ScheduleGrpc.ScheduleImplBase {
    ScheduleJobHandler scheduleJobHandler;

    public WorkerScheduleGrpcService(KJobWorkerConfig kJobWorkerConfig) {
        this.scheduleJobHandler = new ScheduleJobHandler(kJobWorkerConfig);
    }

    @Override
    public void serverScheduleJob(ScheduleCausa.ServerScheduleJobReq request, StreamObserver<CommonCausa.Response> responseObserver) {
        scheduleJobHandler.handle(request, responseObserver);
    }
}
