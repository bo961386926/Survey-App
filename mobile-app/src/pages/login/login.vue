<template>
  <view class="login-container">
    <view class="logo">
      <text class="title">青泓项目勘查</text>
    </view>

    <view class="form">
      <input
        class="input"
        v-model="username"
        placeholder="请输入用户名"
        placeholder-class="placeholder"
      />
      <input
        class="input"
        v-model="password"
        type="password"
        placeholder="请输入密码"
        placeholder-class="placeholder"
      />

      <button class="login-btn" @click="handleLogin">登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const username = ref('')
const password = ref('')

const handleLogin = () => {
  uni.showToast({
    title: '登录成功',
    icon: 'success',
    duration: 1500
  })
  setTimeout(() => {
    uni.switchTab({
      url: '/pages/home/home'
    })
  }, 1500)
}
</script>

<style scoped>
.login-container {
  padding: 40rpx;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.logo {
  text-align: center;
  margin-bottom: 100rpx;
}

.title {
  font-size: 48rpx;
  color: white;
  font-weight: bold;
}

.form {
  background: white;
  border-radius: 20rpx;
  padding: 40rpx;
}

.input {
  border: 2rpx solid #e5e5e5;
  border-radius: 10rpx;
  padding: 20rpx;
  margin-bottom: 30rpx;
  font-size: 28rpx;
}

.placeholder {
  color: #999;
}

.login-btn {
  background: #409EFF;
  color: white;
  border-radius: 10rpx;
  padding: 20rpx;
  font-size: 32rpx;
  margin-top: 40rpx;
}
</style>
