package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.Result;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.entity.SysDictionaryData;
import com.qhiot.survey.service.SysDictionaryDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字典项控制器
 */
@Tag(name = "数据字典项管理", description = "数据字典项管理相关接口")
@RestController
@RequestMapping("/api/v1/dictionary-data")
public class SysDictionaryDataController {

    @Autowired
    private SysDictionaryDataService sysDictionaryDataService;

    @Operation(summary = "分页查询字典项列表", description = "分页查询字典项，支持按字典ID、关键词、状态筛选")
    @GetMapping("/page")
    public Result<Page<SysDictionaryData>> listByPage(
            @RequestParam(required = false) Long dictId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(sysDictionaryDataService.listByPage(dictId, keyword, status, pageNum, pageSize));
    }

    @Operation(summary = "根据字典代码获取字典项列表", description = "根据字典编码获取对应的字典项完整列表")
    @GetMapping("/list/{dictCode}")
    public Result<List<SysDictionaryData>> listByDictCode(@Parameter(description = "字典编码") @PathVariable String dictCode) {
        return Result.success(sysDictionaryDataService.listByDictCode(dictCode));
    }

    @Operation(summary = "获取字典项详情", description = "根据字典项ID获取详细信息")
    @GetMapping("/{id}")
    public Result<SysDictionaryData> getById(@Parameter(description = "字典项ID") @PathVariable Long id) {
        return Result.success(sysDictionaryDataService.getById(id));
    }

    @Operation(summary = "创建字典项", description = "创建新的字典项")
    @PostMapping("/create")
    @OperationLog(module = "数据字典", action = "创建字典项", description = "创建字典项: #data.dataName", riskLevel = 1)
    public Result<SysDictionaryData> create(@RequestBody SysDictionaryData data) {
        return Result.success(sysDictionaryDataService.insert(data));
    }

    @Operation(summary = "更新字典项", description = "更新字典项信息")
    @PutMapping("/update/{id}")
    @OperationLog(module = "数据字典", action = "更新字典项", description = "更新字典项ID: #id", riskLevel = 1)
    public Result<SysDictionaryData> update(@Parameter(description = "字典项ID") @PathVariable Long id, @RequestBody SysDictionaryData data) {
        data.setId(id);
        return Result.success(sysDictionaryDataService.updateSelective(data));
    }

    @Operation(summary = "删除字典项", description = "删除字典项，高风险操作")
    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "数据字典", action = "删除字典项", description = "删除字典项ID: #id", riskLevel = 2)
    public Result<Void> delete(@Parameter(description = "字典项ID") @PathVariable Long id) {
        sysDictionaryDataService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "获取字典项Map（值->名称）", description = "根据字典编码获取值到名称的映射Map")
    @GetMapping("/map/{dictCode}")
    public Result<Map<String, String>> getDictMap(@Parameter(description = "字典编码") @PathVariable String dictCode) {
        return Result.success(sysDictionaryDataService.getDictMap(dictCode));
    }

    @Operation(summary = "根据值获取名称", description = "根据字典编码和值获取对应的名称")
    @GetMapping("/name/{dictCode}/{dataValue}")
    public Result<String> getDictName(@Parameter(description = "字典编码") @PathVariable String dictCode,
                                        @Parameter(description = "字典值") @PathVariable String dataValue) {
        return Result.success(sysDictionaryDataService.getDictName(dictCode, dataValue));
    }

    @Operation(summary = "根据名称获取值", description = "根据字典编码和名称获取对应的值")
    @GetMapping("/value/{dictCode}/{dataName}")
    public Result<String> getDictValue(@Parameter(description = "字典编码") @PathVariable String dictCode,
                                        @Parameter(description = "字典名称") @PathVariable String dataName) {
        return Result.success(sysDictionaryDataService.getDictValue(dictCode, dataName));
    }

    @Operation(summary = "刷新字典缓存", description = "手动刷新字典项缓存")
    @PostMapping("/refresh-cache")
    @OperationLog(module = "数据字典", action = "刷新缓存", description = "刷新字典缓存", riskLevel = 1)
    public Result<Void> refreshCache() {
        sysDictionaryDataService.refreshCache();
        return Result.success();
    }
}