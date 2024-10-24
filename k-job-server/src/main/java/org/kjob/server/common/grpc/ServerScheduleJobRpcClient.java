package org.kjob.server.common.grpc;

import org.kjob.common.constant.RemoteConstant;
import org.kjob.remote.api.ScheduleGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.ScheduleCausa;
import org.kjob.server.extension.singletonpool.GrpcStubSingletonPool;
import org.springframework.stereotype.Component;

@Component
public class ServerScheduleJobRpcClient implements RpcServiceCaller{
    @Override
    public Object call(Object params) {
        ScheduleCausa.ServerScheduleJobReq req = (ScheduleCausa.ServerScheduleJobReq) params;
        ScheduleGrpc.ScheduleBlockingStub stubSingleton = GrpcStubSingletonPool.getStubSingleton(req.getWorkerAddress(), ScheduleGrpc.class, ScheduleGrpc.ScheduleBlockingStub.class, RemoteConstant.WORKER);
        CommonCausa.Response response = stubSingleton.serverScheduleJob(req);
        return response;

    }
}
