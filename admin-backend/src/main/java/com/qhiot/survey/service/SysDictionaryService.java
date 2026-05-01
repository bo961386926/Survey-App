package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.SysDictionary;

import java.util.List;
import java.util.Map;

/**
 * 数据字典分类服务接口
 */
public interface SysDictionaryService extends IService<SysDictionary> {

    /**
     * 分页查询字典分类
     */
    Page<SysDictionary> listByPage(String keyword, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取所有启用的字典分类
     */
    List<SysDictionary> listEnabled();

    /**
     * 根据字典代码获取字典项列表
     */
    List<Map<String, String>> getDictItems(String dictCode);

    /**
     * 获取所有字典数据（用于缓存）
     */
    Map<String, List<Map<String, String>>> getAllDictItems();

    /**
     * 刷新缓存
     */
    void refreshCache();
}