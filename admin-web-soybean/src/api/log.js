import request from './request';

/**
 * 操作日志API
 */

// 分页查询操作日志
export function getOperationLogPage(params) {
  return request({
    url: '/api/v1/log/operation/page',
    method: 'get',
    params
  });
}

// 导出操作日志
export function exportOperationLogs(params) {
  return request({
    url: '/api/v1/log/operation/export',
    method: 'get',
    params,
    responseType: 'blob'
  });
}

// 统计各模块操作次数
export function countByModule() {
  return request({
    url: '/api/v1/log/operation/statistics/module',
    method: 'get'
  });
}

// 统计各用户操作次数
export function countByUser() {
  return request({
    url: '/api/v1/log/operation/statistics/user',
    method: 'get'
  });
}

// 统计各风险等级操作次数
export function countByRiskLevel() {
  return request({
    url: '/api/v1/log/operation/statistics/risk-level',
    method: 'get'
  });
}

// 按时间范围统计操作趋势
export function countByDateRange(params) {
  return request({
    url: '/api/v1/log/operation/statistics/trend',
    method: 'get',
    params
  });
}
