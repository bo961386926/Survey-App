package com.qhiot.survey.service.impl;

import com.qhiot.survey.common.enums.SurveyPointStatus;
import com.qhiot.survey.common.enums.YesNo;
import com.qhiot.survey.entity.SurveyPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SurveyPointService 简单单元测试
 * 测试实体类和枚举的基本功能
 */
class SurveyPointServiceImplTest {

    @Test
    void testSurveyPointEntity() {
        // Given
        SurveyPoint point = new SurveyPoint();
        point.setId(1L);
        point.setPointCode("P001");
        point.setPointName("测试点位");
        point.setProjectId(1L);
        point.setSectionId(1L);
        point.setStatus(SurveyPointStatus.PENDING.getCode());
        point.setIsDeleted(YesNo.NO.getCode());

        // When & Then
        assertNotNull(point);
        assertEquals(1L, point.getId());
        assertEquals("P001", point.getPointCode());
        assertEquals("测试点位", point.getPointName());
        assertEquals(SurveyPointStatus.PENDING.getCode(), point.getStatus());
        assertEquals(YesNo.NO.getCode(), point.getIsDeleted());
    }

    @Test
    void testSurveyPointStatusEnum() {
        // Test enum values
        assertEquals(0, SurveyPointStatus.PENDING.getCode());
        assertEquals(1, SurveyPointStatus.DRAFT.getCode());
        assertEquals(2, SurveyPointStatus.PENDING_AUDIT.getCode());
        assertEquals(3, SurveyPointStatus.AUDIT_PASSED.getCode());
        assertEquals(4, SurveyPointStatus.REJECTED.getCode());
        assertEquals(5, SurveyPointStatus.ARCHIVED.getCode());
        assertEquals(6, SurveyPointStatus.INVALIDATED.getCode());
    }

    @Test
    void testYesNoEnum() {
        // Test enum values
        assertEquals(0, YesNo.NO.getCode());
        assertEquals(1, YesNo.YES.getCode());
    }

    @Test
    void testSurveyPointCopy() {
        // Given
        SurveyPoint original = new SurveyPoint();
        original.setId(1L);
        original.setPointCode("P001");
        original.setPointName("原始点位");
        original.setStatus(SurveyPointStatus.PENDING.getCode());

        // When
        SurveyPoint copy = new SurveyPoint();
        copy.setPointCode(original.getPointCode());
        copy.setPointName(original.getPointName());
        copy.setStatus(original.getStatus());

        // Then
        assertEquals(original.getPointCode(), copy.getPointCode());
        assertEquals(original.getPointName(), copy.getPointName());
        assertEquals(original.getStatus(), copy.getStatus());
    }
}