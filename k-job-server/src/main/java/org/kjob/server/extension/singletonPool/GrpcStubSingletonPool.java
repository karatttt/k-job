package org.kjob.server.extension.singletonPool;

import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.kjob.common.constant.RemoteConstant;
import org.kjob.remote.api.ServerDiscoverGrpc;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GrpcStubSingletonPool {

    // 通过 ConcurrentHashMap（线程安全） 实现单例注册表
    private static final Map<String, Object> stubSingletons = new ConcurrentHashMap<String, Object>(64);
    private static final Map<String, ManagedChannel> channelSingletons = new ConcurrentHashMap<String, ManagedChannel>(64);

    @SuppressWarnings("unchecked")
    // 获取单例对象，若不存在则创建并添加
    public static <T> T getStubSingleton(String serverAddress, Class<?> grpcClientClass, Class<T> stubClass) {
        ManagedChannel channel = getChannelSingleton(serverAddress);
//        new ServerDiscoverGrpc.ServerDiscoverFutureStub(channel);

        Method[] methods = grpcClientClass.getDeclaredMethods();
        Method targetMethod = null;
        for (Method method : methods) {
            if (method.getReturnType().equals(stubClass)) {
                targetMethod = method;
            }
        }
        // 通过 computeIfAbsent 来确保线程安全地创建和存储单例对象

        Method finalTargetMethod = targetMethod;
        return (T) stubSingletons.computeIfAbsent(serverAddress + stubClass.getTypeName(), key -> {
            try {
                // 通过反射创建指定的 Stub 实例
                return finalTargetMethod.invoke(grpcClientClass,channel);

            } catch (Exception e) {
                throw new RuntimeException("Failed to create gRPC stub for server: " + serverAddress, e);
            }
        });
    }

    private static ManagedChannel getChannelSingleton(String serverAddress) {
        // 通过 computeIfAbsent 来确保线程安全地创建和存储单例对象
        return channelSingletons.computeIfAbsent(serverAddress, key -> {
            try {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, RemoteConstant.DEFAULT_SERVER_GRPC_PORT)
                        .usePlaintext()
                        .build();
                // 通过反射创建指定的 Stub 实例
                return channel;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create gRPC channel for server: " + serverAddress, e);
            }
        });
    }


}



