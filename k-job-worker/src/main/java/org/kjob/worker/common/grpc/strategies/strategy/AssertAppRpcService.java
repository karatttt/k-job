package org.kjob.worker.common.grpc.strategies.strategy;

import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.common.domain.WorkerAppInfo;
import org.kjob.common.exception.KJobException;
import org.kjob.common.utils.CommonUtils;
import org.kjob.remote.api.ServerDiscoverGrpc;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.RpcInitializer;
import org.kjob.worker.common.grpc.strategies.GrpcStrategy;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class AssertAppRpcService implements GrpcStrategy<TransportTypeEnum> {

    List<ServerDiscoverGrpc.ServerDiscoverBlockingStub> serverDiscoverStubs = new ArrayList<>();
    @Override
    public void init() {
        HashMap<String, ManagedChannel> ip2ChannelsMap = RpcInitializer.getIp2ChannelsMap();
        for (ManagedChannel channel : ip2ChannelsMap.values()) {
            serverDiscoverStubs.add(ServerDiscoverGrpc.newBlockingStub(channel));
        }
    }

    @Override
    public Object execute(Object params) {
        for (ServerDiscoverGrpc.ServerDiscoverBlockingStub serverDiscoverStub : serverDiscoverStubs) {
            try {
                ServerDiscoverCausa.Response response = CommonUtils.executeWithRetry0(() -> serverDiscoverStub.assertApp((ServerDiscoverCausa.AppName) params));
                if(response.getCode() == RemoteConstant.SUCCESS){
                    return response.getWorkInfo();
                } else {
                    log.error("[KJobWorker] assert appName failed, this appName is invalid, please register the appName  first.");
                    throw new KJobException(response.getMessage());
                }
            }
            catch (Exception e){
                log.error("[KJobWorker] grpc error");
            }
        }
        log.error("[KJobWorker] no available server");
        throw new KJobException("no server available");

    }

    @Override
    public TransportTypeEnum getTypeEnumFromStrategyClass() {
        return TransportTypeEnum.ASSERT_APP;
    }
}