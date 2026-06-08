package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.constant.Permissions;
import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.entity.LoginLog;
import com.qhiot.survey.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志控制器
 */
@Tag(name = "登录日志", description = "登录日志查询")
@RestController
@RequestMapping("/api/v1/log/login")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    @Operation(summary = "分页查询登录日志", description = "管理员分页查询登录日志，支持按用户名和登录状态筛选")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_LOG + "')")
    public Result<Page<LoginLog>> listByPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(loginLogService.listByPage(username, status, pageNum, pageSize));
    }

    @Operation(summary = "导出登录日志", description = "导出登录日志为Excel文件，支持筛选条件")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('" + Permissions.SYSTEM_LOG + "')")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        byte[] excelBytes = loginLogService.exportLogs(username, status);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "login_logs.xlsx");
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}
