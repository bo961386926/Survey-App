/**
 * 修改密码页面
 */
<template>
  <view class="change-password-container">
    <!-- 顶部Header -->
    <view class="password-header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-title">
        <text class="title">修改密码</text>
      </view>
    </view>

    <!-- 表单区域 -->
    <view class="form-section">
      <view class="form-item">
        <text class="label">旧密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="formData.oldPassword" 
          placeholder="请输入旧密码"
          maxlength="20"
        />
      </view>
      
      <view class="form-item">
        <text class="label">新密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="formData.newPassword" 
          placeholder="请输入新密码（至少8位）"
          maxlength="20"
        />
      </view>
      
      <view class="form-item">
        <text class="label">确认新密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="formData.confirmPassword" 
          placeholder="请再次输入新密码"
          maxlength="20"
        />
      </view>
    </view>

    <!-- 提示信息 -->
    <view class="tips-section">
      <text class="tips-title">密码要求</text>
      <text class="tips-item">• 密码长度至少8位</text>
      <text class="tips-item">• 建议包含大小写字母、数字和特殊字符</text>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button class="submit-btn" :loading="submitting" @click="handleSubmit">
        确认修改
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { authApi } from '@/utils/api'

const submitting = ref(false)

const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 返回
function goBack() {
  uni.navigateBack()
}

// 提交修改
async function handleSubmit() {
  const { oldPassword, newPassword, confirmPassword } = formData
  
  // 验证
  if (!oldPassword) {
    uni.showToast({ title: '请输入旧密码', icon: 'none' })
    return
  }
  
  if (!newPassword) {
    uni.showToast({ title: '请输入新密码', icon: 'none' })
    return
  }
  
  if (newPassword.length < 8) {
    uni.showToast({ title: '新密码长度至少8位', icon: 'none' })
    return
  }
  
  if (newPassword !== confirmPassword) {
    uni.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
    return
  }
  
  submitting.value = true
  
  try {
    await authApi.changePassword({
      oldPassword,
      newPassword
    })
    
    uni.showToast({ 
      title: '修改成功', 
      icon: 'success',
      duration: 2000 
    })
    
    // 清空表单
    formData.oldPassword = ''
    formData.newPassword = ''
    formData.confirmPassword = ''
    
    // 延迟返回
    setTimeout(() => {
      uni.navigateBack()
    }, 2000)
  } catch (error) {
    console.error('修改密码失败:', error)
    uni.showToast({ title: error.message || '修改失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.change-password-container {
  min-height: 100vh;
  background-color: #F8FAFC;
}

/* Header */
.password-header {
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

/* 表单区域 */
.form-section {
  padding: 32rpx;
}

.form-item {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.label {
  font-size: 26rpx;
  color: #64748B;
  margin-bottom: 12rpx;
  display: block;
}

.input {
  width: 100%;
  height: 88rpx;
  font-size: 28rpx;
  color: #1E293B;
}

/* 提示信息 */
.tips-section {
  margin: 0 32rpx 32rpx;
  padding: 24rpx;
  background: #FFFBEB;
  border-radius: 16rpx;
  border: 1rpx solid #FDE68A;
}

.tips-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #D97706;
  margin-bottom: 8rpx;
  display: block;
}

.tips-item {
  font-size: 24rpx;
  color: #92400E;
  line-height: 1.6;
  display: block;
}

/* 提交按钮 */
.submit-section {
  padding: 32rpx;
  padding-top: 0;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
  color: #FFFFFF;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
}

.submit-btn::after {
  border: none;
}
</style>