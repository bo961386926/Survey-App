package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.util.SecurityUtils;
import com.qhiot.survey.entity.Announcement;
import com.qhiot.survey.mapper.AnnouncementMapper;
import com.qhiot.survey.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 公告服务实现
 */
@Slf4j
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public Page<Announcement> listByPage(String keyword, String type, Integer status, Integer pageNum, Integer pageSize) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Announcement::getTitle, keyword)
                    .or().like(Announcement::getContent, keyword));
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Announcement::getType, type);
        }
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        wrapper.orderByDesc(Announcement::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public Announcement createAnnouncement(Announcement announcement) {
        announcement.setStatus(0);
        announcement.setPublisherId(SecurityUtils.getCurrentUserId());
        announcement.setCreateTime(LocalDateTime.now());
        save(announcement);
        return announcement;
    }

    @Override
    @Transactional
    public Announcement updateAnnouncement(Long id, Announcement announcement) {
        Announcement existing = getById(id);
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }
        if (existing.getStatus() == 2) {
            throw new BusinessException("已发布的公告不可编辑，请先撤回");
        }
        announcement.setId(id);
        updateById(announcement);
        return getById(id);
    }

    @Override
    @Transactional
    public void publishAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        announcement.setStatus(2);
        announcement.setPublishTime(LocalDateTime.now());
        updateById(announcement);
        log.info("公告已发布: id={}", id);
    }

    @Override
    @Transactional
    public void recallAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() != 2) {
            throw new BusinessException("仅已发布的公告可撤回");
        }
        announcement.setStatus(3);
        updateById(announcement);
        log.info("公告已撤回: id={}", id);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() == 2) {
            throw new BusinessException("已发布的公告不可删除，请先撤回");
        }
        removeById(id);
        log.info("公告已删除: id={}", id);
    }
}
