package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.dto.ProjectCreateRequest;
import com.qhiot.survey.dto.ProjectQueryRequest;
import com.qhiot.survey.entity.Project;

import java.util.List;
import java.util.Map;

public interface ProjectService extends IService<Project> {
    
    /**
     * 分页查询项目列表
     */
    PageResult<Project> queryProjectPage(ProjectQueryRequest request);
    
    /**
     * 创建项目
     */
    boolean createProject(ProjectCreateRequest request);
    
    /**
     * 更新项目
     */
    boolean updateProject(Long id, ProjectCreateRequest request);
    
    /**
     * 删除项目
     */
    boolean deleteProject(Long id);
    
    /**
     * 获取项目详情
     */
    Project getProjectDetail(Long id);
    
    /**
     * 变更项目状态
     * @param id 项目ID
     * @param targetStatus 目标状态
     * @return 是否成功
     */
    boolean changeStatus(Long id, Integer targetStatus);
    
    /**
     * 获取项目统计信息
     */
    Map<String, Object> getProjectStatistics(Long id);
    
    /**
     * 归档项目
     * @param id 项目ID
     * @return 是否成功
     */
    boolean archiveProject(Long id);
    
    /**
     * 恢复项目（取消归档）
     * @param id 项目ID
     * @return 是否成功
     */
    boolean restoreProject(Long id);
}
