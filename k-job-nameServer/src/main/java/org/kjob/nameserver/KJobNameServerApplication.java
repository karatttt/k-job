package org.kjob.nameserver;

import org.kjob.nameserver.config.KJobNameServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KJobNameServerConfig.class)
public class KJobNameServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KJobNameServerApplication.class, args);
    }

}
