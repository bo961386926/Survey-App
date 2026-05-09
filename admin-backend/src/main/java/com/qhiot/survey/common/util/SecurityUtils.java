package com.qhiot.survey.common.util;

import com.qhiot.survey.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
}
