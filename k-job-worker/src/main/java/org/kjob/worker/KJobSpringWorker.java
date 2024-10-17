package org.kjob.worker;

import com.google.common.collect.Lists;
import org.kjob.worker.common.KJobWorkerConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class KJobSpringWorker implements InitializingBean, DisposableBean {

    /**
     * 组合优于继承，持有 kJobWorker，内部重新设置 ProcessorFactory 更优雅
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

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        BuiltInSpringProcessorFactory springProcessorFactory = new BuiltInSpringProcessorFactory(applicationContext);
//
//        BuildInSpringMethodProcessorFactory springMethodProcessorFactory = new BuildInSpringMethodProcessorFactory(applicationContext);
//        // append BuiltInSpringProcessorFactory
//
//        List<ProcessorFactory> processorFactories = Lists.newArrayList(
//                Optional.ofNullable(config.getProcessorFactoryList())
//                        .orElse(Collections.emptyList()));
//        processorFactories.add(springProcessorFactory);
//        processorFactories.add(springMethodProcessorFactory);
//        config.setProcessorFactoryList(processorFactories);
//    }

    @Override
    public void destroy() throws Exception {
        kJobWorker.destroy();
    }
}
