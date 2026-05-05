package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/file")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
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

    @PostMapping("/upload/multiple")
    public Result<Map<String, Object>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
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

    @DeleteMapping("/delete")
    public Result<Boolean> deleteFile(@RequestParam String fileUrl) {
        boolean success = fileUploadService.deleteFile(fileUrl);
        return success ? Result.success(true) : Result.error("文件删除失败");
    }
}