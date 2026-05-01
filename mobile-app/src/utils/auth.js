/**
 * 认证工具类
 * 处理Token存储、用户信息、登录状态检查
 */

const TOKEN_KEY = 'survey_token'
const USER_INFO_KEY = 'survey_user_info'
const LOGIN_TYPE_KEY = 'survey_login_type' // internal/collab

/**
 * 保存登录Token
 */
export function saveToken(token) {
  try {
    uni.setStorageSync(TOKEN_KEY, token)
  } catch (e) {
    console.error('保存Token失败:', e)
  }
}

/**
 * 获取Token
 */
export function getToken() {
  try {
    return uni.getStorageSync(TOKEN_KEY) || ''
  } catch (e) {
    console.error('获取Token失败:', e)
    return ''
  }
}

/**
 * 清除Token
 */
export function clearToken() {
  try {
    uni.removeStorageSync(TOKEN_KEY)
  } catch (e) {
    console.error('清除Token失败:', e)
  }
}

/**
 * 保存用户信息
 */
export function saveUserInfo(userInfo) {
  try {
    uni.setStorageSync(USER_INFO_KEY, JSON.stringify(userInfo))
  } catch (e) {
    console.error('保存用户信息失败:', e)
  }
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  try {
    const userInfo = uni.getStorageSync(USER_INFO_KEY)
    return userInfo ? JSON.parse(userInfo) : null
  } catch (e) {
    console.error('获取用户信息失败:', e)
    return null
  }
}

/**
 * 清除用户信息
 */
export function clearUserInfo() {
  try {
    uni.removeStorageSync(USER_INFO_KEY)
  } catch (e) {
    console.error('清除用户信息失败:', e)
  }
}

/**
 * 保存登录类型
 */
export function saveLoginType(loginType) {
  try {
    uni.setStorageSync(LOGIN_TYPE_KEY, loginType)
  } catch (e) {
    console.error('保存登录类型失败:', e)
  }
}

/**
 * 获取登录类型
 */
export function getLoginType() {
  try {
    return uni.getStorageSync(LOGIN_TYPE_KEY) || 'internal'
  } catch (e) {
    return 'internal'
  }
}

/**
 * 检查是否已登录
 */
export function isLogin() {
  return !!getToken()
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  clearToken()
  clearUserInfo()
  uni.removeStorageSync(LOGIN_TYPE_KEY)
}

/**
 * 检查用户角色
 */
export function hasRole(role) {
  const userInfo = getUserInfo()
  if (!userInfo || !userInfo.roles) return false
  return userInfo.roles.includes(role)
}

/**
 * 检查是否是采集员
 */
export function isCollector() {
  return hasRole('collector') || hasRole('COLLECTOR')
}

/**
 * 检查是否是审核员
 */
export function isAuditor() {
  return hasRole('auditor') || hasRole('AUDITOR')
}

/**
 * 检查是否是第三方协作人员
 */
export function isCollaborator() {
  return getLoginType() === 'collab'
}

/**
 * 需要登录的页面
 */
const AUTH_PAGES = [
  'pages/home/home',
  'pages/point-list/point-list',
  'pages/point-detail/point-detail',
  'pages/point-map/point-map',
  'pages/survey/survey',
  'pages/draft/draft-list',
  'pages/audit/audit-list',
  'pages/audit/audit-detail',
  'pages/message/message-list',
  'pages/message/message-detail',
  'pages/my/my',
  'pages/my/change-password',
  'pages/my/profile'
]

/**
 * 检查当前页面是否需要登录
 */
export function needAuth(pagePath) {
  return AUTH_PAGES.includes(pagePath)
}

/**
 * 登录拦截器
 */
export function checkAuth(redirect) {
  if (!isLogin()) {
    uni.redirectTo({
      url: `/pages/login/login?redirect=${encodeURIComponent(redirect || '')}`
    })
    return false
  }
  return true
}
