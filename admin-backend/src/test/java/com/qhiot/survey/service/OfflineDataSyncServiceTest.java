package com.qhiot.survey.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhiot.survey.entity.LocationCorrectionLog;
import com.qhiot.survey.entity.OfflineDataSync;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SurveyResult;
import com.qhiot.survey.entity.SysFile;
import com.qhiot.survey.mapper.LocationCorrectionLogMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SurveyResultMapper;
import com.qhiot.survey.mapper.SysFileMapper;
import com.qhiot.survey.service.impl.OfflineDataSyncServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OfflineDataSyncService 单元测试
 *
 * <p>覆盖关键同步路径：</p>
 * <ol>
 *   <li>survey_result 同步 - server_wins 冲突策略保留服务端数据</li>
 *   <li>survey_result 同步 - client_wins 冲突策略覆盖服务端数据</li>
 *   <li>survey_result 同步 - merge 字段级合并</li>
 *   <li>photo 同步 - 同 filePath + bizId 视为重复，更新而非插入</li>
 *   <li>location 同步 - 写入纠偏日志并更新点位坐标</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OfflineDataSyncService 单元测试")
class OfflineDataSyncServiceTest {

    @Mock private SurveyResultMapper surveyResultMapper;
    @Mock private SurveyPointMapper surveyPointMapper;
    @Mock private SysFileMapper sysFileMapper;
    @Mock private LocationCorrectionLogMapper locationCorrectionLogMapper;

