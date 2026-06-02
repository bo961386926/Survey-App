package com.qhiot.survey.service;

import com.qhiot.survey.entity.ExportTask;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.mapper.ExportTaskMapper;
import com.qhiot.survey.mapper.SurveyAuditRecordMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import com.qhiot.survey.service.impl.ExportTaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 导出任务服务测试
 *
 * <p>覆盖：</p>
 * <ol>
 *   <li>{@code ExportTaskServiceImpl.createExportTask}：任务落库后状态=待生成(0)，并触发异步执行</li>
 *   <li>{@code ExportTaskProcessor.executeExportTaskAsync}：成功路径落文件，状态推进到已完成(2)</li>
 *   <li>{@code ExportTaskProcessor.executeExportTaskAsync}：非法 taskType 时状态切换为失败(3)并写入 errorMsg</li>
 *   <li>下载文件解析：{@code resolveFile} 通过 fileName 命中实际文件，返回有效 Path</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ExportTaskService 单元测试")
class ExportTaskServiceTest {

    // =============== ExportTaskServiceImpl 测试 ===============

    @Mock private ExportTaskProcessor exportTaskProcessor;

    private ExportTaskServiceImpl exportTaskService;

    @BeforeEach
    void setUpService() {
        exportTaskService = Mockito.spy(new ExportTaskServiceImpl());
        ReflectionTestUtils.setField(exportTaskService, "exportTaskProcessor", exportTaskProcessor);
        // ServiceImpl.save() 调用 baseMapper.insert，这里直接 stub 在 spy 上避免触碰 mapper
        doReturn(true).when(exportTaskService).save(any(ExportTask.class));
    }

    @Test
    @DisplayName("createExportTask - 创建后状态=待生成(0) 并触发异步执行")
    void testCreateExportTask_StatusPendingAndTriggersAsync() {
        // 模拟 MyBatis-Plus save 时回填主键
        doReturn(true).when(exportTaskService).save(Mockito.argThat(task -> {
            if (task != null) {
                task.setId(123L);
            }
            return true;
        }));

        Long taskId = exportTaskService.createExportTask(
                "点位列表导出", "point_list", 99L, 1000L);

        assertEquals(123L, taskId);

        ArgumentCaptor<ExportTask> captor = ArgumentCaptor.forClass(ExportTask.class);
        verify(exportTaskService, times(1)).save(captor.capture());
        ExportTask saved = captor.getValue();
        assertEquals(Integer.valueOf(0), saved.getStatus(), "新建任务必须为 待生成(0) 状态");
        assertEquals("point_list", saved.getTaskType());
        assertEquals(Long.valueOf(99L), saved.getProjectId());
        assertEquals(Long.valueOf(1000L), saved.getCreatorId());
        assertNotNull(saved.getCreateTime());

        // 必须触发异步处理
        verify(exportTaskProcessor, times(1)).executeExportTaskAsync(123L);
    }

