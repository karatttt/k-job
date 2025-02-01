package org.kjob.worker.common.grpc.strategies.strategy;

import com.google.protobuf.ProtocolStringList;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.utils.net.MyNetUtil;
import org.kjob.remote.api.RegisterToNameServerGrpc;
import org.kjob.remote.api.ScheduleGrpc;
import org.kjob.remote.api.ServerDiscoverGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.RegisterCausa;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.RpcInitializer;
import org.kjob.worker.common.grpc.strategies.GrpcStrategy;
import org.kjob.worker.core.discover.ServerDiscoverService;
import org.kjob.worker.subscribe.WorkerSubscribeManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class SubscribeRpcService implements GrpcStrategy<TransportTypeEnum> {

    RegisterToNameServerGrpc.RegisterToNameServerBlockingStub stub;
    HashMap<String, ServerDiscoverGrpc.ServerDiscoverBlockingStub> ip2serverDiscoverStubs = new HashMap<>();

    @Override
    public void init() {
        String nameServerAddress = RpcInitializer.getNameServerAddress();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(nameServerAddress.split(":")[0], Integer.parseInt(nameServerAddress.split(":")[1]))
                .usePlaintext()
                .build();
        stub = RegisterToNameServerGrpc.newBlockingStub(channel);

        HashMap<String, ManagedChannel> ip2ChannelsMap = RpcInitializer.getIp2ChannelsMap();
        for (String ip : ip2ChannelsMap.keySet()) {
            ip2serverDiscoverStubs.put(ip, ServerDiscoverGrpc.newBlockingStub(ip2ChannelsMap.get(ip)));
        }
    }

    @Override
    public Object execute(Object params) {
        RegisterCausa.WorkerSubscribeReq workerSubscribeReq = (RegisterCausa.WorkerSubscribeReq) params;
        // add serverIp and scheduleTime to req
        RegisterCausa.WorkerSubscribeReq build = RegisterCausa.WorkerSubscribeReq.newBuilder()
                .setSubscribeTimestamp(workerSubscribeReq.getSubscribeTimestamp())
                .setAppName(workerSubscribeReq.getAppName())
                .setServerIpAddress(WorkerSubscribeManager.getCurrentServerIp())
                .setScheduleTime(WorkerSubscribeManager.getScheduleTimes().get())
                .setWorkerIpAddress(MyNetUtil.address).build();
        CommonCausa.Response response = stub.workerSubscribe(build);
        RegisterCausa.WorkerSubscribeResponse workerSubscribeResponse = response.getWorkerSubscribeResponse();
        // 标记需要分组, assertApp时会根据标记发起分组请求
        if (workerSubscribeResponse.getIsSplit()) {
            WorkerSubscribeManager.setSplitStatus(true);
            WorkerSubscribeManager.setSubAppName(response.getWorkerSubscribeResponse().getSubAppName());
        }

        // 需要更换server, 发起更换Server请求
        if (workerSubscribeResponse.getIsChangeServer()) {
            ServerDiscoverCausa.ServerChangeReq build1 = ServerDiscoverCausa.ServerChangeReq.newBuilder()
                    .setAppName(workerSubscribeReq.getAppName())
                    .setTargetServer(workerSubscribeResponse.getServerAddressIpListsList().get(0))
                    .build();
            ServerDiscoverGrpc.ServerDiscoverBlockingStub stub1 = ip2serverDiscoverStubs.get(build.getServerIpAddress());
            CommonCausa.Response response1 = stub1.serverChange(build1);
        }

        WorkerSubscribeManager.setServerIpList(response.getWorkerSubscribeResponse().getServerAddressIpListsList());
        log.info("[KJobSubscribeService] subscribe success, schedule :{} Times in this interval", WorkerSubscribeManager.getScheduleTimes());


        // 重置调度时间
        WorkerSubscribeManager.resetScheduleTimes();
        return null;
    }

    @Override
    public TransportTypeEnum getTypeEnumFromStrategyClass() {
        return TransportTypeEnum.REGISTER_TO_NAMESERVER;
    }
}
