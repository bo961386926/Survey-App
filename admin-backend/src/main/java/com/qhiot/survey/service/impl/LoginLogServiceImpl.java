package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.common.util.ExcelUtil;
import com.qhiot.survey.entity.LoginLog;
import com.qhiot.survey.mapper.LoginLogMapper;
import com.qhiot.survey.service.LoginLogService;
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
 * 登录日志服务实现类
 */
@Slf4j
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    @Override
    public Page<LoginLog> listByPage(String username, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(LoginLog::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(LoginLog::getStatus, status);
        }
        wrapper.orderByDesc(LoginLog::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void logLogin(Long userId, String username, String loginType, Integer status, 
                         String failReason, String ip, String userAgent) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setLoginType(loginType);
        log.setStatus(status);
        log.setFailReason(failReason);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }

    @Override
    public byte[] exportLogs(String username, Integer status) {
        try {
            // 查询日志数据
            LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(username)) {
                wrapper.like(LoginLog::getUsername, username);
            }
            if (status != null) {
                wrapper.eq(LoginLog::getStatus, status);
            }
            wrapper.orderByDesc(LoginLog::getCreateTime);
            // 限制最多导出10000条
            wrapper.last("LIMIT 10000");
            
            List<LoginLog> logs = list(wrapper);
            
            // 构建导出数据
            String[] headers = {"时间", "用户名", "登录类型", "状态", "失败原因", "IP地址"};
            List<Map<String, Object>> dataList = new ArrayList<>();
            
            for (LoginLog loginLog : logs) {
                Map<String, Object> row = new HashMap<>();
                row.put("时间", loginLog.getCreateTime() != null ? loginLog.getCreateTime().toString() : "");
                row.put("用户名", loginLog.getUsername());
                row.put("登录类型", "internal".equals(loginLog.getLoginType()) ? "内部登录" : "协作登录");
                row.put("状态", loginLog.getStatus() == 0 ? "成功" : "失败");
                row.put("失败原因", loginLog.getFailReason());
                row.put("IP地址", loginLog.getIp());
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
            log.error("导出登录日志失败", e);
            throw new RuntimeException("导出登录日志失败: " + e.getMessage());
        }
    }
}
