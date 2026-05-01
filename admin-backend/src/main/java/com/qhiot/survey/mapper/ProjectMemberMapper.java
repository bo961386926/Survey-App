package com.qhiot.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhiot.survey.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目成员Mapper接口
 */
@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {
}