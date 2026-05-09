package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.entity.SysRolePermission;
import com.qhiot.survey.entity.SysUserRole;
import com.qhiot.survey.mapper.SysRoleMapper;
import com.qhiot.survey.mapper.SysRolePermissionMapper;
import com.qhiot.survey.mapper.SysUserRoleMapper;
import com.qhiot.survey.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private static final Logger logger = LoggerFactory.getLogger(SysRoleServiceImpl.class);
    
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public Page<SysRole> listByPage(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysRole::getRoleName, keyword)
                    .or().like(SysRole::getRoleCode, keyword);
        }
        wrapper.orderByDesc(SysRole::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<SysRole> listAllEnabled() {
        return lambdaQuery()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSort)
                .list();
    }

    @Override
    public SysRole createRole(SysRole role) {
        // 检查角色编码是否重复
        Long count = lambdaQuery()
                .eq(SysRole::getRoleCode, role.getRoleCode())
                .count();
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }
        role.setStatus(1);
        role.setCreateTime(LocalDateTime.now());
        save(role);
        return role;
    }

    @Override
    public SysRole updateRole(Long id, SysRole role) {
        SysRole existing = getById(id);
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        role.setId(id);
        updateById(role);
        return getById(id);
    }

    @Override
    public void deleteRole(Long id) {
        SysRole existing = getById(id);
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        // 检查是否有用户使用该角色
        Long count = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id)
        );
        if (count > 0) {
            throw new BusinessException("该角色已被用户使用，无法删除");
        }
        removeById(id);
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        SysRole existing = getById(id);
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        SysRole update = new SysRole();
        update.setId(id);
        update.setStatus(status);
        updateById(update);
    }

    @Override
    @Transactional
    public void assignRoleToUser(Long userId, List<Long> roleIds) {
        logger.info("====== [角色分配] 开始分配角色 - userId: {}, roleIds: {} ======",
                userId,
                roleIds);
        
        // 先删除用户原有角色
        int deletedCount = sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        logger.info("====== [角色分配] 删除旧角色记录 - userId: {}, 删除数量: {} ======", userId, deletedCount);
        
        // 检查并清理可能存在的重复记录（防御性编程）
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .notIn(SysUserRole::getRoleId, roleIds != null ? roleIds : List.of())
        );
        
        // 添加新角色（先去重）
        if (roleIds != null && !roleIds.isEmpty()) {
            // 去重
            List<Long> uniqueRoleIds = roleIds.stream().distinct().toList();
            logger.info("====== [角色分配] 去重后角色列表 - userId: {}, roleIds: {} ======", userId, uniqueRoleIds);
            
            for (Long roleId : uniqueRoleIds) {
                // 过滤空值（前端可能传递 null/undefined）
                if (roleId == null) {
                    logger.warn("====== [角色分配] 跳过空 roleId - userId: {} ======", userId);
                    continue;
                }
                // 检查是否已存在（防止并发问题）
                Long count = sysUserRoleMapper.selectCount(
                        new LambdaQueryWrapper<SysUserRole>()
                                .eq(SysUserRole::getUserId, userId)
                                .eq(SysUserRole::getRoleId, roleId)
                );
                
                if (count == 0) {
                    SysUserRole userRole = new SysUserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    sysUserRoleMapper.insert(userRole);
                    logger.info("====== [角色分配] 插入角色记录 - userId: {}, roleId: {} ======", userId, roleId);
                } else {
                    logger.warn("====== [角色分配] 角色记录已存在，跳过 - userId: {}, roleId: {} ======", userId, roleId);
                }
            }
        }
        
        logger.info("====== [角色分配] 角色分配完成 - userId: {} ======", userId);
    }

    @Override
    public List<String> getRolePermissions(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        // 从角色的permissions字段解析权限列表
        String permissions = role.getPermissions();
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }
        return List.of(permissions.split(","));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, List<String> permissions) {
        SysRole existing = getById(roleId);
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        // 1. 更新 sys_role.permissions 字符串字段（保持向后兼容）
        SysRole update = new SysRole();
        update.setId(roleId);
        update.setPermissions(String.join(",", permissions));
        updateById(update);

        // 2. 同步更新 sys_role_permission 关联表
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );
        if (permissions != null && !permissions.isEmpty()) {
            for (String permCode : permissions) {
                String trimmed = permCode.trim();
                if (!trimmed.isEmpty()) {
                    SysRolePermission rolePerm = new SysRolePermission();
                    rolePerm.setRoleId(roleId);
                    rolePerm.setPermCode(trimmed);
                    rolePerm.setCreateTime(LocalDateTime.now());
                    sysRolePermissionMapper.insert(rolePerm);
                }
            }
        }
    }

    @Override
    public List<SysRole> getUserRoles(Long userId) {
        // 查询用户的角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        return listByIds(roleIds);
    }
}
