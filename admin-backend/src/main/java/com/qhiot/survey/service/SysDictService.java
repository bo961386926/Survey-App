package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.SysDict;

import java.util.List;

/**
 * 数据字典服务接口
 */
public interface SysDictService extends IService<SysDict> {
    
    /**
     * 获取所有启用的字典列表
     */
    List<SysDict> listEnabled();
    
    /**
     * 根据字典编码获取字典项列表
     */
    List<Object> getDictItems(String dictCode);
}
