package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.SurveyPointTemplateBinding;
import com.qhiot.survey.entity.SurveyTemplate;
import com.qhiot.survey.entity.SurveyTemplateVersion;

import java.util.List;
import java.util.Map;

public interface SurveyTemplateService extends IService<SurveyTemplate> {
    
    Page<SurveyTemplate> listByPage(String keyword, Integer status, Integer pageNum, Integer pageSize);
    
    List<SurveyTemplate> getTemplateList();
    
    SurveyTemplate getTemplateById(Long id);
    
    SurveyTemplate createTemplate(SurveyTemplate template);
    
    SurveyTemplate updateTemplate(Long id, SurveyTemplate template);
    
    void deleteTemplate(Long id);
    
    SurveyTemplateVersion publishTemplate(Long templateId, String fieldsJson, String rulesJson, String linkageRulesJson);
    
    List<SurveyTemplateVersion> getVersionList(Long templateId);
    
    SurveyTemplateVersion getVersionDetail(Long versionId);

    /**
     * 获取某个模板当前的“已发布”版本详情。未发布返回 null。
     * 该查询在点位加载模板、移动端表单渲染、纯阅场景高频调用，适合加上中期缓存。
     */
    SurveyTemplateVersion getPublishedVersion(Long templateId);

    /**
     * 清除指定模板版本缓存；传 null 表示清除整个命名空间。
     */
    void evictTemplateVersionCache(Long versionId);
    
    void bindOutfallType(Long projectId, Long sectionId, String outfallType, Long templateId, Long templateVersionId);
    
    SurveyPointTemplateBinding getBindingByOutfallType(Long projectId, Long sectionId, String outfallType);
    
    List<SurveyPointTemplateBinding> listBindings(Long projectId);
    
    void deleteBinding(Long bindingId);
    
    Map<String, Object> getFieldConfig(Long versionId);
    
    List<SurveyTemplate> getTemplatesByOutletType(String outletType);

    void saveDraft(Long id, Map<String, Object> draftData);

    Map<String, Object> previewTemplate(Long id);
}
