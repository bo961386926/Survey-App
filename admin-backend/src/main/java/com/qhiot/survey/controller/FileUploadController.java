package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "文件上传", description = "文件上传管理")
@RestController
@RequestMapping("/api/v1/file")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Operation(summary = "上传文件", description = "上传单个文件到OSS，返回文件URL和文件名")
    @PostMapping("/upload")
    @OperationLog(module = "文件管理", action = "上传", description = "上传文件: #file.originalFilename", riskLevel = 0)
    public Result<Map<String, String>> uploadFile(@Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileUploadService.uploadFile(file);

            if (fileUrl != null) {
                Map<String, String> data = new HashMap<>();
                data.put("url", fileUrl);
                data.put("filename", file.getOriginalFilename());
                return Result.success(data);
            } else {
                return Result.error("文件上传失败");
            }
        } catch (IOException e) {
            return Result.error("文件上传异常: " + e.getMessage());
        }
    }

    @Operation(summary = "批量上传文件", description = "批量上传多个文件，返回每个文件的上传结果")
    @PostMapping("/upload/multiple")
    @OperationLog(module = "文件管理", action = "批量上传", description = "批量上传文件, 数量: #files.length", riskLevel = 0)
    public Result<Map<String, Object>> uploadMultipleFiles(@Parameter(description = "上传的文件数组") @RequestParam("files") MultipartFile[] files) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> uploadedFiles = new HashMap<>();
        int successCount = 0;

        for (MultipartFile file : files) {
            try {
                String fileUrl = fileUploadService.uploadFile(file);
                if (fileUrl != null) {
                    uploadedFiles.put(file.getOriginalFilename(), fileUrl);
                    successCount++;
                }
            } catch (IOException e) {
                // 记录失败的文件
                uploadedFiles.put(file.getOriginalFilename(), "上传失败");
            }
        }

        result.put("total", files.length);
        result.put("success", successCount);
        result.put("files", uploadedFiles);

        return Result.success(result);
    }

    @Operation(summary = "删除文件", description = "根据文件URL删除OSS上的文件")
    @DeleteMapping("/delete")
    @OperationLog(module = "文件管理", action = "删除", description = "删除文件", riskLevel = 1)
    public Result<Boolean> deleteFile(@Parameter(description = "文件URL") @RequestParam String fileUrl) {
        boolean success = fileUploadService.deleteFile(fileUrl);
        return success ? Result.success(true) : Result.error("文件删除失败");
    }
}