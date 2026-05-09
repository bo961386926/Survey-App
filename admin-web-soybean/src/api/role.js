import { request } from '../service/request';

/**
 * 角色管理API
 */

/**
 * 分页查询角色列表
 * @param params 查询参数
 */
export function fetchGetRoleList(params) {
  return request({
    url: '/role/page',
    method: 'get',
    params
  });
}

/**
 * 根据ID获取角色详情
 * @param id 角色ID
 */
export function fetchGetRoleDetailById(id) {
  return request({
    url: `/role/${id}`,
    method: 'get'
  });
}

/**
 * 创建角色
 * @param data 角色数据
 */
export function fetchCreateRole(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  });
}

/**
 * 更新角色
 * @param data 角色数据
 */
export function fetchUpdateRole(data) {
  return request({
    url: '/role',
    method: 'put',
    data
  });
}

/**
 * 删除角色
 * @param id 角色ID
 */
export function fetchDeleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  });
}
