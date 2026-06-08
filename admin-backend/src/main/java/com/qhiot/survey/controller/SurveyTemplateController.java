package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.constant.Permissions;
import com.qhiot.survey.entity.SurveyPointTemplateBinding;
import com.qhiot.survey.entity.SurveyTemplate;
import com.qhiot.survey.entity.SurveyTemplateVersion;
import com.qhiot.survey.service.SurveyTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模板管理控制器
 */
@Slf4j
@Tag(name = "模板管理", description = "勘查模板管理相关接口")
@RestController
@RequestMapping("/api/v1/template")
@RequiredArgsConstructor
public class SurveyTemplateController {

    @Autowired
    private SurveyTemplateService surveyTemplateService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "分页查询模板列表", description = "分页查询勘查模板，支持按关键词和状态筛选")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_VIEW + "')")
    public Result<Page<SurveyTemplate>> listByPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(surveyTemplateService.listByPage(keyword, status, pageNum, pageSize));
    }

    @Operation(summary = "获取模板列表", description = "获取所有启用的模板简要列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_VIEW + "')")
    public Result<List<SurveyTemplate>> getTemplateList() {
        return Result.success(surveyTemplateService.getTemplateList());
    }

    @Operation(summary = "获取模板详情", description = "根据模板ID获取详细信息")
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_VIEW + "')")
    public Result<SurveyTemplate> getTemplateDetail(@Parameter(description = "模板ID") @PathVariable Long id) {
        return Result.success(surveyTemplateService.getTemplateById(id));
    }

    @Operation(summary = "创建模板", description = "创建新的勘查模板，需模板编辑权限")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_EDIT + "')")
    @OperationLog(module = "模板管理", action = "创建", description = "创建模板: #template.templateName", riskLevel = 1)
    public Result<SurveyTemplate> createTemplate(@RequestBody SurveyTemplate template) {
        try {
            log.info("====== [模板管理] 创建模板请求 - templateName: {}, templateCode: {} ======", 
                    template.getTemplateName(), template.getTemplateCode());
            SurveyTemplate result = surveyTemplateService.createTemplate(template);
            if (result != null) {
                log.info("====== [模板管理] 模板创建成功 - templateId: {} ======", result.getId());
                return Result.success(result);
            } else {
                log.error("====== [模板管理] 模板创建失败 ======");
                return Result.error("模板创建失败");
            }
        } catch (Exception e) {
            log.error("====== [模板管理] 创建模板异常 - templateName: {}, error: {} ======", 
                    template.getTemplateName(), e.getMessage(), e);
            return Result.error("创建模板失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新模板", description = "更新模板信息，需模板编辑权限")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_EDIT + "')")
    @OperationLog(module = "模板管理", action = "更新", description = "更新模板ID: #id", riskLevel = 1)
    public Result<SurveyTemplate> updateTemplate(@Parameter(description = "模板ID") @PathVariable Long id, @RequestBody SurveyTemplate template) {
        return Result.success(surveyTemplateService.updateTemplate(id, template));
    }

    @Operation(summary = "删除模板", description = "删除勘查模板，需模板编辑权限")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_EDIT + "')")
    @OperationLog(module = "模板管理", action = "删除", description = "删除模板ID: #id", riskLevel = 2)
    public Result<Void> deleteTemplate(@Parameter(description = "模板ID") @PathVariable Long id) {
        surveyTemplateService.deleteTemplate(id);
        return Result.success();
    }

    @Operation(summary = "保存模板草稿（设计器状态）", description = "保存模板设计器的中间状态草稿")
    @PutMapping("/draft/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_EDIT + "')")
    @OperationLog(module = "模板管理", action = "保存草稿", description = "保存模板设计器草稿, 模板ID: #id", riskLevel = 0)
    public Result<Void> saveDraft(@Parameter(description = "模板ID") @PathVariable Long id, @RequestBody Map<String, Object> draftData) {
        surveyTemplateService.saveDraft(id, draftData);
        return Result.success();
    }

    @Operation(summary = "发布模板生成新版本", description = "发布模板并生成新版本号，需模板编辑权限")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_EDIT + "')")
    @OperationLog(module = "模板管理", action = "发布", description = "发布模板ID: #id", riskLevel = 1)
    public Result<SurveyTemplateVersion> publishTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @RequestBody Map<String, Object> publishData) {
        try {
            String fieldsJson = publishData.get("fields") != null
                    ? objectMapper.writeValueAsString(publishData.get("fields"))
                    : "[]";
            String rulesJson = publishData.get("rules") != null
                    ? objectMapper.writeValueAsString(publishData.get("rules"))
                    : "{}";
            String linkageRulesJson = publishData.get("linkageRules") != null
                    ? objectMapper.writeValueAsString(publishData.get("linkageRules"))
                    : "[]";
            return Result.success(surveyTemplateService.publishTemplate(id, fieldsJson, rulesJson, linkageRulesJson));
        } catch (Exception e) {
            log.error("发布模板失败", e);
            return Result.error("发布模板失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取模板预览数据（移动端表单预览）", description = "获取模板在移动端表单中的预览数据")
    @GetMapping("/{id}/preview")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_VIEW + "')")
    public Result<Map<String, Object>> previewTemplate(@Parameter(description = "模板ID") @PathVariable Long id) {
        return Result.success(surveyTemplateService.previewTemplate(id));
    }

    @Operation(summary = "获取模板版本列表", description = "获取模板的所有历史版本列表")
    @GetMapping("/{id}/versions")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_VIEW + "')")
    public Result<List<SurveyTemplateVersion>> getVersionList(@Parameter(description = "模板ID") @PathVariable Long id) {
        return Result.success(surveyTemplateService.getVersionList(id));
    }

    @Operation(summary = "获取模板版本详情", description = "根据版本ID获取模板版本的详细信息")
    @GetMapping("/version/{versionId}")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_VIEW + "')")
    public Result<SurveyTemplateVersion> getVersionDetail(@Parameter(description = "模板版本ID") @PathVariable Long versionId) {
        return Result.success(surveyTemplateService.getVersionDetail(versionId));
    }

    @Operation(summary = "获取模板字段配置", description = "获取模板版本的表单字段配置")
    @GetMapping("/version/{versionId}/fields")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_VIEW + "')")
    public Result<Map<String, Object>> getFieldConfig(@Parameter(description = "模板版本ID") @PathVariable Long versionId) {
        return Result.success(surveyTemplateService.getFieldConfig(versionId));
    }

    @Operation(summary = "绑定排口类型与模板版本", description = "将排口类型与指定模板版本绑定，用于自动匹配表单，需模板绑定权限")
    @PostMapping("/bind-outfall")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_BIND + "')")
    @OperationLog(module = "模板管理", action = "绑定排口", description = "绑定排口类型: #outfallType", riskLevel = 0)
    public Result<Void> bindOutfallType(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam String outfallType,
            @RequestParam Long templateId,
            @RequestParam Long templateVersionId) {
        surveyTemplateService.bindOutfallType(projectId, sectionId, outfallType, templateId, templateVersionId);
        return Result.success();
    }

    @Operation(summary = "获取排口类型绑定的模板", description = "查询排口类型在指定项目/标段下绑定的模板配置")
    @GetMapping("/binding")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_VIEW + "')")
    public Result<SurveyPointTemplateBinding> getBinding(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam String outfallType) {
        return Result.success(surveyTemplateService.getBindingByOutfallType(projectId, sectionId, outfallType));
    }

    @Operation(summary = "获取项目下所有排口类型绑定", description = "获取指定项目下所有排口类型的模板绑定列表")
    @GetMapping("/bindings")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_VIEW + "')")
    public Result<List<SurveyPointTemplateBinding>> listBindings(@RequestParam Long projectId) {
        return Result.success(surveyTemplateService.listBindings(projectId));
    }

    @Operation(summary = "删除排口类型绑定", description = "删除排口类型与模板的绑定关系，需模板绑定权限")
    @DeleteMapping("/binding/{bindingId}")
    @PreAuthorize("hasAuthority('" + Permissions.TEMPLATE_BIND + "')")
    @OperationLog(module = "模板管理", action = "解绑排口", description = "删除排口绑定ID: #bindingId", riskLevel = 0)
    public Result<Void> deleteBinding(@Parameter(description = "绑定关系ID") @PathVariable Long bindingId) {
        surveyTemplateService.deleteBinding(bindingId);
        return Result.success();
    }
}
