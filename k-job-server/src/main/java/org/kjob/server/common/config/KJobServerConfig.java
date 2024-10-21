package org.kjob.server.common.config;

import lombok.Getter;
import lombok.Setter;
import org.kjob.common.utils.net.MyNetUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@ConfigurationProperties(prefix = "kjob.server")
public class KJobServerConfig {

    private String address = MyNetUtils.address;

    public void setAddress(String address) {
        this.address = address;
    }

}
