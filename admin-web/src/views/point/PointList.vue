<template>
  <div class="point-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">勘查点位管理</span>
        </div>
      </template>
      <el-table :data="pointList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="pointName" label="点位名称" />
        <el-table-column prop="longitude" label="经度" />
        <el-table-column prop="latitude" label="纬度" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link size="small">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const pointList = ref([])
const loading = ref(false)

const getStatusType = (status) => {
  const types = { 0: 'info', 1: '', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '未勘查', 1: '已提交', 2: '通过', 3: '驳回' }
  return texts[status] || '未知'
}

onMounted(() => {
  loadData()
})

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pointList.value = []
    loading.value = false
  }, 500)
}
</script>

<style scoped>
.point-list {
  padding: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 16px;
  font-weight: 500;
}
</style>