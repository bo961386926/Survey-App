/**
 * 状态管理
 * 使用uni-app的响应式API实现简单的状态管理
 */

import { reactive, readonly } from 'vue'
import { getToken, saveToken, clearToken, getUserInfo, saveUserInfo, clearUserInfo } from '@/utils/auth'

// 状态
const state = reactive({
  // 用户信息
  user: getUserInfo() || null,
  // Token
  token: getToken() || '',
  // 是否已登录
  isLoggedIn: !!getToken(),
  // 未读消息数量
  unreadMessageCount: 0,
  // 草稿数量
  draftCount: 0,
  // 加载状态
  loading: false,
  // 网络状态
  isOnline: true
})

// Actions
const actions = {
  // 设置Token
  setToken(token) {
    saveToken(token)
    state.token = token
    state.isLoggedIn = !!token
  },
  
  // 设置用户信息
  setUser(user) {
    saveUserInfo(user)
    state.user = user
  },
  
  // 登录成功
  loginSuccess(token, user) {
    this.setToken(token)
    this.setUser(user)
  },
  
  // 退出登录
  logout() {
    clearToken()
    clearUserInfo()
    state.token = ''
    state.user = null
    state.isLoggedIn = false
    state.unreadMessageCount = 0
  },
  
  // 设置未读消息数量
  setUnreadMessageCount(count) {
    state.unreadMessageCount = count
  },
  
  // 设置草稿数量
  setDraftCount(count) {
    state.draftCount = count
  },
  
  // 设置加载状态
  setLoading(loading) {
    state.loading = loading
  },
  
  // 设置网络状态
  setOnlineStatus(isOnline) {
    state.isOnline = isOnline
  }
}

// 导出
export const useStore = () => {
  return {
    state: readonly(state),
    ...actions
  }
}

export default {
  state,
  actions
}
