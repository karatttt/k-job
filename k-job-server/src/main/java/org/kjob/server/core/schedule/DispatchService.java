package org.kjob.server.core.schedule;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


import org.kjob.common.SystemInstanceResult;
import org.kjob.common.enums.TimeExpressionType;
import org.kjob.server.common.Holder;
import org.kjob.server.common.module.WorkerInfo;
import org.kjob.server.persistence.domain.InstanceInfo;
import org.kjob.server.persistence.domain.JobInfo;
import org.kjob.server.persistence.mapper.InstanceInfoMapper;
import org.kjob.server.remote.worker.WorkerClusterQueryService;
import org.kjob.server.remote.worker.selector.TaskTrackerSelectorService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.kjob.common.enums.InstanceStatus.*;


/**
 * 派送服务（将任务从Server派发到Worker）
 *
 * @author tjq
 * @author Echo009
 * @since 2020/4/5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private final WorkerClusterQueryService workerClusterQueryService;

//    private final InstanceManager instanceManager;
//
//    private final InstanceMetadataService instanceMetadataService;

    private final InstanceInfoMapper instanceInfoMapper;

    private final TaskTrackerSelectorService taskTrackerSelectorService;


    /**
     * 将任务从Server派发到Worker（TaskTracker）
     * 只会派发当前状态为等待派发的任务实例
     * **************************************************
     * 2021-02-03 modify by Echo009
     * 1、移除参数 当前运行次数、工作流实例ID、实例参数
     * 更改为从当前任务实例中获取获取以上信息
     * 2、移除运行次数相关的（runningTimes）处理逻辑

     * **************************************************
     *
     * @param jobInfo              任务的元信息
     * @param instanceId           任务实例ID
     * @param instanceInfoOptional 任务实例信息，可选
     * @param overloadOptional     超载信息，可选
     */
