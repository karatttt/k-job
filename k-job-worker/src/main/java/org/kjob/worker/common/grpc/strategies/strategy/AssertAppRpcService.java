package org.kjob.worker.common.grpc.strategies.strategy;

import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.common.domain.WorkerAppInfo;
import org.kjob.common.exception.KJobException;
import org.kjob.common.utils.CommonUtils;
import org.kjob.remote.api.ServerDiscoverGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.RpcInitializer;
import org.kjob.worker.common.grpc.strategies.GrpcStrategy;
import org.kjob.worker.common.grpc.strategies.StrategyManager;
import org.kjob.worker.subscribe.WorkerSubscribeManager;
import org.springframework.beans.BeanUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
        ServerDiscoverCausa.AppName appNameInfo = (ServerDiscoverCausa.AppName) params;
        for (ServerDiscoverGrpc.ServerDiscoverBlockingStub serverDiscoverStub : serverDiscoverStubs) {
            try {
                if(WorkerSubscribeManager.isSplit()) {
                    // 需要分组，依附于新的server，优先选择最小连接的server
                    appNameInfo = ServerDiscoverCausa.AppName.newBuilder().setAppName(appNameInfo.getAppName())
                            .setSubAppName(WorkerSubscribeManager.getSubAppName())
                            .setTargetServer(WorkerSubscribeManager.getServerIpList().get(0))
                            .build();
                    log.info("change server to ip:{}", appNameInfo.getTargetServer());
                }
                // 重置状态，防止多次分组
                WorkerSubscribeManager.setSplitStatus(false);

                ServerDiscoverCausa.AppName finalAppNameInfo = appNameInfo;
                CommonCausa.Response response = CommonUtils.executeWithRetry0(() -> serverDiscoverStub.assertApp(finalAppNameInfo));
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
