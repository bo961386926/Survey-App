import request from './request'

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

export function updateTemplate(data) {
  return request({
    url: '/template/update',
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
