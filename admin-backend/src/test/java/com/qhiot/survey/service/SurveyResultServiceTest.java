package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.ResultCode;
import com.qhiot.survey.common.enums.ResultStatus;
import com.qhiot.survey.common.enums.SurveyPointStatus;
import com.qhiot.survey.entity.SurveyAuditRecord;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.mapper.SurveyAuditRecordMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.service.impl.SurveyResultServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * SurveyResultService 单元测试
 *
 * <p>核心覆盖：</p>
 * <ol>
 *   <li>提交审核 - 版本一致时成功</li>
 *   <li>提交审核 - 版本号低于服务端记录版本（VERSION_CONFLICT=409）</li>
 *   <li>提交审核 - 同一点位存在更新版本（VERSION_CONFLICT=409）</li>
 *   <li>passAudit - 审核通过后写入 audit_record 并更新点位状态</li>
 *   <li>rejectAudit - 驳回标记原记录为 REJECTED 并生成 version+1 的新草稿</li>
 * </ol>
 *
 * <p>实现技巧：{@link SurveyResultServiceImpl} 继承自 MyBatis-Plus 的 ServiceImpl，
 * 直接对 ServiceImpl 提供的便捷方法 (getById/save/updateById/lambdaQuery) 进行 spy 桩，
 * 而不是 mock 底层 baseMapper，可避免 MyBatis-Plus 的链式 wrapper 内部实现耦合。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SurveyResultService 单元测试")
class SurveyResultServiceTest {

    @Mock private SurveyPointMapper surveyPointMapper;
    @Mock private SurveyAuditRecordMapper surveyAuditRecordMapper;

    private SurveyResultServiceImpl service;

    @BeforeEach
    void setUp() {
        service = Mockito.spy(new SurveyResultServiceImpl(surveyPointMapper, surveyAuditRecordMapper));
    }

