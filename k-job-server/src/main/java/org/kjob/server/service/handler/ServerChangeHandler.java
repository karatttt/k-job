package org.kjob.server.service.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.kjob.remote.protos.CommonCausa;
import org.kjob.remote.protos.RegisterCausa;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.server.persistence.domain.AppInfo;
import org.kjob.server.persistence.mapper.AppInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.xml.ws.Response;

@Component
@Slf4j
public class ServerChangeHandler implements RpcHandler{
    @Autowired
    AppInfoMapper appInfoMapper;
    @Override
    public void handle(Object req, StreamObserver<CommonCausa.Response> responseObserver) {
        ServerDiscoverCausa.ServerChangeReq serverChangeReq = (ServerDiscoverCausa.ServerChangeReq) req;
        AppInfo appInfo = AppInfo.builder().currentServer(serverChangeReq.getTargetServer()).build();
        appInfoMapper.update(appInfo, new QueryWrapper<AppInfo>().lambda()
                .eq(AppInfo::getAppName, serverChangeReq.getAppName()));
        log.info("[KJobServerChange] app :{} change to new server :{}", serverChangeReq.getAppName(), serverChangeReq.getTargetServer());
        CommonCausa.Response build = CommonCausa.Response.newBuilder().setCode(200).build();
        responseObserver.onNext(build);
        responseObserver.onCompleted();
    }
}
