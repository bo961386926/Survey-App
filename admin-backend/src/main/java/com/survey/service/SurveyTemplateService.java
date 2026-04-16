package com.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.survey.entity.SurveyTemplate;

import java.util.List;

public interface SurveyTemplateService extends IService<SurveyTemplate> {
    List<SurveyTemplate> getTemplateList();
    SurveyTemplate getTemplateById(Long id);
    boolean createTemplate(SurveyTemplate template);
    boolean updateTemplate(SurveyTemplate template);
    boolean deleteTemplate(Long id);
}
