package org.kjob.server.persistence.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.kjob.server.persistence.domain.JobInfo;
import org.kjob.server.persistence.service.JobInfoService;
import org.kjob.server.persistence.mapper.JobInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author liushizhan
* @description 针对表【job_info】的数据库操作Service实现
* @createDate 2024-10-20 19:56:42
*/
@Service
public class JobInfoServiceImpl extends ServiceImpl<JobInfoMapper, JobInfo>
    implements JobInfoService {

}




