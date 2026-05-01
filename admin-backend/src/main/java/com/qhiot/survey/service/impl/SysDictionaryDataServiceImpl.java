package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.SysDictionary;
import com.qhiot.survey.entity.SysDictionaryData;
import com.qhiot.survey.mapper.SysDictionaryDataMapper;
import com.qhiot.survey.service.SysDictionaryDataService;
import com.qhiot.survey.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据字典项服务实现类
 */
@Service
public class SysDictionaryDataServiceImpl extends ServiceImpl<SysDictionaryDataMapper, SysDictionaryData> implements SysDictionaryDataService {

    @Autowired
    @Lazy
    private SysDictionaryService sysDictionaryService;

    @Override
    public Page<SysDictionaryData> listByPage(Long dictId, String keyword, Integer status, Integer pageNum, Integer pageSize) {
        Page<SysDictionaryData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictionaryData> wrapper = new LambdaQueryWrapper<>();
        if (dictId != null) {
            wrapper.eq(SysDictionaryData::getDictId, dictId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysDictionaryData::getDataName, keyword)
                    .or().like(SysDictionaryData::getDataValue, keyword);
        }
        if (status != null) {
            wrapper.eq(SysDictionaryData::getStatus, status);
        }
        wrapper.orderByAsc(SysDictionaryData::getDataOrder);
        return page(page, wrapper);
    }

    @Override
    public List<SysDictionaryData> listByDictCode(String dictCode) {
        return lambdaQuery()
                .eq(SysDictionaryData::getDictCode, dictCode)
                .eq(SysDictionaryData::getStatus, 1)
                .orderByAsc(SysDictionaryData::getDataOrder)
                .list();
    }

    @Override
    public Map<String, String> getDictMap(String dictCode) {
        Map<String, String> dictMap = new LinkedHashMap<>();
        List<SysDictionaryData> items = listByDictCode(dictCode);
        for (SysDictionaryData item : items) {
            dictMap.put(item.getDataValue(), item.getDataName());
        }
        return dictMap;
    }

    @Override
    public String getDictName(String dictCode, String dataValue) {
        Map<String, String> dictMap = getDictMap(dictCode);
        return dictMap.getOrDefault(dataValue, dataValue);
    }

    @Override
    public String getDictValue(String dictCode, String dataName) {
        List<SysDictionaryData> items = listByDictCode(dictCode);
        for (SysDictionaryData item : items) {
            if (item.getDataName().equals(dataName)) {
                return item.getDataValue();
            }
        }
        return dataName;
    }

    @Override
    public Map<String, Map<String, String>> getAllDictMaps() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        List<SysDictionary> dicts = sysDictionaryService.listEnabled();
        for (SysDictionary dict : dicts) {
            result.put(dict.getDictCode(), getDictMap(dict.getDictCode()));
        }
        return result;
    }

    @Transactional
    public SysDictionaryData insert(SysDictionaryData entity) {
        // 检查字典项值是否重复
        Long count = lambdaQuery()
                .eq(SysDictionaryData::getDictCode, entity.getDictCode())
                .eq(SysDictionaryData::getDataValue, entity.getDataValue())
                .count();
        if (count > 0) {
            throw new BusinessException("字典项值已存在");
        }
        save(entity);
        refreshCache();
        return entity;
    }

    @Transactional
    public SysDictionaryData updateSelective(SysDictionaryData entity) {
        SysDictionaryData oldData = getById(entity.getId());
        if (oldData == null) {
            throw new BusinessException("字典项不存在");
        }
        if (oldData.getIsReadonly() != null && oldData.getIsReadonly() == 1) {
            throw new BusinessException("不能修改只读的字典项");
        }
        // 检查字典项值是否重复
        Long count = lambdaQuery()
                .eq(SysDictionaryData::getDictCode, entity.getDictCode())
                .eq(SysDictionaryData::getDataValue, entity.getDataValue())
                .ne(SysDictionaryData::getId, entity.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("字典项值已存在");
        }
        updateById(entity);
        refreshCache();
        return getById(entity.getId());
    }

    @Transactional
    public void deleteById(Long id) {
        SysDictionaryData oldData = getById(id);
        if (oldData == null) {
            throw new BusinessException("字典项不存在");
        }
        if (oldData.getIsReadonly() != null && oldData.getIsReadonly() == 1) {
            throw new BusinessException("不能删除只读的字典项");
        }
        removeById(id);
        refreshCache();
    }

    @Override
    @Transactional
    public void refreshCache() {
        sysDictionaryService.refreshCache();
    }
}