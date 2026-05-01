package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.entity.SurveyPointTemplateBinding;
import com.qhiot.survey.entity.SurveyTemplate;
import com.qhiot.survey.entity.SurveyTemplateVersion;
import com.qhiot.survey.service.SurveyTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模板管理控制器
 */
@Tag(name = "模板管理", description = "勘查模板管理相关接口")
@RestController
@RequestMapping("/api/template")
public class SurveyTemplateController {

    @Autowired
    private SurveyTemplateService surveyTemplateService;

    @Operation(summary = "分页查询模板列表")
    @GetMapping("/page")
    public Result<Page<SurveyTemplate>> listByPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(surveyTemplateService.listByPage(keyword, status, pageNum, pageSize));
    }

    @Operation(summary = "获取模板列表")
    @GetMapping("/list")
    public Result<List<SurveyTemplate>> getTemplateList() {
        return Result.success(surveyTemplateService.getTemplateList());
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/detail/{id}")
    public Result<SurveyTemplate> getTemplateDetail(@PathVariable Long id) {
        return Result.success(surveyTemplateService.getTemplateById(id));
    }

    @Operation(summary = "创建模板")
    @PostMapping("/create")
    public Result<SurveyTemplate> createTemplate(@RequestBody SurveyTemplate template) {
        return Result.success(surveyTemplateService.createTemplate(template));
    }

    @Operation(summary = "更新模板")
    @PutMapping("/update/{id}")
    public Result<SurveyTemplate> updateTemplate(@PathVariable Long id, @RequestBody SurveyTemplate template) {
        return Result.success(surveyTemplateService.updateTemplate(id, template));
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        surveyTemplateService.deleteTemplate(id);
        return Result.success();
    }

    @Operation(summary = "发布模板生成新版本")
    @PostMapping("/{id}/publish")
    public Result<SurveyTemplateVersion> publishTemplate(
            @PathVariable Long id,
            @RequestParam String fieldsJson,
            @RequestParam(required = false) String rulesJson,
            @RequestParam(required = false) String linkageRulesJson) {
        return Result.success(surveyTemplateService.publishTemplate(id, fieldsJson, rulesJson, linkageRulesJson));
    }

    @Operation(summary = "获取模板版本列表")
    @GetMapping("/{id}/versions")
    public Result<List<SurveyTemplateVersion>> getVersionList(@PathVariable Long id) {
        return Result.success(surveyTemplateService.getVersionList(id));
    }

    @Operation(summary = "获取模板版本详情")
    @GetMapping("/version/{versionId}")
    public Result<SurveyTemplateVersion> getVersionDetail(@PathVariable Long versionId) {
        return Result.success(surveyTemplateService.getVersionDetail(versionId));
    }

    @Operation(summary = "获取模板字段配置")
    @GetMapping("/version/{versionId}/fields")
    public Result<Map<String, Object>> getFieldConfig(@PathVariable Long versionId) {
        return Result.success(surveyTemplateService.getFieldConfig(versionId));
    }

    @Operation(summary = "绑定排口类型与模板版本")
    @PostMapping("/bind-outfall")
    public Result<Void> bindOutfallType(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam String outfallType,
            @RequestParam Long templateId,
            @RequestParam Long templateVersionId) {
        surveyTemplateService.bindOutfallType(projectId, sectionId, outfallType, templateId, templateVersionId);
        return Result.success();
    }

    @Operation(summary = "获取排口类型绑定的模板")
    @GetMapping("/binding")
    public Result<SurveyPointTemplateBinding> getBinding(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam String outfallType) {
        return Result.success(surveyTemplateService.getBindingByOutfallType(projectId, sectionId, outfallType));
    }

    @Operation(summary = "获取项目下所有排口类型绑定")
    @GetMapping("/bindings")
    public Result<List<SurveyPointTemplateBinding>> listBindings(@RequestParam Long projectId) {
        return Result.success(surveyTemplateService.listBindings(projectId));
    }

    @Operation(summary = "删除排口类型绑定")
    @DeleteMapping("/binding/{bindingId}")
    public Result<Void> deleteBinding(@PathVariable Long bindingId) {
        surveyTemplateService.deleteBinding(bindingId);
        return Result.success();
    }
}