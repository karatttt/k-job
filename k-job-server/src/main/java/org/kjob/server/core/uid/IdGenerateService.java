package org.kjob.server.core.uid;

import lombok.extern.slf4j.Slf4j;
import org.kjob.server.common.config.KJobServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 唯一ID生成服务，使用 Twitter snowflake 算法
 * 机房ID：固定为0，占用2位
 * 机器ID：由 ServerIdProvider 提供
 *
 * @author tjq
 * @since 2020/4/6
 */
@Slf4j
@Service
public class IdGenerateService {

    private final SnowFlakeIdGenerator snowFlakeIdGenerator;

    private static final int DATA_CENTER_ID = 0;

    @Autowired
    KJobServerConfig kJobServerConfig;

    public IdGenerateService() {
        String ip = kJobServerConfig.getAddress();
        snowFlakeIdGenerator = new SnowFlakeIdGenerator(DATA_CENTER_ID, ip);
        log.info("[IdGenerateService] initialize IdGenerateService successfully, IP:{}", ip);
    }

    /**
     * 分配分布式唯一ID
     * @return 分布式唯一ID
     */
    public long allocate() {
        return snowFlakeIdGenerator.nextId();
    }

}