    /**
     * 为 {@code service.lambdaQuery().eq(...).select(...).list()} 这样的链式调用
     * 准备一个返回固定列表的 mock wrapper。
     * <p>Mockito 5.x 的 RETURNS_SELF 在 subclass mock-maker 下对泛型 wrapper 表现不稳定，
     * 改用自定义 Answer：凡是返回类型为 wrapper 本身或其超类/接口的方法，
     * 一律返回 mock 实例；list() 返回预设列表。</p>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubLambdaQueryList(List<SurveyResult> returnList) {
        LambdaQueryChainWrapper wrapper = mock(LambdaQueryChainWrapper.class, invocation -> {
            String name = invocation.getMethod().getName();
            if ("list".equals(name)) {
                return returnList;
            }
            Class<?> rt = invocation.getMethod().getReturnType();
            if (rt.isAssignableFrom(LambdaQueryChainWrapper.class)) {
                return invocation.getMock();
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });
        doReturn(wrapper).when(service).lambdaQuery();
    }

    private SurveyResult draftResult(Long id, Long pointId, Integer versionNo, Long userId) {
        SurveyResult r = new SurveyResult();
        r.setId(id);
        r.setPointId(pointId);
        r.setVersionNo(versionNo);
        r.setSurveyUserId(userId);
        r.setResultStatus(ResultStatus.DRAFT.getCode());
        r.setAuditStatus(0);
        r.setOptimisticLockVersion(1);
        r.setFormData("{\"a\":1}");
        return r;
    }

    @Test
    @DisplayName("submitForAudit - 版本号匹配且无更新版本时提交成功")
    void testSubmitForAudit_Success() {
        SurveyResult existing = draftResult(10L, 1L, 1, 100L);
        doReturn(existing).when(service).getById(10L);
        doReturn(true).when(service).updateById(any(SurveyResult.class));
        // 同点位最大版本即为本身的 v1
        stubLambdaQueryList(Collections.singletonList(existing));

        boolean ok = service.submitForAudit(10L, 100L, 1);

        assertEquals(true, ok);
        ArgumentCaptor<SurveyResult> captor = ArgumentCaptor.forClass(SurveyResult.class);
        verify(service, times(1)).updateById(captor.capture());
        SurveyResult updated = captor.getValue();
        assertEquals(ResultStatus.PENDING_AUDIT.getCode(), updated.getResultStatus());
        assertNotNull(updated.getSubmitTime());
    }

    @Test
    @DisplayName("submitForAudit - 客户端版本号低于当前版本应抛 VERSION_CONFLICT(409)")
    void testSubmitForAudit_StaleClientVersion() {
        SurveyResult existing = draftResult(10L, 1L, 2, 100L);
        doReturn(existing).when(service).getById(10L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.submitForAudit(10L, 100L, 1));

        assertEquals(ResultCode.VERSION_CONFLICT.getCode(), ex.getCode());
        verify(service, Mockito.never()).updateById(any(SurveyResult.class));
    }

    @Test
    @DisplayName("submitForAudit - 同点位已存在更新版本应抛 VERSION_CONFLICT(409)")
    void testSubmitForAudit_NewerVersionExistsForSamePoint() {
        SurveyResult existing = draftResult(10L, 1L, 1, 100L);
        SurveyResult newer = draftResult(11L, 1L, 3, 100L); // 同点位更高版本
        doReturn(existing).when(service).getById(10L);
        stubLambdaQueryList(Arrays.asList(existing, newer));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.submitForAudit(10L, 100L, 1));

        assertEquals(ResultCode.VERSION_CONFLICT.getCode(), ex.getCode());
        verify(service, Mockito.never()).updateById(any(SurveyResult.class));
    }

    @Test
    @DisplayName("submitForAudit - 非草稿状态拒绝提交")
    void testSubmitForAudit_NotDraftRejected() {
        SurveyResult existing = draftResult(10L, 1L, 1, 100L);
        existing.setResultStatus(ResultStatus.PASSED.getCode());
        doReturn(existing).when(service).getById(10L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.submitForAudit(10L, 100L, null));
        assertEquals("只有草稿状态的结果可以提交审核", ex.getMessage());
    }

    @Test
    @DisplayName("passAudit - 审核通过后创建审核记录并更新点位状态")
    void testPassAudit_CreatesAuditRecordAndUpdatesPoint() {
        SurveyResult pending = draftResult(10L, 1L, 1, 100L);
        pending.setResultStatus(ResultStatus.PENDING_AUDIT.getCode());
        doReturn(pending).when(service).getById(10L);
        doReturn(true).when(service).updateById(any(SurveyResult.class));

        SurveyPoint point = new SurveyPoint();
        point.setId(1L);
        point.setStatus(SurveyPointStatus.PENDING_AUDIT.getCode());
        when(surveyPointMapper.selectById(1L)).thenReturn(point);
        when(surveyPointMapper.updateById(any(SurveyPoint.class))).thenReturn(1);
        when(surveyAuditRecordMapper.insert(any(SurveyAuditRecord.class))).thenReturn(1);

        boolean ok = service.passAudit(10L, "OK", 999L);

        assertEquals(true, ok);

        // 结果应被更新为 PASSED
        ArgumentCaptor<SurveyResult> resultCaptor = ArgumentCaptor.forClass(SurveyResult.class);
        verify(service).updateById(resultCaptor.capture());
        SurveyResult updated = resultCaptor.getValue();
        assertEquals(ResultStatus.PASSED.getCode(), updated.getResultStatus());
        assertEquals(999L, updated.getAuditorId());
        assertEquals("OK", updated.getAuditRemark());
        assertNotNull(updated.getAuditTime());

        // 点位状态变为 AUDIT_PASSED
        ArgumentCaptor<SurveyPoint> pointCaptor = ArgumentCaptor.forClass(SurveyPoint.class);
        verify(surveyPointMapper).updateById(pointCaptor.capture());
        assertEquals(SurveyPointStatus.AUDIT_PASSED.getCode(), pointCaptor.getValue().getStatus());

        // 必须留存审核记录
        ArgumentCaptor<SurveyAuditRecord> recordCaptor = ArgumentCaptor.forClass(SurveyAuditRecord.class);
        verify(surveyAuditRecordMapper, times(1)).insert(recordCaptor.capture());
        SurveyAuditRecord rec = recordCaptor.getValue();
        assertEquals("pass", rec.getAction());
        assertEquals(999L, rec.getAuditorId());
        assertEquals("OK", rec.getAuditComment());
    }

    @Test
    @DisplayName("rejectAudit - 标记原记录 REJECTED 并生成 version+1 的新草稿")
    void testRejectAudit_MarksOriginalRejectedAndCreatesNewDraft() {
        SurveyResult pending = draftResult(10L, 1L, 1, 100L);
        pending.setResultStatus(ResultStatus.PENDING_AUDIT.getCode());
        doReturn(pending).when(service).getById(10L);
        doReturn(true).when(service).updateById(any(SurveyResult.class));
        doReturn(true).when(service).save(any(SurveyResult.class));

        SurveyPoint point = new SurveyPoint();
        point.setId(1L);
        point.setStatus(SurveyPointStatus.PENDING_AUDIT.getCode());
        when(surveyPointMapper.selectById(1L)).thenReturn(point);
        when(surveyPointMapper.updateById(any(SurveyPoint.class))).thenReturn(1);
        when(surveyAuditRecordMapper.insert(any(SurveyAuditRecord.class))).thenReturn(1);

        boolean ok = service.rejectAudit(10L, "缺少照片，请补充", 999L);

        assertEquals(true, ok);

        // 原记录被标记为 REJECTED
        ArgumentCaptor<SurveyResult> updateCaptor = ArgumentCaptor.forClass(SurveyResult.class);
        verify(service).updateById(updateCaptor.capture());
        SurveyResult rejected = updateCaptor.getValue();
        assertEquals(ResultStatus.REJECTED.getCode(), rejected.getResultStatus());
        assertEquals("缺少照片，请补充", rejected.getAuditRemark());

        // 新草稿：版本号 +1，状态为 DRAFT，审核字段被清空，id 必须由 MP 重新生成（不复用原 id）
        ArgumentCaptor<SurveyResult> saveCaptor = ArgumentCaptor.forClass(SurveyResult.class);
        verify(service).save(saveCaptor.capture());
        SurveyResult newDraft = saveCaptor.getValue();
        assertEquals(Integer.valueOf(2), newDraft.getVersionNo());
        assertEquals(ResultStatus.DRAFT.getCode(), newDraft.getResultStatus());
        assertEquals(Integer.valueOf(0), newDraft.getAuditStatus());
        assertEquals(null, newDraft.getAuditRemark());
        assertEquals(null, newDraft.getAuditorId());
        assertEquals(null, newDraft.getAuditTime());
        assertEquals(Integer.valueOf(1), newDraft.getOptimisticLockVersion());
        // BeanUtils.copyProperties 忽略 id，新草稿的 id 必须为 null（待 MP 分配）
        assertNotEquals(Long.valueOf(10L), newDraft.getId());

        // 点位状态变为 REJECTED 待修改
        ArgumentCaptor<SurveyPoint> pointCaptor = ArgumentCaptor.forClass(SurveyPoint.class);
        verify(surveyPointMapper).updateById(pointCaptor.capture());
        assertEquals(SurveyPointStatus.REJECTED.getCode(), pointCaptor.getValue().getStatus());

        // 审核记录写入：action=reject
        ArgumentCaptor<SurveyAuditRecord> recordCaptor = ArgumentCaptor.forClass(SurveyAuditRecord.class);
        verify(surveyAuditRecordMapper).insert(recordCaptor.capture());
        assertEquals("reject", recordCaptor.getValue().getAction());
        assertEquals("缺少照片，请补充", recordCaptor.getValue().getAuditComment());
    }

    @Test
    @DisplayName("passAudit - 非待审核状态拒绝审核")
    void testPassAudit_RejectsNonPendingStatus() {
        SurveyResult result = draftResult(10L, 1L, 1, 100L); // DRAFT
        doReturn(result).when(service).getById(10L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.passAudit(10L, "OK", 999L));
        assertEquals("只有待审核状态的结果可以审核", ex.getMessage());

        verify(surveyAuditRecordMapper, Mockito.never()).insert(any(SurveyAuditRecord.class));
    }
}
