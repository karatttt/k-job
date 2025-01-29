package org.kjob.server.register;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.remote.api.RegisterToNameServerGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.RegisterCausa;
import org.kjob.server.common.config.KJobServerConfig;
import org.kjob.server.extension.singletonpool.GrpcStubSingletonPool;
import org.kjob.server.persistence.mapper.InstanceInfoMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
@Slf4j
public class ServerRegisterStarter implements InitializingBean {

    @Autowired
    KJobServerConfig kJobServerConfig;
    @Override
    public void afterPropertiesSet() throws Exception {
        ThreadFactory registerThreadFactory = new ThreadFactoryBuilder().setNameFormat("kjob-server-register-%d").build();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3, registerThreadFactory);

        // get stub
        String s = kJobServerConfig.getNameServerAddress().split(":")[0];
        RegisterToNameServerGrpc.RegisterToNameServerBlockingStub stubSingleton = GrpcStubSingletonPool.getStubSingleton(s, RegisterToNameServerGrpc.class, RegisterToNameServerGrpc.RegisterToNameServerBlockingStub.class, RemoteConstant.NAMESERVER);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    RegisterCausa.ServerRegisterReporter build = RegisterCausa.ServerRegisterReporter.newBuilder()
                            .setServerIpAddress(kJobServerConfig.getAddress() + ":" + kJobServerConfig.getServerPort())
                            .setRegisterTimestamp(System.currentTimeMillis())
                            .build();
                    CommonCausa.Response response = stubSingleton.serverRegister(build);
                    log.info("server register to nameServer success");
                } catch (Exception e){
                    log.error("server register to nameServer error");
                }
            }
        }, 0, 10,TimeUnit.SECONDS);
    }
}
