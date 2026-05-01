package com.qhiot.survey.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    @Autowired(required = false)
    private OSS ossClient;

    @Value("${aliyun.oss.bucket-name:survey-images}")
    private String bucketName;

    @Value("${aliyun.oss.folder:survey/}")
    private String folder;

    @Value("${file.upload.path:./uploads}")
    private String localUploadPath;

    public String uploadFile(MultipartFile file) throws IOException {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String fileName = UUID.randomUUID().toString() + extension;

        // 如果OSS客户端可用，使用OSS存储
        if (ossClient != null) {
            String ossKey = folder + "/" + fileName;
            PutObjectResult result = ossClient.putObject(bucketName, ossKey, file.getInputStream());
            if (result != null) {
                return "https://" + bucketName + ".oss-" + getRegion() + ".aliyuncs.com/" + ossKey;
            }
        }

        // 降级使用本地存储
        Path uploadPath = Paths.get(localUploadPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path targetPath = uploadPath.resolve(fileName);
        file.transferTo(targetPath.toFile());
        return "/api/files/" + fileName;
    }

    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl.startsWith("http")) {
                // OSS文件删除
                if (ossClient != null) {
                    String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                    ossClient.deleteObject(bucketName, folder + "/" + fileName);
                }
            } else {
                // 本地文件删除
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(localUploadPath, fileName);
                Files.deleteIfExists(filePath);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getRegion() {
        // 从endpoint中提取区域信息
        return "cn-hangzhou";
    }
}