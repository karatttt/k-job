package org.kjob.worker.processor;

import lombok.extern.slf4j.Slf4j;
import org.kjob.common.exception.KJobException;
import org.kjob.worker.processor.factory.ProcessorFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KJobProcessorLoader
 *
 * @author tjq
 * @since 2023/1/17
 */
@Slf4j
public class KJobProcessorLoader implements ProcessorLoader {

    private final List<ProcessorFactory> processorFactoryList;
    private final Map<ProcessorDefinition, ProcessorBean> def2Bean = new ConcurrentHashMap<>(128);

    public KJobProcessorLoader(List<ProcessorFactory> processorFactoryList) {
        this.processorFactoryList = processorFactoryList;
    }

    @Override
    public ProcessorBean load(ProcessorDefinition definition) {

        ProcessorBean pBean = def2Bean.computeIfAbsent(definition, ignore -> buildProcessorBean(definition));

        if (pBean.isStable()) {
            return pBean;
        }

        return buildProcessorBean(definition);
    }

    private ProcessorBean buildProcessorBean(ProcessorDefinition definition) {
        final String processorType = definition.getProcessorType();
        log.info("[ProcessorFactory] start to load Processor: {}", definition);
        for (ProcessorFactory pf : processorFactoryList) {
            final String pfName = pf.getClass().getSimpleName();
//            if (!Optional.ofNullable(pf.supportTypes()).orElse(Collections.emptySet()).contains(processorType)) {
//                log.info("[ProcessorFactory] [{}] can't load type={}, skip!", pfName, processorType);
//                continue;
//            }
            log.info("[ProcessorFactory] [{}] try to load processor: {}", pfName, definition);
            try {
                ProcessorBean processorBean = pf.build(definition);
                if (processorBean != null) {
                    log.info("[ProcessorFactory] [{}] load processor successfully: {}", pfName, definition);
                    return processorBean;
                }
            } catch (Throwable t) {
                log.error("[ProcessorFactory] [{}] load processor failed: {}", pfName, definition, t);
            }
        }
        throw new KJobException("fetch Processor failed, please check your processorType and processorInfo config");
    }
}
