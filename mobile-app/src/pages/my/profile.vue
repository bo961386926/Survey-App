/**
 * 用户档案页面
 */
<template>
  <view class="profile-container">
    <!-- 顶部Header -->
    <view class="profile-header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-title">
        <text class="title">用户档案</text>
      </view>
    </view>

    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="avatar-section">
        <view class="avatar">{{ (userInfo?.name || '吴')[0] }}</view>
      </view>
      <view class="user-name">{{ userInfo?.name || '未登录' }}</view>
      <view class="user-role">{{ userInfo?.roleName || '-' }}</view>
    </view>

    <!-- 基本信息 -->
    <view class="info-section">
      <view class="section-title">基本信息</view>
      <view class="info-list">
        <view class="info-item">
          <text class="label">姓名</text>
          <text class="value">{{ userInfo?.name || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="label">账号</text>
          <text class="value">{{ userInfo?.username || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="label">邮箱</text>
          <text class="value">{{ userInfo?.email || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="label">手机号</text>
          <text class="value">{{ userInfo?.phone || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="label">角色</text>
          <text class="value role">{{ userInfo?.roleName || '-' }}</text>
        </view>
      </view>
    </view>

    <!-- 工作信息 -->
    <view class="info-section">
      <view class="section-title">工作信息</view>
      <view class="info-list">
        <view class="info-item">
          <text class="label">所属项目</text>
          <text class="value">{{ workInfo?.projectCount || 0 }} 个</text>
        </view>
        <view class="info-item">
          <text class="label">待勘查点位</text>
          <text class="value pending">{{ workInfo?.pendingPoints || 0 }} 个</text>
        </view>
        <view class="info-item">
          <text class="label">已完成点位</text>
          <text class="value">{{ workInfo?.completedPoints || 0 }} 个</text>
        </view>
        <view class="info-item">
          <text class="label">累计勘查</text>
          <text class="value">{{ workInfo?.totalPoints || 0 }} 个</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserInfo } from '@/utils/auth'
import { pointApi, projectApi } from '@/utils/api'

const userInfo = ref(null)
const workInfo = ref({
  projectCount: 0,
  pendingPoints: 0,
  completedPoints: 0,
  totalPoints: 0
})

// 页面加载
onMounted(() => {
  loadUserInfo()
  loadWorkInfo()
})

// 加载用户信息
function loadUserInfo() {
  userInfo.value = getUserInfo()
}

// 加载工作信息
async function loadWorkInfo() {
  try {
    const points = await pointApi.getMyPoints({ status: 'all' })
    const pointList = points.list || points || []
    
    workInfo.value.pendingPoints = pointList.filter(p => p.status === '待勘查').length
    workInfo.value.completedPoints = pointList.filter(p => p.status === '已通过').length
    workInfo.value.totalPoints = pointList.length
    
    const projects = await projectApi.getList({ page: 1, size: 100 })
    const projectList = projects.list || projects || []
    workInfo.value.projectCount = projectList.length
  } catch (error) {
    console.error('加载工作信息失败:', error)
  }
}

// 返回
function goBack() {
  uni.navigateBack()
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background-color: #F8FAFC;
}

/* Header */
.profile-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E2E8F0;
}

.header-left {
  padding: 8rpx;
}

.back-icon {
  font-size: 48rpx;
  color: #1E293B;
  line-height: 1;
}

.header-title {
  flex: 1;
}

.title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1E293B;
}

/* 用户卡片 */
.user-card {
  background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
  padding: 48rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.avatar-section {
  margin-bottom: 8rpx;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  font-weight: 700;
  color: #FFFFFF;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.user-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #FFFFFF;
}

.user-role {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  padding: 8rpx 24rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 24rpx;
}

/* 信息区块 */
.info-section {
  margin: 24rpx 32rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  overflow: hidden;
}

.section-title {
  padding: 24rpx 24rpx 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
}

.info-list {
  padding: 0 24rpx 24rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F1F5F9;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-size: 28rpx;
  color: #64748B;
}

.value {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
}

.value.role {
  color: #2563EB;
}

.value.pending {
  color: #D97706;
}
</style>