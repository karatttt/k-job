package org.kjob.worker.service.handler;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.ScheduleCausa;
import org.kjob.worker.KJobWorker;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.core.schedule.tracker.manager.LightTaskTrackerManager;
import org.kjob.worker.core.schedule.tracker.task.light.LightTaskTracker;
@Slf4j
public class ScheduleJobHandler implements RpcHandler{
    KJobWorkerConfig config;
    public ScheduleJobHandler(KJobWorkerConfig kJobWorkerConfig) {
        config = kJobWorkerConfig;
    }

    public void handle(Object req, StreamObserver<CommonCausa.Response> responseObserver) {
        ScheduleCausa.ServerScheduleJobReq scheduleJobReq = (ScheduleCausa.ServerScheduleJobReq) req;
        final LightTaskTracker taskTracker = LightTaskTrackerManager.getTaskTracker(scheduleJobReq.getInstanceId());
        if (taskTracker != null) {
            log.warn("[TaskTrackerActor] LightTaskTracker({}) for instance(id={}) already exists.", taskTracker, scheduleJobReq.getInstanceId());
            return;
        }
        // 判断是否已经 overload
        if (LightTaskTrackerManager.currentTaskTrackerSize() >= config.getMaxLightweightTaskNum() * LightTaskTrackerManager.OVERLOAD_FACTOR) {
            // ignore this request
            log.warn("[TaskTrackerActor] this worker is overload,ignore this request(instanceId={}),current size = {}!",scheduleJobReq.getInstanceId(),LightTaskTrackerManager.currentTaskTrackerSize());
            return;
        }
        if (LightTaskTrackerManager.currentTaskTrackerSize() >= config.getMaxLightweightTaskNum()) {
            log.warn("[TaskTrackerActor] this worker will be overload soon,current size = {}!",LightTaskTrackerManager.currentTaskTrackerSize());
        }
        // 创建轻量级任务
        LightTaskTrackerManager.atomicCreateTaskTracker(scheduleJobReq.getInstanceId(), ignore -> LightTaskTracker.create(scheduleJobReq, config));
    }
}
