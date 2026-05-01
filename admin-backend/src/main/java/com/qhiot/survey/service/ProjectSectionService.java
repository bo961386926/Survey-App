package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.ProjectSection;

import java.util.List;
import java.util.Map;

/**
 * 标段管理服务接口
 */
public interface ProjectSectionService extends IService<ProjectSection> {

    /**
     * 分页查询标段列表
     */
    Page<ProjectSection> listByPage(Long projectId, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 根据项目ID获取标段列表
     */
    List<ProjectSection> listByProjectId(Long projectId);

    /**
     * 创建标段
     */
    ProjectSection createSection(ProjectSection section);

    /**
     * 更新标段
     */
    ProjectSection updateSection(Long id, ProjectSection section);

    /**
     * 删除标段
     */
    void deleteSection(Long id);

    /**
     * 启用/禁用标段
     */
    void toggleStatus(Long id, Integer status);

    /**
     * 设置标段负责人
     */
    void setManager(Long id, Long managerId);

    /**
     * 绑定点位到标段
     */
    void bindPoints(Long sectionId, List<Long> pointIds);

    /**
     * 解绑点位
     */
    void unbindPoints(Long sectionId, List<Long> pointIds);

    /**
     * 获取标段统计信息 (完成率/审核积压)
     */
    Object getStatistics(Long sectionId);

    /**
     * 标记/取消标记重点区域
     */
    void toggleKeyArea(Long id, Integer isKeyArea);
    
    /**
     * 获取标段审核积压信息
     */
    Map<String, Object> getAuditBacklog(Long sectionId);
}
