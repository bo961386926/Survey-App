package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.entity.LocationCorrectionLog;
import com.qhiot.survey.mapper.LocationCorrectionLogMapper;
import com.qhiot.survey.service.LocationCorrectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 位置纠偏服务实现类
 */
@Service
public class LocationCorrectionServiceImpl implements LocationCorrectionService {

    @Autowired
    private LocationCorrectionLogMapper locationCorrectionLogMapper;

    @Override
    public Page<LocationCorrectionLog> listByPage(Long pointId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<LocationCorrectionLog> wrapper = new LambdaQueryWrapper<>();
        if (pointId != null) {
            wrapper.eq(LocationCorrectionLog::getPointId, pointId);
        }
        wrapper.orderByDesc(LocationCorrectionLog::getCreateTime);
        Page<LocationCorrectionLog> page = new Page<>(pageNum, pageSize);
        return locationCorrectionLogMapper.selectPage(page, wrapper);
    }

    @Override
    public List<LocationCorrectionLog> getCorrectionTrajectory(Long pointId) {
        return locationCorrectionLogMapper.selectList(
                new LambdaQueryWrapper<LocationCorrectionLog>()
                        .eq(LocationCorrectionLog::getPointId, pointId)
                        .orderByAsc(LocationCorrectionLog::getCreateTime)
        );
    }

    @Override
    public void saveCorrectionLog(LocationCorrectionLog log) {
        log.setCreateTime(LocalDateTime.now());
        locationCorrectionLogMapper.insert(log);
    }
}