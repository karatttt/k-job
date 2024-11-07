package org.kjob.producer;

import com.google.protobuf.ProtocolStringList;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.kjob.remote.api.MqGrpc;
import org.kjob.remote.api.RegisterToNameServerGrpc;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.MqCausa;
import org.kjob.remote.protos.RegisterCausa;

import java.util.ArrayList;
import java.util.List;
@Slf4j
public class ProducerManager {
    private List<String> serverAddressList;
    private Long index = 0L;
    List<MqGrpc.MqFutureStub> stubs = new ArrayList<>();

    public ProducerManager(List<String> nameServerAddressList) {
        for (String server : nameServerAddressList) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(server.split(":")[0], Integer.parseInt(server.split(":")[1]))
                    .usePlaintext()
                    .build();

            RegisterToNameServerGrpc.RegisterToNameServerBlockingStub stub = RegisterToNameServerGrpc.newBlockingStub(channel);
            RegisterCausa.FetchServerAddressListReq build = RegisterCausa.FetchServerAddressListReq.newBuilder().build();
            try {
                CommonCausa.Response response = stub.fetchServerList(build);
                serverAddressList = response.getServerAddressList().getServerAddressListList();
                initStubs();
                break;
            } catch (Exception e){
                log.error("nameServer :{} connect error", server);
            }
        }
        log.error("allNameServer lose");


    }
    private void initStubs() {
        for (String server : serverAddressList) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(server.split(":")[0], Integer.parseInt(server.split(":")[1]))
                    .usePlaintext()
                    .build();
            stubs.add(MqGrpc.newFutureStub(channel));
        }
    }
    public MqGrpc.MqFutureStub getStub(){
        return stubs.get((int) (index++ % stubs.size()));
    }


}
