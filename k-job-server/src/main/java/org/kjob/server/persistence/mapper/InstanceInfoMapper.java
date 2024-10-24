package org.kjob.server.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.kjob.server.persistence.domain.InstanceInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author liushizhan
* @description 针对表【instance_info】的数据库操作Mapper
* @createDate 2024-10-20 20:12:43
* @Entity org.kjob.server.persistence.domain.InstanceInfo
*/
@Mapper
public interface InstanceInfoMapper extends BaseMapper<InstanceInfo> {

}




