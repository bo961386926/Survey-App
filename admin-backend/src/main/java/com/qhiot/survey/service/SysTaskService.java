package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.SysTask;

/**
 * 勘察指派任务服务接口
 */
public interface SysTaskService extends IService<SysTask> {

    /**
     * 分页查询任务列表
     */
    Page<SysTask> getTaskPage(Long projectId, Long assigneeId, Integer status, String category, String keyword, int pageNum, int pageSize);

    /**
     * 根据ID获取任务详情
     */
    SysTask getTaskById(Long id);

    /**
     * 创建勘察任务
     */
    boolean createTask(SysTask task);

    /**
     * 更新勘察任务
     */
    boolean updateTask(SysTask task);

    /**
     * 变更任务状态
     */
    boolean changeTaskStatus(Long id, Integer status);

    /**
     * 分配指派任务给采集人员
     */
    boolean assignTask(Long id, Long assigneeId);

    /**
     * 删除任务
     */
    boolean deleteTask(Long id);
}
