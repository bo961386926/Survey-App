/**
 * 首页（工作台）
 * 展示任务概览、快捷入口、待办提醒
 */
<template>
  <view class="home-container">
    <!-- 顶部用户信息 -->
    <view class="header">
      <view class="user-info">
        <image class="avatar" :src="userInfo?.avatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="user-detail">
          <text class="username">你好，{{ userInfo?.name || '采集员' }}</text>
          <text class="role">{{ userInfo?.roleName || '采集员' }}</text>
        </view>
      </view>
      <view class="message-icon" @click="goToMessage">
        <text class="icon">🔔</text>
        <view v-if="unreadCount > 0" class="badge">
          <text class="badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
        </view>
      </view>
    </view>
    
    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stat-card" @click="goToPointList('待采集')">
        <view class="stat-icon" style="background-color: #E6A23C;">📋</view>
        <view class="stat-info">
          <text class="stat-value">{{ stats.pendingCount }}</text>
          <text class="stat-label">待采集</text>
        </view>
      </view>
      <view class="stat-card" @click="goToPointList('草稿中')">
        <view class="stat-icon" style="background-color: #409EFF;">📝</view>
        <view class="stat-info">
          <text class="stat-value">{{ stats.draftCount }}</text>
          <text class="stat-label">草稿中</text>
        </view>
      </view>
      <view class="stat-card" @click="goToPointList('驳回待修改')">
        <view class="stat-icon" style="background-color: #F56C6C;">↩️</view>
        <view class="stat-info">
          <text class="stat-value">{{ stats.rejectedCount }}</text>
          <text class="stat-label">待修改</text>
        </view>
      </view>
      <view class="stat-card" @click="goToPointList('审核通过')">
        <view class="stat-icon" style="background-color: #67C23A;">✅</view>
        <view class="stat-info">
          <text class="stat-value">{{ stats.completedCount }}</text>
          <text class="stat-label">已完成</text>
        </view>
      </view>
    </view>
    
    <!-- 快捷功能 -->
    <view class="quick-actions">
      <view class="section-title">快捷功能</view>
      <view class="action-grid">
        <view class="action-item" @click="goToPointList()">
          <view class="action-icon" style="background-color: #409EFF;">📍</view>
          <text class="action-text">点位列表</text>
        </view>
        <view class="action-item" @click="goToMap()">
          <view class="action-icon" style="background-color: #67C23A;">🗺️</view>
          <text class="action-text">点位地图</text>
        </view>
        <view class="action-item" @click="goToDrafts()">
          <view class="action-icon" style="background-color: #E6A23C;">💾</view>
          <text class="action-text">草稿管理</text>
        </view>
        <view class="action-item" @click="goToAudit()">
          <view class="action-icon" style="background-color: #F56C6C;">📄</view>
          <text class="action-text">审核记录</text>
        </view>
      </view>
    </view>
    
    <!-- 最近任务 -->
    <view class="recent-tasks">
      <view class="section-title">最近任务</view>
      <view v-if="recentPoints.length > 0" class="task-list">
        <view 
          v-for="point in recentPoints" 
          :key="point.id"
          class="task-item"
          @click="goToPointDetail(point.id)"
        >
          <view class="task-info">
            <text class="task-name">{{ point.name }}</text>
            <text class="task-project">{{ point.projectName }}</text>
          </view>
          <status-tag :status="point.status" type="point" />
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无任务</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserInfo } from '@/utils/auth'
import { pointApi, messageApi } from '@/utils/api'
import StatusTag from '@/components/status-tag/status-tag.vue'

const userInfo = ref(null)
const unreadCount = ref(0)

// 统计数据
const stats = reactive({
  pendingCount: 0,
  draftCount: 0,
  rejectedCount: 0,
  completedCount: 0
})

// 最近任务
const recentPoints = ref([])

// 页面加载
onMounted(() => {
  loadUserInfo()
  loadStats()
  loadRecentPoints()
  loadUnreadCount()
})

// 加载用户信息
function loadUserInfo() {
  userInfo.value = getUserInfo()
}

// 加载统计数据
async function loadStats() {
  try {
    const result = await pointApi.getMyPoints({ status: 'all' })
    const points = result.list || result || []
    
    stats.pendingCount = points.filter(p => p.status === '待采集').length
    stats.draftCount = points.filter(p => p.status === '草稿中').length
    stats.rejectedCount = points.filter(p => p.status === '驳回待修改').length
    stats.completedCount = points.filter(p => p.status === '审核通过').length
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载最近任务
async function loadRecentPoints() {
  try {
    const result = await pointApi.getMyPoints({ page: 1, size: 5 })
    recentPoints.value = result.list || result || []
  } catch (error) {
    console.error('加载任务失败:', error)
  }
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

// 导航到点位列表
function goToPointList(status) {
  const url = status ? `/pages/point-list/point-list?status=${status}` : '/pages/point-list/point-list'
  uni.navigateTo({ url })
}

// 导航到地图
function goToMap() {
  uni.navigateTo({ url: '/pages/point-map/point-map' })
}

// 导航到草稿管理
function goToDrafts() {
  uni.navigateTo({ url: '/pages/draft/draft-list' })
}

// 导航到审核记录
function goToAudit() {
  uni.navigateTo({ url: '/pages/audit/audit-list' })
}

// 导航到点位详情
function goToPointDetail(id) {
  uni.navigateTo({ url: `/pages/point-detail/point-detail?id=${id}` })
}

// 导航到消息中心
function goToMessage() {
  uni.navigateTo({ url: '/pages/message/message-list' })
}

// 下拉刷新
onPullDownRefresh(() => {
  loadStats()
  loadRecentPoints()
  loadUnreadCount()
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 1000)
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #F5F7FA;
  padding-bottom: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 12px;
}

.username {
  font-size: 16px;
  font-weight: 500;
  color: #fff;
}

.role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4px;
}

.message-icon {
  position: relative;
  padding: 8px;
}

.message-icon .icon {
  font-size: 24px;
}

.badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 18px;
  height: 18px;
  background-color: #F56C6C;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge-text {
  font-size: 10px;
  color: #fff;
  padding: 0 4px;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px;
  margin-top: -20px;
}

.stat-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.quick-actions,
.recent-tasks {
  background-color: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 16px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 8px;
}

.action-text {
  font-size: 12px;
  color: #666;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.task-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.task-project {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
  color: #999;
}
</style>
