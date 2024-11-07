package org.kjob.server.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.enums.DispatchStrategy;
import org.kjob.common.enums.SwitchableStatus;
import org.kjob.common.module.LifeCycle;
import org.kjob.common.utils.JsonUtils;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.MqCausa;
import org.kjob.server.persistence.domain.JobInfo;
import org.kjob.server.persistence.mapper.JobInfoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class ConsumerHandler implements RpcHandler {
    @Autowired
    JobInfoMapper jobInfoMapper;
    @Override
    public void handle(Object req, StreamObserver<CommonCausa.Response> responseObserver) {
        MqCausa.CreateJobReq jobReq = (MqCausa.CreateJobReq) req;

        JobInfo build2 = JobInfo.builder().jobDescription(jobReq.getJobDescription())
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
                .status(SwitchableStatus.ENABLE.getV()).build();


        jobInfoMapper.insert(build2);
        log.info("insert jobName :{} success", build2.getJobName());
        MqCausa.CreateJobRes build = MqCausa.CreateJobRes.newBuilder().setJobId(build2.getId()).build();
        CommonCausa.Response build1 = CommonCausa.Response.newBuilder().setCreateJobRes(build).build();
        responseObserver.onNext(build1);
        responseObserver.onCompleted();
    }
}
