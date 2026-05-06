package com.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.survey.entity.SurveyResult;

import java.util.List;
import java.util.Map;

public interface SurveyResultService extends IService<SurveyResult> {
    List<SurveyResult> getResultList(Long pointId);
    SurveyResult getLatestResult(Long pointId);
    boolean submitResult(SurveyResult result);
    boolean auditResult(Long id, Integer auditStatus, String auditRemark);
    Map<String, Object> getResultDetail(Long id);
    Map<String, Object> getAuditList(Integer auditStatus);
}