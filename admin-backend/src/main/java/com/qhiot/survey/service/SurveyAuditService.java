package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.entity.SurveyAuditRecord;
import com.qhiot.survey.entity.SurveyResult;

import java.util.List;

/**
 * 审核服务接口
 */
public interface SurveyAuditService {

    /**
     * 分页查询待审核列表
     */
    Page<SurveyResult> getPendingAuditList(Long auditorId, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 获取审核详情（含版本对比）
     */
    SurveyResult getAuditDetail(Long resultId);

    /**
     * 单条审核通过
     */
    void passAudit(Long resultId, Long auditorId, String comment);

    /**
     * 单条审核驳回
     */
    void rejectAudit(Long resultId, Long auditorId, String comment, Long rejectTemplateId);

    /**
     * 批量审核通过
     */
    void batchPass(List<Long> resultIds, Long auditorId, String comment);

    /**
     * 获取审核记录列表
     */
    List<SurveyAuditRecord> getAuditRecords(Long pointId);

    /**
     * 获取版本差异
     */
    Object getVersionDiff(Long pointId, Long currentVersionId, Long compareVersionId);
}