package org.kjob.server.remote.worker.selector;

import com.google.common.collect.Maps;
import org.kjob.server.common.module.WorkerInfo;
import org.kjob.server.persistence.domain.InstanceInfo;
import org.kjob.server.persistence.domain.JobInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * TaskTrackerSelectorService
 *
 * @author tjq
 * @since 2024/2/24
 */
@Service
public class TaskTrackerSelectorService {

    private final Map<Integer, TaskTrackerSelector> taskTrackerSelectorMap = Maps.newHashMap();

    @Autowired
    public TaskTrackerSelectorService(List<TaskTrackerSelector> taskTrackerSelectors) {
        taskTrackerSelectors.forEach(ts -> taskTrackerSelectorMap.put(ts.strategy().getV(), ts));
    }

    public WorkerInfo select(JobInfo jobInfoDO, InstanceInfo instanceInfoDO, List<WorkerInfo> availableWorkers) {
        TaskTrackerSelector taskTrackerSelector = taskTrackerSelectorMap.get(jobInfoDO.getDispatchStrategy());
        return taskTrackerSelector.select(jobInfoDO, instanceInfoDO, availableWorkers);
    }
}
