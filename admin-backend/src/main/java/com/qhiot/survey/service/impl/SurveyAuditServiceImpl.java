package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.enums.AuditStatus;
import com.qhiot.survey.common.enums.ResultStatus;
import com.qhiot.survey.common.enums.SurveyPointStatus;
import com.qhiot.survey.entity.SurveyAuditRecord;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.mapper.SurveyAuditRecordMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import com.qhiot.survey.service.ExportTaskService;
import com.qhiot.survey.service.SurveyAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审核服务实现类
 */
@Slf4j
@Service
public class SurveyAuditServiceImpl implements SurveyAuditService {

    @Autowired
    private SurveyResultMapper surveyResultMapper;

    @Autowired
    private SurveyAuditRecordMapper surveyAuditRecordMapper;

    @Autowired
    private SurveyPointMapper surveyPointMapper;

    @Autowired
    private ExportTaskService exportTaskService;

    @Override
    public Page<SurveyResult> getPendingAuditList(Long auditorId, String keyword, Integer pageNum, Integer pageSize) {
        Page<SurveyResult> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SurveyResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SurveyResult::getResultStatus, ResultStatus.PENDING_AUDIT.getCode());
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SurveyResult::getPointId, keyword);
        }
        wrapper.orderByAsc(SurveyResult::getSubmitTime);
        return surveyResultMapper.selectPage(page, wrapper);
    }

    @Override
    public SurveyResult getAuditDetail(Long resultId) {
        SurveyResult result = surveyResultMapper.selectById(resultId);
        if (result == null) {
            throw new BusinessException("采集结果不存在");
        }
        return result;
    }

    @Override
    @Transactional
    public void passAudit(Long resultId, Long auditorId, String comment) {
        SurveyResult result = surveyResultMapper.selectById(resultId);
        if (result == null) {
            throw new BusinessException("采集结果不存在");
        }
        if (result.getResultStatus() != ResultStatus.PENDING_AUDIT.getCode()) {
            throw new BusinessException("只有待审核状态的结果才能审核");
        }

        // 更新采集结果状态
        result.setResultStatus(ResultStatus.PASSED.getCode());
        result.setAuditStatus(AuditStatus.PASSED.getCode());
        result.setAuditRemark(comment);
        result.setAuditorId(auditorId);
        result.setAuditTime(LocalDateTime.now());
        surveyResultMapper.updateById(result);

        // 更新点位状态
        SurveyPoint point = surveyPointMapper.selectById(result.getPointId());
        if (point != null) {
            point.setStatus(SurveyPointStatus.AUDIT_PASSED.getCode());
            surveyPointMapper.updateById(point);
        }

        // 记录审核日志
        saveAuditRecord(resultId, result.getPointId(), auditorId, "pass", comment, null);

        // 审核通过后自动触发单点位PDF生成
        try {
            exportTaskService.createPdfExportTask(result.getPointId(), resultId, auditorId);
            log.info("审核通过，已自动创建PDF导出任务: pointId={}, resultId={}", result.getPointId(), resultId);
        } catch (Exception e) {
            log.warn("审核通过后自动创建PDF导出任务失败（不影响审核结果）: {}", e.getMessage());
        }

        log.info("审核通过: resultId={}, auditorId={}", resultId, auditorId);
    }

    @Override
    @Transactional
    public void rejectAudit(Long resultId, Long auditorId, String comment, Long rejectTemplateId) {
        SurveyResult result = surveyResultMapper.selectById(resultId);
        if (result == null) {
            throw new BusinessException("采集结果不存在");
        }
        if (result.getResultStatus() != ResultStatus.PENDING_AUDIT.getCode()) {
            throw new BusinessException("只有待审核状态的结果才能审核");
        }
        if (!StringUtils.hasText(comment)) {
            throw new BusinessException("驳回必须填写原因");
        }

        // 创建新版本草稿
        SurveyResult newDraft = new SurveyResult();
        newDraft.setPointId(result.getPointId());
        newDraft.setVersionNo(result.getVersionNo() + 1);
        newDraft.setTemplateVersionId(result.getTemplateVersionId());
        newDraft.setFormData(result.getFormData());
        newDraft.setImages(result.getImages());
        newDraft.setResultStatus(ResultStatus.DRAFT.getCode());
        newDraft.setAuditStatus(AuditStatus.PENDING.getCode());
        newDraft.setSurveyUserId(result.getSurveyUserId());
        newDraft.setOptimisticLockVersion(0);
        surveyResultMapper.insert(newDraft);

        // 更新原结果为驳回状态
        result.setResultStatus(ResultStatus.REJECTED.getCode());
        result.setAuditStatus(AuditStatus.REJECTED.getCode());
        result.setAuditRemark(comment);
        result.setAuditorId(auditorId);
        result.setAuditTime(LocalDateTime.now());
        surveyResultMapper.updateById(result);

        // 更新点位状态
        SurveyPoint point = surveyPointMapper.selectById(result.getPointId());
        if (point != null) {
            point.setStatus(SurveyPointStatus.REJECTED.getCode());
            surveyPointMapper.updateById(point);
        }

        // 记录审核日志
        saveAuditRecord(resultId, result.getPointId(), auditorId, "reject", comment, rejectTemplateId);

        log.info("审核驳回: resultId={}, auditorId={}, newVersion={}", resultId, auditorId, newDraft.getVersionNo());
    }

    @Override
    @Transactional
    public void batchPass(List<Long> resultIds, Long auditorId, String comment) {
        for (Long resultId : resultIds) {
            try {
                passAudit(resultId, auditorId, comment);
            } catch (Exception e) {
                log.error("批量审核失败: resultId={}", resultId, e);
            }
        }
    }

    @Override
    public List<SurveyAuditRecord> getAuditRecords(Long pointId) {
        return surveyAuditRecordMapper.selectList(
                new LambdaQueryWrapper<SurveyAuditRecord>()
                        .eq(SurveyAuditRecord::getPointId, pointId)
                        .orderByDesc(SurveyAuditRecord::getCreateTime)
        );
    }

    @Override
    public Object getVersionDiff(Long pointId, Long currentVersionId, Long compareVersionId) {
        SurveyResult current = surveyResultMapper.selectById(currentVersionId);
        SurveyResult compare = surveyResultMapper.selectById(compareVersionId);
        if (current == null || compare == null) {
            throw new BusinessException("版本不存在");
        }
        // 简化版本差异对比，实际应该使用JSON diff库
        return Map.of(
                "currentVersion", current.getVersionNo(),
                "compareVersion", compare.getVersionNo(),
                "currentData", current.getFormData(),
                "compareData", compare.getFormData()
        );
    }

    private void saveAuditRecord(Long resultId, Long pointId, Long auditorId, String action, String comment, Long rejectTemplateId) {
        SurveyAuditRecord record = new SurveyAuditRecord();
        record.setResultId(resultId);
        record.setPointId(pointId);
        record.setAuditorId(auditorId);
        record.setAction(action);
        record.setAuditComment(comment);
        record.setRejectTemplateId(rejectTemplateId);
        surveyAuditRecordMapper.insert(record);
    }
}