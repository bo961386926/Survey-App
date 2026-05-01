<template>
  <div class="audit-container">
    <!-- 顶部Header -->
    <header class="audit-header">
      <view class="header-left" @click="goBack">
        <text class="material-symbols-outlined">arrow_back</text>
      </view>
      <view class="header-title">
        <text class="title">审核中心</text>
      </view>
    </header>

    <!-- 状态筛选 -->
    <view class="status-tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'pending' }"
        @click="changeTab('pending')"
      >
        <text>待审核</text>
        <view class="badge" v-if="stats.pending > 0">{{ stats.pending }}</view>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'approved' }"
        @click="changeTab('approved')"
      >
        <text>已通过</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'rejected' }"
        @click="changeTab('rejected')"
      >
        <text>已驳回</text>
      </view>
    </view>

    <!-- 审核列表 -->
    <scroll-view class="audit-list" scroll-y @scrolltolower="loadMore">
      <view class="audit-card" v-for="item in filteredList" :key="item.id" @click="goToDetail(item)">
        <view class="card-header">
          <view class="point-info">
            <text class="point-number">{{ item.pointNumber }}</text>
            <text class="point-name">{{ item.pointName }}</text>
          </view>
          <view class="audit-status" :class="item.status">
            <text class="material-symbols-outlined">{{ getStatusIcon(item.status) }}</text>
            <text>{{ getStatusText(item.status) }}</text>
          </view>
        </view>
        
        <view class="card-body">
          <view class="info-row">
            <text class="material-symbols-outlined">person</text>
            <text>{{ item.surveyor }}</text>
          </view>
          <view class="info-row">
            <text class="material-symbols-outlined">schedule</text>
            <text>{{ item.submitTime }}</text>
          </view>
        </view>
        
        <view class="card-footer" v-if="item.status === 'pending'">
          <view class="btn-reject" @click.stop="showRejectModal(item)">
            <text class="material-symbols-outlined">close</text>
            <text>驳回</text>
          </view>
          <view class="btn-approve" @click.stop="approveItem(item)">
            <text class="material-symbols-outlined">check</text>
            <text>通过</text>
          </view>
        </view>
      </view>
      
      <view class="loading-more" v-if="loading">
        <text>加载中...</text>
      </view>
      <view class="empty-state" v-if="filteredList.length === 0 && !loading">
        <text class="material-symbols-outlined empty-icon">inbox</text>
        <text class="empty-text">暂无待审核数据</text>
      </view>
    </scroll-view>

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
import { ref, computed, onMounted } from 'vue'
import { surveyResultApi } from '@/utils/api.js'

const activeTab = ref('pending')
const loading = ref(false)
const showReject = ref(false)
const rejectReason = ref('')
const currentItem = ref(null)

const stats = ref({
  pending: 5,
  approved: 128,
  rejected: 12
})

const auditList = ref([
  { id: 1, pointNumber: 'LJ-001', pointName: '柳江大桥东侧排口', status: 'pending', surveyor: '张建国', submitTime: '2024-01-15 14:30' },
  { id: 2, pointNumber: 'LJ-003', pointName: '柳南小区排口', status: 'pending', surveyor: '王明', submitTime: '2024-01-15 10:20' },
  { id: 3, pointNumber: 'LJ-007', pointName: '柳东新区排口', status: 'pending', surveyor: '李强', submitTime: '2024-01-14 16:45' },
  { id: 4, pointNumber: 'LJ-008', pointName: '阳和工业区排口', status: 'pending', surveyor: '赵伟', submitTime: '2024-01-14 11:30' },
  { id: 5, pointNumber: 'LJ-002', pointName: '河西工业园排口', status: 'approved', surveyor: '李志强', submitTime: '2024-01-13 09:15' },
  { id: 6, pointNumber: 'LJ-005', pointName: '柳北化工厂排口', status: 'approved', surveyor: '赵伟', submitTime: '2024-01-12 14:20' },
  { id: 7, pointNumber: 'LJ-006', pointName: '鱼峰区医院排口', status: 'rejected', surveyor: '刘洋', submitTime: '2024-01-11 10:30', rejectReason: '照片不清晰，需补充现场照片' },
])

