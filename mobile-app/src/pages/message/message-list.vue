/**
 * 消息列表页面
 */
<template>
  <view class="message-container">
    <!-- 顶部Header -->
    <view class="message-header">
      <view class="header-title">
        <text class="title">消息中心</text>
      </view>
      <view class="header-action" @click="markAllRead" v-if="unreadCount > 0">
        <text class="action-text">全部已读</text>
      </view>
    </view>

    <!-- 消息分类 -->
    <view class="message-tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'all' }"
        @click="changeTab('all')"
      >
        <text>全部</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'system' }"
        @click="changeTab('system')"
      >
        <text>系统通知</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'task' }"
        @click="changeTab('task')"
      >
        <text>任务提醒</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'audit' }"
        @click="changeTab('audit')"
      >
        <text>审核通知</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view class="message-list" scroll-y @scrolltolower="loadMore">
      <view 
        v-for="item in filteredList" 
        :key="item.id" 
        class="message-item"
        :class="{ unread: !item.isRead }"
        @click="goToDetail(item)"
      >
        <view class="message-icon" :class="item.type">
          <text class="icon-text">{{ getTypeIcon(item.type) }}</text>
        </view>
        <view class="message-content">
          <view class="message-header-row">
            <text class="message-title">{{ item.title }}</text>
            <text class="message-time">{{ formatTime(item.createTime) }}</text>
          </view>
          <text class="message-desc">{{ item.content }}</text>
        </view>
        <view class="unread-dot" v-if="!item.isRead"></view>
      </view>

      <view class="loading-more" v-if="loading">
        <text>加载中...</text>
      </view>
      <view class="empty-state" v-if="filteredList.length === 0 && !loading">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无消息</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { messageApi } from '@/utils/api.js'

const activeTab = ref('all')
const loading = ref(false)
const messageList = ref([])
const unreadCount = ref(0)

const filteredList = computed(() => {
  if (activeTab.value === 'all') {
    return messageList.value
  }
  return messageList.value.filter(item => item.type === activeTab.value)
})

// 页面加载
onMounted(() => {
  loadMessages()
  loadUnreadCount()
})

// 加载消息列表
async function loadMessages() {
  loading.value = true
  try {
    const result = await messageApi.getList({ page: 1, size: 20 })
    messageList.value = result.list || result || []
  } catch (error) {
    console.error('加载消息列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载未读数量
async function loadUnreadCount() {
  try {
    const result = await messageApi.getUnreadCount()
    unreadCount.value = result || 0
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

// 切换标签
function changeTab(tab) {
  activeTab.value = tab
}

// 全部已读
async function markAllRead() {
  try {
    await messageApi.markAllRead()
    unreadCount.value = 0
    messageList.value.forEach(item => {
      item.isRead = true
    })
    uni.showToast({ title: '全部已读', icon: 'success' })
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 跳转详情
function goToDetail(item) {
  if (!item.isRead) {
    item.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    // 调用标记已读API
    messageApi.markRead(item.id).catch(err => console.error(err))
  }
  uni.navigateTo({
    url: `/pages/message/message-detail?id=${item.id}`
  })
}

// 获取类型图标
function getTypeIcon(type) {
  const icons = {
    system: '📢',
    task: '📋',
    audit: '✅'
  }
  return icons[type] || '📩'
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 一分钟内
  if (diff < 60000) return '刚刚'
  // 一小时内
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }
  // 其他
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

// 加载更多
function loadMore() {
  // 分页加载逻辑
}

// 下拉刷新
onPullDownRefresh(() => {
  loadMessages()
  loadUnreadCount()
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 1000)
})
</script>

<style scoped>
.message-container {
  min-height: 100vh;
  background-color: #F8FAFC;
}

/* Header */
.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E2E8F0;
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
  color: #2563EB;
}

/* 消息分类 */
.message-tabs {
  display: flex;
  background: #FFFFFF;
  padding: 24rpx 32rpx;
  gap: 16rpx;
  border-bottom: 1rpx solid #E2E8F0;
}

.tab-item {
  padding: 12rpx 24rpx;
  border-radius: 24rpx;
  background: #F1F5F9;
  font-size: 26rpx;
  color: #64748B;
}

.tab-item.active {
  background: #EFF6FF;
  color: #2563EB;
  font-weight: 600;
}

/* 消息列表 */
.message-list {
  height: calc(100vh - 220rpx);
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 24rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #F1F5F9;
  position: relative;
}

.message-item.unread {
  background: #FAFCFF;
}

.message-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  flex-shrink: 0;
}

.message-icon.system {
  background: #FEF3C7;
}

.message-icon.task {
  background: #DBEAFE;
}

.message-icon.audit {
  background: #D1FAE5;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.message-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
}

.message-time {
  font-size: 22rpx;
  color: #94A3B8;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.message-desc {
  font-size: 26rpx;
  color: #64748B;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-dot {
  width: 16rpx;
  height: 16rpx;
  background: #EF4444;
  border-radius: 50%;
  position: absolute;
  right: 32rpx;
  top: 32rpx;
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
  font-size: 28rpx;
  color: #94A3B8;
}
</style>