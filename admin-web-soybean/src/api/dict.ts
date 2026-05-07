import { request } from '@/service/request';

/**
 * 数据字典管理 API
 */

/**
 * 分页查询字典列表
 */
export function fetchGetDictList(params: any) {
  return request({
    url: '/api/v1/dict/page',
    method: 'get',
    params
  });
}

/**
 * 获取所有启用的字典
 */
export function fetchGetEnabledDicts() {
  return request({
    url: '/api/v1/dict/enabled',
    method: 'get'
  });
}

/**
 * 获取字典详情
 */
export function fetchGetDictDetail(id: number | string) {
  return request({
    url: `/api/v1/dict/${id}`,
    method: 'get'
  });
}

/**
 * 创建字典
 */
export function fetchCreateDict(data: any) {
  return request({
    url: '/api/v1/dict',
    method: 'post',
    data
  });
}

/**
 * 更新字典
 */
export function fetchUpdateDict(id: number | string, data: any) {
  return request({
    url: `/api/v1/dict/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除字典
 */
export function fetchDeleteDict(id: number | string) {
  return request({
    url: `/api/v1/dict/${id}`,
    method: 'delete'
  });
}

/**
 * 获取字典项列表
 */
export function fetchGetDictItems(dictId: number | string) {
  return request({
    url: `/api/v1/dict/${dictId}/items`,
    method: 'get'
  });
}

/**
 * 根据字典编码获取字典项
 */
export function fetchGetDictItemsByCode(dictCode: string) {
  return request({
    url: `/api/v1/dict/code/${dictCode}/items`,
    method: 'get'
  });
}

/**
 * 批量保存字典项
 */
export function fetchBatchSaveDictItems(dictId: number | string, items: any[]) {
  return request({
    url: `/api/v1/dict/${dictId}/items/batch`,
    method: 'post',
    data: items
  });
}
