package com.qhiot.survey.common.util;

import com.qhiot.survey.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 安全工具类
 */
public class SecurityUtils {
    
    private SecurityUtils() {
    }
    
    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    return ((UserDetails) principal).getUsername();
                }
                return authentication.getName();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        return "system";
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                return loginUser.getUserId();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
    
    /**
     * 获取当前登录用户真实姓名
     */
    public static String getCurrentRealName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                return loginUser.getRealName();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
    
    /**
     * 检查当前用户是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * 获取当前登录用户完整信息
     */
    public static LoginUser getCurrentLoginUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                return loginUser;
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    /**
     * 判断当前用户是否拥有指定权限码
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authority == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities != null && authorities.stream().anyMatch(auth -> authority.equals(auth.getAuthority()));
    }

    /**
     * 判断当前用户是否拥有指定角色。roleCode 可传 ADMIN 或 ROLE_ADMIN。
     */
    public static boolean hasRole(String roleCode) {
        if (roleCode == null) {
            return false;
        }
        String normalized = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
        return hasAuthority(normalized);
    }
}
