import request from './request'

/**
 * 勘察模板 API（迁移版）
 * 后端控制器: SurveyTemplateController, 基础路径 /api/v1/template
 *
 * 备注：以下后端可用接口前端尚未封装：
 *   - GET    /template/page                 模板分页查询
 *   - PUT    /template/draft/{id}            保存模板草稿
 *   - POST   /template/{id}/publish          发布模板
 *   - GET    /template/{id}/preview          模板预览
 *   - GET    /template/{id}/versions         模板版本列表
 *   - GET    /template/version/{versionId}   版本详情
 *   - GET    /template/version/{versionId}/fields 版本字段配置
 *   - POST   /template/bind-outfall          绑定排口类型
 *   - GET    /template/binding               查询绑定关系
 *   - GET    /template/bindings              查询所有绑定关系
 *   - DELETE /template/binding/{bindingId}   解绑
 */

export function getTemplateList() {
  return request({
    url: '/template/list',
    method: 'get'
  })
}

export function getTemplateDetail(id) {
  return request({
    url: `/template/detail/${id}`,
    method: 'get'
  })
}

export function createTemplate(data) {
  return request({
    url: '/template/create',
    method: 'post',
    data
  })
}

/**
 * 更新模板
 * PUT /api/v1/template/update/{id}
 * @param data 包含 id
 */
export function updateTemplate(data) {
  return request({
    url: `/template/update/${data.id}`,
    method: 'put',
    data
  })
}

export function deleteTemplate(id) {
  return request({
    url: `/template/delete/${id}`,
    method: 'delete'
  })
}
