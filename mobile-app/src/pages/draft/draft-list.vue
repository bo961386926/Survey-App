/**
 * 草稿管理页面
 * 查看和管理未提交的草稿
 */
<template>
  <view class="draft-container">
    <!-- 顶部Header -->
    <view class="draft-header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-title">
        <text class="title">草稿管理</text>
      </view>
      <view class="header-action" @click="clearAllDrafts" v-if="draftList.length > 0">
        <text class="action-text">清空全部</text>
      </view>
    </view>

    <!-- 草稿列表 -->
    <scroll-view class="draft-list" scroll-y @scrolltolower="loadMore">
      <view 
        v-for="item in draftList" 
        :key="item.pointId" 
        class="draft-item"
        @click="editDraft(item)"
      >
        <view class="draft-header-row">
          <view class="draft-info">
            <text class="point-name">{{ item.pointName || '未命名点位' }}</text>
            <text class="point-id">点位ID: {{ item.pointId }}</text>
          </view>
          <text class="save-time">{{ formatTime(item.savedAt) }}</text>
        </view>
        
        <view class="draft-status-row">
          <view class="status-badge">
            <text class="icon"></text>
            <text class="text">草稿</text>
          </view>
          <view class="action-buttons">
            <view class="action-btn edit" @click.stop="editDraft(item)">
              <text>继续编辑</text>
            </view>
            <view class="action-btn delete" @click.stop="deleteDraft(item)">
              <text>删除</text>
            </view>
          </view>
        </view>
      </view>

      <view class="loading-more" v-if="loading">
        <text>加载中...</text>
      </view>
      <view class="empty-state" v-if="draftList.length === 0 && !loading">
        <text class="empty-icon">📄</text>
        <text class="empty-text">暂无草稿</text>
        <text class="empty-hint">勘察填报时自动保存的草稿会显示在这里</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDraftList, deleteDraft, clearAllDrafts as clearDrafts } from '@/utils/draft'

const loading = ref(false)
const draftList = ref([])

// 页面加载
onMounted(() => {
  loadDrafts()
})

// 加载草稿列表
function loadDrafts() {
  loading.value = true
  try {
    const list = getDraftList()
    draftList.value = list
  } catch (error) {
    console.error('加载草稿列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 返回
function goBack() {
  uni.navigateBack()
}

// 编辑草稿
function editDraft(item) {
  uni.navigateTo({
    url: `/pages/survey/survey?id=${item.pointId}&mode=draft`
  })
}

// 删除草稿
function deleteDraft(item) {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除「${item.pointName || '未命名点位'}」的草稿吗？`,
    success: (res) => {
      if (res.confirm) {
        deleteDraft(item.pointId)
        draftList.value = getDraftList()
        uni.showToast({ title: '已删除', icon: 'success' })
      }
    }
  })
}

// 清空全部草稿
function clearAllDrafts() {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空所有草稿吗？此操作不可恢复。',
    success: (res) => {
      if (res.confirm) {
        clearDrafts()
        draftList.value = []
        uni.showToast({ title: '已清空', icon: 'success' })
      }
    }
  })
}

// 格式化时间
function formatTime(timestamp) {
  if (!timestamp) return ''
  
  const now = Date.now()
  const diff = now - timestamp
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  
  const date = new Date(timestamp)
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

// 加载更多
function loadMore() {
  // 分页加载逻辑
}

// 下拉刷新
onPullDownRefresh(() => {
  loadDrafts()
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 1000)
})
</script>

<style scoped>
.draft-container {
  min-height: 100vh;
  background-color: #F8FAFC;
}

/* Header */
.draft-header {
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

.header-action {
  padding: 8rpx 16rpx;
}

.action-text {
  font-size: 26rpx;
  color: #EF4444;
}

/* 草稿列表 */
.draft-list {
  height: calc(100vh - 120rpx);
  padding: 24rpx 32rpx;
}

.draft-item {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.draft-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.draft-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.point-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
}

.point-id {
  font-size: 24rpx;
  color: #94A3B8;
}

.save-time {
  font-size: 24rpx;
  color: #94A3B8;
  flex-shrink: 0;
}

.draft-status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 12rpx;
  background: #FEF3C7;
  border-radius: 12rpx;
  font-size: 22rpx;
  color: #D97706;
}

.status-badge .icon {
  font-size: 20rpx;
}

.action-buttons {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  padding: 8rpx 20rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
}

.action-btn.edit {
  background: #EFF6FF;
  color: #2563EB;
}

.action-btn.delete {
  background: #FEF2F2;
  color: #EF4444;
}

/* 加载和空状态 */
.loading-more {
  text-align: center;
  padding: 32rpx;
  color: #94A3B8;
  font-size: 24rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
}

.empty-icon {
  font-size: 96rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #64748B;
  margin-bottom: 8rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #94A3B8;
  text-align: center;
}
</style>