package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.SysDictItem;

import java.util.List;

/**
 * 数据字典项服务接口
 */
public interface SysDictItemService extends IService<SysDictItem> {
    
    /**
     * 根据字典ID获取字典项列表
     */
    List<SysDictItem> listByDictId(Long dictId);
    
    /**
     * 批量保存字典项
     */
    boolean batchSave(Long dictId, List<SysDictItem> items);
}
