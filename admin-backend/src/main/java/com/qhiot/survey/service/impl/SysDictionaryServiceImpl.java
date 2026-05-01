package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.SysDictionary;
import com.qhiot.survey.entity.SysDictionaryData;
import com.qhiot.survey.mapper.SysDictionaryMapper;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据字典分类服务实现类
 */
@Service
public class SysDictionaryServiceImpl extends ServiceImpl<SysDictionaryMapper, SysDictionary> implements SysDictionaryService {

    @Autowired
    @Lazy
    private SysDictionaryDataService sysDictionaryDataService;

    /** 字典缓存 */
    private static final Map<String, Map<String, String>> DICT_CACHE = new ConcurrentHashMap<>();

    @Override
    public Page<SysDictionary> listByPage(String keyword, Integer status, Integer pageNum, Integer pageSize) {
        Page<SysDictionary> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictionary> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysDictionary::getDictName, keyword)
                    .or().like(SysDictionary::getDictCode, keyword);
        }
        if (status != null) {
            wrapper.eq(SysDictionary::getStatus, status);
        }
        wrapper.orderByAsc(SysDictionary::getSortOrder);
        return page(page, wrapper);
    }

    @Override
    public List<SysDictionary> listEnabled() {
        return lambdaQuery()
                .eq(SysDictionary::getStatus, 1)
                .orderByAsc(SysDictionary::getSortOrder)
                .list();
    }

    @Override
    public List<Map<String, String>> getDictItems(String dictCode) {
        return sysDictionaryDataService.listByDictCode(dictCode).stream()
                .map(item -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("label", item.getDataName());
                    map.put("value", item.getDataValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<Map<String, String>>> getAllDictItems() {
        Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();
        List<SysDictionary> dicts = listEnabled();
        for (SysDictionary dict : dicts) {
            result.put(dict.getDictCode(), getDictItems(dict.getDictCode()));
        }
        return result;
    }

    @Override
    @Transactional
    public void refreshCache() {
        DICT_CACHE.clear();
        List<SysDictionary> dicts = listEnabled();
        for (SysDictionary dict : dicts) {
            Map<String, String> dictMap = sysDictionaryDataService.getDictMap(dict.getDictCode());
            DICT_CACHE.put(dict.getDictCode(), dictMap);
        }
    }

    /**
     * 获取字典缓存
     */
    public static Map<String, String> getDictCache(String dictCode) {
        return DICT_CACHE.getOrDefault(dictCode, new HashMap<>());
    }
}