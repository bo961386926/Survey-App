import { request } from '../service/request';

/**
 * 用户管理 API
 * 后端控制器: SysUserController, 基础路径 /api/v1/user
 *
 * 备注：以下后端可用但前端尚未封装的接口（如需调用可补充）：
 *   - GET    /user/export        导出用户
 *   - POST   /user/import        导入用户
 */

/**
 * 分页查询用户列表
 * GET /api/v1/user/page
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
 * GET /api/v1/user/{id}
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
 * POST /api/v1/user/create
 * @param data 用户数据
 */
export function fetchCreateUser(data) {
  return request({
    url: '/user/create',
    method: 'post',
    data
  });
}

/**
 * 更新用户
 * PUT /api/v1/user/update
 * @param data 用户数据（包含 id）
 */
export function fetchUpdateUser(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  });
}

/**
 * 删除用户
 * DELETE /api/v1/user/delete/{id}
 * @param id 用户ID
 */
export function fetchDeleteUser(id) {
  return request({
    url: `/user/delete/${id}`,
    method: 'delete'
  });
}

/**
 * 重置用户密码
 * PUT /api/v1/user/reset-password/{id}
 * @param id 用户ID
 * @param newPassword 新密码
 */
export function fetchResetPassword(id, newPassword) {
  return request({
    url: `/user/reset-password/${id}`,
    method: 'put',
    data: { newPassword }
  });
}

/**
 * 更新用户状态
 * PUT /api/v1/user/status/{id}
 * @param id 用户ID
 * @param status 状态
 */
export function fetchUpdateUserStatus(id, status) {
  return request({
    url: `/user/status/${id}`,
    method: 'put',
    params: { status }
  });
}
