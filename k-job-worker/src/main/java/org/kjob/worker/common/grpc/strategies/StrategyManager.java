package org.kjob.worker.common.grpc.strategies;

import lombok.extern.slf4j.Slf4j;
import org.kjob.common.exception.KJobException;
import org.kjob.worker.common.constant.TransportTypeEnum;

import java.util.HashMap;
import java.util.Map;
@Slf4j
public class StrategyManager {

    public static Map<TransportTypeEnum, GrpcStrategy<?>> strategyMap = new HashMap<>();

    public static <T> void registerCausa(
           TransportTypeEnum ruleType, GrpcStrategy<?> strategy) {
        strategyMap.put(ruleType, strategy);
    }

    @SuppressWarnings("unchecked")
    public static <T> GrpcStrategy<T> match(TransportTypeEnum ruleType) {

        if (!strategyMap.containsKey(ruleType)) {
            throw new KJobException("There's no strategy defined for this type: \"");
        }

        return (GrpcStrategy<T>) strategyMap.get(ruleType);
    }

}
