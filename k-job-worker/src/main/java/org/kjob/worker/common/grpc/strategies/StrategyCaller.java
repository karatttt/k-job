package org.kjob.worker.common.grpc.strategies;

import org.kjob.worker.common.constant.TransportTypeEnum;

public class StrategyCaller {

    public static Object call(TransportTypeEnum rule, Object params){
        GrpcStrategy<Object> match = StrategyManager.match(rule);
        return match.execute(params);
    }

}
