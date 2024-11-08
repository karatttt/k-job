package org.kjob.producer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import org.kjob.common.enums.TimeExpressionType;
import org.kjob.common.module.LifeCycle;
import org.kjob.common.utils.JsonUtils;
import org.kjob.producer.entity.JobCreateReq;
import org.kjob.producer.uid.IdGenerateService;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.MqCausa;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class KJobTemplate {
    MessageSendClient messageSendClient;
    IdGenerateService idGenerateService;
    /**
     *
     * @param nameServerAddress 127.0.0.1:9083,127.0.0.2:9083...
     */
    public KJobTemplate(String nameServerAddress) {
        messageSendClient = new MessageSendClient(nameServerAddress);
        idGenerateService = new IdGenerateService();
    }
    public Long createJob(JobCreateReq jobCreateReq){
        // 生成jobId
        long jobId = idGenerateService.allocate();

        MqCausa.CreateJobReq build = MqCausa.CreateJobReq.newBuilder()
                .setJobId(jobId)
                .setAppName(jobCreateReq.getAppName())
                .setJobName(jobCreateReq.getJobName())
                .setJobParams(jobCreateReq.getJobParams())
                .setJobDescription(jobCreateReq.getJobDescription())
                .setProcessorInfo(jobCreateReq.getProcessorInfo())
                .setTimeExpressionType(TimeExpressionType.getProtoBufTimeExpressionType(jobCreateReq.getTimeExpressionType()))
                .setTimeExpression(jobCreateReq.getTimeExpression())
                .setLifeCycle(JsonUtils.toJSONString(jobCreateReq.getLifeCycle()))
                .setMaxInstanceNum(jobCreateReq.getMaxInstanceNum()).build();
        MqCausa.Message build1 = MqCausa.Message.newBuilder().setCreateJobReq(build)
                .setMessageType(MqCausa.MessageType.JOB_CREATE)
                .build();
        messageSendClient.sendMessageAsync(new AtomicInteger(0), build1);

        return jobId;
    }

    public static void main(String[] args) {
        KJobTemplate kJobTemplate = new KJobTemplate("127.0.0.1:9083");
        JobCreateReq build = JobCreateReq.builder()
                .appName("ds")
                .jobDescription("ds")
                .jobName("ds")
                .lifeCycle(new LifeCycle())
                .processorInfo("ds")
                .timeExpression("ds")
                .maxInstanceNum(5)
                .jobParams("ewew")
                .timeExpressionType(TimeExpressionType.CRON).build();
        kJobTemplate.createJob(build);
    }
}
