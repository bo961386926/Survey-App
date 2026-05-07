package com.qhiot.survey.common.aspect;

import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.util.SecurityUtils;
import com.qhiot.survey.service.OperationLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 操作日志AOP切面单元测试
 * 测试覆盖：日志拦截、参数解析、用户信息获取等
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("操作日志AOP切面测试")
class OperationLogAspectTest {

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Method method;

    @InjectMocks
    private OperationLogAspect operationLogAspect;

    private OperationLog operationLogAnnotation;

    @BeforeEach
    void setUp() throws Exception {
        // 模拟注解
        operationLogAnnotation = new OperationLog() {
            @Override
            public String module() { return "用户管理"; }

            @Override
            public String action() { return "创建"; }

            @Override
            public String description() { return "创建用户: #user.username"; }

            @Override
            public int riskLevel() { return 1; }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return OperationLog.class;
            }
        };

        // 模拟方法签名
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(method.getAnnotation(OperationLog.class)).thenReturn(operationLogAnnotation);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"user"});
    }

    @Test
    @DisplayName("测试正常拦截并记录日志")
    void testDoAfterReturning_Success() throws Exception {
        // Given
        Map<String, Object> mockUser = new HashMap<>();
        mockUser.put("username", "testuser");
        when(joinPoint.getArgs()).thenReturn(new Object[]{mockUser});

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(100L);
            mockedSecurity.when(SecurityUtils::getCurrentUsername).thenReturn("admin");

            // When
            operationLogAspect.doAfterReturning(joinPoint, null);

            // Then
            Thread.sleep(100); // 等待异步执行
            verify(operationLogService, atLeastOnce()).logOperation(
                eq(100L),
                eq("admin"),
                eq("用户管理"),
                eq("创建"),
                anyString(),
                anyString(),
                anyString(),
                eq(1)
            );
        }
    }

    @Test
    @DisplayName("测试用户信息为空时跳过记录")
    void testDoAfterReturning_NullUser() throws Exception {
        // Given
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(null);
            mockedSecurity.when(SecurityUtils::getCurrentUsername).thenReturn("system");

            // When
            operationLogAspect.doAfterReturning(joinPoint, null);

            // Then
            Thread.sleep(100);
            verify(operationLogService, never()).logOperation(any(), any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Test
    @DisplayName("测试异常情况下记录日志")
    void testDoAfterThrowing_Success() throws Exception {
        // Given
        when(joinPoint.getArgs()).thenReturn(new Object[]{new HashMap<>()});
        Exception mockException = new RuntimeException("测试异常");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(100L);
            mockedSecurity.when(SecurityUtils::getCurrentUsername).thenReturn("admin");

            // When
            operationLogAspect.doAfterThrowing(joinPoint, mockException);

            // Then
            Thread.sleep(100);
            verify(operationLogService, atLeastOnce()).logOperation(
                eq(100L),
                eq("admin"),
                eq("用户管理"),
                eq("创建"),
                anyString(),
                anyString(),
                anyString(),
                eq(1)
            );
        }
    }

    @Test
    @DisplayName("测试SpEL表达式解析")
    void testSpELExpressionParsing() throws Exception {
        // Given
        Map<String, Object> user = new HashMap<>();
        user.put("username", "newuser");
        user.put("realName", "新用户");
        when(joinPoint.getArgs()).thenReturn(new Object[]{user});

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(100L);
            mockedSecurity.when(SecurityUtils::getCurrentUsername).thenReturn("admin");

            // When
            operationLogAspect.doAfterReturning(joinPoint, null);

            // Then
            Thread.sleep(100);
            ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
            verify(operationLogService).logOperation(
                anyLong(),
                anyString(),
                anyString(),
                anyString(),
                descriptionCaptor.capture(),
                anyString(),
                anyString(),
                anyInt()
            );

            String description = descriptionCaptor.getValue();
            assertTrue(description.contains("newuser") || description.contains("创建用户"));
        }
    }

    @Test
    @DisplayName("测试没有注解时跳过记录")
    void testNoAnnotation_Skip() {
        // Given
        when(method.getAnnotation(OperationLog.class)).thenReturn(null);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(100L);
            mockedSecurity.when(SecurityUtils::getCurrentUsername).thenReturn("admin");

            // When
            operationLogAspect.doAfterReturning(joinPoint, null);

            // Then
            Thread.sleep(100);
            verify(operationLogService, never()).logOperation(any(), any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Test
    @DisplayName("测试多个参数的方法")
    void testMultipleParameters() throws Exception {
        // Given
        Map<String, Object> user = new HashMap<>();
        user.put("username", "testuser");
        String role = "ADMIN";
        when(joinPoint.getArgs()).thenReturn(new Object[]{user, role});
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"user", "role"});

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(100L);
            mockedSecurity.when(SecurityUtils::getCurrentUsername).thenReturn("admin");

            // When
            operationLogAspect.doAfterReturning(joinPoint, null);

            // Then
            Thread.sleep(100);
            verify(operationLogService, atLeastOnce()).logOperation(
                anyLong(),
                anyString(),
                eq("用户管理"),
                eq("创建"),
                anyString(),
                anyString(),
                anyString(),
                eq(1)
            );
        }
    }
}
