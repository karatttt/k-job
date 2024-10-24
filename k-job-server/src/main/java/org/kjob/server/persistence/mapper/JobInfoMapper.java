package org.kjob.server.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.kjob.server.persistence.domain.JobInfo;

/**
* @author liushizhan
* @description 针对表【job_info】的数据库操作Mapper
* @createDate 2024-10-20 19:56:42
* @Entity org.kjob.server.persistence.domain.JobInfo
*/
@Mapper
public interface JobInfoMapper extends BaseMapper<JobInfo> {

}




