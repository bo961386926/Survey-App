package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.entity.ExportTask;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.service.ExportTaskProcessor;
import com.qhiot.survey.service.ExportTaskService;
import com.qhiot.survey.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 导出任务控制器
 */
@Slf4j
@Tag(name = "导出任务", description = "异步导出任务管理")
@RestController
@RequestMapping("/api/v1/export")
public class ExportTaskController {

    @Autowired
    private ExportTaskService exportTaskService;

    @Autowired
    private ExportTaskProcessor exportTaskProcessor;

    @Autowired
    private SysUserService sysUserService;

    @Operation(summary = "创建导出任务", description = "创建异步导出任务，支持Excel和PDF格式")
    @PostMapping("/create")
    @OperationLog(module = "导出管理", action = "创建任务", description = "创建导出任务, 名称: #taskName, 类型: #taskType", riskLevel = 0)
    public Result<Long> createTask(@Parameter(description = "任务名称") @RequestParam String taskName,
                                   @Parameter(description = "任务类型：excel/pdf") @RequestParam String taskType,
                                   @Parameter(description = "关联项目ID") @RequestParam(required = false) Long projectId) {
        Long creatorId = getCurrentUserId();
        Long taskId = exportTaskService.createExportTask(taskName, taskType, projectId, creatorId);
        return Result.success(taskId);
    }

    @Operation(summary = "创建单点位PDF导出任务", description = "为单个勘察点位创建PDF导出任务")
    @PostMapping("/create-pdf")
    @OperationLog(module = "导出管理", action = "创建PDF任务", description = "创建单点位PDF导出任务, 点位ID: #pointId", riskLevel = 0)
    public Result<Long> createPdfTask(@Parameter(description = "勘察点位ID") @RequestParam Long pointId,
                                      @Parameter(description = "勘察结果ID（可选）") @RequestParam(required = false) Long resultId) {
        Long creatorId = getCurrentUserId();
        Long taskId = exportTaskService.createPdfExportTask(pointId, resultId, creatorId);
        return Result.success(taskId);
    }

    @Operation(summary = "获取我的导出任务列表", description = "获取当前用户创建的所有导出任务")
    @GetMapping("/list")
    public Result<List<ExportTask>> getTaskList() {
        Long userId = getCurrentUserId();
        return Result.success(exportTaskService.getUserTasks(userId));
    }

    @Operation(summary = "获取任务详情", description = "根据任务ID获取导出任务详情，可用于轮询导出进度")
    @GetMapping("/detail/{taskId}")
    public Result<ExportTask> getTaskDetail(@Parameter(description = "导出任务ID") @PathVariable Long taskId) {
        return Result.success(exportTaskService.getTaskDetail(taskId));
    }

    @Operation(summary = "下载导出文件", description = "下载已完成的导出文件，仅已完成(状态=2)且未过期的任务可下载，文件过期返回410")
    @GetMapping("/download/{taskId}")
    public ResponseEntity<Resource> downloadFile(@Parameter(description = "导出任务ID") @PathVariable Long taskId) throws IOException {
        ExportTask task = exportTaskService.getTaskDetail(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        // 仅允许已完成的任务下载
        if (task.getStatus() != 2) {
            return ResponseEntity.badRequest().build();
        }
        // 已过期
        if (task.getExpireTime() != null
                && task.getExpireTime().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(410).build(); // Gone
        }

        Path file = exportTaskProcessor.resolveFile(task);
        if (file == null || !Files.exists(file)) {
            log.warn("[导出] 下载时文件不存在: taskId={}", taskId);
            return ResponseEntity.notFound().build();
        }

        String fileName = file.getFileName().toString();
        String contentType = determineContentType(fileName);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(Files.size(file))
                .body(resource);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        return user.getId();
    }

    private String determineContentType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        }
        return "application/octet-stream";
    }
}
