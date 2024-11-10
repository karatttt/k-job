package org.kjob.server.consumer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.enums.DispatchStrategy;
import org.kjob.common.enums.SwitchableStatus;
import org.kjob.common.enums.TimeExpressionType;
import org.kjob.common.module.LifeCycle;
import org.kjob.remote.protos.MqCausa;
import org.kjob.server.core.schedule.TimingStrategyService;
import org.kjob.server.persistence.domain.JobInfo;
import org.kjob.server.persistence.service.JobInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@Slf4j
public class Consumer {
    @Autowired
    JobInfoService jobInfoService;
    @Autowired
    TimingStrategyService timingStrategyService;


    private void createJob(MqCausa.Message message) {
        MqCausa.CreateJobReq jobReq = message.getCreateJobReq();
        try {
            LifeCycle lifeCycle = LifeCycle.parse(jobReq.getLifeCycle());
            Long nextTriggerTime = timingStrategyService.calculateNextTriggerTime(null,
                    TimeExpressionType.getTimeExpressionTypeByProtoBuf(jobReq.getTimeExpressionType()), jobReq.getTimeExpression(), lifeCycle.getStart(), lifeCycle.getEnd());


            JobInfo build2 = JobInfo.builder().jobDescription(jobReq.getJobDescription())
                    .appName(jobReq.getAppName())
                    .jobId(jobReq.getJobId())
                    .nextTriggerTime(nextTriggerTime)
                    .jobName(jobReq.getJobName())
                    .jobParams(jobReq.getJobParams())
                    .timeExpression(jobReq.getTimeExpression())
                    .timeExpressionType(jobReq.getTimeExpressionTypeValue())
                    .maxInstanceNum(jobReq.getMaxInstanceNum())
                    .gmtCreate(new Date())
                    .gmtModified(new Date())
                    .lifecycle(jobReq.getLifeCycle())
                    .processorInfo(jobReq.getProcessorInfo())
                    .dispatchStrategy(DispatchStrategy.HEALTH_FIRST.getV())
                    .nextTriggerTime(0L)
                    .status(SwitchableStatus.ENABLE.getV()).build();
            int i = 1/0;

            jobInfoService.save(build2);
            log.info("insert jobName :{} success", build2.getJobName());
        } catch (Exception e){
            DelayedQueueManager.reConsume(message);
        }
    }


    private void deleteJob(MqCausa.Message message) {
        try {
            MqCausa.DeleteJobReq jobReq = message.getDeleteJobReq();
            jobInfoService.remove(new QueryWrapper<JobInfo>().lambda()
                    .eq(JobInfo::getJobId, jobReq.getJobId()));
            log.info("delete jobId :{} success", jobReq.getJobId());
        } catch (Exception e){
            DelayedQueueManager.reConsume(message);
        }
    }

    private void updateJob(MqCausa.Message message) {
        try {
            MqCausa.UpdateJobReq jobReq = message.getUpdateJobReq();
            LifeCycle lifeCycle = LifeCycle.parse(jobReq.getLifeCycle());
            Long nextTriggerTime = timingStrategyService.calculateNextTriggerTime(null,
                    TimeExpressionType.getTimeExpressionTypeByProtoBuf(jobReq.getTimeExpressionType()), jobReq.getTimeExpression(), lifeCycle.getStart(), lifeCycle.getEnd());


            JobInfo build2 = JobInfo.builder().jobDescription(jobReq.getJobDescription())
                    .appName(jobReq.getAppName())
                    .jobId(jobReq.getJobId())
                    .nextTriggerTime(nextTriggerTime)
                    .jobName(jobReq.getJobName())
                    .jobParams(jobReq.getJobParams())
                    .timeExpression(jobReq.getTimeExpression())
                    .timeExpressionType(jobReq.getTimeExpressionTypeValue())
                    .maxInstanceNum(jobReq.getMaxInstanceNum())
                    .gmtCreate(new Date())
                    .gmtModified(new Date())
                    .lifecycle(jobReq.getLifeCycle())
                    .processorInfo(jobReq.getProcessorInfo())
                    .dispatchStrategy(DispatchStrategy.HEALTH_FIRST.getV())
                    .nextTriggerTime(0L)
                    .status(SwitchableStatus.ENABLE.getV()).build();

            jobInfoService.update(build2, new QueryWrapper<JobInfo>().lambda()
                    .eq(JobInfo::getJobId, build2.getJobId()));
            log.info("update jobName :{} success", build2.getJobName());
        } catch (Exception e){
            DelayedQueueManager.reConsume(message);
        }


    }
    public void consume(MqCausa.Message message) {
        switch (message.getMessageType()){
            case JOB_CREATE :
                createJob(message);
                break;
            case JOB_UPDATE:
                updateJob(message);
                break;
            case JOB_DELETE:
                deleteJob(message);
                break;
        }
    }

}
