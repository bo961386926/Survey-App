package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.OperationLog;

import java.util.Map;

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
    void logOperation(Long userId, String username, String module, String operation, String description, 
                     String ip, String userAgent, Integer riskLevel);

    /**
     * 导出操作日志为Excel字节数组
     * @param module 模块
     * @param operator 操作人
     * @param keyword 关键词
     * @return Excel字节数组
     */
    byte[] exportLogs(String module, String operator, String keyword);

    /**
     * 统计各模块操作次数
     * @return Map<模块名称, 操作次数>
     */
    Map<String, Long> countByModule();

    /**
     * 统计各用户操作次数
     * @return Map<用户名, 操作次数>
     */
    Map<String, Long> countByUser();

    /**
     * 统计各风险等级操作次数
     * @return Map<风险等级, 操作次数>
     */
    Map<Integer, Long> countByRiskLevel();

    /**
     * 按时间范围统计操作趋势
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Map<日期, 操作次数>
     */
    Map<String, Long> countByDateRange(String startDate, String endDate);
}
