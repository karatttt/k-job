package org.kjob.server.consumer;

import org.kjob.server.consumer.entity.Response;

public interface RemotingResponseCallback {
    void callback(Response response);
}
