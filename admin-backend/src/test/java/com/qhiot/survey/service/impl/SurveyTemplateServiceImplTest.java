package com.qhiot.survey.service.impl;

import com.qhiot.survey.entity.SurveyTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SurveyTemplateService 简单单元测试
 * 测试实体类的基本功能
 */
class SurveyTemplateServiceImplTest {

    @Test
    void testSurveyTemplateEntity() {
        // Given
        SurveyTemplate template = new SurveyTemplate();
        template.setId(1L);
        template.setTemplateName("测试模板");
        template.setTemplateCode("TPL001");

        // When & Then
        assertNotNull(template);
        assertEquals(1L, template.getId());
        assertEquals("测试模板", template.getTemplateName());
        assertEquals("TPL001", template.getTemplateCode());
    }

    @Test
    void testTemplateCopy() {
        // Given
        SurveyTemplate original = new SurveyTemplate();
        original.setId(1L);
        original.setTemplateName("原始模板");

        // When
        SurveyTemplate copy = new SurveyTemplate();
        copy.setTemplateName(original.getTemplateName());

        // Then
        assertEquals(original.getTemplateName(), copy.getTemplateName());
    }
}