import request from './request'

export function getPointList(params) {
  return request({
    url: '/point/list',
    method: 'get',
    params
  })
}

export function getPointPage(params) {
  return request({
    url: '/point/page',
    method: 'get',
    params
  })
}

export function getPointById(id) {
  return request({
    url: `/point/${id}`,
    method: 'get'
  })
}

export function createPoint(data) {
  return request({
    url: '/point/create',
    method: 'post',
    data
  })
}

export function updatePoint(id, data) {
  return request({
    url: `/point/update/${id}`,
    method: 'put',
    data
  })
}

export function deletePoint(id) {
  return request({
    url: `/point/delete/${id}`,
    method: 'delete'
  })
}
