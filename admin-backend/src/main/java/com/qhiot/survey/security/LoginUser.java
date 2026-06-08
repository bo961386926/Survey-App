package com.qhiot.survey.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 自定义登录用户信息
 * 扩展Spring Security的User类，添加用户ID等额外信息
 */
@Getter
public class LoginUser extends User {

    private final Long userId;

    private final String realName;

    /** 租户ID */
    @Setter
    private Long tenantId;

    public LoginUser(Long userId, String username, String password, String realName,
                     Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
        this.realName = realName;
    }

    public LoginUser(Long userId, String username, String password, String realName,
                     Collection<? extends GrantedAuthority> authorities,
                     boolean accountNonExpired, boolean accountNonLocked,
                     boolean credentialsNonExpired, boolean enabled) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
        this.realName = realName;
    }
}
