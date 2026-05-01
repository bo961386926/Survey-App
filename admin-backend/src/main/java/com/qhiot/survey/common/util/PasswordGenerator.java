package com.qhiot.survey.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类（仅用于生成密码哈希）
 */
public class PasswordGenerator {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    public static void main(String[] args) {
        if (args.length > 0) {
            String password = args[0];
            String hashed = encode(password);
            System.out.println("BCrypt hash for '" + password + "':");
            System.out.println(hashed);
        } else {
            // 默认生成admin123的哈希
            String hashed = encode("admin123");
            System.out.println("BCrypt hash for 'admin123':");
            System.out.println(hashed);
        }
    }
}