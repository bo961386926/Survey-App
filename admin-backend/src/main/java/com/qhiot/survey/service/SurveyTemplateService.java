package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.SurveyPointTemplateBinding;
import com.qhiot.survey.entity.SurveyTemplate;
import com.qhiot.survey.entity.SurveyTemplateVersion;

import java.util.List;
import java.util.Map;

public interface SurveyTemplateService extends IService<SurveyTemplate> {
    
    /**
     * 分页查询模板列表
     */
    Page<SurveyTemplate> listByPage(String keyword, Integer status, Integer pageNum, Integer pageSize);
    
    /**
     * 获取模板列表
     */
    List<SurveyTemplate> getTemplateList();
    
    /**
     * 获取模板详情
     */
    SurveyTemplate getTemplateById(Long id);
    
    /**
     * 创建模板
     */
    SurveyTemplate createTemplate(SurveyTemplate template);
    
    /**
     * 更新模板
     */
    SurveyTemplate updateTemplate(Long id, SurveyTemplate template);
    
    /**
     * 删除模板
     */
    void deleteTemplate(Long id);
    
    /**
     * 发布模板生成新版本
     */
    SurveyTemplateVersion publishTemplate(Long templateId, String fieldsJson, String rulesJson, String linkageRulesJson);
    
    /**
     * 获取模板版本列表
     */
    List<SurveyTemplateVersion> getVersionList(Long templateId);
    
    /**
     * 获取模板版本详情
     */
    SurveyTemplateVersion getVersionDetail(Long versionId);
    
    /**
     * 绑定排口类型与模板版本
     */
    void bindOutfallType(Long projectId, Long sectionId, String outfallType, Long templateId, Long templateVersionId);
    
    /**
     * 获取排口类型绑定的模板
     */
    SurveyPointTemplateBinding getBindingByOutfallType(Long projectId, Long sectionId, String outfallType);
    
    /**
     * 获取项目下所有排口类型绑定
     */
    List<SurveyPointTemplateBinding> listBindings(Long projectId);
    
    /**
     * 删除排口类型绑定
     */
    void deleteBinding(Long bindingId);
    
    /**
     * 获取模板字段配置
     */
    Map<String, Object> getFieldConfig(Long versionId);
    
    /**
     * 根据排口类型获取已发布的模板列表
     */
    List<SurveyTemplate> getTemplatesByOutletType(String outletType);
}
