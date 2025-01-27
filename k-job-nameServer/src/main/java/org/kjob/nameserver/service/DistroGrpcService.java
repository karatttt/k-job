package org.kjob.nameserver.service;

import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.devh.boot.grpc.server.service.GrpcService;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.nameserver.core.ServerIpAddressManager;
import org.kjob.remote.api.DistroGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.DistroCausa;

@GrpcService
@AllArgsConstructor
public class DistroGrpcService extends DistroGrpc.DistroImplBase {
    private final ServerIpAddressManager serverIpAddressManager;
    @Override
    public void syncNodeINfo(DistroCausa.SyncNodeInfoReq request, StreamObserver<CommonCausa.Response> responseObserver) {
        if(request.getOperation().equals(RemoteConstant.INCREMENTAL_ADD_SYNC) || request.getOperation().equals(RemoteConstant.FULL_SYNC)) {
            for (String ip : request.getServerIpMapMap().keySet()) {
                serverIpAddressManager.add2ServerAddressSet(ip);
                serverIpAddressManager.addScheduleTimes(ip, 0);
            }
        } else if(request.getOperation().equals(RemoteConstant.INCREMENTAL_REMOVE_SYNC)){
            for (String ip : request.getServerIpMapMap().keySet()) {
                serverIpAddressManager.removeServerAddress(ip);
            }
        }
        CommonCausa.Response build = CommonCausa.Response.newBuilder().build();
        responseObserver.onNext(build);
        responseObserver.onCompleted();
    }
}
