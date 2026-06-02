package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.entity.OperationLog;
import com.qhiot.survey.mapper.OperationLogMapper;
import com.qhiot.survey.service.impl.OperationLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 操作日志Service单元测试
 * 测试覆盖：日志记录、查询、统计等核心功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("操作日志Service测试")
class OperationLogServiceTest {

    @Mock
    private OperationLogMapper operationLogMapper;

    @InjectMocks
    private OperationLogServiceImpl operationLogService;

    private OperationLog testLog;

    @BeforeEach
    void setUp() {
        // ServiceImpl 的 baseMapper 字段不会被 @InjectMocks 自动注入，手动设置
        ReflectionTestUtils.setField(operationLogService, "baseMapper", operationLogMapper);

        testLog = new OperationLog();
        testLog.setId(1L);
        testLog.setUserId(100L);
        testLog.setUsername("admin");
        testLog.setModule("用户管理");
        testLog.setAction("创建");
        testLog.setDetail("创建用户: testuser");
        testLog.setIp("192.168.1.100");
        testLog.setUserAgent("Mozilla/5.0");
        testLog.setRiskLevel(1);
        testLog.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试记录操作日志 - 正常情况")
    void testLogOperation_Success() {
        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        operationLogService.logOperation(
            100L, "admin", "用户管理", "创建",
            "创建用户: testuser", "192.168.1.100",
            "Mozilla/5.0", 1
        );

        verify(operationLogMapper, times(1)).insert(any(OperationLog.class));
    }

    @Test
    @DisplayName("测试分页查询操作日志")
    void testListByPage_Success() {
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<OperationLog> result = operationLogService.listByPage(null, null, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("admin", result.getRecords().get(0).getUsername());
    }

    @Test
    @DisplayName("测试按模块查询操作日志")
    void testListByPage_ByModule() {
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<OperationLog> result = operationLogService.listByPage("用户管理", null, null, 1, 10);

        assertNotNull(result);
        verify(operationLogMapper).selectPage(any(Page.class), any());
    }

    @Test
    @DisplayName("测试按操作人查询操作日志")
    void testListByPage_ByOperator() {
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<OperationLog> result = operationLogService.listByPage(null, "admin", null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试按关键字查询操作日志")
    void testListByPage_ByKeyword() {
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<OperationLog> result = operationLogService.listByPage(null, null, "创建", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试统计各模块操作数量")
    void testCountByModule() {
        when(operationLogMapper.selectList(any())).thenReturn(Arrays.asList(testLog, testLog));

        Map<String, Long> result = operationLogService.countByModule();

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试统计各风险等级操作数量")
    void testCountByRiskLevel() {
        when(operationLogMapper.selectList(any())).thenReturn(Arrays.asList(testLog, testLog));

        Map<Integer, Long> result = operationLogService.countByRiskLevel();

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试统计指定用户的操作数量")
    void testCountByUser() {
        when(operationLogMapper.selectList(any())).thenReturn(Arrays.asList(testLog, testLog));

        Map<String, Long> result = operationLogService.countByUser();

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试按时间范围统计操作趋势")
    void testCountByDateRange() {
        when(operationLogMapper.selectList(any())).thenReturn(Arrays.asList(testLog, testLog));

        Map<String, Long> result = operationLogService.countByDateRange("2024-01-01", "2024-12-31");

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试空结果统计")
    void testCountByModule_EmptyResult() {
        when(operationLogMapper.selectList(any())).thenReturn(Collections.emptyList());

        Map<String, Long> result = operationLogService.countByModule();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
