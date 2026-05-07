package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.entity.SysDictItem;
import com.qhiot.survey.mapper.SysDictItemMapper;
import com.qhiot.survey.service.SysDictItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据字典项服务实现
 */
@Slf4j
@Service
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

    @Override
    public List<SysDictItem> listByDictId(Long dictId) {
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictId, dictId);
        wrapper.orderByAsc(SysDictItem::getSortOrder);
        return list(wrapper);
    }

    @Override
    @Transactional
    public boolean batchSave(Long dictId, List<SysDictItem> items) {
        // 先删除旧的字典项
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictId, dictId);
        remove(wrapper);
        
        // 批量保存新的字典项
        if (items != null && !items.isEmpty()) {
            items.forEach(item -> {
                item.setDictId(dictId);
                if (item.getStatus() == null) {
                    item.setStatus(1);
                }
                if (item.getSortOrder() == null) {
                    item.setSortOrder(0);
                }
            });
            return saveBatch(items);
        }
        return true;
    }
}
