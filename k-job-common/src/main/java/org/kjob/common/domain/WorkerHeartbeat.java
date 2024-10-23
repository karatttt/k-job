package org.kjob.common.domain;

import org.kjob.common.module.SystemMetrics;
import lombok.Data;

import java.util.List;


/**
 * Worker 上报健康信息（worker定时发送的heartbeat）
 *
 * @author tjq
 * @since 2020/3/25
 */
@Data
public class WorkerHeartbeat   {

    /**
     * 本机地址 -> IP:port
     */
    private String workerAddress;
    /**
     * server地址 -> IP
     */
    private String serverIpAddress;
    /**
     * 当前 appName
     */
    private String appName;
    /**
     * 当前 appId
     */
    private Long appId;
    /**
     * 当前时间
     */
    private long heartbeatTime;

    /**
     * worker 版本信息
     */
    private String version;
    /**
     * 使用的通讯协议 AKKA / HTTP
     */
    private String protocol;
    /**
     * worker tag，标识同一个 worker 下的一类集群 ISSUE: 226
     */
    private String tag;
    /**
     * 客户端名称
     */
    private String client;

    /**
     * 是否已经超载，超载的情况下 Server 一段时间内不会再向其派发任务
     */
    private boolean isOverload;

    private int lightTaskTrackerNum;

    private int heavyTaskTrackerNum;


    private SystemMetrics systemMetrics;
}
