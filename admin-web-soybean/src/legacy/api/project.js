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

export function getProjectPage(params) {
  return request({
    url: '/project/page',
    method: 'get',
    params
  })
}

export function createProject(data) {
  return request({
    url: '/project/create',
    method: 'post',
    data
  })
}

export function createProjectV2(data) {
  return request({
    url: '/project',
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

export function updateProjectV2(id, data) {
  return request({
    url: `/project/${id}`,
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

export function deleteProjectV2(id) {
  return request({
    url: `/project/${id}`,
    method: 'delete'
  })
}
