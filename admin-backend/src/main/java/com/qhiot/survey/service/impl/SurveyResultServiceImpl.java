package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.ResultCode;
import com.qhiot.survey.common.enums.ResultStatus;
import com.qhiot.survey.common.enums.SurveyPointStatus;
import com.qhiot.survey.common.enums.YesNo;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.entity.SurveyAuditRecord;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.mapper.SurveyAuditRecordMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import com.qhiot.survey.service.SurveyResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyResultServiceImpl extends ServiceImpl<SurveyResultMapper, SurveyResult> implements SurveyResultService {

    private final SurveyPointMapper surveyPointMapper;
    private final SurveyAuditRecordMapper surveyAuditRecordMapper;

    @Override
    public List<SurveyResult> getResultsByPointId(Long pointId) {
        return lambdaQuery()
                .eq(SurveyResult::getPointId, pointId)
                .orderByDesc(SurveyResult::getVersionNo)
                .list();
    }

    @Override
    public SurveyResult getLatestResultByPointId(Long pointId) {
        return lambdaQuery()
                .eq(SurveyResult::getPointId, pointId)
                .orderByDesc(SurveyResult::getVersionNo)
                .last("LIMIT 1")
                .one();
    }

    @Override
    @Transactional
    public SurveyResult createResult(SurveyResult result) {
        // 检查点位是否存在（@TableLogic 自动过滤已删除点位）
        SurveyPoint point = surveyPointMapper.selectById(result.getPointId());
        if (point == null) {
            throw new BusinessException("点位不存在或已作废");
        }

        // 设置版本号
        Integer maxVersionNo = lambdaQuery()
                .eq(SurveyResult::getPointId, result.getPointId())
                .select(SurveyResult::getVersionNo)
                .list()
                .stream()
                .mapToInt(SurveyResult::getVersionNo)
                .max()
                .orElse(0);
        result.setVersionNo(maxVersionNo + 1);
        result.setResultStatus(ResultStatus.DRAFT.getCode());
        result.setAuditStatus(0);
        result.setIsDeleted(YesNo.NO.getCode());
        result.setOptimisticLockVersion(1);

        save(result);
        return result;
    }

    @Override
    @Transactional
    public SurveyResult updateResult(Long id, SurveyResult result, Integer expectedVersion) {
        // @TableLogic 自动过滤已删除记录，getById 为 null 则不存在
        SurveyResult existing = getById(id);
        if (existing == null) {
            throw new BusinessException("勘查结果不存在");
        }

        // 乐观锁校验 - 并发控制
        if (expectedVersion != null && !existing.getOptimisticLockVersion().equals(expectedVersion)) {
            throw new BusinessException("数据已被他人修改，请刷新后重试");
        }

        // 只有草稿和驳回待修改状态才能编辑
        if (existing.getResultStatus() != ResultStatus.DRAFT.getCode()) {
            throw new BusinessException("只有草稿状态的结果可以编辑");
        }

        result.setId(id);
        result.setOptimisticLockVersion(existing.getOptimisticLockVersion() + 1);
        updateById(result);
        return getById(id);
    }

    @Override
    @Transactional
    public boolean deleteResult(Long id) {
        SurveyResult result = getById(id);
        if (result == null) {
            throw new BusinessException("勘查结果不存在");
        }
        // 逻辑删除（@TableLogic 注解在实体上，调用 removeById 生效）
        return removeById(id);
    }

    @Override
    public PageResult<SurveyResult> queryAuditPage(Long projectId, Long sectionId, Integer status, Integer pageNum, Integer pageSize) {
        Page<SurveyResult> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SurveyResult> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(SurveyResult::getResultStatus, status);
        }

        // 关联点位表进行项目/标段筛选（@TableLogic 自动过滤已删除点位）
        if (projectId != null || sectionId != null) {
            List<Long> pointIds = surveyPointMapper.selectList(
                    new LambdaQueryWrapper<SurveyPoint>()
                            .eq(projectId != null, SurveyPoint::getProjectId, projectId)
                            .eq(sectionId != null, SurveyPoint::getSectionId, sectionId)
            ).stream().map(SurveyPoint::getId).collect(Collectors.toList());

            if (pointIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L, (int) page.getCurrent(), (int) page.getSize(), 0);
            }
            wrapper.in(SurveyResult::getPointId, pointIds);
        }

        wrapper.orderByDesc(SurveyResult::getSubmitTime);

        Page<SurveyResult> resultPage = baseMapper.selectPage(page, wrapper);

        int totalPages = (int) Math.ceil((double) resultPage.getTotal() / resultPage.getSize());
        return new PageResult<>(
                resultPage.getRecords(),
                resultPage.getTotal(),
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                totalPages
        );
    }

    @Override
    @Transactional
    public boolean passAudit(Long id, String auditRemark, Long auditorId) {
        SurveyResult result = getById(id);
        if (result == null) {
            throw new BusinessException("勘查结果不存在");
        }

        // 只有待审核状态才能审核
        if (result.getResultStatus() != ResultStatus.PENDING_AUDIT.getCode()) {
            throw new BusinessException("只有待审核状态的结果可以审核");
        }

        // 更新审核状态
        result.setResultStatus(ResultStatus.PASSED.getCode());
        result.setAuditStatus(1);
        result.setAuditRemark(auditRemark);
        result.setAuditorId(auditorId);
        result.setAuditTime(LocalDateTime.now());
        updateById(result);

        // 更新点位状态为审核通过
        SurveyPoint point = surveyPointMapper.selectById(result.getPointId());
        if (point != null) {
            point.setStatus(SurveyPointStatus.AUDIT_PASSED.getCode());
            surveyPointMapper.updateById(point);
        }

        // 保存审核记录(审核通过)
        saveAuditRecord(result.getId(), result.getPointId(), auditorId, "pass", auditRemark, null);

        return true;
    }

    @Override
    @Transactional
    public boolean rejectAudit(Long id, String auditRemark, Long auditorId) {
        SurveyResult rejectedResult = getById(id);
        if (rejectedResult == null) {
            throw new BusinessException("勘查结果不存在");
        }

        // 只有待审核状态才能审核
        if (rejectedResult.getResultStatus() != ResultStatus.PENDING_AUDIT.getCode()) {
            throw new BusinessException("只有待审核状态的结果可以审核");
        }

        // 1. 更新被驳回版本为已驳回状态（只读保留）
        rejectedResult.setResultStatus(ResultStatus.REJECTED.getCode());
        rejectedResult.setAuditStatus(2);
        rejectedResult.setAuditRemark(auditRemark);
        rejectedResult.setAuditorId(auditorId);
        rejectedResult.setAuditTime(LocalDateTime.now());
        updateById(rejectedResult);

        // 2. 复制被驳回版本数据为新的草稿版本（version_no+1）
        SurveyResult newDraft = new SurveyResult();
        BeanUtils.copyProperties(rejectedResult, newDraft, "id", "createTime", "updateTime");
        newDraft.setVersionNo(rejectedResult.getVersionNo() + 1);
        newDraft.setResultStatus(ResultStatus.DRAFT.getCode());
        newDraft.setAuditStatus(0);
        newDraft.setAuditRemark(null);
        newDraft.setAuditorId(null);
        newDraft.setAuditTime(null);
        newDraft.setSubmitTime(null);
        newDraft.setOptimisticLockVersion(1);
        newDraft.setIsDeleted(YesNo.NO.getCode());
        save(newDraft);

        // 3. 更新点位状态为驳回待修改
        SurveyPoint point = surveyPointMapper.selectById(rejectedResult.getPointId());
        if (point != null) {
            point.setStatus(SurveyPointStatus.REJECTED.getCode());
            surveyPointMapper.updateById(point);
        }

        // 4. 保存审核记录(驳回) - 包含驳回原因、审核人、时间戳、原记录ID
        saveAuditRecord(rejectedResult.getId(), rejectedResult.getPointId(), auditorId, "reject", auditRemark, null);

        return true;
    }

    /**
     * 保存审核记录(保证 survey_audit_record 中留存审核决策、原因、时间)
     */
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

    @Override
    @Transactional
    public boolean batchPassAudit(List<Long> ids, String auditRemark, Long auditorId) {
        for (Long id : ids) {
            passAudit(id, auditRemark, auditorId);
        }
        return true;
    }

    @Override
    public List<SurveyResult> getResultsByUser(Long surveyUserId) {
        return lambdaQuery()
                .eq(SurveyResult::getSurveyUserId, surveyUserId)
                .orderByDesc(SurveyResult::getCreateTime)
                .list();
    }

    @Override
    @Transactional
    public boolean submitForAudit(Long id, Long userId, Integer expectedVersionNo) {
        SurveyResult result = getById(id);
        if (result == null) {
            throw new BusinessException("勘查结果不存在");
        }

        // 只有草稿状态才能提交审核
        if (result.getResultStatus() != ResultStatus.DRAFT.getCode()) {
            throw new BusinessException("只有草稿状态的结果可以提交审核");
        }

        // 校验操作人
        if (!result.getSurveyUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        // 版本冲突检测: 客户端传入的 versionNo 必须与当前记录版本一致，且不能低于同点位的最新版本
        if (expectedVersionNo != null) {
            if (!expectedVersionNo.equals(result.getVersionNo())) {
                throw new BusinessException(ResultCode.VERSION_CONFLICT.getCode(),
                        "提交的版本号(" + expectedVersionNo + ")与当前记录版本(" + result.getVersionNo() + ")不一致，请刷新后重试");
            }
            Integer maxVersionNo = lambdaQuery()
                    .eq(SurveyResult::getPointId, result.getPointId())
                    .select(SurveyResult::getVersionNo)
                    .list()
                    .stream()
                    .mapToInt(SurveyResult::getVersionNo)
                    .max()
                    .orElse(0);
            if (expectedVersionNo < maxVersionNo) {
                throw new BusinessException(ResultCode.VERSION_CONFLICT.getCode(),
                        "该点位已有更新版本(v" + maxVersionNo + ")，当前提交版本(v" + expectedVersionNo + ")已过期");
            }
        }

        result.setResultStatus(ResultStatus.PENDING_AUDIT.getCode());
        result.setSubmitTime(LocalDateTime.now());
        return updateById(result);
    }

    @Override
    @Transactional
    public SurveyResult saveDraft(SurveyResult result, Long userId) {
        if (result.getId() != null) {
            // 更新已有草稿
            SurveyResult existing = getById(result.getId());
            if (existing == null) {
                throw new BusinessException("勘查结果不存在");
            }

            // 校验操作人
            if (!existing.getSurveyUserId().equals(userId)) {
                throw new BusinessException("无权操作");
            }

            // 只有草稿状态才能编辑
            if (existing.getResultStatus() != ResultStatus.DRAFT.getCode()) {
                throw new BusinessException("只有草稿状态的结果可以编辑");
            }

            result.setOptimisticLockVersion(existing.getOptimisticLockVersion() + 1);
            updateById(result);
            return getById(result.getId());
        } else {
            // 创建新草稿
            result.setSurveyUserId(userId);
            return createResult(result);
        }
    }

    @Override
    public Map<String, Object> getVersionDiff(Long currentResultId, Long compareResultId) {
        SurveyResult current = getById(currentResultId);
        SurveyResult compare = getById(compareResultId);

        if (current == null || compare == null) {
            throw new BusinessException("勘查结果不存在");
        }

        Map<String, Object> diff = new HashMap<>();
        diff.put("currentVersion", current.getVersionNo());
        diff.put("compareVersion", compare.getVersionNo());
        diff.put("currentFormData", current.getFormData());
        diff.put("compareFormData", compare.getFormData());
        diff.put("currentImages", current.getImages());
        diff.put("compareImages", compare.getImages());
        diff.put("currentStatus", current.getResultStatus());
        diff.put("compareStatus", compare.getResultStatus());

        return diff;
    }
}