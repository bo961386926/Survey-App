package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.entity.OfflineDataSync;

import java.util.List;
import java.util.Map;

/**
 * 离线数据同步服务接口
 */
public interface OfflineDataSyncService {

    /**
     * 批量接收离线数据
     *
     * @param deviceId   设备ID
     * @param userId     用户ID
     * @param dataList   数据列表
     * @return 接收结果
     */
    Map<String, Object> receiveOfflineData(String deviceId, Long userId, List<Map<String, Object>> dataList);

    /**
     * 查询待同步的数据
     *
     * @param deviceId 设备ID
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 待同步数据列表
     */
    Page<OfflineDataSync> getPendingSyncData(String deviceId, Integer pageNum, Integer pageSize);

    /**
     * 同步单条数据
     *
     * @param syncId 同步记录ID
     * @return 同步结果
     */
    Map<String, Object> syncData(Long syncId);

    /**
     * 批量同步数据
     *
     * @param syncIds 同步记录ID列表
     * @return 同步结果统计
     */
    Map<String, Object> batchSyncData(List<Long> syncIds);

    /**
     * 查询同步状态
     *
     * @param deviceId 设备ID
     * @return 同步状态统计
     */
    Map<String, Object> getSyncStatus(String deviceId);

    /**
     * 处理数据冲突
     *
     * @param syncId           同步记录ID
     * @param resolution       解决方案：server/client/merge
     * @param mergedData       合并后的数据（当resolution=merge时使用）
     * @return 处理结果
     */
    Map<String, Object> resolveConflict(Long syncId, String resolution, String mergedData);

    /**
     * 重试失败的同步
     *
     * @param syncId 同步记录ID
     * @return 重试结果
     */
    Map<String, Object> retrySync(Long syncId);

    /**
     * 清理已过期的同步记录
     *
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanupExpiredRecords(int days);
}
