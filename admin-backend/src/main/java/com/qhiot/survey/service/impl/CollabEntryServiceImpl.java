package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.CollabAccessLog;
import com.qhiot.survey.entity.CollabEntry;
import com.qhiot.survey.mapper.CollabAccessLogMapper;
import com.qhiot.survey.mapper.CollabEntryMapper;
import com.qhiot.survey.service.CollabEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 协作入口服务实现类
 */
@Service
public class CollabEntryServiceImpl extends ServiceImpl<CollabEntryMapper, CollabEntry> implements CollabEntryService {

    @Autowired
    private CollabAccessLogMapper collabAccessLogMapper;

    @Override
    public Page<CollabEntry> listByPage(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<CollabEntry> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null) {
            wrapper.like(CollabEntry::getEntryName, keyword);
        }
        wrapper.orderByDesc(CollabEntry::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public CollabEntry createEntry(CollabEntry entry) {
        // 生成Token
        entry.setToken(UUID.randomUUID().toString().replace("-", ""));
        entry.setStatus(1); // 启用中
        entry.setCreateTime(LocalDateTime.now());
        save(entry);
        return entry;
    }

    @Override
    public CollabEntry updateEntry(Long id, CollabEntry entry) {
        CollabEntry existing = getById(id);
        if (existing == null) {
            throw new BusinessException("协作入口不存在");
        }
        entry.setId(id);
        updateById(entry);
        return getById(id);
    }

    @Override
    public void revokeEntry(Long id) {
        CollabEntry existing = getById(id);
        if (existing == null) {
            throw new BusinessException("协作入口不存在");
        }
        CollabEntry update = new CollabEntry();
        update.setId(id);
        update.setStatus(3); // 已撤销
        updateById(update);
    }

    @Override
    public String resetToken(Long id) {
        CollabEntry existing = getById(id);
        if (existing == null) {
            throw new BusinessException("协作入口不存在");
        }
        String newToken = UUID.randomUUID().toString().replace("-", "");
        CollabEntry update = new CollabEntry();
        update.setId(id);
        update.setToken(newToken);
        updateById(update);
        return newToken;
    }

    @Override
    public CollabEntry getByToken(String token) {
        CollabEntry entry = lambdaQuery()
                .eq(CollabEntry::getToken, token)
                .one();
        if (entry == null) {
            throw new BusinessException("协作入口不存在");
        }
        // 检查有效期
        if (entry.getExpireTime() != null && entry.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("协作入口已过期");
        }
        // 检查状态
        if (entry.getStatus() != 1) {
            throw new BusinessException("协作入口已失效");
        }
        return entry;
    }

    @Override
    public List<Object> getAccessLogs(Long entryId) {
        List<CollabAccessLog> logs = collabAccessLogMapper.selectList(
                new LambdaQueryWrapper<CollabAccessLog>()
                        .eq(CollabAccessLog::getEntryId, entryId)
                        .orderByDesc(CollabAccessLog::getCreateTime)
                        .last("LIMIT 100")
        );
        return logs.stream().map(log -> (Object) log).collect(Collectors.toList());
    }
}