//    @UseCacheLock(type = "processJobInstance", key = "#jobInfo.getMaxInstanceNum() > 0 || T(tech.powerjob.common.enums.TimeExpressionType).FREQUENT_TYPES.contains(#jobInfo.getTimeExpressionType()) ? #jobInfo.getId() : #instanceId", concurrencyLevel = 1024)
    public void dispatch(JobInfo jobInfo, Long instanceId, Optional<InstanceInfo> instanceInfoOptional, Optional<Holder<Boolean>> overloadOptional) {
        // 允许从外部传入实例信息，减少 io 次数
        // 检查当前任务是否被取消
        System.out.println("ds");
        InstanceInfo instanceInfo = instanceInfoMapper.selectById(instanceId);
        Long jobId = instanceInfo.getJobId();
        if (CANCELED.getV() == instanceInfo.getStatus()) {
            log.info("[Dispatcher-{}|{}] cancel dispatch due to instance has been canceled", jobId, instanceId);
            return;
        }
        // 已经被派发过则不再派发
        // fix 并发场景下重复派发的问题
        if (instanceInfo.getStatus() != WAITING_DISPATCH.getV()) {
            log.info("[Dispatcher-{}|{}] cancel dispatch due to instance has been dispatched", jobId, instanceId);
            return;
        }
        // 任务信息已经被删除
        if (jobInfo.getId() == null) {
            log.warn("[Dispatcher-{}|{}] cancel dispatch due to job(id={}) has been deleted!", jobId, instanceId, jobId);
//            instanceManager.processFinishedInstance(instanceId, instanceInfo.getWfInstanceId(), FAILED, "can't find job by id " + jobId);
            return;
        }

        Date now = new Date();
        String dbInstanceParams = instanceInfo.getInstanceParams() == null ? "" : instanceInfo.getInstanceParams();
        log.info("[Dispatcher-{}|{}] start to dispatch job: {};instancePrams: {}.", jobId, instanceId, jobInfo, dbInstanceParams);

        // 查询当前运行的实例数
        long current = System.currentTimeMillis();
        Integer maxInstanceNum = jobInfo.getMaxInstanceNum();
        // 秒级任务只派发到一台机器，具体的 maxInstanceNum 由 TaskTracker 控制
        if (TimeExpressionType.FREQUENT_TYPES.contains(jobInfo.getTimeExpressionType())) {
            maxInstanceNum = 1;
        }

        // 0 代表不限制在线任务，还能省去一次 DB 查询
        if (maxInstanceNum > 0) {
            // 不统计 WAITING_DISPATCH 的状态：使用 OpenAPI 触发的延迟任务不应该统计进去（比如 delay 是 1 天）
            // 由于不统计 WAITING_DISPATCH，所以这个 runningInstanceCount 不包含本任务自身
            Integer runningInstanceCount = instanceInfoMapper.selectCount(new QueryWrapper<InstanceInfo>()
                    .lambda().eq(InstanceInfo::getStatus, WAITING_WORKER_RECEIVE.getV())
                    .or().eq(InstanceInfo::getStatus, RUNNING.getV()));
            // 超出最大同时运行限制，不执行调度
            if (runningInstanceCount >= maxInstanceNum) {
                String result = String.format(SystemInstanceResult.TOO_MANY_INSTANCES, runningInstanceCount, maxInstanceNum);
                log.warn("[Dispatcher-{}|{}] cancel dispatch job due to too much instance is running ({} > {}).", jobId, instanceId, runningInstanceCount, maxInstanceNum);
//                instanceInfoRepository.update4TriggerFailed(instanceId, FAILED.getV(), current, current, RemoteConstant.EMPTY_ADDRESS, result, now);
//                instanceManager.processFinishedInstance(instanceId, instanceInfo.getWfInstanceId(), FAILED, result);
                return;
            }
        }

        // 获取当前最合适的 worker 列表
        List<WorkerInfo> suitableWorkers = workerClusterQueryService.geAvailableWorkers(jobInfo);

        if (CollectionUtils.isEmpty(suitableWorkers)) {
            log.warn("[Dispatcher-{}|{}] cancel dispatch job due to no worker available", jobId, instanceId);
//            instanceInfoRepository.update4TriggerFailed(instanceId, FAILED.getV(), current, current, RemoteConstant.EMPTY_ADDRESS, SystemInstanceResult.NO_WORKER_AVAILABLE, now);

//            instanceManager.processFinishedInstance(instanceId, instanceInfo.getWfInstanceId(), FAILED, SystemInstanceResult.NO_WORKER_AVAILABLE);
            return;
        }
        // 判断是否超载，在所有可用 worker 超载的情况下直接跳过当前任务
        suitableWorkers = filterOverloadWorker(suitableWorkers);
        if (suitableWorkers.isEmpty()) {
            // 直接取消派发，减少一次数据库 io
            overloadOptional.ifPresent(booleanHolder -> booleanHolder.set(true));
            log.warn("[Dispatcher-{}|{}] cancel to dispatch job due to all worker is overload", jobId, instanceId);
            return;
        }
        // todo 这里可能涉及到分片，所以有多个ip，实际只传一个
        List<String> workerIpList = suitableWorkers.stream().map(WorkerInfo::getAddress).collect(Collectors.toList());
        // 构造任务调度请求


        // 发送请求（不可靠，需要一个后台线程定期轮询状态）todo 没做
        WorkerInfo taskTracker = taskTrackerSelectorService.select(jobInfo, instanceInfo, suitableWorkers);
        String taskTrackerAddress = taskTracker.getAddress();

//        URL workerUrl = ServerURLFactory.dispatchJob2Worker(taskTrackerAddress);
//        transportService.tell(taskTracker.getProtocol(), workerUrl, req);
        log.info("[Dispatcher-{}|{}] send schedule request to TaskTracker[protocol:{},address:{}] successfully.", jobId, instanceId, taskTracker.getProtocol(), taskTrackerAddress);

        // 修改状态
        InstanceInfo build = InstanceInfo.builder().id(instanceId).status(WAITING_WORKER_RECEIVE.getV()).taskTrackerAddress(taskTrackerAddress).build();
        instanceInfoMapper.updateById(build);
        // 装载缓存 todo 没看
//        instanceMetadataService.loadJobInfo(instanceId, jobInfo);
    }

    private List<WorkerInfo> filterOverloadWorker(List<WorkerInfo> suitableWorkers) {

        List<WorkerInfo> res = new ArrayList<>(suitableWorkers.size());
        for (WorkerInfo suitableWorker : suitableWorkers) {
            if (suitableWorker.overload()){
                continue;
            }
            res.add(suitableWorker);
        }
        return res;
    }

    /**
     * 构造任务调度请求
     */
//    private ServerScheduleJobReq constructServerScheduleJobReq(JobInfo jobInfo, InstanceInfo instanceInfo, List<String> finalWorkersIpList) {
//        // 构造请求
//        ServerScheduleJobReq req = new ServerScheduleJobReq();
//        BeanUtils.copyProperties(jobInfo, req);
//        // 传入 JobId
//        req.setJobId(jobInfo.getId());
//        // 传入 InstanceParams
//        if (StringUtils.isEmpty(instanceInfo.getInstanceParams())) {
//            req.setInstanceParams(null);
//        } else {
//            req.setInstanceParams(instanceInfo.getInstanceParams());
//        }
//        // 覆盖静态参数
//        if (!StringUtils.isEmpty(instanceInfo.getJobParams())) {
//            req.setJobParams(instanceInfo.getJobParams());
//        }
//        req.setInstanceId(instanceInfo.getInstanceId());
//        req.setAllWorkerAddress(finalWorkersIpList);
//        req.setMaxWorkerCount(jobInfo.getMaxWorkerCount());
//
//        // 设置工作流ID
//        req.setWfInstanceId(instanceInfo.getWfInstanceId());
//
//        req.setExecuteType(ExecuteType.of(jobInfo.getExecuteType()).name());
//        req.setProcessorType(ProcessorType.of(jobInfo.getProcessorType()).name());
//
//        req.setTimeExpressionType(TimeExpressionType.of(jobInfo.getTimeExpressionType()).name());
//        if (jobInfo.getInstanceTimeLimit() != null) {
//            req.setInstanceTimeoutMS(jobInfo.getInstanceTimeLimit());
//        }
//        req.setThreadConcurrency(jobInfo.getConcurrency());
//        return req;
//    }
}
