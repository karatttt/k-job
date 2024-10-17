package org.kjob.worker.transport;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.kjob.common.domain.WorkerAppInfo;
import org.kjob.remote.api.CausaGrpcClientGen;
import org.kjob.remote.api.ServerDiscoverGrpc;

import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.common.grpc.RpcStub;

import java.util.concurrent.ScheduledExecutorService;

public class KJobServerDiscoverService implements ServerDiscoverService{

    KJobWorkerConfig config;

    RpcStub rpcStub;

    public KJobServerDiscoverService(KJobWorkerConfig config) {
        this.config = config;
    }

    @Override
    public WorkerAppInfo assertApp() {
        ServerDiscoverCausa.AppName builder = ServerDiscoverCausa.AppName.newBuilder()
                .setAppName(config.getAppName())
                .build();

        ServerDiscoverCausa.WorkInfo workInfo = rpcStub.serverDiscoverStub.assertApp(builder);
        long appId = workInfo.getAppId();
        System.out.println(appId);
        return null;
    }

    @Override
    public String getCurrentServerAddress() {
        return null;
    }

    @Override
    public void timingCheck(ScheduledExecutorService timingPool) {

    }
}
