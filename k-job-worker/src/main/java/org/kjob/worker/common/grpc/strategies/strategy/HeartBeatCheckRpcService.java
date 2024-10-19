package org.kjob.worker.common.grpc.strategies.strategy;

import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.common.exception.KJobException;
import org.kjob.remote.api.ServerDiscoverGrpc;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.RpcInitializer;
import org.kjob.worker.common.grpc.strategies.GrpcStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
@Slf4j
public class HeartBeatCheckRpcService implements GrpcStrategy<TransportTypeEnum> {
    HashMap<String, ServerDiscoverGrpc.ServerDiscoverBlockingStub> ip2serverDiscoverStubs = new HashMap<>();

    @Override
    public void init() {
        HashMap<String, ManagedChannel> ip2ChannelsMap = RpcInitializer.getIp2ChannelsMap();
        for (String ip : ip2ChannelsMap.keySet()) {
            ip2serverDiscoverStubs.put(ip, ServerDiscoverGrpc.newBlockingStub(ip2ChannelsMap.get(ip)));
        }
    }

    @Override
    public Object execute(Object params) {
        ServerDiscoverCausa.HeartbeatCheck heartbeatCheck = (ServerDiscoverCausa.HeartbeatCheck)params;
        ServerDiscoverGrpc.ServerDiscoverBlockingStub stub= ip2serverDiscoverStubs.get(heartbeatCheck.getCurrentServer());

        try {
            ServerDiscoverCausa.Response response = stub.heartbeatCheck(heartbeatCheck);
            if(response.getCode() == RemoteConstant.SUCCESS){
                return response.getAvailableServer();
            } else {
                log.error("[KJobWorker] heartbeat error");
                throw new KJobException(response.getMessage());
            }
        }
        catch (Exception e){
            log.error("[KJobWorker] grpc error");

        }
        return null;

    }

    @Override
    public TransportTypeEnum getTypeEnumFromStrategyClass() {
        return TransportTypeEnum.HEARTBEAT_CHECK;
    }
}
