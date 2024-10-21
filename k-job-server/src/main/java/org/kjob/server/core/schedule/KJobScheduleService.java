package org.kjob.server.core.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.enums.TimeExpressionType;
import org.kjob.common.module.LifeCycle;
import org.kjob.server.common.config.KJobServerConfig;
import org.kjob.server.common.constant.SwitchableStatus;
import org.kjob.server.core.instance.InstanceService;
import org.kjob.server.core.timewheel.holder.InstanceTimeWheelService;
import org.kjob.server.persistence.domain.AppInfo;
import org.kjob.server.persistence.domain.JobInfo;
import org.kjob.server.persistence.mapper.AppInfoMapper;
import org.kjob.server.persistence.mapper.InstanceInfoMapper;
import org.kjob.server.persistence.mapper.JobInfoMapper;
import org.kjob.server.persistence.service.InstanceInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KJobScheduleService {
    private static final int MAX_APP_NUM = 10;
    public static final long SCHEDULE_RATE = 15000;
    @Autowired
    AppInfoMapper appInfoMapper;
    @Autowired
    KJobServerConfig kJobServerConfig;
    @Autowired
    JobInfoMapper jobInfoMapper;
    @Autowired
    InstanceService instanceService;
    @Autowired
    DispatchService dispatchService;
    @Autowired
    TimingStrategyService timingStrategyService;
    public void scheduleNormalJob(TimeExpressionType timeExpressionType) {
        long start = System.currentTimeMillis();
        // 调度 CRON 表达式 JOB
        try {
            List<Long> allAppIds = appInfoMapper.selectList(new QueryWrapper<AppInfo>().lambda()
                    .eq(AppInfo::getCurrentServer, kJobServerConfig.getAddress()))
                    .stream().map(AppInfo::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(allAppIds)) {
                log.info("[NormalScheduler] current server has no app's job to schedule.");
                return;
            }
            scheduleNormalJob0(timeExpressionType, allAppIds);
        } catch (Exception e) {
            log.error("[NormalScheduler] schedule cron job failed.", e);
        }
        long cost = System.currentTimeMillis() - start;
        log.info("[NormalScheduler] {} job schedule use {} ms.", timeExpressionType, cost);
        if (cost > SCHEDULE_RATE) {
            log.warn("[NormalScheduler] The database query is using too much time({}ms), please check if the database load is too high!", cost);
        }
    }
    private void scheduleNormalJob0(TimeExpressionType timeExpressionType, List<Long> appIds) {

        long nowTime = System.currentTimeMillis();
        long timeThreshold = nowTime + 2 * SCHEDULE_RATE;
        Lists.partition(appIds, MAX_APP_NUM).forEach(partAppIds -> {

            try {

                // 查询条件：任务开启 + 使用CRON表达调度时间 + 指定appId + 即将需要调度执行
                List<JobInfo> jobInfos = jobInfoMapper.selectList(new QueryWrapper<JobInfo>()
                        .lambda()
                        .in(JobInfo::getAppId, appIds)
                        .eq(JobInfo::getStatus, SwitchableStatus.ENABLE)
                        .eq(JobInfo::getTimeExpression, timeExpressionType.getV())
                        .le(JobInfo::getNextTriggerTime, timeThreshold));
                if (CollectionUtils.isEmpty(jobInfos)) {
                    return;
                }

                // 1. 批量写日志表
                Map<Long, Long> jobId2InstanceId = Maps.newHashMap();
                log.info("[NormalScheduler] These {} jobs will be scheduled: {}.", timeExpressionType.name(), jobInfos);

                jobInfos.forEach(jobInfo -> {
                    Long instanceId = instanceService.create(jobInfo.getId(), jobInfo.getAppId(), jobInfo.getJobParams(), null, null, jobInfo.getNextTriggerTime()).getInstanceId();
                    jobId2InstanceId.put(jobInfo.getId(), instanceId);
                });

                // 2. 推入时间轮中等待调度执行
                jobInfos.forEach(JobInfo -> {

                    Long instanceId = jobId2InstanceId.get(JobInfo.getId());

                    long targetTriggerTime = JobInfo.getNextTriggerTime();
                    long delay = 0;
                    // 这里之前可能耗时的是，执行数据库的插入操作，但是其实插入的时间也可以忽略
                    if (targetTriggerTime < nowTime) {
                        log.warn("[Job-{}] schedule delay, expect: {}, current: {}", JobInfo.getId(), targetTriggerTime, System.currentTimeMillis());
                    } else {
                        delay = targetTriggerTime - nowTime;
                    }

                    InstanceTimeWheelService.schedule(instanceId, delay, () -> dispatchService.dispatch(JobInfo, instanceId, Optional.empty(), Optional.empty()));
                });

                // 3. 计算下一次调度时间（忽略5S内的重复执行，即CRON模式下最小的连续执行间隔为 SCHEDULE_RATE ms）
                jobInfos.forEach(JobInfo -> {
                    try {
                        refreshJob(timeExpressionType, JobInfo);
                    } catch (Exception e) {
                        log.error("[Job-{}] refresh job failed.", JobInfo.getId(), e);
                    }
                });


            } catch (Exception e) {
                log.error("[NormalScheduler] schedule {} job failed.", timeExpressionType.name(), e);
            }
        });
    }

    private void refreshJob(TimeExpressionType timeExpressionType, JobInfo jobInfo) {

        LifeCycle lifeCycle = LifeCycle.parse(jobInfo.getLifecycle());
        Long nextTriggerTime = timingStrategyService.calculateNextTriggerTime(jobInfo.getNextTriggerTime(), timeExpressionType, jobInfo.getTimeExpression(), lifeCycle.getStart(), lifeCycle.getEnd());

        JobInfo updatedJobInfo = new JobInfo();
        BeanUtils.copyProperties(jobInfo, updatedJobInfo);

        if (nextTriggerTime == null) {
            log.warn("[Job-{}] this job won't be scheduled anymore, system will set the status to DISABLE!", jobInfo.getId());
            updatedJobInfo.setStatus(SwitchableStatus.DISABLE.getV());
        } else {
            updatedJobInfo.setNextTriggerTime(nextTriggerTime);
        }
        updatedJobInfo.setGmtModified(new Date());

        jobInfoMapper.updateById(updatedJobInfo);
    }

}
