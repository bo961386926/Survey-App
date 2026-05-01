package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.OperationLog;

/**
 * 操作日志服务接口
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 分页查询操作日志
     */
    Page<OperationLog> listByPage(String module, String operator, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 记录操作日志
     */
    void logOperation(Long userId, String module, String operation, String description, String ip, String userAgent);

    /**
     * 导出操作日志为Excel字节数组
     * @param module 模块
     * @param operator 操作人
     * @param keyword 关键词
     * @return Excel字节数组
     */
    byte[] exportLogs(String module, String operator, String keyword);
}