const filteredList = computed(() => {
  if (activeTab.value === 'pending') {
    return auditList.value.filter(item => item.status === 'pending')
  } else if (activeTab.value === 'approved') {
    return auditList.value.filter(item => item.status === 'approved')
  } else {
    return auditList.value.filter(item => item.status === 'rejected')
  }
})

const getStatusIcon = (status) => {
  const icons = {
    pending: 'pending',
    approved: 'check_circle',
    rejected: 'cancel'
  }
  return icons[status] || 'pending'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回'
  }
  return texts[status] || '待审核'
}

const goBack = () => {
  uni.navigateBack()
}

const changeTab = (tab) => {
  activeTab.value = tab
}

const goToDetail = (item) => {
  uni.navigateTo({
    url: `/pages/audit/audit-detail?id=${item.id}`
  })
}

const showRejectModal = (item) => {
  currentItem.value = item
  rejectReason.value = ''
  showReject.value = true
}

const confirmReject = () => {
  if (!rejectReason.value.trim()) {
    uni.showToast({ title: '请输入驳回原因', icon: 'none' })
    return
  }
  
  // 模拟驳回操作
  uni.showLoading({ title: '处理中...' })
  setTimeout(() => {
    uni.hideLoading()
    const index = auditList.value.findIndex(item => item.id === currentItem.value.id)
    if (index > -1) {
      auditList.value[index].status = 'rejected'
      auditList.value[index].rejectReason = rejectReason.value
    }
    stats.value.pending--
    stats.value.rejected++
    showReject.value = false
    uni.showToast({ title: '已驳回', icon: 'success' })
  }, 1000)
}

const approveItem = (item) => {
  uni.showModal({
    title: '确认通过',
    content: '确认审核通过该点位?',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中...' })
        setTimeout(() => {
          uni.hideLoading()
          const index = auditList.value.findIndex(i => i.id === item.id)
          if (index > -1) {
            auditList.value[index].status = 'approved'
          }
          stats.value.pending--
          stats.value.approved++
          uni.showToast({ title: '已通过', icon: 'success' })
        }, 1000)
      }
    }
  })
}

const loadMore = () => {
  // 加载更多逻辑
}

onMounted(() => {
  // 加载审核列表
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

.audit-container {
  min-height: 100vh;
  background: #f8fafc;
}

.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
  display: inline-block;
  line-height: 1;
}

/* Header */
.audit-header {
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

/* 状态筛选 */
.status-tabs {
  display: flex;
  background: white;
  padding: 24rpx;
  gap: 16rpx;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  background: #f1f5f9;
  font-size: 28rpx;
  color: #64748b;
  position: relative;
}

.tab-item.active {
  background: #f0f9ff;
  color: #0284c7;
  font-weight: 600;
}

.badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 36rpx;
  height: 36rpx;
  background: #dc2626;
  color: white;
  font-size: 22rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

/* 审核列表 */
.audit-list {
  padding: 24rpx;
  height: calc(100vh - 240rpx);
}

.audit-card {
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.point-info {
  display: flex;
  flex-direction: column;
}

.point-number {
  font-size: 24rpx;
  color: #64748b;
  background: #f1f5f9;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  display: inline-block;
  width: fit-content;
}

.point-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #1e293b;
  margin-top: 8rpx;
}

.audit-status {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.audit-status .material-symbols-outlined {
  font-size: 28rpx;
}

.audit-status.pending {
  background: #fef3c7;
  color: #d97706;
}

.audit-status.approved {
  background: #dcfce7;
  color: #16a34a;
}

.audit-status.rejected {
  background: #fee2e2;
  color: #dc2626;
}

.card-body {
  padding: 16rpx 0;
  border-top: 2rpx solid #f1f5f9;
  border-bottom: 2rpx solid #f1f5f9;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 8rpx;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .material-symbols-outlined {
  font-size: 28rpx;
}

.card-footer {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
}

.btn-reject, .btn-approve {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  border-radius: 12rpx;
  font-size: 26rpx;
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

/* 加载和空状态 */
.loading-more {
  text-align: center;
  padding: 32rpx;
  color: #94a3b8;
  font-size: 24rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx;
}

.empty-icon {
  font-size: 96rpx;
  color: #cbd5e1;
}

.empty-text {
  font-size: 28rpx;
  color: #94a3b8;
  margin-top: 16rpx;
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
