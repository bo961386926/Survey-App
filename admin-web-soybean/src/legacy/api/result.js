import request from './request'

export function getResultById(id) {
  return request({
    url: `/result/${id}`,
    method: 'get'
  })
}

export function passAudit(id, auditRemark) {
  return request({
    url: `/result/audit/${id}/pass`,
    method: 'post',
    params: { auditRemark }
  })
}

export function rejectAudit(id, auditRemark) {
  return request({
    url: `/result/audit/${id}/reject`,
    method: 'post',
    params: { auditRemark }
  })
}

export function getResultList(params) {
  return request({
    url: '/result/list',
    method: 'get',
    params
  })
}

export function submitForAudit(id) {
  return request({
    url: `/result/submit/${id}`,
    method: 'post'
  })
}
