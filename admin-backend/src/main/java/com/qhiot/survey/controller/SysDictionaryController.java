package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.entity.SysDictionary;
import com.qhiot.survey.service.SysDictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字典分类控制器
 */
@Tag(name = "数据字典分类管理", description = "数据字典分类管理相关接口")
@RestController
@RequestMapping("/api/v1/dictionary")
public class SysDictionaryController {

    @Autowired
    private SysDictionaryService sysDictionaryService;

    @Operation(summary = "分页查询字典分类列表")
    @GetMapping("/page")
    public Result<Page<SysDictionary>> listByPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(sysDictionaryService.listByPage(keyword, status, pageNum, pageSize));
    }

    @Operation(summary = "获取所有启用的字典分类")
    @GetMapping("/enabled")
    public Result<List<SysDictionary>> listEnabled() {
        return Result.success(sysDictionaryService.listEnabled());
    }

    @Operation(summary = "获取字典分类详情")
    @GetMapping("/{id}")
    public Result<SysDictionary> getById(@PathVariable Long id) {
        return Result.success(sysDictionaryService.getById(id));
    }

    @Operation(summary = "创建字典分类")
    @PostMapping("/create")
    @OperationLog(module = "数据字典", action = "创建分类", description = "创建字典分类: #dictionary.dictName", riskLevel = 1)
    public Result<SysDictionary> create(@RequestBody SysDictionary dictionary) {
        sysDictionaryService.save(dictionary);
        return Result.success(dictionary);
    }

    @Operation(summary = "更新字典分类")
    @PutMapping("/update/{id}")
    @OperationLog(module = "数据字典", action = "更新分类", description = "更新字典分类ID: #id", riskLevel = 1)
    public Result<SysDictionary> update(@PathVariable Long id, @RequestBody SysDictionary dictionary) {
        dictionary.setId(id);
        sysDictionaryService.updateById(dictionary);
        return Result.success(dictionary);
    }

    @Operation(summary = "删除字典分类")
    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "数据字典", action = "删除分类", description = "删除字典分类ID: #id", riskLevel = 2)
    public Result<Void> delete(@PathVariable Long id) {
        sysDictionaryService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "获取字典项列表")
    @GetMapping("/{id}/items")
    public Result<List<Map<String, String>>> getDictItems(@PathVariable Long id) {
        SysDictionary dictionary = sysDictionaryService.getById(id);
        if (dictionary == null) {
            return Result.error("字典分类不存在");
        }
        return Result.success(sysDictionaryService.getDictItems(dictionary.getDictCode()));
    }

    @Operation(summary = "获取所有字典数据")
    @GetMapping("/all")
    public Result<Map<String, List<Map<String, String>>>> getAllDictItems() {
        return Result.success(sysDictionaryService.getAllDictItems());
    }

    @Operation(summary = "刷新字典缓存")
    @PostMapping("/refresh-cache")
    @OperationLog(module = "数据字典", action = "刷新缓存", description = "刷新字典缓存", riskLevel = 1)
    public Result<Void> refreshCache() {
        sysDictionaryService.refreshCache();
        return Result.success();
    }
}