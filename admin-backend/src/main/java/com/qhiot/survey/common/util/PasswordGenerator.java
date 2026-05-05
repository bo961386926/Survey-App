package com.qhiot.survey.common.util;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 强密码生成器
 */
public class PasswordGenerator {
    
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@$!%*?&";
    private static final String ALL = LOWERCASE + UPPERCASE + DIGITS + SPECIAL;
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * 生成强密码（默认12位）
     * 包含：大写字母、小写字母、数字、特殊字符
     */
    public static String generateStrongPassword() {
        return generateStrongPassword(12);
    }
    
    /**
     * 生成指定长度的强密码
     * @param length 密码长度（最小8位）
     */
    public static String generateStrongPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("密码长度不能少于8位");
        }
        
        // 确保每种字符至少出现一次
        StringBuilder password = new StringBuilder();
        password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));
        
        // 剩余位随机填充
        IntStream.range(0, length - 4)
            .mapToObj(i -> String.valueOf(ALL.charAt(RANDOM.nextInt(ALL.length()))))
            .forEach(password::append);
        
        // 打乱顺序
        return shuffle(password.toString());
    }
    
    /**
     * 打乱字符串
     */
    private static String shuffle(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int index = RANDOM.nextInt(i + 1);
            char temp = characters[index];
            characters[index] = characters[i];
            characters[i] = temp;
        }
        return new String(characters);
    }
}
