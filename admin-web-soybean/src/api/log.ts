import { request } from '@/service/request';

/**
 * 操作日志API
 */

/**
 * 分页查询操作日志
 * @param params 查询参数
 */
export function fetchGetOperationLogPage(params: any) {
  return request({
    url: '/log/operation/page',
    method: 'get',
    params
  });
}

/**
 * 导出操作日志
 * @param params 查询参数
 */
export function fetchExportOperationLogs(params: any) {
  return request({
    url: '/log/operation/export',
    method: 'get',
    params,
    responseType: 'blob'
  });
}

/**
 * 统计各模块操作次数
 */
export function fetchCountByModule() {
  return request({
    url: '/log/operation/statistics/module',
    method: 'get'
  });
}

/**
 * 统计各用户操作次数
 */
export function fetchCountByUser() {
  return request({
    url: '/log/operation/statistics/user',
    method: 'get'
  });
}

/**
 * 统计各风险等级操作次数
 */
export function fetchCountByRiskLevel() {
  return request({
    url: '/log/operation/statistics/risk-level',
    method: 'get'
  });
}

/**
 * 按时间范围统计操作趋势
 * @param params 时间范围参数
 */
export function fetchCountByDateRange(params: any) {
  return request({
    url: '/log/operation/statistics/trend',
    method: 'get',
    params
  });
}
