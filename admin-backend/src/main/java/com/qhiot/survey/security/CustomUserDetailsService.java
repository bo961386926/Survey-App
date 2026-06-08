package com.qhiot.survey.security;

import com.qhiot.survey.common.util.PermissionRegistry;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.SysRoleService;
import com.qhiot.survey.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 自定义UserDetailsService实现
 * 加载用户角色和权限，支持通配符展开
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用: " + username);
        }

        List<SysRole> roles = sysRoleService.getUserRoles(user.getId());

        // 阶段一：收集原始权限码（去重）
        Set<String> rawPermissions = new HashSet<>();
        List<String> roleCodes = new ArrayList<>();

        for (SysRole role : roles) {
            String roleCode = role.getRoleCode().toUpperCase();
            roleCodes.add("ROLE_" + roleCode);

            String permissions = role.getPermissions();
            if (permissions != null && !permissions.isEmpty()) {
                String[] permissionArray = permissions.split(",");
                for (String perm : permissionArray) {
                    String trimmedPerm = perm.trim().replace("[", "").replace("]", "").replace("\"", "");
                    if (!trimmedPerm.isEmpty()) {
                        rawPermissions.add(trimmedPerm);
                    }
                }
            }
        }

        // 阶段二：展开通配符 — 如果任何角色有 "*"，则拥有所有已注册权限
        Set<String> finalPermissions = PermissionRegistry.expandWildcard(rawPermissions);

        // 阶段三：合并角色前缀 + 展开后的权限
        Set<String> authoritySet = new HashSet<>();
        authoritySet.addAll(roleCodes);
        authoritySet.addAll(finalPermissions);

        // 如果没有任何权限，赋予基础用户权限
        if (authoritySet.isEmpty()) {
            authoritySet.add("ROLE_USER");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String auth : authoritySet) {
            authorities.add(new SimpleGrantedAuthority(auth));
        }

        LoginUser loginUser = new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                authorities
        );
        loginUser.setTenantId(user.getTenantId());
        return loginUser;
    }
}
