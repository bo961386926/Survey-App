import request from './request'

export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export function getProjectList() {
  return request({
    url: '/project/list',
    method: 'get'
  })
}

export function createProject(data) {
  return request({
    url: '/project/create',
    method: 'post',
    data
  })
}

export function updateProject(data) {
  return request({
    url: '/project/update',
    method: 'put',
    data
  })
}

export function deleteProject(id) {
  return request({
    url: `/project/delete/${id}`,
    method: 'delete'
  })
}
