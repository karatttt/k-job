package org.kjob.server.service.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.grpc.stub.StreamObserver;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.remote.protos.ServerDiscoverCausa;
import org.kjob.server.persistence.domain.AppInfo;
import org.kjob.server.persistence.mapper.AppInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppInfoHandler implements RpcHandler {

    @Autowired
    AppInfoMapper appInfoMapper;

    @Override
    public void handle(Object req, StreamObserver<ServerDiscoverCausa.Response> responseObserver) {
        ServerDiscoverCausa.AppName request = (ServerDiscoverCausa.AppName) req;
        System.out.println(("请求中的参数为msg:{},code:{}" + request.getAppName()));
        AppInfo appInfo = appInfoMapper.selectOne(new QueryWrapper<AppInfo>().lambda()
                .eq(AppInfo::getAppName, request.getAppName()));
        ServerDiscoverCausa.Response response;
        if (appInfo != null) {
            response = ServerDiscoverCausa.Response.newBuilder()
                    .setCode(RemoteConstant.SUCCESS)
                    .setWorkInfo(
                            ServerDiscoverCausa.WorkInfo.newBuilder().setAppId(appInfo.getId()).build()
                    ).build();
        } else {
            response = ServerDiscoverCausa.Response.newBuilder()
                    .setCode(RemoteConstant.FAULT)
                    .build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}
