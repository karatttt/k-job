package org.kjob.worker.starter.autoConfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

@ConfigurationProperties(prefix = "kjob")
public class KJobProperties {
    private final Worker worker = new Worker();

    public Worker getWorker() {
        return worker;
    }

    @Deprecated
    @DeprecatedConfigurationProperty(replacement = "kjob.worker.app-name")
    public String getAppName() {
        return getWorker().appName;
    }

    @Deprecated
    public void setAppName(String appName) {
        getWorker().setAppName(appName);
    }


    @DeprecatedConfigurationProperty(replacement = "kjob.worker.grpc-port")
    public int getGrpcPort() {
        return getWorker().getPort();
    }


    public void setGrpcPort(int grpcPort) {
        getWorker().setPort(grpcPort);
    }

    @Deprecated
    @DeprecatedConfigurationProperty(replacement = "kjob.worker.server-address")
    public String getServerAddress() {
        return getWorker().serverAddress;
    }

    @Deprecated
    public void setServerAddress(String serverAddress) {
        getWorker().setServerAddress(serverAddress);
    }



//    @Deprecated
//    @DeprecatedConfigurationProperty(replacement = "kjob.worker.max-result-length")
//    public int getMaxResultLength() {
//        return getWorker().maxResultLength;
//    }
//
//    @Deprecated
//    public void setMaxResultLength(int maxResultLength) {
//        getWorker().setMaxResultLength(maxResultLength);
//    }



    /**
     * kjob worker configuration properties.
     */
    @Setter
    @Getter
    public static class Worker {

        /**
         * Whether to enable kJob Worker
         */
        private boolean enabled = true;

        /**
         * Name of application, String type. Total length of this property should be no more than 255
         * characters. This is one of the required properties when registering a new application. This
         * property should be assigned with the same value as what you entered for the appName.
         */
        private String appName;

        /**
         * port
         */
        private Integer port;
        /**
         * Address(es) of kjob-server node(s). Ip:port or domain.
         * Example of single kjob-server node:
         * <p>
         * 127.0.0.1:7700
         * </p>
         * Example of kjob-server cluster:
         * <p>
         * 192.168.0.10:7700,192.168.0.11:7700,192.168.0.12:7700
         * </p>
         */
        private String serverAddress;


//        /**
//         * Max length of response result. Result that is longer than the value will be truncated.
//         * {@link ProcessResult} max length for #msg
//         */
//        private int maxResultLength = 8192;
//
//        /**
//         * Max length of appended workflow context value length. Appended workflow context value that is longer than the value will be ignored.
//         * {@link WorkflowContext} max length for #appendedContextData
//         */
//        private int maxAppendedWfContextLength = 8192;

        private String tag;
        /**
         * Max numbers of LightTaskTacker
         */
        private Integer maxLightweightTaskNum = 1024;
        /**
         * Max numbers of HeavyTaskTacker
         */
        private Integer maxHeavyweightTaskNum = 64;
        /**
         * Interval(s) of worker health report
         */
        private Integer healthReportInterval = 10;

    }
}
