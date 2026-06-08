package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.Announcement;

/**
 * 公告服务接口
 */
public interface AnnouncementService extends IService<Announcement> {

    Page<Announcement> listByPage(String keyword, String type, Integer status, Integer pageNum, Integer pageSize);

    Announcement createAnnouncement(Announcement announcement);

    Announcement updateAnnouncement(Long id, Announcement announcement);

    void publishAnnouncement(Long id);

    void recallAnnouncement(Long id);

    void deleteAnnouncement(Long id);
}
