package org.kjob.worker;

import com.google.common.collect.Lists;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.processor.factory.ProcessorFactory;
import org.kjob.worker.processor.factory.BuildInSpringMethodProcessorFactory;
import org.kjob.worker.processor.factory.BuiltInSpringProcessorFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class KJobSpringWorker implements InitializingBean, DisposableBean, ApplicationContextAware{

    /**
     * 组合优于继承，持有 kJobWorker，设置ProcessFactoryList，这里可以自定义工厂
     */
    private KJobWorker kJobWorker;
    private final KJobWorkerConfig config;


    public KJobSpringWorker(KJobWorkerConfig config) {
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        kJobWorker = new KJobWorker(config);
        kJobWorker.init();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BuiltInSpringProcessorFactory springProcessorFactory = new BuiltInSpringProcessorFactory(applicationContext);
        BuildInSpringMethodProcessorFactory springMethodProcessorFactory = new BuildInSpringMethodProcessorFactory(applicationContext);

        // append BuiltInSpringProcessorFactory
        List<ProcessorFactory> processorFactories = Lists.newArrayList(
                Optional.ofNullable(config.getProcessorFactoryList())
                        .orElse(Collections.emptyList()));
        processorFactories.add(springProcessorFactory);
        processorFactories.add(springMethodProcessorFactory);
        config.setProcessorFactoryList(processorFactories);
    }

    @Override
    public void destroy() throws Exception {
        kJobWorker.destroy();
    }
}
