package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.entity.LocationCorrectionLog;

import java.util.List;

/**
 * 位置纠偏服务接口
 */
public interface LocationCorrectionService {

    /**
     * 分页查询纠偏日志
     */
    Page<LocationCorrectionLog> listByPage(Long pointId, Integer pageNum, Integer pageSize);

    /**
     * 获取点位的纠偏轨迹
     */
    List<LocationCorrectionLog> getCorrectionTrajectory(Long pointId);

    /**
     * 保存纠偏日志
     */
    void saveCorrectionLog(LocationCorrectionLog log);
}