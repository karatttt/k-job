package org.kjob.worker.subscribe;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.remote.api.RegisterToNameServerGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.RegisterCausa;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.strategies.StrategyCaller;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
@Slf4j
public class WorkerSubscribeStarter {

    public static void start(String appName){
        ThreadFactory registerThreadFactory = new ThreadFactoryBuilder().setNameFormat("kjob-server-register-%d").build();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3, registerThreadFactory);
        RegisterCausa.WorkerSubscribeReq build = RegisterCausa.WorkerSubscribeReq.newBuilder().setAppName(appName).build();
        StrategyCaller.call(TransportTypeEnum.REGISTER_TO_NAMESERVER, build);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    StrategyCaller.call(TransportTypeEnum.REGISTER_TO_NAMESERVER, build);
                    log.info("worker subscribe to nameServer success");

                } catch (Exception e){
                    log.error("worker register to nameServer error");
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

}
