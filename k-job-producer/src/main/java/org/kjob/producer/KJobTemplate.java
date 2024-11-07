package org.kjob.producer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import org.kjob.common.enums.TimeExpressionType;
import org.kjob.common.module.LifeCycle;
import org.kjob.common.utils.JsonUtils;
import org.kjob.producer.entity.JobCreateReq;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.MqCausa;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class KJobTemplate {
    ProducerManager producerManager;

    /**
     *
     * @param nameServerAddress 127.0.0.1:9083,127.0.0.2:9083...
     */
    public KJobTemplate(String nameServerAddress) {
        ArrayList<String> nameServerAddressList = Lists.newArrayList(nameServerAddress);
        producerManager = new ProducerManager(nameServerAddressList);
    }
    public Long createJob(JobCreateReq jobCreateReq){

        MqCausa.CreateJobReq build = MqCausa.CreateJobReq.newBuilder()
                .setAppName(jobCreateReq.getAppName())
                .setJobParams(jobCreateReq.getJobParams())
                .setJobDescription(jobCreateReq.getJobDescription())
                .setProcessorInfo(jobCreateReq.getProcessorInfo())
                .setTimeExpressionType(TimeExpressionType.getProtoBufTimeExpressionType(jobCreateReq.getTimeExpressionType()))
                .setTimeExpression(jobCreateReq.getTimeExpression())
                .setLifeCycle(JsonUtils.toJSONString(jobCreateReq.getLifeCycle()))
                .setMaxInstanceNum(jobCreateReq.getMaxInstanceNum()).build();

        ListenableFuture<CommonCausa.Response> future = producerManager.getStub().createJob(build);
        try {
            CommonCausa.Response response = future.get();
            return response.getCreateJobRes().getJobId();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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
