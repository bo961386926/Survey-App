<template>
  <view class="point-list">
    <view class="search-bar">
      <input class="search-input" placeholder="搜索点位名称" />
    </view>

    <view class="point-item" v-for="item in points" :key="item.id" @click="viewDetail(item)">
      <view class="point-header">
        <text class="point-name">{{ item.pointName }}</text>
        <text class="point-status" :class="'status-' + item.status">
          {{ getStatusText(item.status) }}
        </text>
      </view>
      <view class="point-info">
        <text class="point-coords">📍 {{ formatCoords(item) }}</text>
      </view>
    </view>

    <view v-if="points.length === 0" class="empty">
      <text>暂无点位数据</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from '@dcloudio/uni-app'

const points = ref([])

const getStatusText = (status) => {
  const map = { 0: '未勘查', 1: '已提交', 2: '已通过', 3: '已驳回' }
  return map[status] || '未勘查'
}

const formatCoords = (item) => {
  if (!item.longitude || !item.latitude) return '暂无坐标'
  return `${item.longitude.toFixed(6)}, ${item.latitude.toFixed(6)}`
}

const viewDetail = (item) => {
  uni.navigateTo({
    url: `/pages/point-detail/point-detail?id=${item.id}`
  })
}

onLoad(() => {
  // TODO: 加载点位数据
})
</script>

<style scoped>
.point-list {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.search-bar {
  background: white;
  border-radius: 10rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.search-input {
  padding: 15rpx;
  border: 2rpx solid #e5e5e5;
  border-radius: 8rpx;
}

.point-item {
  background: white;
  border-radius: 10rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.point-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.point-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.point-status {
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
}

.status-0 { background: #e5e5e5; color: #666; }
.status-1 { background: #e6a23c; color: #fff; }
.status-2 { background: #67c23a; color: #fff; }
.status-3 { background: #f56c6c; color: #fff; }

.point-info {
  color: #999;
  font-size: 24rpx;
}

.point-coords {
  color: #67c23a;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}
</style>