    @Test
    @DisplayName("createExportTask - taskType 为空时拒绝创建")
    void testCreateExportTask_RejectsEmptyTaskType() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> exportTaskService.createExportTask("x", null, 1L, 1L));
        verify(exportTaskProcessor, never()).executeExportTaskAsync(anyLong());
    }

    @Test
    @DisplayName("createPdfExportTask - 创建 PDF 任务并触发 PDF 异步处理")
    void testCreatePdfExportTask() {
        doReturn(true).when(exportTaskService).save(Mockito.argThat(task -> {
            if (task != null) {
                task.setId(456L);
            }
            return true;
        }));

        Long taskId = exportTaskService.createPdfExportTask(77L, 88L, 1000L);

        assertEquals(456L, taskId);
        verify(exportTaskProcessor, times(1)).executePdfExportTaskAsync(456L, 77L, 88L);
    }

    // =============== ExportTaskProcessor 测试 ===============

    @Mock private ExportTaskMapper exportTaskMapper;
    @Mock private SurveyPointMapper surveyPointMapper;
    @Mock private SurveyResultMapper surveyResultMapper;
    @Mock private SurveyAuditRecordMapper surveyAuditRecordMapper;

    private ExportTaskProcessor newProcessor(Path exportDir) {
        ExportTaskProcessor processor = new ExportTaskProcessor();
        ReflectionTestUtils.setField(processor, "exportTaskMapper", exportTaskMapper);
        ReflectionTestUtils.setField(processor, "surveyPointMapper", surveyPointMapper);
        ReflectionTestUtils.setField(processor, "surveyResultMapper", surveyResultMapper);
        ReflectionTestUtils.setField(processor, "surveyAuditRecordMapper", surveyAuditRecordMapper);
        ReflectionTestUtils.setField(processor, "exportDir", exportDir.toString());
        ReflectionTestUtils.setField(processor, "retentionDays", 7);
        return processor;
    }

    @Test
    @DisplayName("executeExportTaskAsync - 成功路径生成文件并将状态推进到已完成(2)")
    void testExecuteExportTaskAsync_SuccessProducesFileAndCompletes(@TempDir Path tempDir)
            throws IOException {
        ExportTaskProcessor processor = newProcessor(tempDir);

        ExportTask task = new ExportTask();
        task.setId(7777L);
        task.setTaskType("point_list");
        task.setProjectId(1L);
        task.setStatus(0);
        when(exportTaskMapper.selectById(7777L)).thenReturn(task);
        when(exportTaskMapper.updateById(any(ExportTask.class))).thenReturn(1);
        // 点位为空也能生成一个空表
        when(surveyPointMapper.selectList(any())).thenReturn(Collections.<SurveyPoint>emptyList());

        processor.executeExportTaskAsync(7777L);

        // 至少应有两次 updateById：① 切换为生成中(1) ② 切换为已完成(2)
        ArgumentCaptor<ExportTask> updateCaptor = ArgumentCaptor.forClass(ExportTask.class);
        verify(exportTaskMapper, Mockito.atLeast(2)).updateById(updateCaptor.capture());

        boolean sawCompleted = updateCaptor.getAllValues().stream()
                .anyMatch(u -> Integer.valueOf(2).equals(u.getStatus())
                        && u.getFileName() != null
                        && u.getFileName().startsWith("task_7777_")
                        && u.getFileName().endsWith(".xlsx"));
        assertTrue(sawCompleted, "必须出现一次 status=2（已完成）的更新，且写入了文件名");

        // 落盘文件确实存在
        try (var stream = Files.list(tempDir)) {
            boolean fileExists = stream.anyMatch(p -> p.getFileName().toString()
                    .startsWith("task_7777_") && p.getFileName().toString().endsWith(".xlsx"));
            assertTrue(fileExists, "导出文件应被实际写入磁盘");
        }
    }

    @Test
    @DisplayName("executeExportTaskAsync - 非法 taskType 切换为失败(3) 并写入 errorMsg")
    void testExecuteExportTaskAsync_FailureSetsStatusFailedWithErrorMessage(@TempDir Path tempDir) {
        ExportTaskProcessor processor = newProcessor(tempDir);

        ExportTask task = new ExportTask();
        task.setId(8888L);
        task.setTaskType("unknown_type"); // 触发 IllegalArgumentException
        task.setStatus(0);
        when(exportTaskMapper.selectById(8888L)).thenReturn(task);
        when(exportTaskMapper.updateById(any(ExportTask.class))).thenReturn(1);

        processor.executeExportTaskAsync(8888L);

        ArgumentCaptor<ExportTask> captor = ArgumentCaptor.forClass(ExportTask.class);
        verify(exportTaskMapper, Mockito.atLeast(2)).updateById(captor.capture());

        boolean sawFailed = captor.getAllValues().stream().anyMatch(u ->
                Integer.valueOf(3).equals(u.getStatus())
                        && u.getErrorMsg() != null
                        && u.getErrorMsg().contains("不支持的导出类型"));
        assertTrue(sawFailed, "失败应将状态置为 3 并写入 errorMsg");
    }

    @Test
    @DisplayName("executeExportTaskAsync - 任务不存在时直接返回，不更新任何状态")
    void testExecuteExportTaskAsync_TaskNotFoundReturns(@TempDir Path tempDir) {
        ExportTaskProcessor processor = newProcessor(tempDir);
        when(exportTaskMapper.selectById(9999L)).thenReturn(null);

        processor.executeExportTaskAsync(9999L);

        verify(exportTaskMapper, never()).updateById(any(ExportTask.class));
    }

    @Test
    @DisplayName("resolveFile - 通过 fileName 命中磁盘文件，下载入口可解析出有效 Path")
    void testResolveFile_FindsFileByFileName(@TempDir Path tempDir) throws IOException {
        ExportTaskProcessor processor = newProcessor(tempDir);

        // 提前在 tempDir 中放一个实际文件
        String fileName = "task_555_1700000000000.xlsx";
        Path expected = tempDir.resolve(fileName);
        Files.write(expected, new byte[]{1, 2, 3});

        ExportTask task = new ExportTask();
        task.setId(555L);
        task.setFileName(fileName);

        Path resolved = processor.resolveFile(task);

        assertNotNull(resolved, "resolveFile 应返回有效 Path");
        assertEquals(expected.toAbsolutePath(), resolved.toAbsolutePath());
        assertTrue(Files.exists(resolved));
    }

    @Test
    @DisplayName("resolveFile - fileName 缺失时按前缀匹配回退")
    void testResolveFile_FallbackByPrefix(@TempDir Path tempDir) throws IOException {
        ExportTaskProcessor processor = newProcessor(tempDir);

        String fileName = "survey_report_777_1700000000000.pdf";
        Path expected = tempDir.resolve(fileName);
        Files.write(expected, new byte[]{9});

        ExportTask task = new ExportTask();
        task.setId(123L);          // 与磁盘文件 task_id 不匹配
        task.setPointId(777L);     // 但通过 pointId 前缀命中 survey_report_777_*
        // 没有 fileName，触发回退分支
        Path resolved = processor.resolveFile(task);
        assertNotNull(resolved);
        assertEquals(expected.toAbsolutePath(), resolved.toAbsolutePath());
    }
}
