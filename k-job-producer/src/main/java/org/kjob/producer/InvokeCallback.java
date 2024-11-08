package org.kjob.producer;

import org.kjob.producer.entity.ResponseFuture;

public interface InvokeCallback {
    void operationComplete(final ResponseFuture responseFuture);
}
