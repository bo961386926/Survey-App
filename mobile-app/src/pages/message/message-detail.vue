/**
 * 消息详情页面
 */
<template>
  <view class="detail-container">
    <!-- 顶部Header -->
    <view class="detail-header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-title">
        <text class="title">消息详情</text>
      </view>
    </view>

    <!-- 消息内容 -->
    <view class="detail-content">
      <!-- 消息标题 -->
      <view class="title-section">
        <text class="main-title">{{ message.title }}</text>
        <view class="meta-row">
          <text class="meta-time">{{ formatFullTime(message.createTime) }}</text>
          <view class="type-tag" :class="message.type">
            {{ getTypeText(message.type) }}
          </view>
        </view>
      </view>

      <!-- 分隔线 -->
      <view class="divider"></view>

      <!-- 消息正文 -->
      <view class="body-section">
        <text class="body-text">{{ message.content }}</text>
      </view>

      <!-- 相关链接（如果有） -->
      <view class="link-section" v-if="message.relatedLink">
        <text class="link-label">相关链接</text>
        <view class="link-item" @click="handleLinkClick">
          <text class="link-text">{{ message.relatedLinkText || '查看详情' }}</text>
          <text class="link-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="message.type === 'audit' && message.action">
      <view class="action-btn primary" @click="handleAction">
        <text>{{ getActionText() }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const message = ref({
  id: '',
  title: '',
  content: '',
  type: 'system',
  createTime: '',
  isRead: true,
  relatedLink: '',
  relatedLinkText: '',
  action: ''
})

// 页面加载
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  
  if (options.id) {
    loadMessageDetail(options.id)
  }
})

// 加载消息详情
async function loadMessageDetail(id) {
  try {
    // 模拟数据，实际应调用API
    message.value = {
      id,
      title: '任务分配通知',
      content: '您被分配了新的勘察任务，请及时查看并开始工作。',
      type: 'task',
      createTime: '2024-01-15 14:30:00',
      isRead: true,
      relatedLink: '/pages/point-list/point-list',
      relatedLinkText: '查看任务列表',
      action: ''
    }
  } catch (error) {
    console.error('加载消息详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 返回
function goBack() {
  uni.navigateBack()
}

// 获取类型文本
function getTypeText(type) {
  const texts = {
    system: '系统通知',
    task: '任务提醒',
    audit: '审核通知'
  }
  return texts[type] || '通知'
}

// 获取操作按钮文本
function getActionText() {
  return '前往处理'
}

// 处理链接点击
function handleLinkClick() {
  if (message.value.relatedLink) {
    uni.navigateTo({
      url: message.value.relatedLink
    })
  }
}

// 处理操作按钮
function handleAction() {
  // 根据action类型执行不同操作
  uni.showToast({ title: '处理中...', icon: 'none' })
}

// 格式化完整时间
function formatFullTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background-color: #F8FAFC;
}

/* Header */
.detail-header {
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

/* 消息内容 */
.detail-content {
  padding: 32rpx;
}

.title-section {
  margin-bottom: 24rpx;
}

.main-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1E293B;
  line-height: 1.4;
  display: block;
  margin-bottom: 16rpx;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.meta-time {
  font-size: 24rpx;
  color: #94A3B8;
}

.type-tag {
  padding: 4rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
}

.type-tag.system {
  background: #FEF3C7;
  color: #D97706;
}

.type-tag.task {
  background: #DBEAFE;
  color: #2563EB;
}

.type-tag.audit {
  background: #D1FAE5;
  color: #059669;
}

.divider {
  height: 1rpx;
  background: #E2E8F0;
  margin: 24rpx 0;
}

.body-section {
  margin-bottom: 32rpx;
}

.body-text {
  font-size: 28rpx;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
}

.link-section {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  border: 1rpx solid #E2E8F0;
}

.link-label {
  font-size: 24rpx;
  color: #94A3B8;
  margin-bottom: 12rpx;
  display: block;
}

.link-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.link-text {
  font-size: 28rpx;
  color: #2563EB;
}

.link-arrow {
  font-size: 32rpx;
  color: #2563EB;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  background: #FFFFFF;
  border-top: 1rpx solid #E2E8F0;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.action-btn {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
}

.action-btn.primary {
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
  color: #FFFFFF;
}
</style>