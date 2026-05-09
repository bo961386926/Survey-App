package com.qhiot.survey.common.init;

import com.qhiot.survey.common.util.PermissionRegistry;
import com.qhiot.survey.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 权限初始化器
 * 在应用完全启动后，将 PermissionRegistry 中收集的所有权限码同步到 sys_permission 表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInitializer {

    private final SysPermissionService sysPermissionService;

    @EventListener(ApplicationReadyEvent.class)
    public void syncPermissionsToDatabase() {
        try {
            log.info("====== 开始同步权限码到 sys_permission 表 ======");
            var allPerms = PermissionRegistry.getAllPermissions();
            if (allPerms.isEmpty()) {
                log.warn("====== PermissionRegistry 中未发现任何权限码，跳过同步 ======");
                return;
            }
            sysPermissionService.syncPermissions(allPerms);
            log.info("====== 权限码同步完成，共 {} 个权限码 ======", allPerms.size());
        } catch (Exception e) {
            log.error("====== 权限码同步失败 ======", e);
        }
    }
}
