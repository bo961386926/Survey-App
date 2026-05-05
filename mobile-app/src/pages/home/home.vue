/**
 * 首页(工作台)
 * 展示任务概览、项目列表、驳回提醒
 */
<template>
  <view class="home-container">
    <!-- 顶部欢迎区域 -->
    <view class="header">
      <view class="welcome-section">
        <text class="welcome-text">欢迎回来</text>
        <text class="username">{{ userInfo?.name || '采集员' }}</text>
      </view>
      <view class="location-icon" @click="goToMap">
        <text class="icon">📍</text>
      </view>
    </view>
    
    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stat-card" @click="goToPointList('待勘查')">
        <view class="stat-icon" style="background-color: #FEF3C7;">
          <text class="icon">⏱️</text>
        </view>
        <view class="stat-content">
          <text class="stat-label">待勘查</text>
          <text class="stat-value">{{ stats.pendingCount }}</text>
        </view>
      </view>
      <view class="stat-card" @click="goToPointList('已提交')">
        <view class="stat-icon" style="background-color: #DBEAFE;">
          <text class="icon">📤</text>
        </view>
        <view class="stat-content">
          <text class="stat-label">已提交</text>
          <text class="stat-value">{{ stats.submittedCount }}</text>
        </view>
      </view>
      <view class="stat-card" @click="goToPointList('已通过')">
        <view class="stat-icon" style="background-color: #D1FAE5;">
          <text class="icon">✅</text>
        </view>
        <view class="stat-content">
          <text class="stat-label">已通过</text>
          <text class="stat-value">{{ stats.approvedCount }}</text>
        </view>
      </view>
      <view class="stat-card" @click="goToPointList('已驳回')">
        <view class="stat-icon" style="background-color: #FEE2E2;">
          <text class="icon"></text>
        </view>
        <view class="stat-content">
          <text class="stat-label">已驳回</text>
          <text class="stat-value">{{ stats.rejectedCount }}</text>
        </view>
      </view>
    </view>
    
    <!-- 驳回提醒 -->
    <view v-if="rejectedPoints.length > 0" class="reject-notice">
      <view class="notice-header">
        <view class="notice-icon">❌</view>
        <view class="notice-content">
          <text class="notice-title">有 {{ rejectedPoints.length }} 个点位被驳回</text>
          <text class="notice-desc">请根据驳回意见修改后重新提交</text>
        </view>
      </view>
      <view class="reject-list">
        <view 
          v-for="point in rejectedPoints" 
          :key="point.id" 
          class="reject-item"
          @click="modifyRejected(point)"
        >
          <text class="reject-name">{{ point.name }}</text>
          <view class="reject-action">
            <text class="action-text">重新勘查</text>
            <text class="arrow">›</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 开始勘查按钮 -->
    <view class="start-survey-btn" @click="goToPointList('待勘查')">
      <text class="btn-icon">📍</text>
      <text class="btn-text">开始勘查</text>
    </view>
    
    <!-- 我的项目 -->
    <view class="projects-section">
      <view class="section-header">
        <text class="section-title">我的项目</text>
        <text class="section-count">{{ projectList.length }} 个项目</text>
      </view>
      
      <view class="project-list">
        <view 
          v-for="project in projectList" 
          :key="project.id" 
          class="project-card"
          @click="goToProjectDetail(project.id)"
        >
          <view class="project-header">
            <view class="project-info">
              <text class="project-name">{{ project.name }}</text>
              <text class="project-code">{{ project.code }}</text>
            </view>
            <text class="arrow">›</text>
          </view>
          
          <progress-bar :percentage="project.progress" />
          
          <view class="project-stats">
            <view class="stat-tag pending" v-if="project.pendingCount > 0">
              <text class="icon">⏱️</text>
              <text class="text">{{ project.pendingCount }} 待勘查</text>
            </view>
            <view class="stat-tag submitted" v-if="project.submittedCount > 0">
              <text class="icon">📤</text>
              <text class="text">{{ project.submittedCount }} 已提交</text>
            </view>
            <view class="stat-tag approved" v-if="project.approvedCount > 0">
              <text class="icon">✅</text>
              <text class="text">{{ project.approvedCount }} 已通过</text>
            </view>
            <view class="stat-tag rejected" v-if="project.rejectedCount > 0">
              <text class="icon">❌</text>
              <text class="text">{{ project.rejectedCount }} 已驳回</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserInfo } from '@/utils/auth'
import { pointApi, projectApi, messageApi } from '@/utils/api'
import ProgressBar from '@/components/progress-bar/progress-bar.vue'

const userInfo = ref(null)
const unreadCount = ref(0)

// 统计数据
const stats = reactive({
  pendingCount: 0,
  submittedCount: 0,
  approvedCount: 0,
  rejectedCount: 0
})

// 驳回点位
const rejectedPoints = ref([])

// 项目列表
const projectList = ref([])

