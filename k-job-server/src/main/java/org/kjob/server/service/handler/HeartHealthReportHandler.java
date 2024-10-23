package org.kjob.server.service.handler;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.domain.WorkerHeartbeat;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.ScheduleCausa;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.server.remote.worker.WorkerClusterManagerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HeartHealthReportHandler implements RpcHandler{

    @Override
    public void handle(Object req, StreamObserver<CommonCausa.Response> responseObserver) {
        WorkerHeartbeat workerHeartbeat = new WorkerHeartbeat();
        BeanUtils.copyProperties(req, workerHeartbeat);
        WorkerClusterManagerService.updateStatus(workerHeartbeat);
        responseObserver.onNext(null);
        responseObserver.onCompleted();
    }
}
