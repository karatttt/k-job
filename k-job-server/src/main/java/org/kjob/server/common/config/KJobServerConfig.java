package org.kjob.server.common.config;

import lombok.Getter;
import org.kjob.common.utils.net.MyNetUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@ConfigurationProperties(prefix = "kjob.server")
public class KJobServerConfig {

    private String address = MyNetUtil.address;

    public void setAddress(String address) {
        this.address = address;
    }

}
