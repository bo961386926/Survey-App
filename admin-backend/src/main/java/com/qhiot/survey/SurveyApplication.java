package com.qhiot.survey;

import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.SysRoleService;
import com.qhiot.survey.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.qhiot.survey", "com.qhiot.survey"})
@MapperScan("com.qhiot.survey.mapper")
@EnableAsync
@EnableScheduling
public class SurveyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SurveyApplication.class, args);
    }

    @Bean
    public CommandLineRunner fixAdminAccount(SysUserService userService, SysRoleService roleService, PasswordEncoder encoder) {
        return args -> {
            try {
                SysUser admin = userService.lambdaQuery().eq(SysUser::getUsername, "admin").one();
                if (admin == null) {
                    log.warn("====== [启动修复] admin 用户不存在，跳过 ======");
                    return;
                }

                boolean changed = false;

                // 1. 检查密码是否已加密，如果未加密则重新加密
                String pwd = admin.getPassword();
                boolean needsRehash = pwd == null || pwd.isEmpty()
                    || !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$") && !pwd.startsWith("$2y$");
                if (needsRehash) {
                    log.warn("====== [启动修复] admin 密码未加密（当前: {}），正在修复 ======", pwd);
                    admin.setPassword(encoder.encode("Admin@123"));
                    admin.setIsFirstLogin(1);
                    userService.updateById(admin);
                    changed = true;
                    log.info("====== [启动修复] admin 密码已重置为 Admin@123 ======");
                }

                // 2. 确保 admin 拥有管理员角色（兼容大小写）
                List<SysRole> roles = roleService.getUserRoles(admin.getId());
                boolean hasAdminRole = roles.stream().anyMatch(r ->
                    "admin".equalsIgnoreCase(r.getRoleCode())
                );
                if (!hasAdminRole) {
                    log.warn("====== [启动修复] admin 缺少管理员角色，正在修复 ======");
                    SysRole adminRole = roleService.lambdaQuery()
                        .eq(SysRole::getRoleCode, "admin")
                        .one();
                    // 如果小写没找到，尝试大写
                    if (adminRole == null) {
                        adminRole = roleService.lambdaQuery()
                            .eq(SysRole::getRoleCode, "ADMIN")
                            .one();
                    }
                    if (adminRole != null) {
                        roleService.assignRoleToUser(admin.getId(), List.of(adminRole.getId()));
                        changed = true;
                        log.info("====== [启动修复] admin 已获得管理员角色 ======");
                    }
                }

                if (changed) {
                    log.info("====== [启动修复] admin 账号修复完成，请使用密码 Admin@123 登录 ======");
                } else {
                    log.info("====== [启动修复] admin 账号状态正常，无需修复 ======");
                }
            } catch (Exception e) {
                log.error("====== [启动修复] 检查 admin 账号失败: {} ======", e.getMessage());
            }
        };
    }
}