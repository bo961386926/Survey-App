package com.qhiot.survey.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhiot.survey.common.util.ExcelUtil;
import com.qhiot.survey.common.util.PdfGeneratorUtil;
import com.qhiot.survey.entity.ExportTask;
import com.qhiot.survey.entity.SurveyAuditRecord;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.mapper.ExportTaskMapper;
import com.qhiot.survey.mapper.SurveyAuditRecordMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出任务处理器
 *
 * 拆分到独立的Spring Bean以避免 {@link Service} 类内部 this 方法调用导致 @Async 失效
 * （Spring AOP 的代理仅在跨 Bean 调用时才会生效）。
 */
@Slf4j
@Component
public class ExportTaskProcessor {

    @Autowired
    private ExportTaskMapper exportTaskMapper;

    @Autowired
    private SurveyPointMapper surveyPointMapper;

    @Autowired
    private SurveyResultMapper surveyResultMapper;

    @Autowired
    private SurveyAuditRecordMapper surveyAuditRecordMapper;

    @Value("${export.storage.path:#{systemProperties['user.dir'] + '/exports'}}")
    private String exportDir;

    /**
     * 导出文件保留天数
     */
    @Value("${export.retention-days:7}")
    private int retentionDays;

    /**
     * 通用异步导出（点位列表 / 审核结果 等表格数据）
     */
    @Async("exportTaskExecutor")
    public void executeExportTaskAsync(Long taskId) {
        log.info("[导出] 开始执行通用导出任务: taskId={}", taskId);

        ExportTask task = exportTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("[导出] 任务不存在: taskId={}", taskId);
            return;
        }

        // 状态机: 0待生成 → 1生成中
        if (!transitionStatus(taskId, 1, null)) {
            return;
        }

