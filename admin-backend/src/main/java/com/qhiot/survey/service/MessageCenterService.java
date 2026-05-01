package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.MessageCenter;

import java.util.List;

/**
 * 消息中心服务接口
 */
public interface MessageCenterService extends IService<MessageCenter> {

    /**
     * 分页查询消息列表
     */
    Page<MessageCenter> listByPage(Long userId, Integer type, Integer pageNum, Integer pageSize);

    /**
     * 获取未读消息数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 批量标记已读
     */
    void batchMarkAsRead(Long userId);

    /**
     * 创建系统消息
     */
    void createSystemMessage(String title, String content, String type, Long targetUserId);

    /**
     * 批量推送消息给指定角色的用户
     * @param title 消息标题
     * @param content 消息内容
     * @param type 消息类型
     * @param roles 角色列表（逗号分隔），为空则推送给所有用户
     * @return 推送成功的消息数量
     */
    int pushMessageToRoles(String title, String content, String type, String roles);

    /**
     * 推送消息给指定用户
     * @param title 消息标题
     * @param content 消息内容
     * @param type 消息类型
     * @param userIds 用户ID列表
     * @return 推送成功的消息数量
     */
    int pushMessageToUsers(String title, String content, String type, List<Long> userIds);
}
