package org.kjob.worker.common.grpc.strategies.strategy;

import org.kjob.worker.common.constant.TransportTypeEnum;
import org.kjob.worker.common.grpc.strategies.GrpcStrategy;

public class HeartHealthReportRpcService implements GrpcStrategy<TransportTypeEnum> {
    @Override
    public void init() {

    }

    @Override
    public Object execute(Object params) {
        return null;
    }

    @Override
    public TransportTypeEnum getTypeEnumFromStrategyClass() {
        return null;
    }
}
