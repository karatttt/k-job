package org.kjob.server.persistence.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 
 * @TableName job_info
 */
@TableName(value ="job_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobInfo implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 
     */
    private Long appId;
    /**
     * 
     */
    private Integer dispatchStrategy;
    /**
     *
     */
    private Integer maxInstanceNum;

    /**
     * 
     */
    private Date gmtCreate;
    /**
     * 
     */
    private Date gmtModified;
    /**
     * 
     */
    private String jobDescription;

    /**
     * 
     */
    private String jobName;

    /**
     * 
     */
    private String jobParams;

    /**
     * 
     */
    private String lifecycle;

    /**
     * 
     */
    private Long nextTriggerTime;

    /**
     * 
     */
    private String processorInfo;

    /**
     * 
     */
    private Integer status;


    /**
     * 
     */
    private String timeExpression;

    /**
     * 
     */
    private Integer timeExpressionType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public Long getId() {
        return id;
    }

    /**
     * 
     */
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMaxInstanceNum() {
        return maxInstanceNum;
    }

    public void setMaxInstanceNum(Integer maxInstanceNum) {
        this.maxInstanceNum = maxInstanceNum;
    }

    /**
     * 
     */
    public Long getAppId() {
        return appId;
    }

    /**
     * 
     */
    public void setAppId(Long appId) {
        this.appId = appId;
    }

    /**
     * 
     */
    public Integer getDispatchStrategy() {
        return dispatchStrategy;
    }

    /**
     * 
     */
    public void setDispatchStrategy(Integer dispatchStrategy) {
        this.dispatchStrategy = dispatchStrategy;
    }

    /**
     * 

     * 
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * 
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * 
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * 
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    @Override
    public String toString() {
        return "JobInfo{" +
                "id=" + id +
                ", appId=" + appId +
                ", dispatchStrategy=" + dispatchStrategy +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                ", jobDescription='" + jobDescription + '\'' +
                ", jobName='" + jobName + '\'' +
                ", jobParams='" + jobParams + '\'' +
                ", lifecycle='" + lifecycle + '\'' +
                ", nextTriggerTime=" + nextTriggerTime +
                ", processorInfo='" + processorInfo + '\'' +
                ", status=" + status +
                ", timeExpression='" + timeExpression + '\'' +
                ", timeExpressionType=" + timeExpressionType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobInfo jobInfo = (JobInfo) o;
        return Objects.equals(id, jobInfo.id) && Objects.equals(appId, jobInfo.appId) && Objects.equals(dispatchStrategy, jobInfo.dispatchStrategy)  && Objects.equals(gmtCreate, jobInfo.gmtCreate) && Objects.equals(gmtModified, jobInfo.gmtModified) && Objects.equals(jobDescription, jobInfo.jobDescription) && Objects.equals(jobName, jobInfo.jobName) && Objects.equals(jobParams, jobInfo.jobParams) && Objects.equals(lifecycle, jobInfo.lifecycle) && Objects.equals(nextTriggerTime, jobInfo.nextTriggerTime) && Objects.equals(processorInfo, jobInfo.processorInfo) && Objects.equals(status, jobInfo.status) && Objects.equals(timeExpression, jobInfo.timeExpression) && Objects.equals(timeExpressionType, jobInfo.timeExpressionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, appId, dispatchStrategy, gmtCreate, gmtModified, jobDescription, jobName, jobParams, lifecycle, nextTriggerTime, processorInfo, status, timeExpression, timeExpressionType);
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobParams() {
        return jobParams;
    }

    public void setJobParams(String jobParams) {
        this.jobParams = jobParams;
    }

    public String getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(String lifecycle) {
        this.lifecycle = lifecycle;
    }

    public Long getNextTriggerTime() {
        return nextTriggerTime;
    }

    public void setNextTriggerTime(Long nextTriggerTime) {
        this.nextTriggerTime = nextTriggerTime;
    }

    public String getProcessorInfo() {
        return processorInfo;
    }

    public void setProcessorInfo(String processorInfo) {
        this.processorInfo = processorInfo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTimeExpression() {
        return timeExpression;
    }

    public void setTimeExpression(String timeExpression) {
        this.timeExpression = timeExpression;
    }

    public Integer getTimeExpressionType() {
        return timeExpressionType;
    }

    public void setTimeExpressionType(Integer timeExpressionType) {
        this.timeExpressionType = timeExpressionType;
    }
}