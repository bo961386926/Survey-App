package com.qhiot.survey.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.entity.SysDict;
import com.qhiot.survey.entity.SysDictItem;
import com.qhiot.survey.service.SysDictItemService;
import com.qhiot.survey.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典控制器
 */
@Slf4j
@Tag(name = "数据字典管理")
@RestController
@RequestMapping(value = "/api/v1/dict", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService dictService;
    private final SysDictItemService dictItemService;

    @Operation(summary = "分页查询字典列表")
    @GetMapping("/page")
    public Result<PageResult<SysDict>> queryDictPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String dictCode,
            @RequestParam(required = false) String dictName) {
        
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        if (dictCode != null && !dictCode.isEmpty()) {
            wrapper.like(SysDict::getDictCode, dictCode);
        }
        if (dictName != null && !dictName.isEmpty()) {
            wrapper.like(SysDict::getDictName, dictName);
        }
        wrapper.orderByDesc(SysDict::getCreateTime);
        
        Page<SysDict> page = new Page<>(pageNum, pageSize);
        Page<SysDict> result = dictService.page(page, wrapper);
        
        long totalPages = (result.getTotal() + pageSize - 1) / pageSize;
        PageResult<SysDict> pageResult = new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                pageNum,
                pageSize,
                (int) totalPages
        );
        
        return Result.success(pageResult);
    }

    @Operation(summary = "获取所有启用的字典")
    @GetMapping("/enabled")
    public Result<List<SysDict>> listEnabled() {
        List<SysDict> list = dictService.listEnabled();
        return Result.success(list);
    }

    @Operation(summary = "获取字典详情")
    @GetMapping("/{id}")
    public Result<SysDict> getDictDetail(@PathVariable Long id) {
        SysDict dict = dictService.getById(id);
        return Result.success(dict);
    }

    @Operation(summary = "创建字典")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "数据字典", action = "创建", description = "创建字典: #dict.dictName", riskLevel = 1)
    public Result<Void> createDict(@RequestBody SysDict dict) {
        log.info("====== [数据字典] 创建字典请求 - dictName: {} ======", dict.getDictName());
        
        // 检查字典编码是否已存在
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictCode, dict.getDictCode());
        if (dictService.count(wrapper) > 0) {
            return Result.error("字典编码已存在");
        }
        
        if (dict.getStatus() == null) {
            dict.setStatus(1);
        }
        if (dict.getIsSystem() == null) {
            dict.setIsSystem(0);
        }
        if (dict.getSortOrder() == null) {
            dict.setSortOrder(0);
        }
        
        boolean success = dictService.save(dict);
        if (success) {
            log.info("====== [数据字典] 字典创建成功 - dictId: {} ======", dict.getId());
        }
        return success ? Result.success() : Result.error("创建字典失败");
    }

    @Operation(summary = "更新字典")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "数据字典", action = "更新", description = "更新字典ID: #id", riskLevel = 1)
    public Result<Void> updateDict(@PathVariable Long id, @RequestBody SysDict dict) {
        log.info("====== [数据字典] 更新字典请求 - dictId: {} ======", id);
        
        dict.setId(id);
        boolean success = dictService.updateById(dict);
        if (success) {
            log.info("====== [数据字典] 字典更新成功 - dictId: {} ======", id);
        }
        return success ? Result.success() : Result.error("更新字典失败");
    }

    @Operation(summary = "删除字典")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "数据字典", action = "删除", description = "删除字典ID: #id", riskLevel = 2)
    public Result<Void> deleteDict(@PathVariable Long id) {
        log.info("====== [数据字典] 删除字典请求 - dictId: {} ======", id);
        
        // 先删除关联的字典项
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictId, id);
        dictItemService.remove(wrapper);
        
        // 再删除字典
        boolean success = dictService.removeById(id);
        if (success) {
            log.info("====== [数据字典] 字典删除成功 - dictId: {} ======", id);
        }
        return success ? Result.success() : Result.error("删除字典失败");
    }

    @Operation(summary = "获取字典项列表")
    @GetMapping("/{id}/items")
    public Result<List<SysDictItem>> getDictItems(@PathVariable Long id) {
        List<SysDictItem> items = dictItemService.listByDictId(id);
        return Result.success(items);
    }

    @Operation(summary = "根据字典编码获取字典项")
    @GetMapping("/code/{dictCode}/items")
    public Result<List<Object>> getDictItemsByCode(@PathVariable String dictCode) {
        List<Object> items = dictService.getDictItems(dictCode);
        return Result.success(items);
    }

    @Operation(summary = "批量保存字典项")
    @PostMapping("/{id}/items/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "数据字典", action = "批量保存字典项", description = "批量保存字典项，字典ID: #id", riskLevel = 1)
    public Result<Void> batchSaveDictItems(@PathVariable Long id, @RequestBody List<SysDictItem> items) {
        log.info("====== [数据字典] 批量保存字典项请求 - dictId: {}, itemCount: {} ======", id, items.size());
        
        // 设置字典项的dictCode
        SysDict dict = dictService.getById(id);
        if (dict != null) {
            items.forEach(item -> {
                item.setDictId(id);
                item.setDictCode(dict.getDictCode());
                if (item.getStatus() == null) {
                    item.setStatus(1);
                }
                if (item.getIsReadonly() == null) {
                    item.setIsReadonly(0);
                }
                if (item.getSortOrder() == null) {
                    item.setSortOrder(0);
                }
            });
        }
        
        boolean success = dictItemService.batchSave(id, items);
        if (success) {
            log.info("====== [数据字典] 字典项保存成功 - dictId: {} ======", id);
        }
        return success ? Result.success() : Result.error("保存字典项失败");
    }
}
