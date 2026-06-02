package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.LocationCorrectionLog;
import com.qhiot.survey.entity.OfflineDataSync;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.entity.SysFile;
import com.qhiot.survey.mapper.LocationCorrectionLogMapper;
import com.qhiot.survey.mapper.OfflineDataSyncMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import com.qhiot.survey.mapper.SysFileMapper;
import com.qhiot.survey.service.OfflineDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 离线数据同步服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfflineDataSyncServiceImpl extends ServiceImpl<OfflineDataSyncMapper, OfflineDataSync> 
        implements OfflineDataSyncService {

    private final SurveyResultMapper surveyResultMapper;
    private final SurveyPointMapper surveyPointMapper;
    private final SysFileMapper sysFileMapper;
    private final LocationCorrectionLogMapper locationCorrectionLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 默认最大重试次数
     */
    private static final int DEFAULT_MAX_RETRY = 3;

    /**
     * 冲突解决方案常量
     */
    private static final String RESOLUTION_SERVER_WINS = "server_wins";
    private static final String RESOLUTION_CLIENT_WINS = "client_wins";
    private static final String RESOLUTION_MERGE = "merge";
    private static final String RESOLUTION_SERVER = "server";
    private static final String RESOLUTION_CLIENT = "client";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> receiveOfflineData(String deviceId, Long userId, List<Map<String, Object>> dataList) {
        log.info("接收离线数据: deviceId={}, userId={}, dataCount={}", deviceId, userId, dataList.size());

        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> data : dataList) {
            try {
                OfflineDataSync sync = new OfflineDataSync();
                sync.setDeviceId(deviceId);
                sync.setUserId(userId);
                sync.setDataType((String) data.get("dataType"));
                sync.setDataId((String) data.get("dataId"));
                sync.setDataContent((String) data.get("dataContent"));
                sync.setSyncStatus(0); // 待同步
                sync.setRetryCount(0);
                sync.setMaxRetryCount(DEFAULT_MAX_RETRY);
                sync.setClientCreateTime(LocalDateTime.now());
                sync.setServerReceiveTime(LocalDateTime.now());
                sync.setVersionNo((Integer) data.getOrDefault("versionNo", 1));
                
                save(sync);
                successCount++;
            } catch (Exception e) {
                log.error("接收离线数据失败: {}", e.getMessage(), e);
                failCount++;
                errors.add("数据ID " + data.get("dataId") + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        result.put("message", "成功接收 " + successCount + " 条数据，失败 " + failCount + " 条");

        // 异步触发同步
        if (successCount > 0) {
            triggerAsyncSync(deviceId);
        }

        return result;
    }

    @Override
    public Page<OfflineDataSync> getPendingSyncData(String deviceId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<OfflineDataSync> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OfflineDataSync::getDeviceId, deviceId)
               .eq(OfflineDataSync::getSyncStatus, 0) // 待同步
               .orderByAsc(OfflineDataSync::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncData(Long syncId) {
        OfflineDataSync sync = getById(syncId);
        if (sync == null) {
            throw new BusinessException("同步记录不存在");
        }

        if (sync.getSyncStatus() == 2) {
            throw new BusinessException("该数据已同步");
        }

        // 更新状态为同步中
        sync.setSyncStatus(1);
        updateById(sync);

        try {
            // 根据数据类型执行不同的同步逻辑
            switch (sync.getDataType()) {
                case "survey_result":
                    syncSurveyResult(sync);
                    break;
                case "photo":
                    syncPhoto(sync);
                    break;
                case "location":
                    syncLocation(sync);
                    break;
                default:
                    throw new BusinessException("不支持的数据类型: " + sync.getDataType());
            }

            // 同步成功
            sync.setSyncStatus(2);
            sync.setSyncCompleteTime(LocalDateTime.now());
            updateById(sync);

            log.info("数据同步成功: syncId={}, dataType={}, dataId={}", syncId, sync.getDataType(), sync.getDataId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "同步成功");
            return result;

        } catch (Exception e) {
            log.error("数据同步失败: syncId={}, error={}", syncId, e.getMessage(), e);
            
            // 更新重试次数和状态
            sync.setRetryCount(sync.getRetryCount() + 1);
            sync.setErrorMessage(e.getMessage());
            
            if (sync.getRetryCount() >= sync.getMaxRetryCount()) {
                sync.setSyncStatus(3); // 同步失败
            } else {
                sync.setSyncStatus(0); // 恢复为待同步，等待下次重试
            }
            updateById(sync);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "同步失败: " + e.getMessage());
            result.put("retryCount", sync.getRetryCount());
            return result;
        }
    }

    @Override
    public Map<String, Object> batchSyncData(List<Long> syncIds) {
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> results = new ArrayList<>();

        for (Long syncId : syncIds) {
            try {
                Map<String, Object> result = syncData(syncId);
                results.add(result);
                if ((Boolean) result.get("success")) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
                log.error("批量同步单条数据失败: syncId={}, error={}", syncId, e.getMessage());
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", syncIds.size());
        summary.put("successCount", successCount);
        summary.put("failCount", failCount);
        summary.put("details", results);
        return summary;
    }

    @Override
    public Map<String, Object> getSyncStatus(String deviceId) {
        LambdaQueryWrapper<OfflineDataSync> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OfflineDataSync::getDeviceId, deviceId);
        
        List<OfflineDataSync> allSyncs = list(wrapper);
        
        Map<Integer, Long> statusCount = allSyncs.stream()
            .collect(Collectors.groupingBy(OfflineDataSync::getSyncStatus, Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        result.put("total", allSyncs.size());
        result.put("pending", statusCount.getOrDefault(0, 0L));
        result.put("syncing", statusCount.getOrDefault(1, 0L));
        result.put("completed", statusCount.getOrDefault(2, 0L));
        result.put("failed", statusCount.getOrDefault(3, 0L));
        result.put("lastSyncTime", allSyncs.stream()
            .filter(s -> s.getSyncCompleteTime() != null)
            .max(Comparator.comparing(OfflineDataSync::getSyncCompleteTime))
            .map(OfflineDataSync::getSyncCompleteTime)
            .orElse(null));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> resolveConflict(Long syncId, String resolution, String mergedData) {
        OfflineDataSync sync = getById(syncId);
        if (sync == null) {
            throw new BusinessException("同步记录不存在");
        }

        log.info("处理数据冲突: syncId={}, resolution={}", syncId, resolution);

        switch (resolution) {
            case "server":
                // 以服务器数据为准，删除客户端数据
                sync.setSyncStatus(2);
                sync.setConflictResolution("server");
                sync.setSyncCompleteTime(LocalDateTime.now());
                break;
            case "client":
                // 以客户端数据为准，使用客户端数据
                if (mergedData != null) {
                    sync.setDataContent(mergedData);
                }
                sync.setSyncStatus(0); // 重新同步
                sync.setConflictResolution("client");
                break;
            case "merge":
                // 合并数据
                if (mergedData == null) {
                    throw new BusinessException("合并方案需要提供合并后的数据");
                }
                sync.setDataContent(mergedData);
                sync.setSyncStatus(0); // 重新同步
                sync.setConflictResolution("merge");
                break;
            default:
                throw new BusinessException("不支持的冲突解决方案: " + resolution);
        }

        updateById(sync);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "冲突已处理");
        result.put("resolution", resolution);
        return result;
    }

    @Override
    public Map<String, Object> retrySync(Long syncId) {
        OfflineDataSync sync = getById(syncId);
        if (sync == null) {
            throw new BusinessException("同步记录不存在");
        }

        if (sync.getSyncStatus() != 3) {
            throw new BusinessException("只有同步失败的记录才能重试");
        }

        if (sync.getRetryCount() >= sync.getMaxRetryCount()) {
            throw new BusinessException("已达到最大重试次数");
        }

        // 重置为待同步状态
        sync.setSyncStatus(0);
        sync.setErrorMessage(null);
        updateById(sync);

        return syncData(syncId);
    }

    @Override
    public int cleanupExpiredRecords(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        
        LambdaQueryWrapper<OfflineDataSync> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OfflineDataSync::getCreateTime, cutoffDate)
               .eq(OfflineDataSync::getSyncStatus, 2); // 只清理已同步的

        long countLong = count(wrapper);
        int count = (int) countLong;
        if (count > 0) {
            remove(wrapper);
            log.info("清理过期同步记录: {} 条", count);
        }
        
        return count;
    }

    /**
     * 异步触发同步
     */
    @Async
    public void triggerAsyncSync(String deviceId) {
        try {
            log.info("开始异步同步设备数据: deviceId={}", deviceId);
            
            // 查询该设备所有待同步数据
            LambdaQueryWrapper<OfflineDataSync> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OfflineDataSync::getDeviceId, deviceId)
                   .eq(OfflineDataSync::getSyncStatus, 0);
            
            List<OfflineDataSync> pendingList = list(wrapper);
            
            for (OfflineDataSync sync : pendingList) {
                try {
                    syncData(sync.getId());
                } catch (Exception e) {
                    log.error("异步同步单条数据失败: syncId={}, error={}", sync.getId(), e.getMessage());
                }
            }
            
            log.info("设备数据异步同步完成: deviceId={}", deviceId);
        } catch (Exception e) {
            log.error("异步同步失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }
    }

    /**
     * 同步勘察结果数据
     * 流程：解析JSON -> 版本校验 -> 冲突解决 -> 保存/更新结果 -> 更新点位状态
     */
    private void syncSurveyResult(OfflineDataSync sync) {
        log.info("同步勘察结果: dataId={}, versionNo={}", sync.getDataId(), sync.getVersionNo());

        Map<String, Object> clientData = parseDataContent(sync);
        Long pointId = parseLong(clientData.get("pointId"));
        if (pointId == null) {
            throw new BusinessException("勘察结果同步失败: 缺少pointId");
        }

        // 查询当前数据库中该点位的最新结果（用于版本校验和冲突检测）
        LambdaQueryWrapper<SurveyResult> resultWrapper = new LambdaQueryWrapper<>();
        resultWrapper.eq(SurveyResult::getPointId, pointId)
                     .orderByDesc(SurveyResult::getVersionNo)
                     .last("LIMIT 1");
        SurveyResult serverResult = surveyResultMapper.selectOne(resultWrapper);

        Integer clientVersion = sync.getVersionNo() == null ? 1 : sync.getVersionNo();
        Integer serverVersion = serverResult == null ? 0 : (serverResult.getVersionNo() == null ? 0 : serverResult.getVersionNo());

        // 版本冲突检测：客户端版本落后于服务端
        if (serverResult != null && clientVersion < serverVersion) {
            String resolution = sync.getConflictResolution();
            if (resolution == null || resolution.isEmpty()) {
                throw new BusinessException("勘察结果版本冲突: 客户端v" + clientVersion + " < 服务端v" + serverVersion + "，待人工解决");
            }
            if (isServerWins(resolution)) {
                log.info("勘察结果冲突解决[server_wins]: 保留服务端版本, syncId={}", sync.getId());
                return;
            }
            if (RESOLUTION_MERGE.equalsIgnoreCase(resolution)) {
                Map<String, Object> serverFormData = parseJsonToMap(serverResult.getFormData());
                Map<String, Object> clientFormData = parseJsonToMap((String) clientData.get("formData"));
                Map<String, Object> merged = mergeFields(serverFormData, clientFormData);
                clientData.put("formData", writeJson(merged));
                log.info("勘察结果冲突解决[merge]: 字段级合并, syncId={}", sync.getId());
            }
            // client_wins 走下面的覆盖逻辑
        }

        // 构建/更新 SurveyResult
        SurveyResult target;
        boolean isUpdate = serverResult != null && Objects.equals(clientVersion, serverVersion);
        if (isUpdate) {
            target = serverResult;
        } else {
            target = new SurveyResult();
            target.setPointId(pointId);
            target.setCreateTime(LocalDateTime.now());
        }

        target.setVersionNo(Math.max(clientVersion, serverVersion + (isUpdate ? 0 : 1)));
        target.setTemplateVersionId(parseLong(clientData.get("templateVersionId")));
        Object formData = clientData.get("formData");
        target.setFormData(formData instanceof String ? (String) formData : writeJson(formData));
        Object images = clientData.get("images");
        if (images != null) {
            target.setImages(images instanceof String ? (String) images : writeJson(images));
        }
        Integer resultStatus = parseInt(clientData.get("resultStatus"));
        target.setResultStatus(resultStatus == null ? 1 : resultStatus);
        Integer auditStatus = parseInt(clientData.get("auditStatus"));
        target.setAuditStatus(auditStatus == null ? 0 : auditStatus);
        Long surveyUserId = parseLong(clientData.get("surveyUserId"));
        target.setSurveyUserId(surveyUserId == null ? sync.getUserId() : surveyUserId);
        target.setSubmitTime(LocalDateTime.now());
        target.setUpdateTime(LocalDateTime.now());

        if (isUpdate) {
            surveyResultMapper.updateById(target);
        } else {
            surveyResultMapper.insert(target);
        }

        // 更新点位状态：草稿/已提交 -> 待审核
        SurveyPoint point = surveyPointMapper.selectById(pointId);
        if (point != null) {
            Integer newStatus = mapResultStatusToPointStatus(target.getResultStatus(), point.getStatus());
            if (newStatus != null && !newStatus.equals(point.getStatus())) {
                point.setStatus(newStatus);
                point.setUpdateTime(LocalDateTime.now());
                surveyPointMapper.updateById(point);
            }
        }
    }

    /**
     * 同步照片数据
     * 流程：解析照片元数据 -> 创建/更新 SysFile -> 关联到勘察结果(更新images字段)
     */
    private void syncPhoto(OfflineDataSync sync) {
        log.info("同步照片: dataId={}", sync.getDataId());

        Map<String, Object> photoData = parseDataContent(sync);
        String filePath = (String) photoData.get("filePath");
        String fileName = (String) photoData.get("fileName");
        if (filePath == null || filePath.isEmpty()) {
            throw new BusinessException("照片同步失败: 缺少filePath");
        }

        Long resultId = parseLong(photoData.get("resultId"));
        Long pointId = parseLong(photoData.get("pointId"));
        Long bizId = resultId != null ? resultId : pointId;
        String bizType = (String) photoData.getOrDefault("bizType", "survey_photo");

        // 通过 filePath + bizId 检测重复，避免重复入库
        LambdaQueryWrapper<SysFile> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(SysFile::getFilePath, filePath)
                   .eq(SysFile::getBizType, bizType);
        if (bizId != null) {
            fileWrapper.eq(SysFile::getBizId, bizId);
        }
        SysFile existing = sysFileMapper.selectOne(fileWrapper);

        if (existing != null) {
            // 冲突：服务端已存在同路径文件
            String resolution = sync.getConflictResolution();
            if (isServerWins(resolution)) {
                log.info("照片同步[server_wins]: 服务端已存在, 跳过, syncId={}", sync.getId());
                return;
            }
            // client_wins / merge: 更新元数据
            existing.setFileName(fileName != null ? fileName : existing.getFileName());
            Long size = parseLong(photoData.get("fileSize"));
            if (size != null) existing.setFileSize(size);
            String fileType = (String) photoData.get("fileType");
            if (fileType != null) existing.setFileType(fileType);
            sysFileMapper.updateById(existing);
        } else {
            SysFile file = new SysFile();
            file.setFileName(fileName);
            file.setFilePath(filePath);
            file.setFileSize(parseLong(photoData.get("fileSize")));
            file.setFileType((String) photoData.get("fileType"));
            file.setBizType(bizType);
            file.setBizId(bizId);
            file.setCreatorId(sync.getUserId());
            file.setCreateTime(LocalDateTime.now());
            sysFileMapper.insert(file);
            existing = file;
        }

        // 将照片URL/ID追加到 SurveyResult.images
        if (resultId != null) {
            SurveyResult result = surveyResultMapper.selectById(resultId);
            if (result != null) {
                List<String> imageList = parseJsonToList(result.getImages());
                String photoUrl = filePath;
                if (!imageList.contains(photoUrl)) {
                    imageList.add(photoUrl);
                    result.setImages(writeJson(imageList));
                    result.setUpdateTime(LocalDateTime.now());
                    surveyResultMapper.updateById(result);
                }
            } else {
                log.warn("照片关联的勘察结果不存在: resultId={}", resultId);
            }
        }
    }

    /**
     * 同步位置数据
     * 流程：解析坐标 -> 保存到 location_correction_log -> 视情况更新 SurveyPoint 坐标
     */
    private void syncLocation(OfflineDataSync sync) {
        log.info("同步位置: dataId={}", sync.getDataId());

        Map<String, Object> locationData = parseDataContent(sync);
        Long pointId = parseLong(locationData.get("pointId"));
        if (pointId == null) {
            throw new BusinessException("位置同步失败: 缺少pointId");
        }

        BigDecimal correctedLng = parseBigDecimal(locationData.get("correctedLng"));
        BigDecimal correctedLat = parseBigDecimal(locationData.get("correctedLat"));
        if (correctedLng == null || correctedLat == null) {
            throw new BusinessException("位置同步失败: 缺少纠偏坐标");
        }

        SurveyPoint point = surveyPointMapper.selectById(pointId);
        if (point == null) {
            throw new BusinessException("位置同步失败: 点位不存在 pointId=" + pointId);
        }

        BigDecimal originalLng = parseBigDecimal(locationData.get("originalLng"));
        BigDecimal originalLat = parseBigDecimal(locationData.get("originalLat"));
        if (originalLng == null) originalLng = point.getLongitude();
        if (originalLat == null) originalLat = point.getLatitude();

        // 写入纠偏日志
        LocationCorrectionLog correctionLog = new LocationCorrectionLog();
        correctionLog.setPointId(pointId);
        correctionLog.setResultId(parseLong(locationData.get("resultId")));
        correctionLog.setOriginalLng(originalLng);
        correctionLog.setOriginalLat(originalLat);
        correctionLog.setCorrectedLng(correctedLng);
        correctionLog.setCorrectedLat(correctedLat);
        correctionLog.setUserId(sync.getUserId());
        correctionLog.setCreateTime(LocalDateTime.now());
        locationCorrectionLogMapper.insert(correctionLog);

        // 是否更新点位坐标：默认 applyToPoint=true，server_wins 时拒绝
        boolean applyToPoint = !Boolean.FALSE.equals(locationData.get("applyToPoint"));
        String resolution = sync.getConflictResolution();
        if (applyToPoint && isServerWins(resolution)
                && point.getLongitude() != null && point.getLatitude() != null) {
            log.info("位置同步[server_wins]: 保留服务端坐标, syncId={}", sync.getId());
            applyToPoint = false;
        }

        if (applyToPoint) {
            point.setLongitude(correctedLng);
            point.setLatitude(correctedLat);
            point.setUpdateTime(LocalDateTime.now());
            surveyPointMapper.updateById(point);
        }
    }

    // ============================== 辅助方法 ==============================

    /**
     * 解析 dataContent JSON 为 Map
     */
    private Map<String, Object> parseDataContent(OfflineDataSync sync) {
        String content = sync.getDataContent();
        if (content == null || content.isEmpty()) {
            throw new BusinessException("数据内容为空");
        }
        try {
            return objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new BusinessException("数据内容JSON解析失败: " + e.getMessage());
        }
    }

    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("JSON转Map失败, 返回空Map: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("JSON转List失败, 返回空List: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String writeJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BusinessException("对象序列化为JSON失败: " + e.getMessage());
        }
    }

    /**
     * 字段级合并：以客户端为主，仅在客户端缺失时回退到服务端
     */
    private Map<String, Object> mergeFields(Map<String, Object> server, Map<String, Object> client) {
        Map<String, Object> merged = new HashMap<>(server == null ? new HashMap<>() : server);
        if (client != null) {
            for (Map.Entry<String, Object> entry : client.entrySet()) {
                if (entry.getValue() != null) {
                    merged.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return merged;
    }

    /**
     * 兼容 server / server_wins 两种命名
     */
    private boolean isServerWins(String resolution) {
        return RESOLUTION_SERVER_WINS.equalsIgnoreCase(resolution)
                || RESOLUTION_SERVER.equalsIgnoreCase(resolution);
    }

    /**
     * 根据结果状态映射点位状态
     * resultStatus: 0草稿 1已提交 2待审核 3已通过 4已驳回 5已归档
     * pointStatus:  0待采集 1草稿中 2待审核 3审核通过 4驳回待修改 5已归档
     */
    private Integer mapResultStatusToPointStatus(Integer resultStatus, Integer currentPointStatus) {
        if (resultStatus == null) return null;
        switch (resultStatus) {
            case 0: return 1; // 草稿
            case 1:
            case 2: return 2; // 已提交/待审核 -> 待审核
            case 3: return 3; // 已通过
            case 4: return 4; // 已驳回
            case 5: return 5; // 已归档
            default: return currentPointStatus;
        }
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
