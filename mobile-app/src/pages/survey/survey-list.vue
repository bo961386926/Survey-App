/**
 * 我的勘查页面
 * 展示勘查记录列表和统计信息
 */
<template>
  <view class="survey-list-container">
    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stat-card">
        <text class="stat-value" style="color: #F59E0B;">{{ stats.reviewingCount }}</text>
        <text class="stat-label">审核中</text>
      </view>
      <view class="stat-card">
        <text class="stat-value" style="color: #10B981;">{{ stats.approvedCount }}</text>
        <text class="stat-label">已通过</text>
      </view>
      <view class="stat-card">
        <text class="stat-value" style="color: #EF4444;">{{ stats.rejectedCount }}</text>
        <text class="stat-label">已驳回</text>
      </view>
    </view>
    
    <!-- 勘查记录列表 -->
    <view class="list-section">
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
      
      <view v-else-if="surveyList.length === 0" class="empty-state">
        <text class="empty-icon">📄</text>
        <text class="empty-text">暂无勘查记录</text>
      </view>
      
      <view v-else class="survey-list">
        <view 
          v-for="item in surveyList" 
          :key="item.id"
          class="survey-item"
          @click="goToSurveyDetail(item)"
        >
          <view class="survey-header">
            <text class="point-name">{{ item.pointName }}</text>
            <status-tag :status="item.status" type="result" />
          </view>
          <view class="survey-info">
            <text class="info-item">项目: {{ item.projectName }}</text>
            <text class="info-item">提交时间: {{ formatTime(item.submitTime) }}</text>
          </view>
          <view v-if="item.rejectReason" class="reject-reason">
            <text class="reason-label">驳回意见:</text>
            <text class="reason-text">{{ item.rejectReason }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 加载更多 -->
    <view v-if="hasMore && surveyList.length > 0" class="load-more" @click="loadMore">
      <text>{{ loadingMore ? '加载中...' : '加载更多' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { resultApi, pointApi } from '@/utils/api'
import StatusTag from '@/components/status-tag/status-tag.vue'

const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10

const surveyList = ref([])

// 统计数据
const stats = reactive({
  reviewingCount: 0,
  approvedCount: 0,
  rejectedCount: 0
})

onMounted(() => {
  loadSurveyList()
  loadStats()
})

// 加载勘查记录列表
async function loadSurveyList(isRefresh = false) {
  if (isRefresh) {
    page.value = 1
    hasMore.value = true
  }
  
  loading.value = !isRefresh
  loadingMore.value = isRefresh
  
  try {
    const result = await resultApi.getMyResults({
      page: page.value,
      size: pageSize
    })
    const list = result.list || result || []
    
    if (isRefresh) {
      surveyList.value = list
    } else {
      surveyList.value = [...surveyList.value, ...list]
    }
    
    hasMore.value = list.length >= pageSize
    page.value++
  } catch (error) {
    console.error('加载勘查记录失败:', error)
    // 如果接口不存在,使用点位数据
    await loadFromPoints()
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 从点位数据加载(备用方案)
async function loadFromPoints() {
  try {
    const result = await pointApi.getMyPoints({})
    const points = result.list || result || []
    
    // 过滤已提交的点位
    const submittedPoints = points.filter(p => 
      p.status === '已提交' || 
      p.status === '审核中' || 
      p.status === '已通过' || 
      p.status === '已驳回'
    )
    
    surveyList.value = submittedPoints.map(point => ({
      id: point.id,
      pointName: point.name,
      projectName: point.projectName,
      status: point.status,
      submitTime: point.submitTime || point.updateTime
    }))
  } catch (error) {
    console.error('加载失败:', error)
  }
}

// 加载统计数据
async function loadStats() {
  try {
    const result = await pointApi.getMyPoints({ status: 'all' })
    const points = result.list || result || []
    
    stats.reviewingCount = points.filter(p => 
      p.status === '已提交' || p.status === '审核中'
    ).length
    stats.approvedCount = points.filter(p => p.status === '已通过').length
    stats.rejectedCount = points.filter(p => 
      p.status === '已驳回' || p.status === '驳回待修改'
    ).length
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载更多
function loadMore() {
  if (!loadingMore.value && hasMore.value) {
    loadSurveyList(true)
  }
}

// 跳转到勘查详情
function goToSurveyDetail(item) {
  uni.navigateTo({ url: `/pages/point-detail/point-detail?id=${item.id}` })
}

// 格式化时间
function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

// 下拉刷新
onPullDownRefresh(() => {
  loadSurveyList(true)
  loadStats()
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 1000)
})
</script>

<style scoped>
.survey-list-container {
  min-height: 100vh;
  background-color: #F8FAFC;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 16px;
  background: #FFFFFF;
  border-bottom: 1px solid #E2E8F0;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: #F8FAFC;
  border-radius: 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #64748B;
}

.list-section {
  padding: 12px 16px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: #94A3B8;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 15px;
}

.survey-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.survey-item {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #E2E8F0;
}

.survey-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.point-name {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
}

.survey-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.info-item {
  font-size: 13px;
  color: #64748B;
}

.reject-reason {
  padding: 10px 12px;
  background: #FEF2F2;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reason-label {
  font-size: 12px;
  color: #EF4444;
  font-weight: 500;
}

.reason-text {
  font-size: 13px;
  color: #DC2626;
  line-height: 1.5;
}

.load-more {
  text-align: center;
  padding: 16px;
  color: #64748B;
  font-size: 14px;
}
</style>
