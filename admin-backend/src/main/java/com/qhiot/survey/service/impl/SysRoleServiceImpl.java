package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.entity.SysUserRole;
import com.qhiot.survey.mapper.SysRoleMapper;
import com.qhiot.survey.mapper.SysUserRoleMapper;
import com.qhiot.survey.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

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
        // 先删除用户原有角色
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        // 添加新角色
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }
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
    public void updateRolePermissions(Long roleId, List<String> permissions) {
        SysRole existing = getById(roleId);
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        SysRole update = new SysRole();
        update.setId(roleId);
        update.setPermissions(String.join(",", permissions));
        updateById(update);
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
