package org.kjob.server.core.schedule;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.kjob.common.enums.TimeExpressionType;
import org.kjob.common.exception.KJobException;
import org.kjob.server.core.schedule.auxiliary.TimingStrategyHandler;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Echo009
 * @since 2022/3/21
 */
@Slf4j
@Service
public class TimingStrategyService {

    private static final int NEXT_N_TIMES = 5;

    private static final List<String> TIPS = Collections.singletonList("It is valid, but has not trigger time list!");


    private final Map<TimeExpressionType, TimingStrategyHandler> strategyContainer;

    public TimingStrategyService(List<TimingStrategyHandler> timingStrategyHandlers) {
        // init
        strategyContainer = new EnumMap<>(TimeExpressionType.class);
        for (TimingStrategyHandler timingStrategyHandler : timingStrategyHandlers) {
            strategyContainer.put(timingStrategyHandler.supportType(), timingStrategyHandler);
        }
    }


    /**
     * 计算下次的调度时间
     *
     * @param preTriggerTime     上次触发时间(nullable)
     * @param timeExpressionType 定时表达式类型
     * @param timeExpression     表达式
     * @param startTime          起始时间(include)
     * @param endTime            结束时间(include)
     * @return 下次的调度时间
     */
    public Long calculateNextTriggerTime(Long preTriggerTime, TimeExpressionType timeExpressionType, String timeExpression, Long startTime, Long endTime) {
        if (preTriggerTime == null || preTriggerTime < System.currentTimeMillis()) {
            preTriggerTime = System.currentTimeMillis();
        }
        return getHandler(timeExpressionType).calculateNextTriggerTime(preTriggerTime, timeExpression, startTime, endTime);
    }



    private TimingStrategyHandler getHandler(TimeExpressionType timeExpressionType) {
        TimingStrategyHandler timingStrategyHandler = strategyContainer.get(timeExpressionType);
        if (timingStrategyHandler == null) {
            throw new KJobException("No matching TimingStrategyHandler for this TimeExpressionType:" + timeExpressionType);
        }
        return timingStrategyHandler;
    }

}
