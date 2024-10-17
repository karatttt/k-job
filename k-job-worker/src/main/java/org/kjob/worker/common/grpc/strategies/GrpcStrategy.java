package org.kjob.worker.common.grpc.strategies;


public interface GrpcStrategy<T> {

    void init();

    /**
     * called  by StrategyCaller
     * @param params
     * @return
     */
    String execute(Object params);


}

