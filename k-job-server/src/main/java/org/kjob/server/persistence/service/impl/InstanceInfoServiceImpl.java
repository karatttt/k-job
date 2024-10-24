package org.kjob.server.persistence.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.kjob.server.persistence.domain.InstanceInfo;
import org.kjob.server.persistence.service.InstanceInfoService;
import org.kjob.server.persistence.mapper.InstanceInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author liushizhan
* @description 针对表【instance_info】的数据库操作Service实现
* @createDate 2024-10-20 20:12:43
*/
@Service
public class InstanceInfoServiceImpl extends ServiceImpl<InstanceInfoMapper, InstanceInfo>
    implements InstanceInfoService{

}




