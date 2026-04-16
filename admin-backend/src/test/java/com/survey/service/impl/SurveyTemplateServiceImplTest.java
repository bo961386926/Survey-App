package com.survey.service.impl;

import com.survey.entity.SurveyTemplate;
import com.survey.mapper.SurveyTemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SurveyTemplateServiceImplTest {

    @Mock
    private SurveyTemplateMapper surveyTemplateMapper;

    @InjectMocks
    private SurveyTemplateServiceImpl surveyTemplateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTemplateList() {
        // Given
        SurveyTemplate template1 = new SurveyTemplate();
        template1.setId(1L);
        template1.setTemplateName("模板1");

        SurveyTemplate template2 = new SurveyTemplate();
        template2.setId(2L);
        template2.setTemplateName("模板2");

        List<SurveyTemplate> mockTemplates = Arrays.asList(template1, template2);
        when(surveyTemplateMapper.selectList(null)).thenReturn(mockTemplates);

        // When
        List<SurveyTemplate> result = surveyTemplateService.getTemplateList();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("模板1", result.get(0).getTemplateName());
        verify(surveyTemplateMapper).selectList(null);
    }

    @Test
    void getTemplateById() {
        // Given
        Long templateId = 1L;
        SurveyTemplate expected = new SurveyTemplate();
        expected.setId(templateId);
        expected.setTemplateName("测试模板");
        when(surveyTemplateMapper.selectById(templateId)).thenReturn(expected);

        // When
        SurveyTemplate result = surveyTemplateService.getTemplateById(templateId);

        // Then
        assertNotNull(result);
        assertEquals(templateId, result.getId());
        assertEquals("测试模板", result.getTemplateName());
        verify(surveyTemplateMapper).selectById(templateId);
    }

    @Test
    void createTemplate() {
        // Given
        SurveyTemplate template = new SurveyTemplate();
        template.setTemplateName("新建模板");
        when(surveyTemplateMapper.insert(template)).thenReturn(1);

        // When
        boolean result = surveyTemplateService.createTemplate(template);

        // Then
        assertTrue(result);
        verify(surveyTemplateMapper).insert(template);
    }

    @Test
    void updateTemplate() {
        // Given
        SurveyTemplate template = new SurveyTemplate();
        template.setId(1L);
        template.setTemplateName("更新模板");
        when(surveyTemplateMapper.updateById(template)).thenReturn(1);

        // When
        boolean result = surveyTemplateService.updateTemplate(template);

        // Then
        assertTrue(result);
        verify(surveyTemplateMapper).updateById(template);
    }

    @Test
    void deleteTemplate() {
        // Given
        Long templateId = 1L;
        when(surveyTemplateMapper.deleteById(templateId)).thenReturn(1);

        // When
        boolean result = surveyTemplateService.deleteTemplate(templateId);

        // Then
        assertTrue(result);
        verify(surveyTemplateMapper).deleteById(templateId);
    }
}
