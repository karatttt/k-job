package org.kjob.worker.autoConfig;

import org.kjob.common.utils.CommonUtils;
//import org.kjob.common.utils.NetUtils;
import org.kjob.worker.KJobSpringWorker;
import org.kjob.worker.common.KJobWorkerConfig;
import org.kjob.worker.common.grpc.RpcStub;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableConfigurationProperties(KJobProperties.class)
@ConditionalOnProperty(prefix = "kjob.worker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KJobAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KJobSpringWorker initKJob(KJobProperties properties) {

        KJobProperties.Worker worker = properties.getWorker();

        /*
         * Address of PowerJob-server node(s). Do not mistake for ActorSystem port. Do not add
         * any prefix, i.e. http://.
         */
//        CommonUtils.requireNonNull(worker.getServerAddress(), "serverAddress can't be empty! " +
//                "if you don't want to enable powerjob, please config program arguments: powerjob.worker.enabled=false");
        List<String> serverAddress = Arrays.asList(worker.getServerAddress().split(","));

        /*
         * Create OhMyConfig object for setting properties.
         */
        KJobWorkerConfig config = new KJobWorkerConfig();
        /*
         * Configuration of worker port. Random port is enabled when port is set with non-positive number.
         */
        if (worker.getPort() != null) {
            config.setPort(worker.getPort());
        } else {
            int port = worker.getPort();
            if (port <= 0) {
//                port = NetUtils.getRandomPort();
            }
            config.setPort(port);
        }
        /*
         * appName, name of the application. Applications should be registered in advance to prevent
         * error. This property should be the same with what you entered for appName when getting
         * registered.
         */
        config.setAppName(worker.getAppName());
        config.setServerAddress(serverAddress);

        /*
         * For non-Map/MapReduce tasks, {@code memory} is recommended for speeding up calculation.
         * Map/MapReduce tasks may produce batches of subtasks, which could lead to OutOfMemory
         * exception or error, {@code disk} should be applied.
         */




//        config.setTag(worker.getTag());

        config.setMaxHeavyweightTaskNum(worker.getMaxHeavyweightTaskNum());

        config.setMaxLightweightTaskNum(worker.getMaxLightweightTaskNum());

        config.setHealthReportInterval(worker.getHealthReportInterval());
        /*
         * Create PowerJobSpringWorker object and set properties.
         */
        return new KJobSpringWorker(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcStub initRpcStub() {

        return new RpcStub();

    }

}
