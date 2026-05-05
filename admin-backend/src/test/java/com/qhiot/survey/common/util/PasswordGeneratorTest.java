package com.qhiot.survey.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码生成器单元测试
 */
class PasswordGeneratorTest {

    @Test
    void testGenerateStrongPasswordDefaultLength() {
        String password = PasswordGenerator.generateStrongPassword();
        
        assertNotNull(password);
        assertEquals(12, password.length());
        assertPasswordComplexity(password);
    }

    @Test
    void testGenerateStrongPasswordCustomLength() {
        String password = PasswordGenerator.generateStrongPassword(16);
        
        assertEquals(16, password.length());
        assertPasswordComplexity(password);
    }

    @Test
    void testGenerateStrongPasswordMinLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordGenerator.generateStrongPassword(5);
        });
    }

    @Test
    void testPasswordUniqueness() {
        // 生成100个密码，验证都不相同
        for (int i = 0; i < 100; i++) {
            String p1 = PasswordGenerator.generateStrongPassword();
            String p2 = PasswordGenerator.generateStrongPassword();
            assertNotEquals(p1, p2, "每次生成的密码应该不同");
        }
    }

    @Test
    void testPasswordComplexityMultiple() {
        // 测试生成的50个密码都符合复杂度要求
        for (int i = 0; i < 50; i++) {
            String password = PasswordGenerator.generateStrongPassword();
            assertPasswordComplexity(password);
        }
    }

    private void assertPasswordComplexity(String password) {
        assertTrue(password.length() >= 8, "密码长度应>=8: " + password);
        assertTrue(password.length() <= 64, "密码长度应<=64: " + password);
        assertTrue(password.matches(".*[a-z].*"), "应包含小写字母: " + password);
        assertTrue(password.matches(".*[A-Z].*"), "应包含大写字母: " + password);
        assertTrue(password.matches(".*\\d.*"), "应包含数字: " + password);
        assertTrue(password.matches(".*[@$!%*?&].*"), "应包含特殊字符: " + password);
    }
}
