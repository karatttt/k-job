package org.kjob.nameserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class KJobNameServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KJobNameServerApplication.class, args);
    }

}
