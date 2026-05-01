/**
 * API请求封装
 * 基于uni.request封装的请求工具
 */

// 基础配置
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
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

// 认证相关
export const authApi = {
  // 账号密码登录
  login: (data) => post('/auth/login', data),
  // 短信验证码登录
  smsLogin: (data) => post('/auth/sms-login', data),
  // 发送短信验证码
  sendSmsCode: (data) => post('/auth/sms-code', data),
  // 修改密码
  changePassword: (data) => post('/auth/change-password', data),
  // 重置密码
  resetPassword: (data) => post('/auth/reset-password', data),
  // 退出登录
  logout: () => post('/auth/logout')
}

// 用户相关
export const userApi = {
  // 获取用户信息
  getUserInfo: () => get('/user/info'),
  // 更新用户信息
  updateUserInfo: (data) => put('/user/info', data),
  // 获取用户档案
  getProfile: () => get('/user/profile')
}

// 项目相关
export const projectApi = {
  // 获取项目列表
  getList: (params) => get('/project/list', params),
  // 获取项目详情
  getDetail: (id) => get(`/project/${id}`)
}

// 点位相关
export const pointApi = {
  // 获取点位列表
  getList: (params) => get('/survey-point/list', params),
  // 获取点位详情
  getDetail: (id) => get(`/survey-point/${id}`),
  // 获取我的点位
  getMyPoints: (params) => get('/survey-point/my', params),
  // 开始采集（认领任务）
  startCollect: (id) => post(`/survey-point/${id}/start`),
  // 获取点位地图数据
  getMapData: (params) => get('/survey-point/map', params)
}

// 采集结果相关
export const resultApi = {
  // 别名，兼容旧代码
}

// 审核结果相关（别名，与resultApi相同）
export const surveyResultApi = {
  // 获取采集结果
  getDetail: (id) => get(`/survey-result/${id}`),
  // 保存草稿
  saveDraft: (data) => post('/survey-result/draft', data),
  // 提交审核
  submit: (data) => post('/survey-result/submit', data),
  // 获取版本历史
  getVersionHistory: (pointId) => get(`/survey-result/versions/${pointId}`),
  // 获取驳回意见
  getRejectReason: (resultId) => get(`/survey-result/reject-reason/${resultId}`)
}

// 模板相关
export const templateApi = {
  // 获取模板详情
  getDetail: (id) => get(`/survey-template/${id}`),
  // 根据排口类型获取模板
  getByOutfallType: (outfallType) => get(`/survey-template/outfall/${outfallType}`)
}

// 审核相关
export const auditApi = {
  // 获取审核列表
  getList: (params) => get('/survey-audit/list', params),
  // 获取审核详情
  getDetail: (id) => get(`/survey-audit/${id}`),
  // 获取版本差异
  getVersionDiff: (resultId, compareId) => get(`/survey-audit/diff/${resultId}`, { compareId })
}

// 消息相关
export const messageApi = {
  // 获取消息列表
  getList: (params) => get('/message/list', params),
  // 获取未读数量
  getUnreadCount: () => get('/message/unread-count'),
  // 标记已读
  markRead: (id) => put(`/message/${id}/read`),
  // 全部标记已读
  markAllRead: () => put('/message/read-all')
}

// 位置纠偏相关
export const locationApi = {
  // 记录纠偏日志
  saveCorrection: (data) => post('/location-correction', data),
  // 获取纠偏轨迹
  getCorrectionLog: (pointId) => get(`/location-correction/${pointId}`)
}

// 字典相关
export const dictApi = {
  // 获取字典数据
  getData: (dictType) => get(`/sys-dictionary/data/${dictType}`)
}

// 默认导出
export default {
  get,
  post,
  put,
  del,
  upload
}
