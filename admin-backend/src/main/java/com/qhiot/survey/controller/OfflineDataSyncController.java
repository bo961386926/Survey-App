package com.qhiot.survey.controller;

import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
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
@Tag(name = "离线数据同步", description = "移动端离线数据同步、冲突处理、状态查询等接口")
@RestController
@RequestMapping("/api/v1/offline-sync")
@RequiredArgsConstructor
public class OfflineDataSyncController {

    private final OfflineDataSyncService offlineDataSyncService;

    @Operation(summary = "批量接收离线数据", description = "接收移动端离线采集的批量数据，需携带Device-Id请求头标识设备")
    @PostMapping("/receive")
    @OperationLog(module = "离线数据同步", action = "接收数据", description = "批量接收离线数据, 设备ID: #deviceId, 数据条数: #dataList.size()", riskLevel = 0)
    public Result<Map<String, Object>> receiveOfflineData(
            @RequestHeader("Device-Id") String deviceId,
            @RequestParam Long userId,
            @RequestBody List<Map<String, Object>> dataList) {
        
        Map<String, Object> result = offlineDataSyncService.receiveOfflineData(deviceId, userId, dataList);
        return Result.success(result);
    }

    @Operation(summary = "查询待同步数据", description = "分页查询指定设备待同步的数据列表")
    @GetMapping("/pending")
    public Result<?> getPendingSyncData(
            @RequestHeader("Device-Id") String deviceId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        return Result.success(offlineDataSyncService.getPendingSyncData(deviceId, pageNum, pageSize));
    }

    @Operation(summary = "同步单条数据", description = "将单条待同步数据标记为已同步")
    @PostMapping("/sync/{syncId}")
    @OperationLog(module = "离线数据同步", action = "同步数据", description = "同步单条数据, syncId: #syncId", riskLevel = 0)
    public Result<Map<String, Object>> syncData(@PathVariable Long syncId) {
        return Result.success(offlineDataSyncService.syncData(syncId));
    }

    @Operation(summary = "批量同步数据", description = "批量将多条待同步数据标记为已同步")
    @PostMapping("/sync/batch")
    @OperationLog(module = "离线数据同步", action = "批量同步", description = "批量同步数据, 数量: #syncIds.size()", riskLevel = 0)
    public Result<Map<String, Object>> batchSyncData(@RequestBody List<Long> syncIds) {
        return Result.success(offlineDataSyncService.batchSyncData(syncIds));
    }

    @Operation(summary = "查询同步状态", description = "查询指定设备的同步状态统计信息")
    @GetMapping("/status")
    public Result<Map<String, Object>> getSyncStatus(@RequestHeader("Device-Id") String deviceId) {
        return Result.success(offlineDataSyncService.getSyncStatus(deviceId));
    }

    @Operation(summary = "处理数据冲突", description = "处理离线数据与服务端数据的冲突，支持覆盖、合并等解决方案")
    @PostMapping("/conflict/{syncId}")
    @OperationLog(module = "离线数据同步", action = "解决冲突", description = "处理数据冲突, syncId: #syncId, 解决方案: #resolution", riskLevel = 1)
    public Result<Map<String, Object>> resolveConflict(
            @PathVariable Long syncId,
            @RequestParam String resolution,
            @RequestBody(required = false) Map<String, String> body) {
        
        String mergedData = body != null ? body.get("mergedData") : null;
        return Result.success(offlineDataSyncService.resolveConflict(syncId, resolution, mergedData));
    }

    @Operation(summary = "重试失败的同步", description = "重新尝试同步失败的数据记录")
    @PostMapping("/retry/{syncId}")
    @OperationLog(module = "离线数据同步", action = "重试同步", description = "重试失败的同步, syncId: #syncId", riskLevel = 0)
    public Result<Map<String, Object>> retrySync(@PathVariable Long syncId) {
        return Result.success(offlineDataSyncService.retrySync(syncId));
    }

    @Operation(summary = "清理过期同步记录", description = "清理超过保留天数的已完成同步记录，默认保留30天")
    @PostMapping("/cleanup")
    @OperationLog(module = "离线数据同步", action = "清理数据", description = "清理过期同步记录, 保留天数: #days", riskLevel = 1)
    public Result<Integer> cleanupExpiredRecords(
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(offlineDataSyncService.cleanupExpiredRecords(days));
    }
}
