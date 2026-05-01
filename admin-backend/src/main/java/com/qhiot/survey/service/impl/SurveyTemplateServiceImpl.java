package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.enums.TemplateStatus;
import com.qhiot.survey.entity.SurveyPointTemplateBinding;
import com.qhiot.survey.entity.SurveyTemplate;
import com.qhiot.survey.entity.SurveyTemplateVersion;
import com.qhiot.survey.mapper.SurveyPointTemplateBindingMapper;
import com.qhiot.survey.mapper.SurveyTemplateMapper;
import com.qhiot.survey.mapper.SurveyTemplateVersionMapper;
import com.qhiot.survey.service.SurveyTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SurveyTemplateServiceImpl extends ServiceImpl<SurveyTemplateMapper, SurveyTemplate> implements SurveyTemplateService {

    @Autowired
    private SurveyTemplateVersionMapper surveyTemplateVersionMapper;

    @Autowired
    private SurveyPointTemplateBindingMapper surveyPointTemplateBindingMapper;

    @Override
    public Page<SurveyTemplate> listByPage(String keyword, Integer status, Integer pageNum, Integer pageSize) {
        Page<SurveyTemplate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SurveyTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SurveyTemplate::getTemplateName, keyword);
        }
        if (status != null) {
            wrapper.eq(SurveyTemplate::getStatus, status);
        }
        wrapper.orderByDesc(SurveyTemplate::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SurveyTemplate> getTemplateList() {
        return list();
    }

    @Override
    public SurveyTemplate getTemplateById(Long id) {
        SurveyTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        return template;
    }

    @Override
    @Transactional
    public SurveyTemplate createTemplate(SurveyTemplate template) {
        // 检查模板编码是否重复
        Long count = lambdaQuery()
                .eq(SurveyTemplate::getTemplateCode, template.getTemplateCode())
                .count();
        if (count > 0) {
            throw new BusinessException("模板编码已存在");
        }
        template.setStatus(TemplateStatus.DRAFT.getCode());
        save(template);
        return template;
    }

    @Override
    public List<SurveyTemplate> getTemplatesByOutletType(String outletType) {
        LambdaQueryWrapper<SurveyTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SurveyTemplate::getOutletType, outletType)
               .eq(SurveyTemplate::getStatus, TemplateStatus.PUBLISHED.getCode())
               .orderByDesc(SurveyTemplate::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional
    public SurveyTemplate updateTemplate(Long id, SurveyTemplate template) {
        SurveyTemplate existing = getById(id);
        if (existing == null) {
            throw new BusinessException("模板不存在");
        }
        template.setId(id);
        updateById(template);
        return getById(id);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        SurveyTemplate existing = getById(id);
        if (existing == null) {
            throw new BusinessException("模板不存在");
        }
        removeById(id);
    }

    @Override
    @Transactional
    public SurveyTemplateVersion publishTemplate(Long templateId, String fieldsJson, String rulesJson, String linkageRulesJson) {
        SurveyTemplate template = getById(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        // 获取当前最大版本号
        LambdaQueryWrapper<SurveyTemplateVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SurveyTemplateVersion::getTemplateId, templateId)
                .orderByDesc(SurveyTemplateVersion::getVersionNo)
                .last("LIMIT 1");
        SurveyTemplateVersion latestVersion = surveyTemplateVersionMapper.selectOne(wrapper);
        
        int newVersionNo = latestVersion != null ? latestVersion.getVersionNo() + 1 : 1;

        // 创建新版本
        SurveyTemplateVersion version = new SurveyTemplateVersion();
        version.setTemplateId(templateId);
        version.setVersionNo(newVersionNo);
        version.setFieldsJson(fieldsJson);
        version.setRulesJson(rulesJson);
        version.setLinkageRulesJson(linkageRulesJson);
        version.setStatus(TemplateStatus.PUBLISHED.getCode());
        version.setPublishTime(LocalDateTime.now());
        version.setCreatorId(template.getCreatorId());
        surveyTemplateVersionMapper.insert(version);

        // 更新模板的当前版本ID和状态
        template.setCurrentVersionId(version.getId());
        template.setStatus(TemplateStatus.PUBLISHED.getCode());
        updateById(template);

        return version;
    }

    @Override
    public List<SurveyTemplateVersion> getVersionList(Long templateId) {
        return surveyTemplateVersionMapper.selectList(
                new LambdaQueryWrapper<SurveyTemplateVersion>()
                        .eq(SurveyTemplateVersion::getTemplateId, templateId)
                        .orderByDesc(SurveyTemplateVersion::getVersionNo)
        );
    }

    @Override
    public SurveyTemplateVersion getVersionDetail(Long versionId) {
        SurveyTemplateVersion version = surveyTemplateVersionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException("版本不存在");
        }
        return version;
    }

    @Override
    @Transactional
    public void bindOutfallType(Long projectId, Long sectionId, String outfallType, Long templateId, Long templateVersionId) {
        // 检查是否已存在绑定
        LambdaQueryWrapper<SurveyPointTemplateBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SurveyPointTemplateBinding::getProjectId, projectId)
                .eq(SurveyPointTemplateBinding::getOutfallType, outfallType);
        if (sectionId != null) {
            wrapper.eq(SurveyPointTemplateBinding::getSectionId, sectionId);
        } else {
            wrapper.isNull(SurveyPointTemplateBinding::getSectionId);
        }
        
        SurveyPointTemplateBinding existing = surveyPointTemplateBindingMapper.selectOne(wrapper);
        if (existing != null) {
            // 更新现有绑定
            existing.setTemplateId(templateId);
            existing.setTemplateVersionId(templateVersionId);
            surveyPointTemplateBindingMapper.updateById(existing);
        } else {
            // 创建新绑定
            SurveyPointTemplateBinding binding = new SurveyPointTemplateBinding();
            binding.setProjectId(projectId);
            binding.setSectionId(sectionId);
            binding.setOutfallType(outfallType);
            binding.setTemplateId(templateId);
            binding.setTemplateVersionId(templateVersionId);
            surveyPointTemplateBindingMapper.insert(binding);
        }
    }

    @Override
    public SurveyPointTemplateBinding getBindingByOutfallType(Long projectId, Long sectionId, String outfallType) {
        // 先查找标段级别的绑定
        LambdaQueryWrapper<SurveyPointTemplateBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SurveyPointTemplateBinding::getProjectId, projectId)
                .eq(SurveyPointTemplateBinding::getSectionId, sectionId)
                .eq(SurveyPointTemplateBinding::getOutfallType, outfallType);
        SurveyPointTemplateBinding binding = surveyPointTemplateBindingMapper.selectOne(wrapper);
        
        if (binding == null) {
            // 查找项目级别的绑定（sectionId为null）
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SurveyPointTemplateBinding::getProjectId, projectId)
                    .isNull(SurveyPointTemplateBinding::getSectionId)
                    .eq(SurveyPointTemplateBinding::getOutfallType, outfallType);
            binding = surveyPointTemplateBindingMapper.selectOne(wrapper);
        }
        
        return binding;
    }

    @Override
    public List<SurveyPointTemplateBinding> listBindings(Long projectId) {
        return surveyPointTemplateBindingMapper.selectList(
                new LambdaQueryWrapper<SurveyPointTemplateBinding>()
                        .eq(SurveyPointTemplateBinding::getProjectId, projectId)
        );
    }

    @Override
    @Transactional
    public void deleteBinding(Long bindingId) {
        surveyPointTemplateBindingMapper.deleteById(bindingId);
    }

    @Override
    public Map<String, Object> getFieldConfig(Long versionId) {
        SurveyTemplateVersion version = surveyTemplateVersionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException("版本不存在");
        }
        
        Map<String, Object> config = new HashMap<>();
        config.put("versionId", versionId);
        config.put("versionNo", version.getVersionNo());
        config.put("fields", version.getFieldsJson());
        config.put("rules", version.getRulesJson());
        config.put("linkageRules", version.getLinkageRulesJson());
        
        return config;
    }
}