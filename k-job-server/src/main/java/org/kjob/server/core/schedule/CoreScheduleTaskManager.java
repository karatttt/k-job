package org.kjob.server.core.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.enums.TimeExpressionType;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CoreScheduleTaskManager implements InitializingBean, DisposableBean {
    private final List<Thread> coreThreadContainer = new ArrayList<>();
    @Autowired
    KJobScheduleService kJobScheduleService;

    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    @Override
    public void afterPropertiesSet() {
        // 定时调度
        coreThreadContainer.add(new Thread(new LoopRunnable("ScheduleCronJob", KJobScheduleService.SCHEDULE_RATE, () -> kJobScheduleService.scheduleNormalJob(TimeExpressionType.CRON)), "Thread-ScheduleCronJob"));
//        coreThreadContainer.add(new Thread(new LoopRunnable("ScheduleDailyTimeIntervalJob", KJobScheduleService.SCHEDULE_RATE, () -> kJobScheduleService.scheduleNormalJob(TimeExpressionType.DAILY_TIME_INTERVAL)), "Thread-ScheduleDailyTimeIntervalJob"));
//        coreThreadContainer.add(new Thread(new LoopRunnable("ScheduleCronWorkflow", KJobScheduleService.SCHEDULE_RATE, kJobScheduleService::scheduleCronWorkflow), "Thread-ScheduleCronWorkflow"));
//        coreThreadContainer.add(new Thread(new LoopRunnable("ScheduleFrequentJob", KJobScheduleService.SCHEDULE_RATE, kJobScheduleService::scheduleFrequentJob), "Thread-ScheduleFrequentJob"));
//        // 数据清理
//        coreThreadContainer.add(new Thread(new LoopRunnable("CleanWorkerData", KJobScheduleService.SCHEDULE_RATE, kJobScheduleService::cleanData), "Thread-CleanWorkerData"));
        // 状态检查
//        coreThreadContainer.add(new Thread(new LoopRunnable("CheckRunningInstance", InstanceStatusCheckService.CHECK_INTERVAL, instanceStatusCheckService::checkRunningInstance), "Thread-CheckRunningInstance"));
//        coreThreadContainer.add(new Thread(new LoopRunnable("CheckWaitingDispatchInstance", InstanceStatusCheckService.CHECK_INTERVAL, instanceStatusCheckService::checkWaitingDispatchInstance), "Thread-CheckWaitingDispatchInstance"));
//        coreThreadContainer.add(new Thread(new LoopRunnable("CheckWaitingWorkerReceiveInstance", InstanceStatusCheckService.CHECK_INTERVAL, instanceStatusCheckService::checkWaitingWorkerReceiveInstance), "Thread-CheckWaitingWorkerReceiveInstance"));
//        coreThreadContainer.add(new Thread(new LoopRunnable("CheckWorkflowInstance", InstanceStatusCheckService.CHECK_INTERVAL, instanceStatusCheckService::checkWorkflowInstance), "Thread-CheckWorkflowInstance"));

        coreThreadContainer.forEach(Thread::start);

    }

    @Override
    public void destroy() throws Exception {
        coreThreadContainer.forEach(Thread::interrupt);

    }
   @RequiredArgsConstructor
    private static final class LoopRunnable implements Runnable {

        private final String taskName;

        private final Long runningInterval;

        private final Runnable innerRunnable;

        @SuppressWarnings("BusyWait")
        @Override
        public void run() {
            log.info("start task : {}.", taskName);
            while (true) {
                try {

                    Thread.sleep(runningInterval);

                    innerRunnable.run();
                } catch (InterruptedException e) {
                    log.warn("[{}] task has been interrupted!", taskName, e);
                    break;
                } catch (Exception e) {
                    log.error("[{}] task failed!", taskName, e);
                }
            }
        }
    }
}
