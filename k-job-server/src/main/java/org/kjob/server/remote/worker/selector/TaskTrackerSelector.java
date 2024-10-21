package org.kjob.server.remote.worker.selector;

import org.kjob.common.enums.DispatchStrategy;
import org.kjob.server.common.module.WorkerInfo;
import org.kjob.server.persistence.domain.InstanceInfo;
import org.kjob.server.persistence.domain.JobInfo;

import java.util.List;

/**
 * 主节点选择方式
 *
 * @author tjq
 * @since 2024/2/24
 */
public interface TaskTrackerSelector {

    /**
     * 支持的策略
     * @return 派发策略
     */
    DispatchStrategy strategy();

    /**
     * 选择主节点
     * @param jobInfoDO 任务信息
     * @param instanceInfoDO 任务实例
     * @param availableWorkers 可用 workers
     * @return 主节点 worker
     */
    WorkerInfo select(JobInfo jobInfoDO, InstanceInfo instanceInfoDO, List<WorkerInfo> availableWorkers);
}
