/**
 * API请求封装
 * 基于uni.request封装的请求工具
 */

// 基础配置
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
const TIMEOUT = 10000

/**
 * 获取Token
 */
function getToken() {
  try {
    return uni.getStorageSync('survey_token') || ''
  } catch (e) {
    return ''
  }
}

/**
 * 请求拦截器
 */
function requestInterceptor(config) {
  // 添加Token
  const token = getToken()
  if (token) {
    config.header = config.header || {}
    config.header['Authorization'] = `Bearer ${token}`
  }
  
  // 添加Content-Type
  if (!config.header['Content-Type']) {
    config.header['Content-Type'] = 'application/json'
  }
  
  return config
}

/**
 * 响应拦截器
 */
function responseInterceptor(response) {
  const { statusCode, data } = response
  
  // HTTP状态码检查
  if (statusCode === 401) {
    // Token过期或无效
    uni.removeStorageSync('survey_token')
    uni.removeStorageSync('survey_user_info')
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return Promise.reject(new Error('登录已过期，请重新登录'))
  }
  
  if (statusCode === 403) {
    return Promise.reject(new Error('没有权限访问'))
  }
  
  if (statusCode !== 200) {
    return Promise.reject(new Error(`请求失败: ${statusCode}`))
  }
  
  // 业务状态码检查
  if (data.code !== 200 && data.code !== 0) {
    return Promise.reject(new Error(data.message || '请求失败'))
  }
  
  return data.data !== undefined ? data.data : data
}

/**
 * 发起请求
 */
function request(options) {
  const config = requestInterceptor({
    url: `${BASE_URL}${options.url}`,
    method: options.method || 'GET',
    data: options.data || {},
    header: options.header || {},
    timeout: options.timeout || TIMEOUT
  })
  
  return new Promise((resolve, reject) => {
    uni.request({
      ...config,
      success: (response) => {
        try {
          const result = responseInterceptor(response)
          resolve(result)
        } catch (error) {
          reject(error)
        }
      },
      fail: (error) => {
        reject(new Error('网络请求失败: ' + (error.errMsg || '未知错误')))
      }
    })
  })
}

/**
 * GET请求
 */
export function get(url, params = {}) {
  const queryString = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  
  const fullUrl = queryString ? `${url}?${queryString}` : url
  
  return request({
    url: fullUrl,
    method: 'GET'
  })
}

/**
 * POST请求
 */
export function post(url, data = {}) {
  return request({
    url,
    method: 'POST',
    data
  })
}

/**
 * PUT请求
 */
export function put(url, data = {}) {
  return request({
    url,
    method: 'PUT',
    data
  })
}

/**
 * DELETE请求
 */
export function del(url, params = {}) {
  const queryString = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  
  const fullUrl = queryString ? `${url}?${queryString}` : url
  
  return request({
    url: fullUrl,
    method: 'DELETE'
  })
}

/**
 * 文件上传
 */
