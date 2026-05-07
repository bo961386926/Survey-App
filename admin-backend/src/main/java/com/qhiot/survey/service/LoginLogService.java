package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.LoginLog;

/**
 * 登录日志服务接口
 */
public interface LoginLogService extends IService<LoginLog> {

    /**
     * 分页查询登录日志
     */
    Page<LoginLog> listByPage(String username, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 记录登录日志
     */
    void logLogin(Long userId, String username, String loginType, Integer status, 
                  String failReason, String ip, String userAgent);

    /**
     * 导出登录日志为Excel字节数组
     */
    byte[] exportLogs(String username, Integer status);
}