    private OfflineDataSyncServiceImpl service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = Mockito.spy(new OfflineDataSyncServiceImpl(
                surveyResultMapper, surveyPointMapper, sysFileMapper, locationCorrectionLogMapper));
        // 默认让 updateById 返回 true，避免 syncData 中 setSyncStatus 流程出错
        doReturn(true).when(service).updateById(any(OfflineDataSync.class));
    }

    /** 组装一个 survey_result 类型的离线同步记录 */
    private OfflineDataSync buildSurveyResultSync(Long pointId,
                                                  Integer clientVersion,
                                                  String resolution,
                                                  Map<String, Object> extra) throws Exception {
        Map<String, Object> content = new HashMap<>();
        content.put("pointId", pointId);
        content.put("templateVersionId", 1L);
        content.put("resultStatus", 1);
        content.put("auditStatus", 0);
        if (extra != null) content.putAll(extra);

        OfflineDataSync sync = new OfflineDataSync();
        sync.setId(100L);
        sync.setDeviceId("device-001");
        sync.setUserId(99L);
        sync.setDataType("survey_result");
        sync.setDataId("biz-001");
        sync.setDataContent(objectMapper.writeValueAsString(content));
        sync.setVersionNo(clientVersion);
        sync.setSyncStatus(0);
        sync.setRetryCount(0);
        sync.setMaxRetryCount(3);
        sync.setConflictResolution(resolution);
        return sync;
    }

    @Test
    @DisplayName("survey_result - server_wins：保留服务端版本，不写入任何 SurveyResult")
    void testSyncSurveyResult_ServerWins() throws Exception {
        // 客户端 v1 < 服务端 v2
        Map<String, Object> extra = new HashMap<>();
        extra.put("formData", "{\"a\":\"client\"}");
        OfflineDataSync sync = buildSurveyResultSync(1L, 1, "server_wins", extra);

        SurveyResult serverResult = new SurveyResult();
        serverResult.setId(500L);
        serverResult.setPointId(1L);
        serverResult.setVersionNo(2);
        serverResult.setFormData("{\"a\":\"server\"}");
        when(surveyResultMapper.selectOne(any())).thenReturn(serverResult);

        doReturn(sync).when(service).getById(100L);

        Map<String, Object> result = service.syncData(100L);

        assertTrue((Boolean) result.get("success"));
        // 关键：服务端获胜路径直接 return，不应有写操作
        verify(surveyResultMapper, never()).insert(any(SurveyResult.class));
        verify(surveyResultMapper, never()).updateById(any(SurveyResult.class));
    }

    @Test
    @DisplayName("survey_result - client_wins：客户端版本旧但策略允许覆盖")
    void testSyncSurveyResult_ClientWins() throws Exception {
        // 客户端 v1 < 服务端 v2，但 client_wins 应继续覆盖
        Map<String, Object> extra = new HashMap<>();
        extra.put("formData", "{\"a\":\"client\"}");
        OfflineDataSync sync = buildSurveyResultSync(1L, 1, "client_wins", extra);

        SurveyResult serverResult = new SurveyResult();
        serverResult.setId(500L);
        serverResult.setPointId(1L);
        serverResult.setVersionNo(2);
        serverResult.setFormData("{\"a\":\"server\"}");
        when(surveyResultMapper.selectOne(any())).thenReturn(serverResult);
        when(surveyResultMapper.insert(any(SurveyResult.class))).thenReturn(1);
        when(surveyResultMapper.updateById(any(SurveyResult.class))).thenReturn(1);

        doReturn(sync).when(service).getById(100L);

        Map<String, Object> result = service.syncData(100L);

        assertTrue((Boolean) result.get("success"));
        // client_wins 时客户端版本 != 服务端版本 → 走 insert 分支
        ArgumentCaptor<SurveyResult> insertCaptor = ArgumentCaptor.forClass(SurveyResult.class);
        verify(surveyResultMapper, times(1)).insert(insertCaptor.capture());
        SurveyResult inserted = insertCaptor.getValue();
        // 版本号取 max(client=1, server+1=3) = 3
        assertEquals(Integer.valueOf(3), inserted.getVersionNo());
        // 内容采用客户端
        assertTrue(inserted.getFormData().contains("client"));
    }

    @Test
    @DisplayName("survey_result - merge：服务端字段保留，客户端字段覆盖同名字段")
    void testSyncSurveyResult_Merge() throws Exception {
        // 客户端 v1 < 服务端 v2 → 走 merge 分支
        Map<String, Object> extra = new HashMap<>();
        extra.put("formData", "{\"clientField\":\"c\",\"shared\":\"client\"}");
        OfflineDataSync sync = buildSurveyResultSync(1L, 1, "merge", extra);

        SurveyResult serverResult = new SurveyResult();
        serverResult.setId(500L);
        serverResult.setPointId(1L);
        serverResult.setVersionNo(2);
        serverResult.setFormData("{\"serverField\":\"s\",\"shared\":\"server\"}");
        when(surveyResultMapper.selectOne(any())).thenReturn(serverResult);
        when(surveyResultMapper.insert(any(SurveyResult.class))).thenReturn(1);

        doReturn(sync).when(service).getById(100L);

        Map<String, Object> result = service.syncData(100L);

        assertTrue((Boolean) result.get("success"));
        ArgumentCaptor<SurveyResult> captor = ArgumentCaptor.forClass(SurveyResult.class);
        verify(surveyResultMapper, times(1)).insert(captor.capture());
        String merged = captor.getValue().getFormData();
        assertNotNull(merged);
        // 服务端独有字段被保留
        assertTrue(merged.contains("serverField"), "merge 应保留服务端独有字段：" + merged);
        // 客户端独有字段也被纳入
        assertTrue(merged.contains("clientField"), "merge 应包含客户端独有字段：" + merged);
        // 同名字段以客户端为准
        assertTrue(merged.contains("\"shared\":\"client\""),
                "merge 同名字段应以客户端为准：" + merged);
    }

    @Test
    @DisplayName("photo - 相同 filePath+bizId 视为重复：执行更新而非插入")
    void testSyncPhoto_Deduplication() throws Exception {
        Map<String, Object> content = new HashMap<>();
        content.put("filePath", "/oss/survey/2026/abc.jpg");
        content.put("fileName", "abc.jpg");
        content.put("fileSize", 12345L);
        content.put("fileType", "image/jpeg");
        content.put("bizType", "survey_photo");
        content.put("resultId", 500L);
        content.put("pointId", 1L);

        OfflineDataSync sync = new OfflineDataSync();
        sync.setId(101L);
        sync.setDeviceId("device-001");
        sync.setUserId(99L);
        sync.setDataType("photo");
        sync.setDataContent(objectMapper.writeValueAsString(content));
        sync.setSyncStatus(0);
        sync.setRetryCount(0);
        sync.setMaxRetryCount(3);
        sync.setVersionNo(1);

        // 服务端已存在同路径文件 → 命中去重
        SysFile existing = new SysFile();
        existing.setId(7L);
        existing.setFilePath("/oss/survey/2026/abc.jpg");
        existing.setBizType("survey_photo");
        existing.setBizId(500L);
        existing.setFileName("abc.jpg");
        when(sysFileMapper.selectOne(any())).thenReturn(existing);
        when(sysFileMapper.updateById(any(SysFile.class))).thenReturn(1);

        // 关联的 SurveyResult 存在但 images 字段已包含该 URL → 不重复追加
        SurveyResult result = new SurveyResult();
        result.setId(500L);
        result.setImages("[\"/oss/survey/2026/abc.jpg\"]");
        when(surveyResultMapper.selectById(500L)).thenReturn(result);

        doReturn(sync).when(service).getById(101L);

        Map<String, Object> outcome = service.syncData(101L);

        assertTrue((Boolean) outcome.get("success"));
        // 重复 → 不应 insert，而是 update
        verify(sysFileMapper, never()).insert(any(SysFile.class));
        verify(sysFileMapper, times(1)).updateById(any(SysFile.class));
        // 不应重复追加图片
        verify(surveyResultMapper, never()).updateById(any(SurveyResult.class));
    }

    @Test
    @DisplayName("location - 写入纠偏日志并更新点位坐标")
    void testSyncLocation_WritesCorrectionLogAndUpdatesPoint() throws Exception {
        Map<String, Object> content = new HashMap<>();
        content.put("pointId", 1L);
        content.put("originalLng", "120.100000");
        content.put("originalLat", "30.100000");
        content.put("correctedLng", "120.123456");
        content.put("correctedLat", "30.654321");
        content.put("resultId", 500L);

        OfflineDataSync sync = new OfflineDataSync();
        sync.setId(102L);
        sync.setDeviceId("device-001");
        sync.setUserId(99L);
        sync.setDataType("location");
        sync.setDataContent(objectMapper.writeValueAsString(content));
        sync.setSyncStatus(0);
        sync.setRetryCount(0);
        sync.setMaxRetryCount(3);
        sync.setVersionNo(1);

        SurveyPoint point = new SurveyPoint();
        point.setId(1L);
        point.setLongitude(new BigDecimal("120.100000"));
        point.setLatitude(new BigDecimal("30.100000"));
        when(surveyPointMapper.selectById(1L)).thenReturn(point);
        when(surveyPointMapper.updateById(any(SurveyPoint.class))).thenReturn(1);
        when(locationCorrectionLogMapper.insert(any(LocationCorrectionLog.class))).thenReturn(1);

        doReturn(sync).when(service).getById(102L);

        Map<String, Object> outcome = service.syncData(102L);

        assertTrue((Boolean) outcome.get("success"));

        // 必须写一条纠偏日志
        ArgumentCaptor<LocationCorrectionLog> logCaptor =
                ArgumentCaptor.forClass(LocationCorrectionLog.class);
        verify(locationCorrectionLogMapper, times(1)).insert(logCaptor.capture());
        LocationCorrectionLog log = logCaptor.getValue();
        assertEquals(Long.valueOf(1L), log.getPointId());
        assertEquals(Long.valueOf(500L), log.getResultId());
        assertEquals(new BigDecimal("120.123456"), log.getCorrectedLng());
        assertEquals(new BigDecimal("30.654321"), log.getCorrectedLat());
        assertEquals(Long.valueOf(99L), log.getUserId());

        // 点位坐标被更新为纠偏后坐标
        ArgumentCaptor<SurveyPoint> pointCaptor = ArgumentCaptor.forClass(SurveyPoint.class);
        verify(surveyPointMapper, times(1)).updateById(pointCaptor.capture());
        assertEquals(new BigDecimal("120.123456"), pointCaptor.getValue().getLongitude());
        assertEquals(new BigDecimal("30.654321"), pointCaptor.getValue().getLatitude());
    }

    @Test
    @DisplayName("location - server_wins 时不覆盖已有点位坐标")
    void testSyncLocation_ServerWinsKeepsPointCoordinates() throws Exception {
        Map<String, Object> content = new HashMap<>();
        content.put("pointId", 1L);
        content.put("correctedLng", "120.999999");
        content.put("correctedLat", "30.999999");

        OfflineDataSync sync = new OfflineDataSync();
        sync.setId(103L);
        sync.setUserId(99L);
        sync.setDataType("location");
        sync.setDataContent(objectMapper.writeValueAsString(content));
        sync.setSyncStatus(0);
        sync.setRetryCount(0);
        sync.setMaxRetryCount(3);
        sync.setVersionNo(1);
        sync.setConflictResolution("server_wins");

        SurveyPoint point = new SurveyPoint();
        point.setId(1L);
        point.setLongitude(new BigDecimal("120.100000"));
        point.setLatitude(new BigDecimal("30.100000"));
        when(surveyPointMapper.selectById(1L)).thenReturn(point);
        when(locationCorrectionLogMapper.insert(any(LocationCorrectionLog.class))).thenReturn(1);

        doReturn(sync).when(service).getById(103L);

        Map<String, Object> outcome = service.syncData(103L);
        assertTrue((Boolean) outcome.get("success"));

        // 即使在 server_wins 下，仍应写纠偏日志保留审计轨迹
        verify(locationCorrectionLogMapper, times(1)).insert(any(LocationCorrectionLog.class));
        // 但不应更新点位坐标
        verify(surveyPointMapper, never()).updateById(any(SurveyPoint.class));
    }
}
