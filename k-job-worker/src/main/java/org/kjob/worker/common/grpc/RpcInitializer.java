package org.kjob.worker.common.grpc;


import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.strategies.GrpcStrategy;
import org.kjob.worker.common.grpc.strategies.StrategyManager;
import org.kjob.worker.common.grpc.strategies.strategy.AssertAppRpcService;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
@Slf4j
public class RpcInitializer {
    private final int port;
    private final List<String> serverList;


    @Getter
    private static final HashMap<String, ManagedChannel> ip2ChannelsMap = new HashMap<>();
    public RpcInitializer(int port, List<String> serverList){
        this.port = port;
        this.serverList = serverList;

    }

    @SuppressWarnings("rawtypes")
    public void initRpcStrategies(){
        // register channels for stub

        for (String server : serverList) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(server, port)
                    .usePlaintext()
                    .build();
            ip2ChannelsMap.put(server,channel);
        }


        Reflections reflections = new Reflections("org.kjob.worker.common.grpc.strategies.strategy");
        Set<Class<? extends GrpcStrategy>> strategyClasses = reflections.getSubTypesOf(GrpcStrategy.class);

        for (Class<? extends GrpcStrategy> strategyClass : strategyClasses) {
            try {
                GrpcStrategy strategyInstance = strategyClass.getDeclaredConstructor().newInstance();
                TransportTypeEnum typeEnum = strategyInstance.getTypeEnumFromStrategyClass();
                strategyInstance.init();
                StrategyManager.registerCausa(typeEnum, strategyInstance);
            } catch (Exception e) {
               log.warn("creating strategy error");
            }
        }
    }

}
