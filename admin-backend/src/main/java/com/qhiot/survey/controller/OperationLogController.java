package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.entity.OperationLog;
import com.qhiot.survey.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志", description = "操作日志查询")
@RestController
@RequestMapping("/api/v1/log/operation")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<OperationLog>> listByPage(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(operationLogService.listByPage(module, operator, keyword, pageNum, pageSize));
    }

    @Operation(summary = "导出操作日志")
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String keyword) {
        byte[] excelBytes = operationLogService.exportLogs(module, operator, keyword);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "operation_logs.xlsx");
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @Operation(summary = "统计各模块操作次数")
    @GetMapping("/statistics/module")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Long>> countByModule() {
        return Result.success(operationLogService.countByModule());
    }

    @Operation(summary = "统计各用户操作次数")
    @GetMapping("/statistics/user")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Long>> countByUser() {
        return Result.success(operationLogService.countByUser());
    }

    @Operation(summary = "统计各风险等级操作次数")
    @GetMapping("/statistics/risk-level")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<Integer, Long>> countByRiskLevel() {
        return Result.success(operationLogService.countByRiskLevel());
    }

    @Operation(summary = "按时间范围统计操作趋势")
    @GetMapping("/statistics/trend")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Long>> countByDateRange(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(operationLogService.countByDateRange(startDate, endDate));
    }
}
