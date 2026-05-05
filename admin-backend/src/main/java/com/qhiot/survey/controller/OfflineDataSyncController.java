package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.entity.OfflineDataSync;
import com.qhiot.survey.service.OfflineDataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 离线数据同步控制器
 */
@Tag(name = "离线数据同步")
@RestController
@RequestMapping("/api/v1/offline-sync")
@RequiredArgsConstructor
public class OfflineDataSyncController {

    private final OfflineDataSyncService offlineDataSyncService;

    @Operation(summary = "批量接收离线数据")
    @PostMapping("/receive")
    public Result<Map<String, Object>> receiveOfflineData(
            @RequestHeader("Device-Id") String deviceId,
            @RequestParam Long userId,
            @RequestBody List<Map<String, Object>> dataList) {
        
        Map<String, Object> result = offlineDataSyncService.receiveOfflineData(deviceId, userId, dataList);
        return Result.success(result);
    }

    @Operation(summary = "查询待同步数据")
    @GetMapping("/pending")
    public Result<?> getPendingSyncData(
            @RequestHeader("Device-Id") String deviceId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        return Result.success(offlineDataSyncService.getPendingSyncData(deviceId, pageNum, pageSize));
    }

    @Operation(summary = "同步单条数据")
    @PostMapping("/sync/{syncId}")
    public Result<Map<String, Object>> syncData(@PathVariable Long syncId) {
        return Result.success(offlineDataSyncService.syncData(syncId));
    }

    @Operation(summary = "批量同步数据")
    @PostMapping("/sync/batch")
    public Result<Map<String, Object>> batchSyncData(@RequestBody List<Long> syncIds) {
        return Result.success(offlineDataSyncService.batchSyncData(syncIds));
    }

    @Operation(summary = "查询同步状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getSyncStatus(@RequestHeader("Device-Id") String deviceId) {
        return Result.success(offlineDataSyncService.getSyncStatus(deviceId));
    }

    @Operation(summary = "处理数据冲突")
    @PostMapping("/conflict/{syncId}")
    public Result<Map<String, Object>> resolveConflict(
            @PathVariable Long syncId,
            @RequestParam String resolution,
            @RequestBody(required = false) Map<String, String> body) {
        
        String mergedData = body != null ? body.get("mergedData") : null;
        return Result.success(offlineDataSyncService.resolveConflict(syncId, resolution, mergedData));
    }

    @Operation(summary = "重试失败的同步")
    @PostMapping("/retry/{syncId}")
    public Result<Map<String, Object>> retrySync(@PathVariable Long syncId) {
        return Result.success(offlineDataSyncService.retrySync(syncId));
    }

    @Operation(summary = "清理过期同步记录")
    @PostMapping("/cleanup")
    public Result<Integer> cleanupExpiredRecords(
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(offlineDataSyncService.cleanupExpiredRecords(days));
    }
}