        try {
            byte[] bytes;
            String fileExt;
            String taskType = task.getTaskType();
            if ("point_list".equalsIgnoreCase(taskType)) {
                bytes = generatePointListExcel(task.getProjectId());
                fileExt = "xlsx";
            } else if ("audit_result".equalsIgnoreCase(taskType)) {
                bytes = generateAuditResultExcel(task.getProjectId());
                fileExt = "xlsx";
            } else if ("pdf_single".equalsIgnoreCase(taskType)) {
                // 兼容: 单点位 PDF 也可以用通用入口创建（pointId 必填）
                bytes = generateSinglePdfBytes(task.getPointId(), task.getResultId());
                fileExt = "pdf";
            } else {
                throw new IllegalArgumentException("不支持的导出类型: " + taskType);
            }

            String fileName = "task_" + taskId + "_" + System.currentTimeMillis() + "." + fileExt;
            Path file = persistFile(fileName, bytes);

            ExportTask update = new ExportTask();
            update.setId(taskId);
            update.setStatus(2); // 已完成
            update.setFileUrl("/api/v1/export/download/" + taskId);
            update.setFileName(fileName);
            update.setFileSize(Files.size(file));
            update.setExpireTime(LocalDateTime.now().plusDays(retentionDays));
            update.setUpdateTime(LocalDateTime.now());
            update.setErrorMsg(null);
            exportTaskMapper.updateById(update);

            log.info("[导出] 通用导出任务完成: taskId={}, file={}, size={} bytes",
                    taskId, file, bytes.length);
        } catch (Exception e) {
            log.error("[导出] 通用导出任务失败: taskId=" + taskId, e);
            markFailed(taskId, e.getMessage());
        }
    }

    /**
     * 单点位 PDF 异步导出
     */
    @Async("exportTaskExecutor")
    public void executePdfExportTaskAsync(Long taskId, Long pointId, Long resultId) {
        log.info("[导出] 开始执行PDF导出任务: taskId={}, pointId={}, resultId={}",
                taskId, pointId, resultId);

        if (!transitionStatus(taskId, 1, null)) {
            return;
        }

        try {
            byte[] pdfBytes = generateSinglePdfBytes(pointId, resultId);
            String fileName = "survey_report_" + pointId + "_" + System.currentTimeMillis() + ".pdf";
            Path file = persistFile(fileName, pdfBytes);

            ExportTask update = new ExportTask();
            update.setId(taskId);
            update.setStatus(2);
            update.setFileUrl("/api/v1/export/download/" + taskId);
            update.setFileName(fileName);
            update.setFileSize((long) pdfBytes.length);
            update.setExpireTime(LocalDateTime.now().plusDays(retentionDays));
            update.setUpdateTime(LocalDateTime.now());
            exportTaskMapper.updateById(update);
            log.info("[导出] PDF导出完成: taskId={}, file={}, size={} bytes",
                    taskId, file, pdfBytes.length);
        } catch (Exception e) {
            log.error("[导出] PDF导出失败: taskId=" + taskId, e);
            markFailed(taskId, e.getMessage());
        }
    }

    /**
     * 解析任务对应的磁盘文件。
     */
    public Path resolveFile(ExportTask task) throws IOException {
        ensureExportDir();
        if (task.getFileName() != null && !task.getFileName().isEmpty()) {
            Path file = Paths.get(exportDir).resolve(task.getFileName());
            if (Files.exists(file)) {
                return file;
            }
        }
        // 回退：按约定的文件名前缀匹配
        Path dir = Paths.get(exportDir);
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().startsWith("task_" + task.getId() + "_")
                            || (task.getPointId() != null
                                && p.getFileName().toString()
                                       .startsWith("survey_report_" + task.getPointId() + "_")))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 定时清理过期导出任务（每天凌晨 03:30）。
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanupExpiredExports() {
        log.info("[导出] 开始清理过期导出任务");
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ExportTask> wrapper = new LambdaQueryWrapper<ExportTask>()
                .lt(ExportTask::getExpireTime, now)
                .in(ExportTask::getStatus, 2, 3);
        List<ExportTask> expired = exportTaskMapper.selectList(wrapper);
        int fileDeleted = 0;
        for (ExportTask t : expired) {
            try {
                Path file = resolveFile(t);
                if (file != null && Files.deleteIfExists(file)) {
                    fileDeleted++;
                }
            } catch (Exception ex) {
                log.warn("[导出] 删除文件失败 taskId={}", t.getId(), ex);
            }
            ExportTask u = new ExportTask();
            u.setId(t.getId());
            u.setStatus(4); // 已过期
            u.setUpdateTime(now);
            exportTaskMapper.updateById(u);
        }
        log.info("[导出] 清理完成: 任务={}个, 删除文件={}个", expired.size(), fileDeleted);
    }

    // ------------- 内部辅助方法 -------------

    private boolean transitionStatus(Long taskId, int expectedNewStatus, String errMsg) {
        ExportTask u = new ExportTask();
        u.setId(taskId);
        u.setStatus(expectedNewStatus);
        u.setUpdateTime(LocalDateTime.now());
        if (errMsg != null) {
            u.setErrorMsg(errMsg);
        }
        return exportTaskMapper.updateById(u) > 0;
    }

    private void markFailed(Long taskId, String msg) {
        ExportTask u = new ExportTask();
        u.setId(taskId);
        u.setStatus(3);
        u.setErrorMsg(truncate(msg, 500));
        u.setUpdateTime(LocalDateTime.now());
        exportTaskMapper.updateById(u);
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    private void ensureExportDir() throws IOException {
        Path dir = Paths.get(exportDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    private Path persistFile(String fileName, byte[] bytes) throws IOException {
        ensureExportDir();
        Path file = Paths.get(exportDir).resolve(fileName);
        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            fos.write(bytes);
        }
        return file;
    }

    /**
     * 生成单点位勘察 PDF
     */
    private byte[] generateSinglePdfBytes(Long pointId, Long resultId) {
        if (pointId == null) {
            throw new IllegalArgumentException("pointId 不能为空");
        }
        SurveyPoint point = surveyPointMapper.selectById(pointId);
        if (point == null) {
            throw new IllegalArgumentException("点位不存在: " + pointId);
        }

        SurveyResult result = (resultId != null)
                ? surveyResultMapper.selectById(resultId)
                : surveyResultMapper.selectLatestByPointId(pointId);

        SurveyAuditRecord auditRecord = (result != null)
                ? surveyAuditRecordMapper.selectLatestByResultId(result.getId())
                : null;

        Map<String, Object> pointData = buildPointData(point);
        Map<String, Object> surveyData = buildSurveyData(result);
        Map<String, Object> auditData = buildAuditData(auditRecord);
        return PdfGeneratorUtil.generateSurveyReportPdf(pointData, surveyData, auditData);
    }

    /**
     * 生成点位列表 Excel
     */
    private byte[] generatePointListExcel(Long projectId) throws IOException {
        LambdaQueryWrapper<SurveyPoint> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(SurveyPoint::getProjectId, projectId);
        }
        wrapper.orderByDesc(SurveyPoint::getCreateTime);
        List<SurveyPoint> points = surveyPointMapper.selectList(wrapper);

        String[] headers = {"点位编号", "点位名称", "排口类型", "经度", "纬度",
                "行政区", "状态", "创建时间"};
        List<Map<String, Object>> rows = new ArrayList<>(points.size());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (SurveyPoint p : points) {
            Map<String, Object> row = new HashMap<>();
            row.put("点位编号", p.getPointCode());
            row.put("点位名称", p.getPointName());
            row.put("排口类型", p.getOutfallType());
            row.put("经度", p.getLongitude());
            row.put("纬度", p.getLatitude());
            row.put("行政区", p.getRegion());
            row.put("状态", statusText(p.getStatus()));
            row.put("创建时间", p.getCreateTime() != null ? p.getCreateTime().format(fmt) : "");
            rows.add(row);
        }
        return writeWorkbookBytes(ExcelUtil.createExcel(headers, rows));
    }

    /**
     * 生成审核结果 Excel
     */
    private byte[] generateAuditResultExcel(Long projectId) throws IOException {
        LambdaQueryWrapper<SurveyResult> wrapper = new LambdaQueryWrapper<>();
        // 仅按状态过滤已提交/已审核数据
        wrapper.in(SurveyResult::getResultStatus, 1, 2, 3, 4);
        wrapper.orderByDesc(SurveyResult::getCreateTime);
        List<SurveyResult> results = surveyResultMapper.selectList(wrapper);

        String[] headers = {"结果ID", "点位ID", "版本号", "结果状态", "审核状态",
                "勘察人ID", "审核人ID", "提交时间", "审核时间", "驳回意见"};
        List<Map<String, Object>> rows = new ArrayList<>(results.size());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (SurveyResult r : results) {
            // 当指定 projectId 时，按点位过滤
            if (projectId != null) {
                SurveyPoint p = surveyPointMapper.selectById(r.getPointId());
                if (p == null || !projectId.equals(p.getProjectId())) {
                    continue;
                }
            }
            Map<String, Object> row = new HashMap<>();
            row.put("结果ID", r.getId());
            row.put("点位ID", r.getPointId());
            row.put("版本号", r.getVersionNo());
            row.put("结果状态", resultStatusText(r.getResultStatus()));
            row.put("审核状态", auditStatusText(r.getAuditStatus()));
            row.put("勘察人ID", r.getSurveyUserId());
            row.put("审核人ID", r.getAuditorId());
            row.put("提交时间", r.getSubmitTime() != null ? r.getSubmitTime().format(fmt) : "");
            row.put("审核时间", r.getAuditTime() != null ? r.getAuditTime().format(fmt) : "");
            row.put("驳回意见", r.getAuditRemark());
            rows.add(row);
        }
        return writeWorkbookBytes(ExcelUtil.createExcel(headers, rows));
    }

    private byte[] writeWorkbookBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); workbook) {
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

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
        data.put("pointStatus", statusText(point.getStatus()));
        data.put("assigneeId", point.getAssigneeId());
        data.put("collectorId", point.getCollectorId());
        return data;
    }

    private Map<String, Object> buildSurveyData(SurveyResult result) {
        Map<String, Object> data = new HashMap<>();
        if (result != null) {
            if (result.getFormData() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> formData = mapper.readValue(result.getFormData(),
                            new TypeReference<Map<String, Object>>() {});
                    data.putAll(formData);
                } catch (Exception e) {
                    log.warn("[导出] 解析勘察表单JSON失败: {}", result.getFormData(), e);
                }
            }
            data.put("versionNo", result.getVersionNo());
            data.put("submitTime", result.getSubmitTime());
        }
        return data;
    }

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

    private String statusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待采集";
            case 1: return "草稿中";
            case 2: return "待审核";
            case 3: return "审核通过";
            case 4: return "驳回待修改";
            case 5: return "已归档";
            case 6: return "作废";
            default: return String.valueOf(status);
        }
    }

    private String resultStatusText(Integer s) {
        if (s == null) return "";
        switch (s) {
            case 0: return "草稿";
            case 1: return "已提交";
            case 2: return "待审核";
            case 3: return "已通过";
            case 4: return "已驳回";
            case 5: return "已归档";
            default: return String.valueOf(s);
        }
    }

    private String auditStatusText(Integer s) {
        if (s == null) return "";
        switch (s) {
            case 0: return "待审";
            case 1: return "通过";
            case 2: return "驳回";
            default: return String.valueOf(s);
        }
    }
}
