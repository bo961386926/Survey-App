<template>
  <div class="login-container">
    <div class="login-card">
      <div class="card-header">
        <span>青泓项目勘查系统</span>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-item">
          <label>用户名</label>
          <input v-model="loginForm.username" placeholder="请输入用户名" />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </div>
        <div class="form-item">
          <button type="submit" class="primary" style="width:100%">登录</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/project'

const router = useRouter()
const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    window.alert('请输入用户名和密码')
    return
  }
  try {
    const res = await login(loginForm)
    localStorage.setItem('token', res.data.token)
    window.alert('登录成功')
    router.push('/layout/dashboard')
  } catch (error) {
    window.alert(error?.response?.data?.message || '登录失败')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}
</style>
