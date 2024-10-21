package org.kjob.server.remote.worker;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.kjob.server.common.module.WorkerInfo;
import org.kjob.server.persistence.domain.JobInfo;
import org.kjob.server.remote.worker.filter.WorkerFilter;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 获取 worker 集群信息
 *
 * @author tjq
 * @since 2021/2/19
 */
@Slf4j
@Service
public class WorkerClusterQueryService {

    private final List<WorkerFilter> workerFilters;

    public WorkerClusterQueryService(List<WorkerFilter> workerFilters) {
        this.workerFilters = workerFilters;
    }

    /**
     * get worker for job
     *
     * @param jobInfo job
     * @return worker cluster info, sorted by metrics desc
     */
    public List<WorkerInfo> geAvailableWorkers(JobInfo jobInfo) {

        List<WorkerInfo> workers = Lists.newLinkedList(getWorkerInfosByAppId(jobInfo.getAppId()).values());

        // 过滤不符合要求的机器
        workers.removeIf(workerInfo -> filterWorker(workerInfo, jobInfo));

        // 限定集群大小（0代表不限制）
        if (!workers.isEmpty() && jobInfo.getMaxWorkerCount() > 0 && workers.size() > jobInfo.getMaxWorkerCount()) {
            workers = workers.subList(0, jobInfo.getMaxWorkerCount());
        }
        return workers;
    }

//    @DesignateServer
//    public List<WorkerInfo> getAllWorkers(Long appId) {
//        List<WorkerInfo> workers = Lists.newLinkedList(getWorkerInfosByAppId(appId).values());
//        workers.sort((o1, o2) -> o2.getSystemMetrics().calculateScore() - o1.getSystemMetrics().calculateScore());
//        return workers;
//    }

    /**
     * get all alive workers
     *
     * @param appId appId
     * @return alive workers
     */
//    @DesignateServer
//    public List<WorkerInfo> getAllAliveWorkers(Long appId) {
//        List<WorkerInfo> workers = Lists.newLinkedList(getWorkerInfosByAppId(appId).values());
//        workers.removeIf(WorkerInfo::timeout);
//        return workers;
//    }

    /**
     * Gets worker info by address.
     *
     * @param appId   the app id
     * @param address the address
     * @return the worker info by address
     */
    public Optional<WorkerInfo> getWorkerInfoByAddress(Long appId, String address) {
        // this may cause NPE while address value is null .
        final Map<String, WorkerInfo> workerInfosByAppId = getWorkerInfosByAppId(appId);
        //add null check for both workerInfos Map and  address
        if (null != workerInfosByAppId && null != address) {
            return Optional.ofNullable(workerInfosByAppId.get(address));
        }
        return Optional.empty();
    }

    public Map<Long, ClusterStatusHolder> getAppId2ClusterStatus() {
        return WorkerClusterManagerService.getAppId2ClusterStatus();
    }

    private Map<String, WorkerInfo> getWorkerInfosByAppId(Long appId) {
        ClusterStatusHolder clusterStatusHolder = getAppId2ClusterStatus().get(appId);
        if (clusterStatusHolder == null) {
            log.warn("[WorkerManagerService] can't find any worker for app(appId={}) yet.", appId);
            return Collections.emptyMap();
        }
        return clusterStatusHolder.getAllWorkers();
    }

    /**
     * filter invalid worker for job
     *
     * @param workerInfo worker info
     * @param jobInfo    job info
     * @return filter this worker when return true
     */
    private boolean filterWorker(WorkerInfo workerInfo, JobInfo jobInfo) {
        for (WorkerFilter filter : workerFilters) {
            if (filter.filter(workerInfo, jobInfo)) {
                return true;
            }
        }
        return false;
    }
}
