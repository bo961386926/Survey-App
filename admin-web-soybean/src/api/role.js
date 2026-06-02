import { request } from '../service/request';

/**
 * 角色管理 API
 * 后端控制器: SysRoleController, 基础路径 /api/v1/role
 *
 * 备注：以下后端可用但前端尚未封装的接口（如需调用可补充）：
 *   - GET    /role/list                    获取所有启用角色
 *   - PUT    /role/{id}/status             启用/停用角色
 *   - POST   /role/assign                  给用户分配角色
 *   - GET    /role/{id}/permissions        获取角色权限
 *   - PUT    /role/{id}/permissions        更新角色权限
 *   - GET    /role/user/{userId}           获取用户已分配角色
 */

/**
 * 分页查询角色列表
 * GET /api/v1/role/page
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
 * GET /api/v1/role/{id}
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
 * POST /api/v1/role
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
 * PUT /api/v1/role/{id}
 * @param data 角色数据（必须包含 id）
 */
export function fetchUpdateRole(data) {
  return request({
    url: `/role/${data.id}`,
    method: 'put',
    data
  });
}

/**
 * 删除角色
 * DELETE /api/v1/role/{id}
 * @param id 角色ID
 */
export function fetchDeleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  });
}
