<template>
  <div class="audit-detail-container">
    <!-- 顶部Header -->
    <header class="detail-header">
      <view class="header-left" @click="goBack">
        <text class="material-symbols-outlined">arrow_back</text>
      </view>
      <view class="header-title">
        <text class="title">审核详情</text>
      </view>
    </header>

    <scroll-view class="detail-content" scroll-y>
      <!-- 点位基本信息 -->
      <view class="section-card">
        <view class="section-title">
          <text class="material-symbols-outlined">location_on</text>
          <text>点位信息</text>
        </view>
        <view class="info-grid">
          <view class="info-item">
            <text class="label">点位编号</text>
            <text class="value">{{ detail.pointNumber }}</text>
          </view>
          <view class="info-item">
            <text class="label">点位名称</text>
            <text class="value">{{ detail.pointName }}</text>
          </view>
          <view class="info-item full">
            <text class="label">详细地址</text>
            <text class="value">{{ detail.address }}</text>
          </view>
          <view class="info-item">
            <text class="label">经度</text>
            <text class="value">{{ detail.longitude }}</text>
          </view>
          <view class="info-item">
            <text class="label">纬度</text>
            <text class="value">{{ detail.latitude }}</text>
          </view>
        </view>
      </view>

      <!-- 勘查信息 -->
      <view class="section-card">
        <view class="section-title">
          <text class="material-symbols-outlined">assignment</text>
          <text>勘查信息</text>
        </view>
        <view class="info-grid">
          <view class="info-item">
            <text class="label">勘查人员</text>
            <text class="value">{{ detail.surveyor }}</text>
          </view>
          <view class="info-item">
            <text class="label">提交时间</text>
            <text class="value">{{ detail.submitTime }}</text>
          </view>
          <view class="info-item">
            <text class="label">排口类型</text>
            <text class="value">{{ detail.outletType }}</text>
          </view>
          <view class="info-item">
            <text class="label">排口形态</text>
            <text class="value">{{ detail.outletShape }}</text>
          </view>
          <view class="info-item full">
            <text class="label">排口描述</text>
            <text class="value">{{ detail.description }}</text>
          </view>
        </view>
      </view>

      <!-- 现场照片 -->
      <view class="section-card">
        <view class="section-title">
          <text class="material-symbols-outlined">photo_library</text>
          <text>现场照片</text>
        </view>
        <view class="photo-grid">
          <view class="photo-item" v-for="(photo, index) in detail.photos" :key="index" @click="previewPhoto(index)">
            <image class="photo-img" :src="photo" mode="aspectFill" />
          </view>
        </view>
      </view>

      <!-- 审核记录 -->
      <view class="section-card" v-if="detail.auditRecords && detail.auditRecords.length > 0">
        <view class="section-title">
          <text class="material-symbols-outlined">history</text>
          <text>审核记录</text>
        </view>
        <view class="audit-timeline">
          <view class="timeline-item" v-for="(record, index) in detail.auditRecords" :key="index">
            <view class="timeline-dot" :class="record.status"></view>
            <view class="timeline-content">
              <view class="timeline-header">
                <text class="auditor">{{ record.auditor }}</text>
                <text class="time">{{ record.time }}</text>
              </view>
              <view class="timeline-body">
                <text class="action">{{ record.action }}</text>
                <text class="reason" v-if="record.reason">{{ record.reason }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 驳回原因 -->
      <view class="section-card reject-reason" v-if="detail.status === 'rejected' && detail.rejectReason">
        <view class="section-title">
          <text class="material-symbols-outlined">error</text>
          <text>驳回原因</text>
        </view>
        <view class="reject-content">
          <text>{{ detail.rejectReason }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="action-bar" v-if="detail.status === 'pending'">
      <view class="btn-reject" @click="showRejectModal">
        <text class="material-symbols-outlined">close</text>
        <text>驳回</text>
      </view>
      <view class="btn-approve" @click="approveItem">
        <text class="material-symbols-outlined">check</text>
        <text>通过</text>
      </view>
    </view>

    <!-- 驳回原因弹窗 -->
    <view class="reject-mask" v-if="showReject" @click="showReject = false">
      <view class="reject-panel" @click.stop>
        <view class="panel-header">
          <text class="panel-title">驳回原因</text>
          <text class="material-symbols-outlined close-icon" @click="showReject = false">close</text>
        </view>
        <textarea 
          class="reject-input" 
          v-model="rejectReason" 
          placeholder="请输入驳回原因..."
          :maxlength="200"
        />
        <view class="panel-footer">
          <view class="btn-cancel" @click="showReject = false">取消</view>
          <view class="btn-confirm" @click="confirmReject">确认驳回</view>
        </view>
      </view>
    </view>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { surveyResultApi } from '@/utils/api.js'

const showReject = ref(false)
const rejectReason = ref('')

const detail = ref({
  id: 1,
  pointNumber: 'LJ-001',
  pointName: '柳江大桥东侧排口',
  address: '广西柳州市柳南区柳江大桥东侧',
  longitude: '109.428756',
  latitude: '24.325689',
  surveyor: '张建国',
  submitTime: '2024-01-15 14:30',
  outletType: '雨水排口',
  outletShape: '明渠',
  description: '排口位于柳江大桥东侧桥墩下方，排水量中等，水质清澈，无明显异味。周边为居民区，距离最近的居民楼约50米。',
  status: 'pending',
  photos: [
    'https://picsum.photos/400/300?random=1',
    'https://picsum.photos/400/300?random=2',
    'https://picsum.photos/400/300?random=3',
    'https://picsum.photos/400/300?random=4'
  ],
  auditRecords: [
    { status: 'pending', auditor: '张建国', time: '2024-01-15 14:30', action: '提交审核', reason: '' }
  ],
  rejectReason: ''
})

const goBack = () => {
  uni.navigateBack()
}

const previewPhoto = (index) => {
  uni.previewImage({
    urls: detail.value.photos,
    current: index
  })
}

const showRejectModal = () => {
  rejectReason.value = ''
  showReject.value = true
}

const confirmReject = () => {
  if (!rejectReason.value.trim()) {
    uni.showToast({ title: '请输入驳回原因', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '处理中...' })
  setTimeout(() => {
    uni.hideLoading()
    detail.value.status = 'rejected'
    detail.value.rejectReason = rejectReason.value
    detail.value.auditRecords.push({
      status: 'rejected',
      auditor: '审核员',
      time: new Date().toLocaleString(),
      action: '审核驳回',
      reason: rejectReason.value
    })
    showReject.value = false
    uni.showToast({ title: '已驳回', icon: 'success' })
  }, 1000)
}

const approveItem = () => {
  uni.showModal({
    title: '确认通过',
    content: '确认审核通过该点位?',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中...' })
        setTimeout(() => {
          uni.hideLoading()
          detail.value.status = 'approved'
          detail.value.auditRecords.push({
            status: 'approved',
            auditor: '审核员',
            time: new Date().toLocaleString(),
            action: '审核通过',
            reason: ''
          })
          uni.showToast({ title: '已通过', icon: 'success' })
        }, 1000)
      }
    }
  })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

.audit-detail-container {
  min-height: 100vh;
  background: #f8fafc;
  padding-bottom: 140rpx;
}

.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
  display: inline-block;
  line-height: 1;
}

/* Header */
.detail-header {
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 100%);
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-left {
  padding: 12rpx;
}

.header-left .material-symbols-outlined {
  font-size: 40rpx;
  color: white;
}

.header-title {
  flex: 1;
}

.title {
  font-size: 32rpx;
  font-weight: 700;
  color: white;
}

/* 内容区域 */
.detail-content {
  padding: 24rpx;
  height: calc(100vh - 120rpx);
}

/* 卡片 */
.section-card {
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #f1f5f9;
}

.section-title .material-symbols-outlined {
  font-size: 36rpx;
  color: #0284c7;
}

/* 信息网格 */
.info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.info-item {
  width: calc(50% - 8rpx);
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.info-item.full {
  width: 100%;
}

.label {
  font-size: 24rpx;
  color: #64748b;
}

.value {
  font-size: 28rpx;
  color: #1e293b;
  font-weight: 500;
}

/* 照片网格 */
.photo-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.photo-item {
  width: calc(50% - 6rpx);
  height: 200rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.photo-img {
  width: 100%;
  height: 100%;
}

/* 审核时间线 */
.audit-timeline {
  padding-left: 20rpx;
}

.timeline-item {
  display: flex;
  gap: 16rpx;
  padding-bottom: 24rpx;
  position: relative;
}

.timeline-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 10rpx;
  top: 24rpx;
  bottom: 0;
  width: 2rpx;
  background: #e2e8f0;
}

.timeline-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  margin-top: 6rpx;
  flex-shrink: 0;
}

.timeline-dot.pending {
  background: #f59e0b;
}

.timeline-dot.approved {
  background: #10b981;
}

.timeline-dot.rejected {
  background: #ef4444;
}

.timeline-content {
  flex: 1;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.auditor {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
}

.time {
  font-size: 24rpx;
  color: #94a3b8;
}

.timeline-body {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.action {
  font-size: 26rpx;
  color: #64748b;
}

.reason {
  font-size: 26rpx;
  color: #ef4444;
  background: #fef2f2;
  padding: 12rpx;
  border-radius: 8rpx;
}

/* 驳回原因 */
.reject-reason {
  background: #fef2f2;
  border: 2rpx solid #fecaca;
}

.reject-content {
  font-size: 28rpx;
  color: #dc2626;
  line-height: 1.6;
}

/* 底部操作栏 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  gap: 16rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.btn-reject, .btn-approve {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
}

.btn-reject {
  background: #fee2e2;
  color: #dc2626;
}

.btn-approve {
  background: linear-gradient(135deg, #4ade80 0%, #16a34a 100%);
  color: white;
}

/* 驳回弹窗 */
.reject-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}

.reject-panel {
  width: 100%;
  background: white;
  border-radius: 24rpx;
  padding: 32rpx;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.panel-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1e293b;
}

.close-icon {
  font-size: 40rpx;
  color: #94a3b8;
}

.reject-input {
  width: 100%;
  min-height: 200rpx;
  background: #f8fafc;
  border: 2rpx solid #e2e8f0;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
}

.panel-footer {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}

.btn-cancel, .btn-confirm {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.btn-cancel {
  background: #f1f5f9;
  color: #64748b;
}

.btn-confirm {
  background: linear-gradient(135deg, #f87171 0%, #dc2626 100%);
  color: white;
}
</style>
