package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.config.CacheConfig;
import com.qhiot.survey.entity.SysDict;
import com.qhiot.survey.entity.SysDictItem;
import com.qhiot.survey.mapper.SysDictMapper;
import com.qhiot.survey.service.SysDictItemService;
import com.qhiot.survey.service.SysDictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据字典服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    private final SysDictItemService dictItemService;

    @Override
    @Cacheable(cacheNames = CacheConfig.DICT_CACHE, key = "'listEnabled'")
    public List<SysDict> listEnabled() {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getStatus, 1);
        wrapper.orderByAsc(SysDict::getDictCode);
        return list(wrapper);
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.DICT_CACHE, key = "'items:' + #dictCode", unless = "#result == null || #result.isEmpty()")
    public List<Object> getDictItems(String dictCode) {
        // 查询字典
        LambdaQueryWrapper<SysDict> dictWrapper = new LambdaQueryWrapper<>();
        dictWrapper.eq(SysDict::getDictCode, dictCode);
        SysDict dict = getOne(dictWrapper);
        
        if (dict == null) {
            log.warn("字典不存在: {}", dictCode);
            return List.of();
        }
        
        // 查询字典项
        LambdaQueryWrapper<SysDictItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(SysDictItem::getDictId, dict.getId());
        itemWrapper.eq(SysDictItem::getStatus, 1);
        itemWrapper.orderByAsc(SysDictItem::getSortOrder);
        List<SysDictItem> items = dictItemService.list(itemWrapper);
        
        // 转换为前端需要的格式
        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("label", item.getItemLabel());
            map.put("value", item.getItemValue());
            map.put("sortOrder", item.getSortOrder());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 字典或字典项发生变更时清空整个字典缓存命名空间。
     * 业务侧在 save / update / delete 后调用本方法。
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.DICT_CACHE, allEntries = true)
    })
    public void evictDictCache() {
        log.debug("字典缓存已清空");
    }
}
