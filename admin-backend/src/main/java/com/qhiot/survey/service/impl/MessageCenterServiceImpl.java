package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.entity.MessageCenter;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.mapper.MessageCenterMapper;
import com.qhiot.survey.mapper.SysUserMapper;
import com.qhiot.survey.service.MessageCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 消息中心服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCenterServiceImpl extends ServiceImpl<MessageCenterMapper, MessageCenter> implements MessageCenterService {

    private final SysUserMapper sysUserMapper;

    @Override
    public Page<MessageCenter> listByPage(Long userId, Integer type, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<MessageCenter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageCenter::getUserId, userId);
        if (type != null) {
            wrapper.eq(MessageCenter::getMsgType, type);
        }
        wrapper.orderByDesc(MessageCenter::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return lambdaQuery()
                .eq(MessageCenter::getUserId, userId)
                .eq(MessageCenter::getIsRead, 0)
                .count();
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        MessageCenter message = getById(messageId);
        if (message != null && message.getUserId().equals(userId)) {
            MessageCenter update = new MessageCenter();
            update.setId(messageId);
            update.setIsRead(1);
            update.setReadTime(LocalDateTime.now());
            updateById(update);
        }
    }

    @Override
    public void batchMarkAsRead(Long userId) {
        lambdaUpdate()
                .eq(MessageCenter::getUserId, userId)
                .eq(MessageCenter::getIsRead, 0)
                .set(MessageCenter::getIsRead, 1)
                .set(MessageCenter::getReadTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void createSystemMessage(String title, String content, String type, Long targetUserId) {
        MessageCenter message = new MessageCenter();
        message.setMsgTitle(title);
        message.setMsgContent(content);
        message.setMsgType(type);
        message.setUserId(targetUserId);
        message.setIsRead(0);
        message.setPushStatus(0);
        message.setCreateTime(LocalDateTime.now());
        save(message);
    }

    @Override
    public int pushMessageToRoles(String title, String content, String type, String roles) {
        // 查询符合条件的用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1); // 只推送给启用状态的用户
        
        if (StringUtils.hasText(roles)) {
            // 解析角色列表
            List<Integer> roleList = Arrays.stream(roles.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(Integer::parseInt)
                    .toList();
            wrapper.in(SysUser::getRole, roleList);
        }
        
        List<SysUser> users = sysUserMapper.selectList(wrapper);
        
        // 为每个用户创建消息
        int count = 0;
        for (SysUser user : users) {
            try {
                createSystemMessage(title, content, type, user.getId());
                count++;
            } catch (Exception e) {
                log.error("推送消息给用户失败: userId={}", user.getId(), e);
            }
        }
        
        return count;
    }

    @Override
    public int pushMessageToUsers(String title, String content, String type, List<Long> userIds) {
        int count = 0;
        for (Long userId : userIds) {
            try {
                createSystemMessage(title, content, type, userId);
                count++;
            } catch (Exception e) {
                log.error("推送消息给用户失败: userId={}", userId, e);
            }
        }
        return count;
    }
}
