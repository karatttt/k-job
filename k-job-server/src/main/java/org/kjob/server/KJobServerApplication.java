package org.kjob.server;

import org.kjob.server.common.config.KJobServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(KJobServerConfig.class)
public class KJobServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KJobServerApplication.class, args);
	}

}
