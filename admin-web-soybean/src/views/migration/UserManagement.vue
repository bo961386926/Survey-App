<template>
  <div class="user-management">
    <a-page-header title="用户管理" style="padding:16px 24px" />
    <a-card style="margin:16px">
      <a-table :columns="columns" :data-source="users" rowKey="id" />
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'

const users = ref([])

const columns = [
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '角色', dataIndex: 'role', key: 'role' },
  { title: '邮箱', dataIndex: 'email', key: 'email' }
]

onMounted(() => {
  // placeholder data until backend user API ready
  users.value = [
    { id: 1, username: 'admin', role: '管理员', email: 'admin@example.com' },
    { id: 2, username: 'user1', role: '审阅者', email: 'user1@example.com' }
  ]
})
</script>

<style scoped>
.user-management { padding-bottom:40px }
</style>
