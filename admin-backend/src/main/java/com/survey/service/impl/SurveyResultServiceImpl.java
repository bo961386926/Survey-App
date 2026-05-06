package com.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.survey.entity.SurveyResult;
import com.survey.mapper.SurveyResultMapper;
import com.survey.service.SurveyResultService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SurveyResultServiceImpl extends ServiceImpl<SurveyResultMapper, SurveyResult> implements SurveyResultService {

    @Override
    public List<SurveyResult> getResultList(Long pointId) {
        QueryWrapper<SurveyResult> queryWrapper = new QueryWrapper<>();
        if (pointId != null) {
            queryWrapper.eq("point_id", pointId);
        }
        return list(queryWrapper);
    }

    @Override
    public SurveyResult getLatestResult(Long pointId) {
        QueryWrapper<SurveyResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("point_id", pointId)
                    .eq("is_latest", 1)
                    .orderByDesc("version")
                    .last("LIMIT 1");
        return getOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean submitResult(SurveyResult result) {
        // 将旧版本标记为非最新
        UpdateWrapper<SurveyResult> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("point_id", result.getPointId())
                     .eq("is_latest", 1);
        SurveyResult oldResult = new SurveyResult();
        oldResult.setIsLatest(0);
        update(oldResult, updateWrapper);
        
        // 设置新版本为最新
        result.setIsLatest(1);
        if (result.getVersion() == null) {
            result.setVersion(1);
        }
        return save(result);
    }

    @Override
    @Transactional
    public boolean auditResult(Long id, Integer auditStatus, String auditRemark) {
        SurveyResult result = getById(id);
        if (result == null) {
            return false;
        }
        result.setAuditStatus(auditStatus);
        result.setAuditRemark(auditRemark);
        return updateById(result);
    }

    @Override
    public Map<String, Object> getResultDetail(Long id) {
        SurveyResult result = getById(id);
        if (result == null) {
            return null;
        }
        Map<String, Object> detail = new HashMap<>();
        detail.put("result", result);
        return detail;
    }

    @Override
    public Map<String, Object> getAuditList(Integer auditStatus) {
        QueryWrapper<SurveyResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_latest", 1);
        if (auditStatus != null) {
            queryWrapper.eq("audit_status", auditStatus);
        }
        List<SurveyResult> results = list(queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", results);
        return map;
    }
}