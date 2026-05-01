/**
 * 我的页面
 * 用户信息、设置、退出登录
 */
<template>
  <view class="my-container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <image class="avatar" :src="userInfo?.avatar || '/static/default-avatar.png'" mode="aspectFill" />
      <view class="user-info">
        <text class="username">{{ userInfo?.name || '未登录' }}</text>
        <text class="user-role">{{ userInfo?.roleName || '-' }}</text>
        <text class="user-phone">{{ userInfo?.phone || '-' }}</text>
      </view>
    </view>
    
    <!-- 功能列表 -->
    <view class="menu-section">
      <view class="menu-item" @click="goToProfile">
        <view class="menu-left">
          <text class="menu-icon">👤</text>
          <text class="menu-text">用户档案</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToChangePassword">
        <view class="menu-left">
          <text class="menu-icon">🔒</text>
          <text class="menu-text">修改密码</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
    </view>
    
    <view class="menu-section">
      <view class="menu-item" @click="goToDraftList">
        <view class="menu-left">
          <text class="menu-icon">💾</text>
          <text class="menu-text">草稿管理</text>
        </view>
        <view class="menu-right">
          <text v-if="draftCount > 0" class="badge">{{ draftCount }}</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
      <view class="menu-item" @click="goToMessageList">
        <view class="menu-left">
          <text class="menu-icon">🔔</text>
          <text class="menu-text">消息中心</text>
        </view>
        <view class="menu-right">
          <text v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>
    
    <view class="menu-section">
      <view class="menu-item" @click="goToAbout">
        <view class="menu-left">
          <text class="menu-icon">ℹ️</text>
          <text class="menu-text">关于系统</text>
        </view>
        <view class="menu-right">
          <text class="menu-value">v1.0.0</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>
    
    <!-- 退出登录 -->
    <view class="logout-section">
      <button class="btn-logout" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserInfo, clearToken, clearUserInfo } from '@/utils/auth'
import { messageApi } from '@/utils/api'
import { getDraftList } from '@/utils/draft'

const userInfo = ref(null)
const unreadCount = ref(0)
const draftCount = ref(0)

onMounted(() => {
  loadUserInfo()
  loadUnreadCount()
  loadDraftCount()
})

// 加载用户信息
function loadUserInfo() {
  userInfo.value = getUserInfo()
}

// 加载未读消息数
async function loadUnreadCount() {
  try {
    const result = await messageApi.getUnreadCount()
    unreadCount.value = result.count || 0
  } catch (error) {
    console.error('加载消息数失败:', error)
  }
}

// 加载草稿数量
function loadDraftCount() {
  try {
    const drafts = getDraftList()
    draftCount.value = drafts.length
  } catch (error) {
    console.error('加载草稿数失败:', error)
  }
}

// 跳转到用户档案
function goToProfile() {
  uni.navigateTo({ url: '/pages/my/profile' })
}

// 跳转到修改密码
function goToChangePassword() {
  uni.navigateTo({ url: '/pages/my/change-password' })
}

// 跳转到草稿管理
function goToDraftList() {
  uni.navigateTo({ url: '/pages/draft/draft-list' })
}

// 跳转到消息中心
function goToMessageList() {
  uni.navigateTo({ url: '/pages/message/message-list' })
}

// 跳转到关于系统
function goToAbout() {
  uni.showModal({
    title: '关于系统',
    content: '青泓项目勘察系统 移动采集端 v1.0.0',
    showCancel: false
  })
}

// 退出登录
function handleLogout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        clearToken()
        clearUserInfo()
        uni.reLaunch({ url: '/pages/login/login' })
      }
    }
  })
}
</script>

<style scoped>
.my-container {
  min-height: 100vh;
  background-color: #F5F7FA;
}

.user-card {
  display: flex;
  align-items: center;
  padding: 30px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  margin-right: 16px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 18px;
  font-weight: 500;
  color: #fff;
}

.user-role {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4px;
}

.user-phone {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 2px;
}

.menu-section {
  background-color: #fff;
  margin-top: 12px;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-left {
  display: flex;
  align-items: center;
}

.menu-icon {
  font-size: 20px;
  margin-right: 12px;
}

.menu-text {
  font-size: 15px;
  color: #333;
}

.menu-right {
  display: flex;
  align-items: center;
}

.menu-value {
  font-size: 13px;
  color: #999;
  margin-right: 8px;
}

.menu-arrow {
  font-size: 20px;
  color: #ccc;
}

.badge {
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  background-color: #F56C6C;
  color: #fff;
  font-size: 10px;
  border-radius: 9px;
  padding: 0 4px;
  margin-right: 8px;
}

.logout-section {
  padding: 30px 20px;
}

.btn-logout {
  width: 100%;
  height: 44px;
  line-height: 44px;
  background-color: #fff;
  color: #F56C6C;
  border: 1px solid #F56C6C;
  border-radius: 8px;
  font-size: 16px;
}
</style>
