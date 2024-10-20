package org.kjob.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Sets;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.StringUtils;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.common.exception.KJobException;
import org.kjob.remote.api.ServerDiscoverGrpc;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.server.common.grpc.PingServerRpcClient;
import org.kjob.server.persistence.domain.AppInfo;
import org.kjob.server.extension.lock.LockService;
import org.kjob.server.persistence.mapper.AppInfoMapper;
import org.kjob.server.service.handler.AppInfoHandler;
import org.kjob.server.service.handler.HeartbeatHandler;
import org.kjob.server.service.handler.PongHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Set;

@GrpcService
@Slf4j
public class ServerDiscoverGrpcService extends ServerDiscoverGrpc.ServerDiscoverImplBase {

    @Autowired
    HeartbeatHandler heartbeatHandler;
    @Autowired
    AppInfoHandler appInfoHandler;
    @Autowired
    PongHandler pongHandler;

    public void heartbeatCheck(ServerDiscoverCausa.HeartbeatCheck request, StreamObserver<ServerDiscoverCausa.Response> responseObserver) {
        heartbeatHandler.handle(request, responseObserver);
    }
    public void assertApp(ServerDiscoverCausa.AppName request, StreamObserver<ServerDiscoverCausa.Response> responseObserver) {
        appInfoHandler.handle(request, responseObserver);
    }
    public void pingServer(ServerDiscoverCausa.Ping request, StreamObserver<ServerDiscoverCausa.Response> responseObserver) {
        pongHandler.handle(request, responseObserver);
    }
}
