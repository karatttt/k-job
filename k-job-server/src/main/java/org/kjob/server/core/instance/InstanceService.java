package org.kjob.server.core.instance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.enums.InstanceStatus;
import org.kjob.server.core.uid.IdGenerateService;
import org.kjob.server.persistence.domain.InstanceInfo;
import org.kjob.server.persistence.mapper.InstanceInfoMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstanceService {
    private final InstanceInfoMapper instanceInfoMapper;
    private final IdGenerateService idGenerateService;
    public InstanceInfo create(Long jobId, Long appId, String jobParams, String instanceParams, Long wfInstanceId, Long expectTriggerTime) {

        Long instanceId = idGenerateService.allocate();
        Date now = new Date();
        InstanceInfo newInstanceInfo = new InstanceInfo();
        newInstanceInfo.setJobId(jobId);
        newInstanceInfo.setAppId(appId);
        newInstanceInfo.setInstanceId(instanceId);
        newInstanceInfo.setJobParams(jobParams);
        newInstanceInfo.setInstanceParams(instanceParams);
        newInstanceInfo.setWfInstanceId(wfInstanceId);

        newInstanceInfo.setStatus(InstanceStatus.WAITING_DISPATCH.getV());
        newInstanceInfo.setRunningTimes(0L);
        newInstanceInfo.setExpectedTriggerTime(expectTriggerTime);
        newInstanceInfo.setLastReportTime(-1L);
        newInstanceInfo.setGmtCreate(now);
        newInstanceInfo.setGmtModified(now);

        instanceInfoMapper.insert(newInstanceInfo);
        return newInstanceInfo;
    }
}
