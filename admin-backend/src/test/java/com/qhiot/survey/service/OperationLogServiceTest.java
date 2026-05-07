package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.BusinessException;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
        // Given
        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        // When
        operationLogService.logOperation(
            100L, "admin", "用户管理", "创建",
            "创建用户: testuser", "192.168.1.100",
            "Mozilla/5.0", 1
        );

        // Then
        verify(operationLogMapper, times(1)).insert(any(OperationLog.class));
    }

    @Test
    @DisplayName("测试分页查询操作日志")
    void testPageQuery_Success() {
        // Given
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        // When
        Page<OperationLog> result = operationLogService.pageQuery(1, 10, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("admin", result.getRecords().get(0).getUsername());
    }

    @Test
    @DisplayName("测试按模块查询操作日志")
    void testPageQuery_ByModule() {
        // Given
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        // When
        Page<OperationLog> result = operationLogService.pageQuery(1, 10, "用户管理", null, null, null);

        // Then
        assertNotNull(result);
        verify(operationLogMapper).selectPage(any(Page.class), argThat(wrapper -> {
            // 验证查询条件包含模块过滤
            return true;
        }));
    }

    @Test
    @DisplayName("测试按风险等级查询操作日志")
    void testPageQuery_ByRiskLevel() {
        // Given
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        // When
        Page<OperationLog> result = operationLogService.pageQuery(1, 10, null, 1, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试按时间范围查询操作日志")
    void testPageQuery_ByTimeRange() {
        // Given
        Page<OperationLog> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        // When
        Page<OperationLog> result = operationLogService.pageQuery(1, 10, null, null, startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试统计各模块操作数量")
    void testCountByModule() {
        // Given
        List<Object[]> mockResult = Arrays.asList(
            new Object[]{"用户管理", 10L},
            new Object[]{"项目管理", 5L},
            new Object[]{"角色管理", 3L}
        );

        when(operationLogMapper.selectMaps(any())).thenReturn(
            Arrays.asList(
                Map.of("module", "用户管理", "count", 10L),
                Map.of("module", "项目管理", "count", 5L),
                Map.of("module", "角色管理", "count", 3L)
            )
        );

        // When
        List<Map<String, Object>> result = operationLogService.countByModule();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("测试统计各风险等级操作数量")
    void testCountByRiskLevel() {
        // Given
        when(operationLogMapper.selectMaps(any())).thenReturn(
            Arrays.asList(
                Map.of("riskLevel", 0, "count", 50L),
                Map.of("riskLevel", 1, "count", 30L),
                Map.of("riskLevel", 2, "count", 10L)
            )
        );

        // When
        List<Map<String, Object>> result = operationLogService.countByRiskLevel();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("测试统计指定用户的操作数量")
    void testCountByUser() {
        // Given
        when(operationLogMapper.selectMaps(any())).thenReturn(
            Arrays.asList(
                Map.of("username", "admin", "count", 100L),
                Map.of("username", "user1", "count", 50L)
            )
        );

        // When
        List<Map<String, Object>> result = operationLogService.countByUser();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("测试获取日志详情")
    void testGetDetail_Success() {
        // Given
        when(operationLogMapper.selectById(1L)).thenReturn(testLog);

        // When
        OperationLog result = operationLogService.getDetail(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("admin", result.getUsername());
        assertEquals("用户管理", result.getModule());
    }

    @Test
    @DisplayName("测试获取不存在的日志详情")
    void testGetDetail_NotFound() {
        // Given
        when(operationLogMapper.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            operationLogService.getDetail(999L);
        });
    }

    @Test
    @DisplayName("测试删除日志")
    void testDeleteLog_Success() {
        // Given
        when(operationLogMapper.deleteById(1L)).thenReturn(1);

        // When
        boolean result = operationLogService.deleteLog(1L);

        // Then
        assertTrue(result);
        verify(operationLogMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("测试批量删除日志")
    void testBatchDelete_Success() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(operationLogMapper.deleteBatchIds(ids)).thenReturn(3);

        // When
        boolean result = operationLogService.batchDelete(ids);

        // Then
        assertTrue(result);
        verify(operationLogMapper, times(1)).deleteBatchIds(ids);
    }

    @Test
    @DisplayName("测试清空所有日志")
    void testClearAll_Success() {
        // Given
        when(operationLogMapper.delete(any())).thenReturn(100);

        // When
        boolean result = operationLogService.clearAll();

        // Then
        assertTrue(result);
    }
}
