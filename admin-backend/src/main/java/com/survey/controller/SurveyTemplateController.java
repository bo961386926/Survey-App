package com.survey.controller;

import com.survey.common.Result;
import com.survey.entity.SurveyTemplate;
import com.survey.service.SurveyTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/template")
@CrossOrigin(origins = "*")
public class SurveyTemplateController {

    @Autowired
    private SurveyTemplateService surveyTemplateService;

    @GetMapping("/list")
    public Result<List<SurveyTemplate>> getTemplateList() {
        List<SurveyTemplate> templates = surveyTemplateService.getTemplateList();
        return Result.success(templates);
    }

    @GetMapping("/detail/{id}")
    public Result<SurveyTemplate> getTemplateDetail(@PathVariable Long id) {
        SurveyTemplate template = surveyTemplateService.getTemplateById(id);
        return Result.success(template);
    }

    @PostMapping("/create")
    public Result<Boolean> createTemplate(@RequestBody SurveyTemplate template) {
        boolean success = surveyTemplateService.createTemplate(template);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @PutMapping("/update")
    public Result<Boolean> updateTemplate(@RequestBody SurveyTemplate template) {
        boolean success = surveyTemplateService.updateTemplate(template);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteTemplate(@PathVariable Long id) {
        boolean success = surveyTemplateService.deleteTemplate(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }
}
