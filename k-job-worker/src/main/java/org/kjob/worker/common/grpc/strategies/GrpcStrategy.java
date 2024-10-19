package org.kjob.worker.common.grpc.strategies;


import io.grpc.ManagedChannel;
import org.kjob.worker.common.constant.TransportTypeEnum;

public interface GrpcStrategy<T> {

    /**
     * called  by StrategyCaller
     * @param params
     * @return
     */
    void init();
    Object execute(Object params);

    TransportTypeEnum getTypeEnumFromStrategyClass();


}

