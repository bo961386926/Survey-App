package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.entity.SurveyResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface SurveyResultService extends IService<SurveyResult> {

    /**
     * 根据点位ID获取勘查结果
     */
    List<SurveyResult> getResultsByPointId(Long pointId);

    /**
     * 获取最新的勘查结果
     */
    SurveyResult getLatestResultByPointId(Long pointId);

    /**
     * 创建勘查结果
     */
    SurveyResult createResult(SurveyResult result);

    /**
     * 更新勘查结果（带乐观锁）
     */
    SurveyResult updateResult(Long id, SurveyResult result, Integer expectedVersion);

    /**
     * 删除勘查结果（逻辑删除）
     */
    boolean deleteResult(Long id);

    /**
     * 分页查询审核列表
     */
    PageResult<SurveyResult> queryAuditPage(Long projectId, Long sectionId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 审核勘查结果（通过）
     */
    boolean passAudit(Long id, String auditRemark, Long auditorId);

    /**
     * 审核勘查结果（驳回）
     * 驳回后系统将被驳回版本数据复制为新的草稿版本（version_no+1）
     */
    boolean rejectAudit(Long id, String auditRemark, Long auditorId);

    /**
     * 批量审核通过
     */
    boolean batchPassAudit(List<Long> ids, String auditRemark, Long auditorId);

    /**
     * 获取用户提交的勘查结果
     */
    List<SurveyResult> getResultsByUser(Long surveyUserId);

    /**
     * 提交审核
     */
    boolean submitForAudit(Long id, Long userId);

    /**
     * 保存草稿
     */
    SurveyResult saveDraft(SurveyResult result, Long userId);

    /**
     * 获取版本差异对比
     */
    Map<String, Object> getVersionDiff(Long currentResultId, Long compareResultId);
}