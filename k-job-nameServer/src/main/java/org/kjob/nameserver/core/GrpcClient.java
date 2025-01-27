package org.kjob.nameserver.core;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.kjob.nameserver.config.KJobNameServerConfig;
import org.kjob.remote.api.DistroGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.DistroCausa;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class GrpcClient {
    private final Map<String, DistroGrpc.DistroFutureStub> clusterStubMap = new HashMap<>();

    public GrpcClient(KJobNameServerConfig kJobNameServerConfig){
        for (String ip : kJobNameServerConfig.getServerAddressList()) {
            String[] split = ip.split(":");
            ManagedChannel channel = ManagedChannelBuilder.forAddress(split[0], Integer.parseInt(split[1])).build();
            DistroGrpc.DistroFutureStub distroFutureStub = DistroGrpc.newFutureStub(channel);
            clusterStubMap.put(ip, distroFutureStub);
        }
    }
    public  void SyncNodeInfo(HashMap<String, Long> map, String targetServerIp, String operation){
        DistroCausa.SyncNodeInfoReq.Builder builder = DistroCausa.SyncNodeInfoReq.newBuilder();
        DistroCausa.SyncNodeInfoReq build = builder.putAllServerIpMap(map)
                .setOperation(operation).build();
        DistroGrpc.DistroFutureStub distroFutureStub = clusterStubMap.get(targetServerIp);

        distroFutureStub.syncNodeINfo(build);
    }
}
