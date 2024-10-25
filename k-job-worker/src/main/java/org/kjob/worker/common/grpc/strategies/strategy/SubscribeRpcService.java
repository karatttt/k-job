package org.kjob.worker.common.grpc.strategies.strategy;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.kjob.remote.api.RegisterToNameServerGrpc;
import org.kjob.remote.api.ScheduleGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.RegisterCausa;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.RpcInitializer;
import org.kjob.worker.common.grpc.strategies.GrpcStrategy;

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
        CommonCausa.Response response = stub.workerSubscribe(workerSubscribeReq);
        RegisterCausa.WorkerSubscribeResponse workerSubscribeResponse = response.getWorkerSubscribeResponse();
        workerSubscribeResponse.getServerAddressIpLists();
    }

    @Override
    public TransportTypeEnum getTypeEnumFromStrategyClass() {
        return TransportTypeEnum.REGISTER_TO_NAMESERVER;
    }
}
