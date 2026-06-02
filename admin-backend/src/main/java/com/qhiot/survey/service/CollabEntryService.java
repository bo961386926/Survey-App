package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.CollabEntry;

import java.util.List;

/**
 * 协作入口服务接口
 */
public interface CollabEntryService extends IService<CollabEntry> {

    /**
     * 分页查询协作入口列表
     */
    Page<CollabEntry> listByPage(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 创建协作入口
     */
    CollabEntry createEntry(CollabEntry entry);

    /**
     * 更新协作入口
     */
    CollabEntry updateEntry(Long id, CollabEntry entry);

    /**
     * 撤销协作入口
     */
    void revokeEntry(Long id);

    /**
     * 重置Token
     */
    String resetToken(Long id);

    /**
     * 根据Token获取协作入口
     */
    CollabEntry getByToken(String token);

    /**
     * 获取访问日志
     */
    List<Object> getAccessLogs(Long entryId);

    /**
     * 为指定协作入口签发一个 loginType=collab 的 JWT，供第三方使用
     */
    String issueCollabToken(Long entryId);
}