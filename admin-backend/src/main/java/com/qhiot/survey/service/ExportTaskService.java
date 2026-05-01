package com.qhiot.survey.service;

import com.qhiot.survey.entity.ExportTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 导出任务服务接口
 */
public interface ExportTaskService extends IService<ExportTask> {

    /**
     * 创建导出任务（异步）
     * @param taskName 任务名称
     * @param taskType 任务类型
     * @param projectId 项目ID
     * @param creatorId 创建人ID
     * @return 任务ID
     */
    Long createExportTask(String taskName, String taskType, Long projectId, Long creatorId);

    /**
     * 创建单点位PDF导出任务
     * @param pointId 点位ID
     * @param resultId 勘察结果ID（可选）
     * @param creatorId 创建人ID
     * @return 任务ID
     */
    Long createPdfExportTask(Long pointId, Long resultId, Long creatorId);

    /**
     * 获取用户的导出任务列表
     * @param userId 用户ID
     * @return 任务列表
     */
    List<ExportTask> getUserTasks(Long userId);

    /**
     * 获取任务详情
     * @param taskId 任务ID
     * @return 任务详情
     */
    ExportTask getTaskDetail(Long taskId);

    /**
     * 生成PDF字节数组
     * @param pointData 点位数据
     * @param surveyData 勘察数据
     * @param auditData 审核数据
     * @return PDF字节数组
     */
    byte[] generatePdfBytes(Map<String, Object> pointData, Map<String, Object> surveyData, Map<String, Object> auditData);
}
