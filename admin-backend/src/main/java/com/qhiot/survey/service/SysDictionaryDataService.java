package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.SysDictionaryData;

import java.util.List;
import java.util.Map;

/**
 * 数据字典项服务接口
 */
public interface SysDictionaryDataService extends IService<SysDictionaryData> {

    /**
     * 分页查询字典项
     */
    Page<SysDictionaryData> listByPage(Long dictId, String keyword, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 根据字典代码获取字典项列表
     */
    List<SysDictionaryData> listByDictCode(String dictCode);

    /**
     * 根据字典代码获取字典项Map（值->名称）
     */
    Map<String, String> getDictMap(String dictCode);

    /**
     * 根据字典代码和值获取名称
     */
    String getDictName(String dictCode, String dataValue);

    /**
     * 根据字典代码和名称获取值
     */
    String getDictValue(String dictCode, String dataName);

    /**
     * 获取所有字典数据（用于缓存）
     */
    Map<String, Map<String, String>> getAllDictMaps();

    /**
     * 刷新缓存
     */
    void refreshCache();

    /**
     * 新增字典项
     */
    SysDictionaryData insert(SysDictionaryData entity);

    /**
     * 选择性更新字典项
     */
    SysDictionaryData updateSelective(SysDictionaryData entity);

    /**
     * 根据ID删除字典项
     */
    void deleteById(Long id);
}
