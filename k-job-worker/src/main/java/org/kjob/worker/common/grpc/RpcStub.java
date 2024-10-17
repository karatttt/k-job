package org.kjob.worker.common.grpc;


import net.devh.boot.grpc.client.inject.GrpcClient;
import org.kjob.remote.api.ServerDiscoverGrpc;

public class RpcStub {

    @GrpcClient("grpc-server")
    public ServerDiscoverGrpc.ServerDiscoverBlockingStub serverDiscoverStub;

}
