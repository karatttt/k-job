package org.kjob.server.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.kjob.server.persistence.domain.DistributedLock;

/**
* @author liushizhan
* @description 针对表【distributed_lock】的数据库操作Mapper
* @createDate 2024-10-19 14:58:59
* @Entity persistnece.domain.DistributedLock
*/
@Mapper
public interface DistributedLockMapper extends BaseMapper<DistributedLock> {

}




