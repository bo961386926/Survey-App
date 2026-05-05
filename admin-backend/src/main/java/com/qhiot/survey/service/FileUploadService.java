package com.qhiot.survey.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.qhiot.survey.common.util.ImageWatermarkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
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

    /**
     * 上传文件（不带水印）
     */
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, null, null, null);
    }

    /**
     * 上传文件（带水印）
     *
     * @param file      文件
     * @param collector 采集人姓名
     * @param longitude 经度
     * @param latitude  纬度
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file, String collector, Double longitude, Double latitude) throws IOException {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String fileName = UUID.randomUUID().toString() + extension;

        InputStream inputStream;
        
        // 如果是图片且提供了水印信息，则添加水印
        boolean isImage = extension.matches("(?i)\\.(jpg|jpeg|png|gif|bmp|webp)");
        if (isImage && collector != null) {
            try {
                log.info("为图片添加水印: 采集人={}, 经度={}, 纬度={}", collector, longitude, latitude);
                byte[] watermarkedBytes = ImageWatermarkUtil.addWatermark(
                    file.getInputStream(), collector, longitude, latitude
                );
                inputStream = new ByteArrayInputStream(watermarkedBytes);
            } catch (Exception e) {
                log.error("添加水印失败，使用原图上传: {}", e.getMessage(), e);
                inputStream = file.getInputStream();
            }
        } else {
            inputStream = file.getInputStream();
        }

        // 如果OSS客户端可用，使用OSS存储
        if (ossClient != null) {
            String ossKey = folder + "/" + fileName;
            PutObjectResult result = ossClient.putObject(bucketName, ossKey, inputStream);
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
        Files.copy(inputStream, targetPath);
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