package com.survey.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.survey.entity.SurveyTemplate;
import com.survey.mapper.SurveyTemplateMapper;
import com.survey.service.SurveyTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SurveyTemplateServiceImpl extends ServiceImpl<SurveyTemplateMapper, SurveyTemplate> implements SurveyTemplateService {

    @Override
    public List<SurveyTemplate> getTemplateList() {
        return list();
    }

    @Override
    public SurveyTemplate getTemplateById(Long id) {
        return getById(id);
    }

    @Override
    @Transactional
    public boolean createTemplate(SurveyTemplate template) {
        return save(template);
    }

    @Override
    @Transactional
    public boolean updateTemplate(SurveyTemplate template) {
        return updateById(template);
    }

    @Override
    @Transactional
    public boolean deleteTemplate(Long id) {
        return removeById(id);
    }
}