export function upload(url, filePath, formData = {}) {
  const token = getToken()
  
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${BASE_URL}${url}`,
      filePath,
      name: 'file',
      formData,
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (response) => {
        try {
          const data = JSON.parse(response.data)
          if (data.code === 200 || data.code === 0) {
            resolve(data.data)
          } else {
            reject(new Error(data.message || '上传失败'))
          }
        } catch (e) {
          reject(new Error('解析响应失败'))
        }
      },
      fail: (error) => {
        reject(new Error('上传失败: ' + (error.errMsg || '未知错误')))
      }
    })
  })
}

// ==================== API接口定义 ====================
//
// 后端Base: /api/v1, 各控制器路径：
//   /auth, /user, /project, /point, /result, /template, /audit,
//   /message, /location-correction, /dict, /dictionary, /dictionary-data
// 响应统一格式: { code: 200, message: 'success', data: {...} }
// 响应拦截器会提取 data 字段后返回。

// 认证相关
export const authApi = {
  // 账号密码登录
  login: (data) => post('/auth/login', data),
  // 短信验证码登录
  smsLogin: (data) => post('/auth/sms-login', data),
  // 发送短信验证码
  sendSmsCode: (data) => post('/auth/send-sms-code', data),
  // 修改密码
  changePassword: (data) => post('/auth/change-password', data),
  // 重置密码
  resetPassword: (data) => post('/auth/reset-password', data),
  // 刷新 Token
  refresh: (data) => post('/auth/refresh', data),
  // 退出登录
  logout: () => post('/auth/logout'),
  // 获取当前登录用户信息
  getUserInfo: () => get('/auth/getUserInfo'),
  // 获取图形验证码
  getCaptcha: () => get('/auth/captcha')
}

// 用户相关
export const userApi = {
  // 获取当前用户信息（后端由 AuthController 提供）
  getUserInfo: () => get('/auth/getUserInfo'),
  // 更新用户信息（复用后台管理接口，data 必须包含 id）
  updateUserInfo: (data) => put('/user/update', data)
  // TODO 后端未提供独立的用户档案接口 (/user/profile)。
  // 如需个人详情，调用 /auth/getUserInfo 后再调 GET /user/{id}
}

// 项目相关
export const projectApi = {
  // 获取项目列表（分页）
  getList: (params) => get('/project/page', params),
  // 获取项目详情
  getDetail: (id) => get(`/project/${id}`),
  // 获取项目统计信息
  getStatistics: (id) => get(`/project/${id}/statistics`)
  // TODO 后端未提供 /project/{id}/progress，进度信息包含于 statistics 响应中
}

// 点位相关
export const pointApi = {
  // 获取点位列表
  getList: (params) => get('/point/list', params),
  // 获取点位分页列表
  getPage: (params) => get('/point/page', params),
  // 获取点位详情
  getDetail: (id) => get(`/point/${id}`),
  // 获取指定状态的点位列表
  getByStatus: (status, params) => get(`/point/status/${status}`, params),
  // 获取点位历史记录
  getHistory: (id) => get(`/point/${id}/history`),
  // 获取我的点位（复用 /point/list 接口，后端根据登录用户过滤分配人）
  getMyPoints: (params) => get('/point/list', { ...(params || {}), assignee: 'me' }),
  // 获取点位地图数据（复用 /point/list，需后端返回经纬度字段）
  getMapData: (params) => get('/point/list', params)
  // TODO 后端未提供 /point/{id}/start（开始采集）接口：
  // 请使用 /api/v1/task/{id}/status 将任务状态置为进行中
}

// 采集结果相关
export const resultApi = {
  // 获取我的勘查结果列表（后端: GET /result/list 以 surveyUserId 过滤）
  getMyResults: (params) => get('/result/list', params),
  // 获取指定用户的采集结果
  getByUser: (surveyUserId, params) => get(`/result/user/${surveyUserId}`, params),
  // 获取采集结果详情
  getDetail: (id) => get(`/result/${id}`),
  // 获取点位最新的采集结果
  getLatestByPoint: (pointId) => get(`/result/point/${pointId}/latest`),
  // 保存草稿
  saveDraft: (data) => post('/result/draft', data),
  // 提交审核（必须提供 id）
  submit: (data) => post(`/result/submit/${data.id}`, data),
  // 创建采集结果
  create: (data) => post('/result/create', data),
  // 更新采集结果
  update: (id, data) => put(`/result/update/${id}`, data),
  // 版本差异对比
  getVersionDiff: (params) => get('/result/version/diff', params)
  // TODO 后端未提供 /result/versions/{pointId} 和 /result/reject-reason/{resultId}，
  // 驳回原因可从 /result/{id} 响应中的 auditRemark 字段读取。
}

// 采集结果别名（与 resultApi 保持同步）
export const surveyResultApi = {
  getDetail: (id) => get(`/result/${id}`),
  saveDraft: (data) => post('/result/draft', data),
  submit: (data) => post(`/result/submit/${data.id}`, data),
  getLatestByPoint: (pointId) => get(`/result/point/${pointId}/latest`)
}

// 模板相关
export const templateApi = {
  // 获取模板详情
  getDetail: (id) => get(`/template/detail/${id}`),
  // 根据项目/排口类型获取模板绑定关系
  getBinding: (params) => get('/template/binding', params),
  // 获取模板版本字段配置
  getVersionFields: (versionId) => get(`/template/version/${versionId}/fields`),
  // 获取模板列表
  getList: () => get('/template/list')
}

// 审核相关
export const auditApi = {
  // 待审核列表
  getPending: (params) => get('/audit/pending', params),
  // 审核记录列表（可按 pointId 、resultId 过滤）
  getList: (params) => get('/audit/records', params),
  // 审核详情
  getDetail: (resultId) => get(`/audit/detail/${resultId}`),
  // 版本差异对比
  getVersionDiff: (params) => get('/audit/version-diff', params),
  // 审核通过
  pass: (data) => post('/audit/pass', data),
  // 审核驳回
  reject: (data) => post('/audit/reject', data)
}

// 消息相关
export const messageApi = {
  // 获取消息分页列表
  getList: (params) => get('/message/page', params),
  // 获取未读数量
  getUnreadCount: () => get('/message/unread-count'),
  // 标记已读
  markRead: (id) => put(`/message/${id}/read`),
  // 全部标记已读
  markAllRead: () => put('/message/read-all'),
  // 获取消息详情
  getDetail: (id) => get(`/message/${id}`)
}

// 位置纠偏相关
export const locationApi = {
  // 记录纠偏日志
  saveCorrection: (data) => post('/location-correction', data),
  // 获取点位纠偏轨迹
  getCorrectionLog: (pointId) => get(`/location-correction/trajectory/${pointId}`)
}

// 字典相关
export const dictApi = {
  // 根据字典编码获取字典项 (后端 SysDictController)
  getData: (dictCode) => get(`/dict/code/${dictCode}/items`),
  // 备选路径：较早版 SysDictionaryDataController
  getDataLegacy: (dictCode) => get(`/dictionary-data/list/${dictCode}`)
}

// 文件上传
export const fileApi = {
  // 上传单个文件
  upload: (filePath, formData) => upload('/file/upload', filePath, formData),
  // 上传多个文件
  uploadMultiple: (filePath, formData) => upload('/file/upload/multiple', filePath, formData)
}

// 默认导出
export default {
  get,
  post,
  put,
  del,
  upload
}