// 页面加载
onMounted(() => {
  loadUserInfo()
  loadStats()
  loadRejectedPoints()
  loadProjects()
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
    
    stats.pendingCount = points.filter(p => p.status === '待勘查').length
    stats.submittedCount = points.filter(p => p.status === '已提交' || p.status === '审核中').length
    stats.approvedCount = points.filter(p => p.status === '已通过').length
    stats.rejectedCount = points.filter(p => p.status === '已驳回' || p.status === '驳回待修改').length
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载驳回点位
async function loadRejectedPoints() {
  try {
    const result = await pointApi.getMyPoints({ status: '已驳回' })
    const points = result.list || result || []
    rejectedPoints.value = points.slice(0, 5) // 最多显示5个
  } catch (error) {
    console.error('加载驳回点位失败:', error)
  }
}

// 加载项目列表
async function loadProjects() {
  try {
    const result = await projectApi.getList({ page: 1, size: 10 })
    const projects = result.list || result || []
    
    // 为每个项目加载统计信息
    projectList.value = await Promise.all(
      projects.map(async (project) => {
        try {
          const stats = await projectApi.getStatistics(project.id)
          return {
            ...project,
            progress: stats.progress || 0,
            pendingCount: stats.pending || 0,
            submittedCount: stats.submitted || 0,
            approvedCount: stats.approved || 0,
            rejectedCount: stats.rejected || 0
          }
        } catch (e) {
          return {
            ...project,
            progress: 0,
            pendingCount: 0,
            submittedCount: 0,
            approvedCount: 0,
            rejectedCount: 0
          }
        }
      })
    )
  } catch (error) {
    console.error('加载项目失败:', error)
  }
}

// 导航到点位列表
function goToPointList(status) {
  const url = status ? `/pages/point-list/point-list?status=${status}` : '/pages/point-list/point-list'
  uni.switchTab({ url: '/pages/point-list/point-list' })
}

// 导航到地图
function goToMap() {
  uni.switchTab({ url: '/pages/point-map/point-map' })
}

// 修改驳回点位
function modifyRejected(point) {
  uni.navigateTo({ url: `/pages/survey/survey?id=${point.id}&mode=modify` })
}

// 导航到项目详情
function goToProjectDetail(id) {
  // 可以跳转到项目详情页面
  uni.showToast({ title: '项目详情开发中', icon: 'none' })
}

// 下拉刷新
onPullDownRefresh(() => {
  loadStats()
  loadRejectedPoints()
  loadProjects()
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 1000)
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #F8FAFC;
  padding-bottom: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 16px 16px;
  background: #FFFFFF;
}

.welcome-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.welcome-text {
  font-size: 13px;
  color: #94A3B8;
}

.username {
  font-size: 20px;
  font-weight: 700;
  color: #1E293B;
}

.location-icon {
  padding: 8px;
}

.location-icon .icon {
  font-size: 24px;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 16px;
  margin-top: 0;
}

.stat-card {
  background-color: #FFFFFF;
  border-radius: 12px;
  padding: 14px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  border: 1px solid #E2E8F0;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  line-height: 1;
}

.stat-label {
  font-size: 11px;
  color: #94A3B8;
}

.reject-notice {
  margin: 0 16px 16px;
  padding: 16px;
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 16px;
}

.notice-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.notice-icon {
  font-size: 24px;
}

.notice-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notice-title {
  font-size: 15px;
  font-weight: 600;
  color: #EF4444;
}

.notice-desc {
  font-size: 13px;
  color: #F87171;
}

.reject-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reject-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #FFFFFF;
  border-radius: 10px;
}

.reject-name {
  font-size: 14px;
  color: #1E293B;
  font-weight: 500;
}

.reject-action {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-text {
  font-size: 13px;
  color: #EF4444;
  font-weight: 500;
}

.arrow {
  font-size: 18px;
  color: #EF4444;
}

.start-survey-btn {
  margin: 0 16px 16px;
  padding: 16px;
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.25);
}

.btn-icon {
  font-size: 20px;
}

.btn-text {
  font-size: 17px;
  font-weight: 600;
  color: #FFFFFF;
}

.projects-section {
  padding: 0 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 17px;
  font-weight: 700;
  color: #1E293B;
}

.section-count {
  font-size: 13px;
  color: #94A3B8;
}

.project-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.project-card {
  background-color: #FFFFFF;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid #E2E8F0;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.project-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.project-name {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
}

.project-code {
  font-size: 13px;
  color: #94A3B8;
  font-family: monospace;
}

.project-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.stat-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.stat-tag .icon {
  font-size: 12px;
}

.stat-tag.pending {
  background: #FEF3C7;
  color: #D97706;
}

.stat-tag.submitted {
  background: #DBEAFE;
  color: #2563EB;
}

.stat-tag.approved {
  background: #D1FAE5;
  color: #059669;
}

.stat-tag.rejected {
  background: #FEE2E2;
  color: #DC2626;
}
</style>
