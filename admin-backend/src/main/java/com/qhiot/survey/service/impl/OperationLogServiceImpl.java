package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.util.ExcelUtil;
import com.qhiot.survey.entity.OperationLog;
import com.qhiot.survey.mapper.OperationLogMapper;
import com.qhiot.survey.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务实现类
 */
@Slf4j
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public Page<OperationLog> listByPage(String module, String operator, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (StringUtils.hasText(operator)) {
            wrapper.like(OperationLog::getUsername, operator);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(OperationLog::getAction, keyword)
                    .or().like(OperationLog::getDetail, keyword));
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void logOperation(Long userId, String module, String operation, String description, String ip, String userAgent) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setModule(module);
        log.setAction(operation);
        log.setDetail(description);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }

    @Override
    public byte[] exportLogs(String module, String operator, String keyword) {
        try {
            // 查询日志数据
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(module)) {
                wrapper.eq(OperationLog::getModule, module);
            }
            if (StringUtils.hasText(operator)) {
                wrapper.like(OperationLog::getUsername, operator);
            }
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(OperationLog::getAction, keyword)
                        .or().like(OperationLog::getDetail, keyword));
            }
            wrapper.orderByDesc(OperationLog::getCreateTime);
            // 限制最多导出10000条
            wrapper.last("LIMIT 10000");
            
            List<OperationLog> logs = list(wrapper);
            
            // 构建导出数据
            String[] headers = {"时间", "操作人", "模块", "操作", "详情", "IP", "风险等级"};
            List<Map<String, Object>> dataList = new ArrayList<>();
            
            for (OperationLog log : logs) {
                Map<String, Object> row = new HashMap<>();
                row.put("时间", log.getCreateTime() != null ? log.getCreateTime().toString() : "");
                row.put("操作人", log.getUsername());
                row.put("模块", log.getModule());
                row.put("操作", log.getAction());
                row.put("详情", log.getDetail());
                row.put("IP", log.getIp());
                row.put("风险等级", getRiskLevelName(log.getRiskLevel()));
                dataList.add(row);
            }
            
            // 生成Excel
            org.apache.poi.ss.usermodel.Workbook workbook = ExcelUtil.createExcel(headers, dataList);
            
            // 转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("导出操作日志失败", e);
            throw new RuntimeException("导出操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取风险等级名称
     */
    private String getRiskLevelName(Integer riskLevel) {
        if (riskLevel == null) {
            return "未知";
        }
        switch (riskLevel) {
            case 0: return "低";
            case 1: return "中";
            case 2: return "高";
            default: return "未知";
        }
    }
}
