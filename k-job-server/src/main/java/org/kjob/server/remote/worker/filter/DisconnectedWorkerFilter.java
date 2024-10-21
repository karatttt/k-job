package org.kjob.server.remote.worker.filter;

import lombok.extern.slf4j.Slf4j;
import org.kjob.server.common.module.WorkerInfo;
import org.kjob.server.persistence.domain.JobInfo;
import org.springframework.stereotype.Component;

/**
 * filter disconnected worker
 *
 * @author tjq
 * @since 2021/2/19
 */
@Slf4j
@Component
public class DisconnectedWorkerFilter implements WorkerFilter {

    @Override
    public boolean filter(WorkerInfo workerInfo, JobInfo jobInfo) {
        boolean timeout = workerInfo.timeout();
        if (timeout) {
            log.info("[Job-{}] filter worker[{}] due to timeout(lastActiveTime={})", jobInfo.getId(), workerInfo.getAddress(), workerInfo.getLastActiveTime());
        }
        return timeout;
    }
}
