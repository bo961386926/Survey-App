package com.qhiot.survey.security;

import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义UserDetailsService实现
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用: " + username);
        }

        // 根据角色设置权限
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            switch (user.getRole()) {
                case 1:
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    break;
                case 2:
                    authorities.add(new SimpleGrantedAuthority("ROLE_SURVEYOR"));
                    break;
                case 3:
                    authorities.add(new SimpleGrantedAuthority("ROLE_AUDITOR"));
                    break;
                default:
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}