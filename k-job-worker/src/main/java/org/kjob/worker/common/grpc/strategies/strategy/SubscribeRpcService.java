package org.kjob.worker.common.grpc.strategies.strategy;

import com.google.protobuf.ProtocolStringList;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.kjob.common.utils.net.MyNetUtil;
import org.kjob.remote.api.RegisterToNameServerGrpc;
import org.kjob.remote.api.ScheduleGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.RegisterCausa;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.RpcInitializer;
import org.kjob.worker.common.grpc.strategies.GrpcStrategy;
import org.kjob.worker.subscribe.WorkerSubscribeManager;

import java.util.HashMap;

public class SubscribeRpcService implements GrpcStrategy<TransportTypeEnum> {

    RegisterToNameServerGrpc.RegisterToNameServerBlockingStub stub;
    @Override
    public void init() {
        String nameServerAddress = RpcInitializer.getNameServerAddress();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(nameServerAddress.split(":")[0], Integer.parseInt(nameServerAddress.split(":")[1]))
                .usePlaintext()
                .build();
        stub = RegisterToNameServerGrpc.newBlockingStub(channel);
    }

    @Override
    public Object execute(Object params) {
        RegisterCausa.WorkerSubscribeReq workerSubscribeReq = (RegisterCausa.WorkerSubscribeReq)params;
        RegisterCausa.WorkerSubscribeReq build = RegisterCausa.WorkerSubscribeReq.newBuilder()
                .setAppName(workerSubscribeReq.getAppName())
                .setWorkerIpAddress(MyNetUtil.address).build();
        CommonCausa.Response response = stub.workerSubscribe(build);
        RegisterCausa.WorkerSubscribeResponse workerSubscribeResponse = response.getWorkerSubscribeResponse();

        if(workerSubscribeResponse.getIsSplit()){
            WorkerSubscribeManager.setSplitStatus(true);
            WorkerSubscribeManager.setSubAppName(response.getWorkerSubscribeResponse().getSubAppName());
        }
        WorkerSubscribeManager.setServerIpList(response.getWorkerSubscribeResponse().getServerAddressIpListsList());
        return null;
    }

    @Override
    public TransportTypeEnum getTypeEnumFromStrategyClass() {
        return TransportTypeEnum.REGISTER_TO_NAMESERVER;
    }
}
