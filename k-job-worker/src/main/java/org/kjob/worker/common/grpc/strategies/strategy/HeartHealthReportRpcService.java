package org.kjob.worker.common.grpc.strategies.strategy;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import org.kjob.remote.api.ScheduleGrpc;
import org.kjob.remote.api.ServerDiscoverGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.ScheduleCausa;
import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.RpcInitializer;
import org.kjob.worker.common.grpc.strategies.GrpcStrategy;

import java.util.HashMap;

public class HeartHealthReportRpcService implements GrpcStrategy<TransportTypeEnum> {
    HashMap<String, ScheduleGrpc.ScheduleBlockingStub> ip2Stubs = new HashMap<>();

    @Override
    public void init() {
        HashMap<String, ManagedChannel> ip2ChannelsMap = RpcInitializer.getIp2ChannelsMap();
        for (String ip : ip2ChannelsMap.keySet()) {
            ip2Stubs.put(ip, ScheduleGrpc.newBlockingStub(ip2ChannelsMap.get(ip)));
        }
    }

    @Override
    public Object execute(Object params) {
        ScheduleCausa.WorkerHeartbeat workerHeartbeat = (ScheduleCausa.WorkerHeartbeat) params;
        ScheduleGrpc.ScheduleBlockingStub stub = ip2Stubs.get(workerHeartbeat.getServerIpAddress());
        CommonCausa.Response response = stub.reportWorkerHeartbeat(workerHeartbeat);
        return null;
    }

    @Override
    public TransportTypeEnum getTypeEnumFromStrategyClass() {
        return TransportTypeEnum.HEARTBEAT_HEALTH_REPORT;
    }
}
