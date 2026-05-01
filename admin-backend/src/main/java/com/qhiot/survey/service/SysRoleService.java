package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     */
    Page<SysRole> listByPage(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 获取所有启用角色
     */
    List<SysRole> listAllEnabled();

    /**
     * 创建角色
     */
    SysRole createRole(SysRole role);

    /**
     * 更新角色
     */
    SysRole updateRole(Long id, SysRole role);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 启用/禁用角色
     */
    void toggleStatus(Long id, Integer status);

    /**
     * 为用户分配角色
     */
    void assignRoleToUser(Long userId, List<Long> roleIds);

    /**
     * 获取角色的权限配置
     */
    List<String> getRolePermissions(Long roleId);

    /**
     * 更新角色的权限配置
     */
    void updateRolePermissions(Long roleId, List<String> permissions);

    /**
     * 获取用户的角色列表
     */
    List<SysRole> getUserRoles(Long userId);
}
