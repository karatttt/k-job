package org.kjob.server.remote.worker.selector.impl;

import org.kjob.common.enums.DispatchStrategy;
import org.kjob.server.common.module.WorkerInfo;
import org.kjob.server.persistence.domain.InstanceInfo;
import org.kjob.server.persistence.domain.JobInfo;
import org.kjob.server.remote.worker.selector.TaskTrackerSelector;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RANDOM
 *
 * @author （疑似）新冠帕鲁
 * @since 2024/2/24
 */
@Component
public class RandomTaskTrackerSelector implements TaskTrackerSelector {

    @Override
    public DispatchStrategy strategy() {
        return DispatchStrategy.RANDOM;
    }

    @Override
    public WorkerInfo select(JobInfo jobInfoDO, InstanceInfo instanceInfoDO, List<WorkerInfo> availableWorkers) {
        int randomIdx = ThreadLocalRandom.current().nextInt(availableWorkers.size());
        return availableWorkers.get(randomIdx);
    }
}
