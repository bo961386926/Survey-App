package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.OfflineDataSync;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.mapper.OfflineDataSyncMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import com.qhiot.survey.service.OfflineDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 默认最大重试次数
     */
    private static final int DEFAULT_MAX_RETRY = 3;

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
     */
    private void syncSurveyResult(OfflineDataSync sync) {
        // 这里应该解析dataContent并保存到SurveyResult表
        // 简化实现，实际需要根据业务逻辑处理
        log.info("同步勘察结果: dataId={}", sync.getDataId());
        
        // TODO: 实现具体的同步逻辑
        // 1. 检查是否存在冲突（同一数据点是否有更新版本）
        // 2. 如果有冲突，标记为冲突状态
        // 3. 如果没有冲突，保存到数据库
    }

    /**
     * 同步照片数据
     */
    private void syncPhoto(OfflineDataSync sync) {
        log.info("同步照片: dataId={}", sync.getDataId());
        // TODO: 实现照片同步逻辑
    }

    /**
     * 同步位置数据
     */
    private void syncLocation(OfflineDataSync sync) {
        log.info("同步位置: dataId={}", sync.getDataId());
        // TODO: 实现位置数据同步逻辑
    }
}
