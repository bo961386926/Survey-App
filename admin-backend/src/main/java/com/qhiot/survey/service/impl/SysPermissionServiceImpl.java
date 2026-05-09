package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.entity.SysPermission;
import com.qhiot.survey.mapper.SysPermissionMapper;
import com.qhiot.survey.service.SysPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Override
    public List<SysPermission> listEnabled() {
        return lambdaQuery()
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSort)
                .list();
    }

    @Override
    public List<SysPermission> listByModule(String module) {
        return lambdaQuery()
                .eq(SysPermission::getModule, module)
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSort)
                .list();
    }

    @Override
    public Set<String> getAllPermCodes() {
        List<SysPermission> list = list();
        return list.stream()
                .map(SysPermission::getPermCode)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPermissions(Set<String> allPermCodes) {
        if (allPermCodes == null || allPermCodes.isEmpty()) {
            return;
        }

        // 查询数据库中已有的权限码
        Set<String> existingCodes = getAllPermCodes();
        Set<String> codesToAdd = new HashSet<>(allPermCodes);
        codesToAdd.removeAll(existingCodes);

        // 插入新增的权限码
        for (String permCode : codesToAdd) {
            SysPermission permission = new SysPermission();
            permission.setPermCode(permCode);
            // 从权限码中推导名称和模块，如 "point:view" -> module="point", name="查看point"
            String[] parts = permCode.split(":");
            permission.setModule(parts.length > 0 ? parts[0] : "other");
            permission.setPermName(permCode);
            permission.setDescription(permCode);
            permission.setStatus(1);
            permission.setSort(0);
            permission.setCreateTime(LocalDateTime.now());
            save(permission);
            log.info("新增权限码到数据库: {}", permCode);
        }

        // 标记已删除的权限码为禁用（数据库中存在但代码中已不存在）
        Set<String> codesToDisable = new HashSet<>(existingCodes);
        codesToDisable.removeAll(allPermCodes);
        if (!codesToDisable.isEmpty()) {
            lambdaUpdate()
                    .in(SysPermission::getPermCode, codesToDisable)
                    .set(SysPermission::getStatus, 0)
                    .update();
            log.info("标记废弃权限码为禁用: {}", codesToDisable);
        }

        if (!codesToAdd.isEmpty() || !codesToDisable.isEmpty()) {
            log.info("权限同步完成 - 新增: {}, 禁用: {}", codesToAdd.size(), codesToDisable.size());
        }
    }
}
