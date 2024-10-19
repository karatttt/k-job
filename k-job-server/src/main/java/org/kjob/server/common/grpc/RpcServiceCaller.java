package org.kjob.server.common.grpc;

public interface RpcServiceCaller {

    Object call(Object params);

}
