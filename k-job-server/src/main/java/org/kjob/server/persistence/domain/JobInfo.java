package org.kjob.server.persistence.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName job_info
 */
@TableName(value ="job_info")
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
    private Integer executeType;

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
    private Integer instanceRetryNum;

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
    private Integer maxInstanceNum;

    /**
     * 
     */
    private Integer maxWorkerCount;

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
    private Integer processorType;

    /**
     * 
     */
    private Integer status;

    /**
     * 
     */
    private Integer taskRetryNum;

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
     */
    public Integer getExecuteType() {
        return executeType;
    }

    /**
     * 
     */
    public void setExecuteType(Integer executeType) {
        this.executeType = executeType;
    }

    /**
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

    /**
     * 
     */
    public Integer getInstanceRetryNum() {
        return instanceRetryNum;
    }

    /**
     * 
     */
    public void setInstanceRetryNum(Integer instanceRetryNum) {
        this.instanceRetryNum = instanceRetryNum;
    }

    /**
     * 
     */
    public String getJobDescription() {
        return jobDescription;
    }

    /**
     * 
     */
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    /**
     * 
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * 
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * 
     */
    public String getJobParams() {
        return jobParams;
    }

    /**
     * 
     */
    public void setJobParams(String jobParams) {
        this.jobParams = jobParams;
    }

    /**
     * 
     */
    public String getLifecycle() {
        return lifecycle;
    }

    /**
     * 
     */
    public void setLifecycle(String lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * 
     */
    public Integer getMaxInstanceNum() {
        return maxInstanceNum;
    }

    /**
     * 
     */
    public void setMaxInstanceNum(Integer maxInstanceNum) {
        this.maxInstanceNum = maxInstanceNum;
    }

    /**
     * 
     */
    public Integer getMaxWorkerCount() {
        return maxWorkerCount;
    }

    /**
     * 
     */
    public void setMaxWorkerCount(Integer maxWorkerCount) {
        this.maxWorkerCount = maxWorkerCount;
    }

    /**
     * 
     */
    public Long getNextTriggerTime() {
        return nextTriggerTime;
    }

    /**
     * 
     */
    public void setNextTriggerTime(Long nextTriggerTime) {
        this.nextTriggerTime = nextTriggerTime;
    }

    /**
     * 
     */
    public String getProcessorInfo() {
        return processorInfo;
    }

    /**
     * 
     */
    public void setProcessorInfo(String processorInfo) {
        this.processorInfo = processorInfo;
    }

    /**
     * 
     */
    public Integer getProcessorType() {
        return processorType;
    }

    /**
     * 
     */
    public void setProcessorType(Integer processorType) {
        this.processorType = processorType;
    }

    /**
     * 
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 
     */
    public Integer getTaskRetryNum() {
        return taskRetryNum;
    }

    /**
     * 
     */
    public void setTaskRetryNum(Integer taskRetryNum) {
        this.taskRetryNum = taskRetryNum;
    }

    /**
     * 
     */
    public String getTimeExpression() {
        return timeExpression;
    }

    /**
     * 
     */
    public void setTimeExpression(String timeExpression) {
        this.timeExpression = timeExpression;
    }

    /**
     * 
     */
    public Integer getTimeExpressionType() {
        return timeExpressionType;
    }

    /**
     * 
     */
    public void setTimeExpressionType(Integer timeExpressionType) {
        this.timeExpressionType = timeExpressionType;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        JobInfo other = (JobInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAppId() == null ? other.getAppId() == null : this.getAppId().equals(other.getAppId()))
            && (this.getDispatchStrategy() == null ? other.getDispatchStrategy() == null : this.getDispatchStrategy().equals(other.getDispatchStrategy()))
            && (this.getExecuteType() == null ? other.getExecuteType() == null : this.getExecuteType().equals(other.getExecuteType()))
            && (this.getGmtCreate() == null ? other.getGmtCreate() == null : this.getGmtCreate().equals(other.getGmtCreate()))
            && (this.getGmtModified() == null ? other.getGmtModified() == null : this.getGmtModified().equals(other.getGmtModified()))
            && (this.getInstanceRetryNum() == null ? other.getInstanceRetryNum() == null : this.getInstanceRetryNum().equals(other.getInstanceRetryNum()))
            && (this.getJobDescription() == null ? other.getJobDescription() == null : this.getJobDescription().equals(other.getJobDescription()))
            && (this.getJobName() == null ? other.getJobName() == null : this.getJobName().equals(other.getJobName()))
            && (this.getJobParams() == null ? other.getJobParams() == null : this.getJobParams().equals(other.getJobParams()))
            && (this.getLifecycle() == null ? other.getLifecycle() == null : this.getLifecycle().equals(other.getLifecycle()))
            && (this.getMaxInstanceNum() == null ? other.getMaxInstanceNum() == null : this.getMaxInstanceNum().equals(other.getMaxInstanceNum()))
            && (this.getMaxWorkerCount() == null ? other.getMaxWorkerCount() == null : this.getMaxWorkerCount().equals(other.getMaxWorkerCount()))
            && (this.getNextTriggerTime() == null ? other.getNextTriggerTime() == null : this.getNextTriggerTime().equals(other.getNextTriggerTime()))
            && (this.getProcessorInfo() == null ? other.getProcessorInfo() == null : this.getProcessorInfo().equals(other.getProcessorInfo()))
            && (this.getProcessorType() == null ? other.getProcessorType() == null : this.getProcessorType().equals(other.getProcessorType()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getTaskRetryNum() == null ? other.getTaskRetryNum() == null : this.getTaskRetryNum().equals(other.getTaskRetryNum()))
            && (this.getTimeExpression() == null ? other.getTimeExpression() == null : this.getTimeExpression().equals(other.getTimeExpression()))
            && (this.getTimeExpressionType() == null ? other.getTimeExpressionType() == null : this.getTimeExpressionType().equals(other.getTimeExpressionType()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAppId() == null) ? 0 : getAppId().hashCode());
        result = prime * result + ((getDispatchStrategy() == null) ? 0 : getDispatchStrategy().hashCode());
        result = prime * result + ((getExecuteType() == null) ? 0 : getExecuteType().hashCode());
        result = prime * result + ((getGmtCreate() == null) ? 0 : getGmtCreate().hashCode());
        result = prime * result + ((getGmtModified() == null) ? 0 : getGmtModified().hashCode());
        result = prime * result + ((getInstanceRetryNum() == null) ? 0 : getInstanceRetryNum().hashCode());
        result = prime * result + ((getJobDescription() == null) ? 0 : getJobDescription().hashCode());
        result = prime * result + ((getJobName() == null) ? 0 : getJobName().hashCode());
        result = prime * result + ((getJobParams() == null) ? 0 : getJobParams().hashCode());
        result = prime * result + ((getLifecycle() == null) ? 0 : getLifecycle().hashCode());
        result = prime * result + ((getMaxInstanceNum() == null) ? 0 : getMaxInstanceNum().hashCode());
        result = prime * result + ((getMaxWorkerCount() == null) ? 0 : getMaxWorkerCount().hashCode());
        result = prime * result + ((getNextTriggerTime() == null) ? 0 : getNextTriggerTime().hashCode());
        result = prime * result + ((getProcessorInfo() == null) ? 0 : getProcessorInfo().hashCode());
        result = prime * result + ((getProcessorType() == null) ? 0 : getProcessorType().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getTaskRetryNum() == null) ? 0 : getTaskRetryNum().hashCode());
        result = prime * result + ((getTimeExpression() == null) ? 0 : getTimeExpression().hashCode());
        result = prime * result + ((getTimeExpressionType() == null) ? 0 : getTimeExpressionType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", appId=").append(appId);
        sb.append(", dispatchStrategy=").append(dispatchStrategy);
        sb.append(", executeType=").append(executeType);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", gmtModified=").append(gmtModified);
        sb.append(", instanceRetryNum=").append(instanceRetryNum);
        sb.append(", jobDescription=").append(jobDescription);
        sb.append(", jobName=").append(jobName);
        sb.append(", jobParams=").append(jobParams);
        sb.append(", lifecycle=").append(lifecycle);
        sb.append(", maxInstanceNum=").append(maxInstanceNum);
        sb.append(", maxWorkerCount=").append(maxWorkerCount);
        sb.append(", nextTriggerTime=").append(nextTriggerTime);
        sb.append(", processorInfo=").append(processorInfo);
        sb.append(", processorType=").append(processorType);
        sb.append(", status=").append(status);
        sb.append(", taskRetryNum=").append(taskRetryNum);
        sb.append(", timeExpression=").append(timeExpression);
        sb.append(", timeExpressionType=").append(timeExpressionType);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}