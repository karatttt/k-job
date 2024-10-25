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


    @Deprecated
    @DeprecatedConfigurationProperty(replacement = "kjob.worker.name-server-address")
    public String getNameServerAddress() {
        return getWorker().nameServerAddress;
    }


    @Deprecated
    public void setNameServerAddress(String address) {
        getWorker().setNameServerAddress(address);
    }

    @Deprecated
    @DeprecatedConfigurationProperty(replacement = "kjob.worker.server-port")
    public int getServerPort(){
        return getWorker().serverPort;
    }

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
         only ip address
         */
        private String serverAddress;
        /**
         only server port
         */
        private Integer serverPort;

        private String nameServerAddress;
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
