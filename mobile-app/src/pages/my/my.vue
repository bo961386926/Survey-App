/**
 * 我的页面
 * 用户信息、设置、退出登录
 */
<template>
  <view class="my-container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-header">
        <view class="avatar">{{ (userInfo?.name || '吴')[0] }}</view>
        <view class="user-info">
          <text class="username">{{ userInfo?.name || '未登录' }}</text>
          <text class="user-email">{{ userInfo?.email || '-' }}</text>
          <view class="role-tag">
            <text class="icon">🛡️</text>
            <text class="role">{{ userInfo?.roleName || '-' }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 账号信息 -->
    <view class="section">
      <view class="section-title">账号信息</view>
      <view class="section-content">
        <view class="info-item">
          <view class="item-left">
            <view class="item-icon" style="background: #ECFDF5;">
              <text class="icon"></text>
            </view>
            <text class="item-label">姓名</text>
          </view>
          <text class="item-value">{{ userInfo?.name || '-' }}</text>
        </view>
        <view class="divider"></view>
        <view class="info-item">
          <view class="item-left">
            <view class="item-icon" style="background: #ECFDF5;">
              <text class="icon"></text>
            </view>
            <text class="item-label">邮箱</text>
          </view>
          <text class="item-value">{{ userInfo?.email || '-' }}</text>
        </view>
        <view class="divider"></view>
        <view class="info-item">
          <view class="item-left">
            <view class="item-icon" style="background: #ECFDF5;">
              <text class="icon"></text>
            </view>
            <text class="item-label">角色</text>
          </view>
          <text class="item-value role-text">{{ userInfo?.roleName || '-' }}</text>
        </view>
      </view>
    </view>
    
    <!-- 安全设置 -->
    <view class="section">
      <view class="section-title">安全设置</view>
      <view class="section-content">
        <view class="menu-item" @click="goToChangePassword">
          <view class="item-left">
            <view class="item-icon" style="background: #ECFDF5;">
              <text class="icon">🔒</text>
            </view>
            <text class="item-label">修改密码</text>
          </view>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>
    
    <!-- 关于 -->
    <view class="section">
      <view class="section-title">关于</view>
      <view class="section-content">
        <view class="info-item">
          <view class="item-left">
            <view class="item-icon" style="background: #ECFDF5;">
              <text class="icon">ℹ️</text>
            </view>
            <text class="item-label">版本</text>
          </view>
          <text class="item-value">v1.0.0</text>
        </view>
        <view class="divider"></view>
        <view class="info-item">
          <view class="item-left">
            <view class="item-icon" style="background: #ECFDF5;">
              <text class="icon">🏢</text>
            </view>
            <text class="item-label">系统名称</text>
          </view>
          <text class="item-value">青泓勘察系统</text>
        </view>
      </view>
    </view>
    
    <!-- 退出登录 -->
    <view class="logout-section">
      <button class="btn-logout" @click="handleLogout">
        <text class="icon">🚪</text>
        <text class="text">退出登录</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserInfo, clearToken, clearUserInfo } from '@/utils/auth'

const userInfo = ref(null)

onMounted(() => {
  loadUserInfo()
})

// 加载用户信息
function loadUserInfo() {
  userInfo.value = getUserInfo()
}

// 跳转到修改密码
function goToChangePassword() {
  uni.navigateTo({ url: '/pages/my/change-password' })
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
  background-color: #F8FAFC;
}

.user-card {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  padding: 30px 20px 40px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #FFFFFF;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.username {
  font-size: 20px;
  font-weight: 700;
  color: #FFFFFF;
}

.user-email {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.role-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  width: fit-content;
}

.role-tag .icon {
  font-size: 14px;
}

.role {
  font-size: 13px;
  color: #FFFFFF;
  font-weight: 500;
}

.section {
  background: #FFFFFF;
  margin-top: 12px;
}

.section-title {
  padding: 16px 20px 12px;
  font-size: 14px;
  font-weight: 600;
  color: #94A3B8;
}

.section-content {
  padding: 0 20px 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.item-label {
  font-size: 15px;
  color: #1E293B;
}

.item-value {
  font-size: 15px;
  color: #64748B;
}

.item-value.role-text {
  color: #EF4444;
  font-weight: 500;
}

.divider {
  height: 1px;
  background: #F1F5F9;
  margin-left: 44px;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
}

.arrow {
  font-size: 20px;
  color: #CBD5E1;
}

.logout-section {
  padding: 30px 20px;
}

.btn-logout {
  width: 100%;
  height: 48px;
  background: #FFFFFF;
  border: 1px solid #FEE2E2;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  color: #EF4444;
  font-weight: 500;
}

.btn-logout .icon {
  font-size: 20px;
}

.btn-logout .text {
  font-size: 16px;
}
</style>
