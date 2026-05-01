package com.qhiot.survey.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhiot.survey.common.util.PdfGeneratorUtil;
import com.qhiot.survey.entity.ExportTask;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.entity.SurveyAuditRecord;
import com.qhiot.survey.mapper.ExportTaskMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import com.qhiot.survey.mapper.SurveyAuditRecordMapper;
import com.qhiot.survey.service.ExportTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出任务服务实现类
 * 使用@Async实现异步导出
 */
@Slf4j
@Service
public class ExportTaskServiceImpl extends ServiceImpl<ExportTaskMapper, ExportTask> implements ExportTaskService {

    @Autowired
    private SurveyPointMapper surveyPointMapper;

    @Autowired
    private SurveyResultMapper surveyResultMapper;

    @Autowired
    private SurveyAuditRecordMapper surveyAuditRecordMapper;

    // 导出文件存储目录
    private static final String EXPORT_DIR = System.getProperty("user.dir") + "/exports/";

    @Override
    public Long createExportTask(String taskName, String taskType, Long projectId, Long creatorId) {
        ExportTask task = new ExportTask();
        task.setTaskName(taskName);
        task.setTaskType(taskType);
        task.setProjectId(projectId);
        task.setStatus(0); // 待生成
        task.setCreatorId(creatorId);
        task.setCreateTime(LocalDateTime.now());
        save(task);

        // 异步执行导出任务
        executeExportTaskAsync(task.getId());

        return task.getId();
    }

    @Override
    public Long createPdfExportTask(Long pointId, Long resultId, Long creatorId) {
        ExportTask task = new ExportTask();
        task.setTaskName("点位勘察报告PDF-" + pointId);
        task.setTaskType("PDF");
        task.setPointId(pointId);
        task.setResultId(resultId);
        task.setStatus(0); // 待生成
        task.setCreatorId(creatorId);
        task.setCreateTime(LocalDateTime.now());
        save(task);

        // 异步执行PDF导出
        executePdfExportTaskAsync(task.getId(), pointId, resultId);

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
    public byte[] generatePdfBytes(Map<String, Object> pointData, Map<String, Object> surveyData, Map<String, Object> auditData) {
        return PdfGeneratorUtil.generateSurveyReportPdf(pointData, surveyData, auditData);
    }

    /**
     * 异步执行导出任务
     * @param taskId 任务ID
     */
    @Async
    public void executeExportTaskAsync(Long taskId) {
        log.info("开始执行导出任务: {}", taskId);

        // 更新状态为生成中
        ExportTask task = new ExportTask();
        task.setId(taskId);
        task.setStatus(1);
        updateById(task);

        try {
            // 模拟导出耗时操作
            Thread.sleep(3000);

            // 实际导出逻辑应该在这里实现
            // 1. 查询数据
            // 2. 生成文件
            // 3. 上传到OSS
            // 4. 更新任务状态

            // 更新为已完成
            task.setStatus(2);
            task.setFileUrl("/exports/task_" + taskId + ".xlsx");
            task.setFileSize(1024L);
            task.setExpireTime(LocalDateTime.now().plusDays(7));
            updateById(task);

            log.info("导出任务完成: {}", taskId);
        } catch (Exception e) {
            log.error("导出任务失败: {}", taskId, e);
            task.setStatus(3);
            task.setErrorMsg(e.getMessage());
            updateById(task);
        }
    }

    /**
     * 异步执行PDF导出任务
     * @param taskId 任务ID
     * @param pointId 点位ID
     * @param resultId 勘察结果ID
     */
    @Async
    public void executePdfExportTaskAsync(Long taskId, Long pointId, Long resultId) {
        log.info("开始执行PDF导出任务: {}, pointId: {}, resultId: {}", taskId, pointId, resultId);

        ExportTask task = new ExportTask();
        task.setId(taskId);
        task.setStatus(1); // 生成中
        updateById(task);

        try {
            // 1. 查询点位数据
            SurveyPoint point = surveyPointMapper.selectById(pointId);
            if (point == null) {
                throw new RuntimeException("点位不存在: " + pointId);
            }

            // 2. 查询勘察结果数据
            SurveyResult result = null;
            if (resultId != null) {
                result = surveyResultMapper.selectById(resultId);
            } else {
                // 如果没有指定resultId，获取最新的已审核结果
                result = surveyResultMapper.selectLatestByPointId(pointId);
            }

            // 3. 查询审核记录
            SurveyAuditRecord auditRecord = null;
            if (result != null) {
                auditRecord = surveyAuditRecordMapper.selectLatestByResultId(result.getId());
            }

            // 4. 构建PDF数据
            Map<String, Object> pointData = buildPointData(point);
            Map<String, Object> surveyData = buildSurveyData(result);
            Map<String, Object> auditData = buildAuditData(auditRecord);

            // 5. 生成PDF
            byte[] pdfBytes = PdfGeneratorUtil.generateSurveyReportPdf(pointData, surveyData, auditData);

            // 6. 保存PDF文件
            String fileName = "survey_report_" + pointId + "_" + System.currentTimeMillis() + ".pdf";
            Path exportPath = Path.of(EXPORT_DIR);
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }
            Path filePath = exportPath.resolve(fileName);
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(pdfBytes);
            }

            // 7. 更新任务状态为已完成
            task.setStatus(2);
            task.setFileUrl("/exports/" + fileName);
            task.setFileSize((long) pdfBytes.length);
            task.setExpireTime(LocalDateTime.now().plusDays(7));
            updateById(task);

            log.info("PDF导出任务完成: {}, 文件大小: {} bytes", taskId, pdfBytes.length);
        } catch (Exception e) {
            log.error("PDF导出任务失败: {}", taskId, e);
            task.setStatus(3); // 失败
            task.setErrorMsg(e.getMessage());
            updateById(task);
        }
    }

    /**
     * 构建点位数据
     */
    private Map<String, Object> buildPointData(SurveyPoint point) {
        Map<String, Object> data = new HashMap<>();
        data.put("pointCode", point.getPointCode());
        data.put("pointName", point.getPointName());
        data.put("outfallType", point.getOutfallType());
        data.put("projectId", point.getProjectId());
        data.put("sectionId", point.getSectionId());
        data.put("longitude", point.getLongitude());
        data.put("latitude", point.getLatitude());
        data.put("region", point.getRegion());
        data.put("pointStatus", point.getStatus());
        data.put("assigneeId", point.getAssigneeId());
        data.put("collectorId", point.getCollectorId());
        return data;
    }

    /**
     * 构建勘察数据
     */
    private Map<String, Object> buildSurveyData(SurveyResult result) {
        Map<String, Object> data = new HashMap<>();
        if (result != null) {
            // 解析表单JSON数据
            if (result.getFormData() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> formData = mapper.readValue(result.getFormData(), new TypeReference<Map<String, Object>>(){});
                    data.putAll(formData);
                } catch (Exception e) {
                    log.warn("解析勘察表单JSON失败: {}", result.getFormData(), e);
                }
            }
            data.put("versionNo", result.getVersionNo());
            data.put("submitTime", result.getSubmitTime());
        }
        return data;
    }

    /**
     * 构建审核数据
     */
    private Map<String, Object> buildAuditData(SurveyAuditRecord auditRecord) {
        Map<String, Object> data = new HashMap<>();
        if (auditRecord != null) {
            data.put("auditStatus", auditRecord.getAction());
            data.put("auditorId", auditRecord.getAuditorId());
            data.put("auditTime", auditRecord.getCreateTime());
            data.put("auditComment", auditRecord.getAuditComment());
        }
        return data;
    }
}
