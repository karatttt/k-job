package org.kjob.server.service.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Sets;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.common.exception.KJobException;
import org.kjob.common.utils.net.MyNetUtils;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.server.common.config.KJobServerConfig;
import org.kjob.server.common.grpc.PingServerRpcClient;
import org.kjob.server.extension.lock.LockService;
import org.kjob.server.persistence.domain.AppInfo;
import org.kjob.server.persistence.mapper.AppInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
public class HeartbeatHandler implements RpcHandler{
    private static final int RETRY_TIMES = 10;
    private static final String SERVER_ELECT_LOCK = "server_elect_%d";
    private String ownerIp;

    @Autowired
    private LockService lockService;
    @Autowired
    AppInfoMapper appInfoMapper;
    @Autowired
    PingServerRpcClient pingServerRpcService;
    @Autowired
    KJobServerConfig kJobServerConfig;

    @Override
    public void handle(Object req,StreamObserver<ServerDiscoverCausa.Response> responseObserver) {
        ServerDiscoverCausa.HeartbeatCheck request = (ServerDiscoverCausa.HeartbeatCheck) req;
        // if is local ,return now
        if (checkLocalServer(request.getCurrentServer())) {
            parseResponse(request.getCurrentServer(), responseObserver);
            return;
        }
        log.info("no local, req ip is {}", request.getCurrentServer());

        // origin server

        // cache for avoid ask down server
        Set<String> downServerCache = Sets.newHashSet();
        Long appId = request.getAppId();
        for (int i = 0; i < RETRY_TIMES; i++) {

            // get server in db
            AppInfo appInfo = appInfoMapper.selectOne(new QueryWrapper<AppInfo>()
                    .lambda().eq(AppInfo::getId, appId));
            if (appInfo == null) {
                throw new KJobException(appId + " is not registered!");
            }
            String appName = appInfo.getAppName();
            String originServer = appInfo.getCurrentServer();
            // check the server in db
            String activeAddress = activeAddress(originServer, downServerCache);
            if (StringUtils.isNotEmpty(activeAddress)) {
                parseResponse(activeAddress, responseObserver);
                return;
            }

            // the server in db is no available, so elect new server
            // need lock for avoiding other server elect the different master for the same appId
            String lockName = String.format(SERVER_ELECT_LOCK, appId);
            boolean lockStatus = lockService.tryLock(lockName, 30000);
            if (!lockStatus) {
                try {
                    Thread.sleep(500);
                } catch (Exception ignore) {
                }
                continue;
            }
            try {

                // double check
                appInfo = appInfoMapper.selectOne(new QueryWrapper<AppInfo>()
                        .lambda().eq(AppInfo::getId, appId));
                String address = activeAddress(appInfo.getCurrentServer(), downServerCache);
                if (StringUtils.isNotEmpty(address)) {
                    parseResponse(address, responseObserver);
                    return;
                }

                // this machine be the server of the appid

                appInfo.setCurrentServer(ownerIp);
                appInfoMapper.updateById(appInfo);
                log.info("[ServerElection] this server({}) become the new server for app(appId={}).", appInfo.getCurrentServer(), appId);
                parseResponse(ownerIp, responseObserver);
                return;

            } catch (Exception e) {
                log.error("[ServerElection] write new server to db failed for app {}.", appName, e);
            } finally {
                lockService.unlock(lockName);
            }
        }
        throw new KJobException("server elect failed for app " + appId);
    }

    private String activeAddress(String serverAddress, Set<String> downServerCache) {

        if (downServerCache.contains(serverAddress)) {
            return null;
        }
        if (StringUtils.isEmpty(serverAddress)) {
            return null;
        }

        ServerDiscoverCausa.Ping ping = ServerDiscoverCausa.Ping.newBuilder().setTargetServer(serverAddress).build();

        // ping the targetServer
        ServerDiscoverCausa.Response response = (ServerDiscoverCausa.Response) pingServerRpcService.call(ping);
        if (response.getCode() == RemoteConstant.SUCCESS) {
            return serverAddress;
        }
        downServerCache.add(serverAddress);
        return null;


    }

    private void parseResponse(String currentServer, StreamObserver<ServerDiscoverCausa.Response> responseObserver) {
        ServerDiscoverCausa.Response response = ServerDiscoverCausa.Response.newBuilder()
                .setCode(RemoteConstant.SUCCESS)
                .setAvailableServer(
                        ServerDiscoverCausa.AvailableServer.newBuilder().setAvailableServer(currentServer).build()
                ).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    private boolean checkLocalServer(String currentServer) {
        // 获取本机的InetAddress对象
        ownerIp = kJobServerConfig.getAddress();
        return Objects.equals(ownerIp, currentServer);
    }
}
