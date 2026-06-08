package com.qhiot.survey.service.impl;

import com.qhiot.survey.common.util.PdfGeneratorUtil;
import com.qhiot.survey.entity.ExportTask;
import com.qhiot.survey.mapper.ExportTaskMapper;
import com.qhiot.survey.service.ExportTaskProcessor;
import com.qhiot.survey.service.ExportTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导出任务服务实现类
 *
 * 注意：异步导出执行逻辑被拆分到 {@link ExportTaskProcessor} 中，
 * 以避免 Spring AOP 同类内调用导致 @Async 失效的问题。
 */
@Slf4j
@Service
public class ExportTaskServiceImpl extends ServiceImpl<ExportTaskMapper, ExportTask> implements ExportTaskService {

    @Autowired
    private ExportTaskProcessor exportTaskProcessor;

    @Override
    public Long createExportTask(String taskName, String taskType, Long projectId, Long creatorId) {
        if (taskType == null || taskType.isEmpty()) {
            throw new IllegalArgumentException("taskType 不能为空");
        }
        ExportTask task = new ExportTask();
        task.setTaskName(taskName);
        task.setTaskType(taskType);
        task.setProjectId(projectId);
        task.setStatus(0); // 待生成
        task.setCreatorId(creatorId);
        task.setCreateTime(LocalDateTime.now());
        save(task);

        // 通过独立 Bean 触发异步执行（保证 @Async 生效）
        exportTaskProcessor.executeExportTaskAsync(task.getId());
        return task.getId();
    }

    @Override
    public Long createPdfExportTask(Long pointId, Long resultId, Long creatorId) {
        if (pointId == null) {
            throw new IllegalArgumentException("pointId 不能为空");
        }
        ExportTask task = new ExportTask();
        task.setTaskName("点位勘察报告PDF-" + pointId);
        task.setTaskType("pdf_single");
        task.setPointId(pointId);
        task.setResultId(resultId);
        task.setStatus(0);
        task.setCreatorId(creatorId);
        task.setCreateTime(LocalDateTime.now());
        save(task);

        exportTaskProcessor.executePdfExportTaskAsync(task.getId(), pointId, resultId);
        return task.getId();
    }

    @Override
    public List<ExportTask> getUserTasks(Long userId) {
        return lambdaQuery()
                .eq(ExportTask::getCreatorId, userId)
                .orderByDesc(ExportTask::getCreateTime)
                .last("LIMIT 50")
                .list();
    }

    @Override
    public ExportTask getTaskDetail(Long taskId) {
        return getById(taskId);
    }

    @Override
    public byte[] generatePdfBytes(Map<String, Object> pointData,
                                    Map<String, Object> surveyData,
                                    Map<String, Object> auditData) {
        return PdfGeneratorUtil.generateSurveyReportPdf(pointData, surveyData, auditData);
    }

    @Override
    public Long createBatchPdfExportTask(Long projectId, List<Long> pointIds, Long creatorId) {
        ExportTask task = new ExportTask();
        task.setTaskName("批量点位报告PDF-" + (projectId != null ? "proj" + projectId : "custom"));
        task.setTaskType("pdf_batch");
        task.setProjectId(projectId);
        task.setStatus(0);
        task.setCreatorId(creatorId);
        task.setCreateTime(LocalDateTime.now());
        save(task);

        exportTaskProcessor.executeBatchPdfExportTaskAsync(task.getId(), projectId, pointIds);
        return task.getId();
    }

    @Override
    public List<ExportTask> getProjectTasks(Long projectId) {
        return lambdaQuery()
                .eq(ExportTask::getProjectId, projectId)
                .orderByDesc(ExportTask::getCreateTime)
                .last("LIMIT 50")
                .list();
    }
}
