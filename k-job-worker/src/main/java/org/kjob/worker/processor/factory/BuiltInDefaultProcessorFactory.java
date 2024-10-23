package org.kjob.worker.processor.factory;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.kjob.common.enums.ProcessorType;
import org.kjob.worker.processor.ProcessorBean;
import org.kjob.worker.processor.ProcessorDefinition;
import org.kjob.worker.processor.type.BasicProcessor;

import java.util.Set;

/**
 * 内建的默认处理器工厂，通过全限定类名加载处理器，但无法享受 IOC 框架的 DI 功能
 *
 * @author tjq
 * @since 2023/1/17
 */
@Slf4j
public class BuiltInDefaultProcessorFactory implements ProcessorFactory {

    @Override
    public Set<String> supportTypes() {
        return Sets.newHashSet(ProcessorType.BUILT_IN.name());
    }

    @Override
    public ProcessorBean build(ProcessorDefinition processorDefinition) {

        String className = processorDefinition.getProcessorInfo();

        try {
            Class<?> clz = Class.forName(className);
            BasicProcessor basicProcessor = (BasicProcessor) clz.getDeclaredConstructor().newInstance();
            return new ProcessorBean()
                    .setProcessor(basicProcessor)
                    .setClassLoader(basicProcessor.getClass().getClassLoader());
        }catch (Exception e) {
            log.warn("[ProcessorFactory] load local Processor(className = {}) failed.", className, e);
        }
        return null;
    }
}
