package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.ProjectMember;

import java.util.List;

/**
 * 项目成员服务接口
 */
public interface ProjectMemberService extends IService<ProjectMember> {

    /**
     * 添加项目成员
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param role 角色
     * @return 是否成功
     */
    boolean addMember(Long projectId, Long userId, String role);

    /**
     * 批量添加项目成员
     * @param projectId 项目ID
     * @param userIds 用户ID列表
     * @param role 角色
     * @return 成功添加的数量
     */
    int batchAddMembers(Long projectId, List<Long> userIds, String role);

    /**
     * 移除项目成员
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean removeMember(Long projectId, Long userId);

    /**
     * 获取项目成员列表
     * @param projectId 项目ID
     * @return 成员列表
     */
    List<ProjectMember> getProjectMembers(Long projectId);

    /**
     * 更新成员角色
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param newRole 新角色
     * @return 是否成功
     */
    boolean updateMemberRole(Long projectId, Long userId, String newRole);

    /**
     * 检查用户是否是项目成员
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否是成员
     */
    boolean isProjectMember(Long projectId, Long userId);

    /**
     * 获取用户在项目中的角色
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 角色
     */
    String getUserRoleInProject(Long projectId, Long userId);
}