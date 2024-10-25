package org.kjob.worker.subscribe;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.remote.api.RegisterToNameServerGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.RegisterCausa;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class WorkerSubscribeStarter {

    public static void start(){
        ThreadFactory registerThreadFactory = new ThreadFactoryBuilder().setNameFormat("kjob-server-register-%d").build();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3, registerThreadFactory);

        // get stub
        String s = kJobServerConfig.getNameServerAddress().split(":")[0];
        RegisterToNameServerGrpc.RegisterToNameServerBlockingStub stubSingleton = GrpcStubSingletonPool.getStubSingleton(s, RegisterToNameServerGrpc.class, RegisterToNameServerGrpc.RegisterToNameServerBlockingStub.class, RemoteConstant.NAMESERVER);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    RegisterCausa.serverRegisterReporter build = RegisterCausa.serverRegisterReporter.newBuilder().setServerIpAddress(kJobServerConfig.getAddress()).build();
                    CommonCausa.Response response = stubSingleton.serverRegister(build);
                } catch (Exception e){
                    log.error("server register to nameServer error");
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

}
