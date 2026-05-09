import { request } from '../service/request';

/**
 * 用户管理API
 */

/**
 * 分页查询用户列表
 * @param params 查询参数
 */
export function fetchGetUserList(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params
  });
}

/**
 * 根据ID获取用户详情
 * @param id 用户ID
 */
export function fetchGetUserDetailById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  });
}

/**
 * 创建用户
 * @param data 用户数据
 */
export function fetchCreateUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  });
}

/**
 * 更新用户
 * @param data 用户数据
 */
export function fetchUpdateUser(data) {
  return request({
    url: '/user',
    method: 'put',
    data
  });
}

/**
 * 删除用户
 * @param id 用户ID
 */
export function fetchDeleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  });
}

/**
 * 重置用户密码
 * @param id 用户ID
 * @param newPassword 新密码
 */
export function fetchResetPassword(id, newPassword) {
  return request({
    url: '/user/reset-password',
    method: 'post',
    data: { id, newPassword }
  });
}

/**
 * 更新用户状态
 * @param id 用户ID
 * @param status 状态
 */
export function fetchUpdateUserStatus(id, status) {
  return request({
    url: '/user/status',
    method: 'post',
    data: { id, status }
  });
}
