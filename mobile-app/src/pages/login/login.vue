/**
 * 登录页面
 * 支持账号密码登录和短信验证码登录
 */
<template>
  <view class="login-container">
    <!-- 顶部Logo -->
    <view class="logo-section">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="app-name">青泓项目勘察</text>
      <text class="app-desc">移动采集端</text>
    </view>
    
    <!-- 登录表单 -->
    <view class="form-section">
      <!-- 登录方式切换 -->
      <view class="login-type-tabs">
        <view 
          class="tab-item" 
          :class="{ active: loginType === 'password' }"
          @click="loginType = 'password'"
        >
          账号密码
        </view>
        <view 
          class="tab-item" 
          :class="{ active: loginType === 'sms' }"
          @click="loginType = 'sms'"
        >
          短信验证
        </view>
      </view>
      
      <!-- 账号密码登录 -->
      <view v-if="loginType === 'password'" class="login-form">
        <view class="form-item">
          <view class="input-wrapper">
            <text class="icon">👤</text>
            <input 
              class="input" 
              v-model="loginForm.username"
              placeholder="请输入用户名"
              placeholder-class="placeholder"
              :maxlength="50"
            />
          </view>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="icon">🔒</text>
            <input 
              class="input" 
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              placeholder-class="placeholder"
              :maxlength="50"
            />
          </view>
        </view>
      </view>
      
      <!-- 短信验证码登录 -->
      <view v-else class="login-form">
        <view class="form-item">
          <view class="input-wrapper">
            <text class="icon">📱</text>
            <input 
              class="input" 
              v-model="loginForm.phone"
              type="number"
              placeholder="请输入手机号"
              placeholder-class="placeholder"
              :maxlength="11"
            />
          </view>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="icon">🔑</text>
            <input 
              class="input" 
              v-model="loginForm.smsCode"
              type="number"
              placeholder="请输入验证码"
              placeholder-class="placeholder"
              :maxlength="6"
            />
            <button 
              class="sms-btn" 
              :disabled="smsCounting"
              @click="sendSmsCode"
            >
              {{ smsCounting ? `${smsCountdown}s后重发` : '获取验证码' }}
            </button>
          </view>
        </view>
      </view>
      
      <!-- 登录按钮 -->
      <button class="login-btn" :loading="loading" @click="handleLogin">
        登 录
      </button>
      
      <!-- 其他操作 -->
      <view class="other-actions">
        <text class="link" @click="showForgotPassword">忘记密码？</text>
      </view>
    </view>
    
    <!-- 底部版权 -->
    <view class="footer">
      <text>© 2026 青泓项目勘察系统</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { authApi } from '@/utils/api'
import { saveToken, saveUserInfo, saveLoginType, isLogin } from '@/utils/auth'

const loginType = ref('password') // password | sms
const loading = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  phone: '',
  smsCode: ''
})

// 短信验证码倒计时
const smsCounting = ref(false)
const smsCountdown = ref(0)
let smsTimer = null

onMounted(() => {
  // 如果已登录，直接跳转到首页
  if (isLogin()) {
    uni.switchTab({ url: '/pages/home/home' })
  }
})

// 发送短信验证码
async function sendSmsCode() {
  if (!loginForm.phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(loginForm.phone)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return
  }
  
  try {
    await authApi.sendSmsCode({ phone: loginForm.phone })
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    
    // 开始倒计时
    smsCounting.value = true
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        smsCounting.value = false
        clearInterval(smsTimer)
      }
    }, 1000)
  } catch (error) {
    uni.showToast({ title: error.message || '发送失败', icon: 'none' })
  }
}

// 登录
async function handleLogin() {
  if (loginType.value === 'password') {
    if (!loginForm.username) {
      uni.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    if (!loginForm.password) {
      uni.showToast({ title: '请输入密码', icon: 'none' })
      return
    }
  } else {
    if (!loginForm.phone) {
      uni.showToast({ title: '请输入手机号', icon: 'none' })
      return
    }
    if (!loginForm.smsCode) {
      uni.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }
  }
  
  loading.value = true
  try {
    let result
    if (loginType.value === 'password') {
      result = await authApi.login({
        username: loginForm.username,
        password: loginForm.password
      })
    } else {
      result = await authApi.smsLogin({
        phone: loginForm.phone,
        code: loginForm.smsCode
      })
    }
    
    console.log('登录结果:', result)
    
    // 保存登录信息
    saveToken(result.token || result.accessToken)
    saveUserInfo(result.user || result.userInfo || result)
    saveLoginType(result.loginType || 'internal')
    
    uni.showToast({ title: '登录成功', icon: 'success' })
    
    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({ 
        url: '/pages/home/home',
        fail: (err) => {
          console.error('跳转失败:', err)
          uni.reLaunch({ url: '/pages/home/home' })
        }
      })
    }, 500)
  } catch (error) {
    console.error('登录失败:', error)
    uni.showToast({ title: error.message || '登录失败，请检查网络连接', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

// 显示忘记密码提示
function showForgotPassword() {
  uni.showToast({ title: '请联系管理员重置密码', icon: 'none' })
}

// 页面卸载时清除定时器
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (smsTimer) {
    clearInterval(smsTimer)
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 30px 30px;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 50px;
}

.logo {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
}

.app-name {
  font-size: 24px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8px;
}

.app-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.form-section {
  width: 100%;
  background-color: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.login-type-tabs {
  display: flex;
  margin-bottom: 24px;
  background-color: #f5f5f5;
  border-radius: 8px;
  padding: 4px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px;
  font-size: 14px;
  color: #666;
  border-radius: 6px;
}

.tab-item.active {
  background-color: #fff;
  color: #409EFF;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-item {
  margin-bottom: 16px;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 0 12px;
  height: 48px;
}

.icon {
  font-size: 20px;
  margin-right: 12px;
}

.input {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.placeholder {
  color: #c0c4cc;
}

.sms-btn {
  padding: 0 12px;
  height: 32px;
  line-height: 32px;
  font-size: 12px;
  color: #409EFF;
  background-color: #ecf5ff;
  border-radius: 4px;
  border: none;
  margin: 0;
}

.sms-btn[disabled] {
  color: #999;
  background-color: #f5f5f5;
}

.login-btn {
  width: 100%;
  height: 48px;
  line-height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 24px;
}

.other-actions {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.link {
  font-size: 14px;
  color: #409EFF;
}

.footer {
  margin-top: auto;
  padding-top: 30px;
}

.footer text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}
</style>
