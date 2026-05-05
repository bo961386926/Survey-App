<template>
  <div class="login-page">
    <a-card style="max-width:420px;margin:100px auto;padding:24px">
      <div style="text-align:center;margin-bottom:12px">
        <h2>青泓项目勘查系统</h2>
      </div>
      <a-form
        :model="loginForm"
        @submit.prevent="handleLogin"
        layout="vertical"
      >
        <a-form-item label="用户名">
          <a-input v-model:value="loginForm.username" placeholder="请输入用户名" />
        </a-form-item>

        <a-form-item label="密码">
          <a-input-password v-model:value="loginForm.password" placeholder="请输入密码" />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" block @click="handleLogin">登录</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { login } from '@/legacy/api/project'

const router = useRouter()

const loginForm = reactive({
  username: '',
  password: ''
})

async function handleLogin() {
  try {
    const res = await login({ username: loginForm.username, password: loginForm.password })
    // legacy request returns data directly in some implementations
    // try to extract token
    const token = res?.token || res?.data?.token || res
    if (token) {
      localStorage.setItem('token', token)
      message.success('登录成功')
      router.push('/legacy/dashboard')
    } else {
      message.error('登录失败：未返回 token')
    }
  } catch (err) {
    message.error(err?.response?.data?.message || '登录失败')
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg,#667eea 0%,#764ba2 100%);
}
</style>
