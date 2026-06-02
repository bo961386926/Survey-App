import request from './request'

/**
 * 项目管理 API（迁移版）
 * 后端控制器: ProjectController, 基础路径 /api/v1/project
 *
 * 说明：legacy 时期使用的 /list, /create, /update, /delete/{id} 风格
 * 后端已统一为 RESTful 风格(POST /, PUT /{id}, DELETE /{id})。
 * 这里保留旧的导出函数以避免破坏既有视图，URL 已对齐到后端。
 *
 * 备注：以下后端可用接口前端尚未封装：
 *   - PUT  /project/{id}/status     更新项目状态
 *   - GET  /project/{id}/statistics 项目统计
 *   - PUT  /project/{id}/archive    归档项目
 *   - PUT  /project/{id}/restore    恢复项目
 */

/**
 * @deprecated 后端无独立 /user/login，请使用 authApi -> POST /auth/login
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 获取项目列表（兼容旧调用，内部走分页接口）
 * 后端: GET /api/v1/project/page
 * 返回: 适配为数组形式，便于旧代码 res.data.length 这类访问
 */
export async function getProjectList() {
  const res = await request({
    url: '/project/page',
    method: 'get',
    params: { page: 1, size: 1000 }
  })
  // 适配旧调用：将 Page<Project>.records 暴露为 data
  if (res && typeof res === 'object') {
    const page = res.data || {}
    res.data = page.records || page.list || []
  }
  return res
}

/**
 * 项目分页查询
 * GET /api/v1/project/page
 */
export function getProjectPage(params) {
  return request({
    url: '/project/page',
    method: 'get',
    params
  })
}

/**
 * 创建项目
 * POST /api/v1/project
 */
export function createProject(data) {
  return request({
    url: '/project',
    method: 'post',
    data
  })
}

/**
 * 创建项目（V2 别名，与 createProject 一致）
 */
export function createProjectV2(data) {
  return request({
    url: '/project',
    method: 'post',
    data
  })
}

/**
 * 更新项目
 * PUT /api/v1/project/{id}
 * @param data 包含 id 字段
 */
export function updateProject(data) {
  return request({
    url: `/project/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 更新项目（V2，使用显式 id 参数）
 * PUT /api/v1/project/{id}
 */
export function updateProjectV2(id, data) {
  return request({
    url: `/project/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除项目
 * DELETE /api/v1/project/{id}
 */
export function deleteProject(id) {
  return request({
    url: `/project/${id}`,
    method: 'delete'
  })
}

/**
 * 删除项目（V2 别名，与 deleteProject 一致）
 */
export function deleteProjectV2(id) {
  return request({
    url: `/project/${id}`,
    method: 'delete'
  })
}
