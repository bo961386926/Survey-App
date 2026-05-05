package com.qhiot.survey.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具类
 */
public class SecurityUtils {
    
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
            // 未登录或获取失败
        }
        return "system";
    }
    
    /**
     * 获取当前用户ID
     * 注意：需要根据实际的UserDetails实现调整
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                // 如果您的UserDetails包含用户ID，可以在这里解析
                // 例如：((CustomUserDetails) authentication.getPrincipal()).getUserId()
                return 1L; // 临时返回默认值
            }
        } catch (Exception e) {
            // 未登录或获取失败
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
}
