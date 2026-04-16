<template>
  <view class="home-container">
    <view class="header">
      <text class="welcome">欢迎使用青泓项目勘查系统</text>
    </view>

    <view class="quick-actions">
      <view class="action-card" @click="navigateToPointList">
        <text class="action-icon">📍</text>
        <text class="action-title">点位管理</text>
        <text class="action-desc">查看和管理勘查点位</text>
      </view>
      <view class="action-card" @click="navigateToSurvey">
        <text class="action-icon">📝</text>
        <text class="action-title">开始勘查</text>
        <text class="action-desc">进行现场信息采集</text>
      </view>
    </view>

    <view class="stats">
      <view class="stat-item">
        <text class="stat-value">0</text>
        <text class="stat-label">待勘查</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">0</text>
        <text class="stat-label">已提交</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">0</text>
        <text class="stat-label">已通过</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app'

const navigateToPointList = () => {
  uni.navigateTo({
    url: '/pages/point-list/point-list'
  })
}

const navigateToSurvey = () => {
  uni.navigateTo({
    url: '/pages/survey/survey'
  })
}
</script>

<style scoped>
.home-container {
  padding: 30rpx;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  margin-bottom: 40rpx;
  text-align: center;
}

.welcome {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.quick-actions {
  margin-bottom: 40rpx;
}

.action-card {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.action-icon {
  font-size: 50rpx;
  margin-right: 20rpx;
}

.action-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
}

.action-desc {
  font-size: 24rpx;
  color: #999;
}

.stats {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  background: white;
  border-radius: 10rpx;
  padding: 20rpx;
  text-align: center;
  width: 200rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}
</style>
