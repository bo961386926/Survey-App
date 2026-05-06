<template>
  <div class="audit-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">审核管理</span>
        </div>
      </template>
      <el-table :data="auditList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="pointName" label="点位名称" />
        <el-table-column prop="surveyUser" label="勘查人员" />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.auditStatus)">
              {{ getAuditStatusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link size="small">查看</el-button>
            <el-button type="success" link size="small">通过</el-button>
            <el-button type="danger" link size="small">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const auditList = ref([])
const loading = ref(false)

const getAuditStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getAuditStatusText = (status) => {
  const texts = { 0: '待审', 1: '通过', 2: '驳回' }
  return texts[status] || '未知'
}

onMounted(() => {
  loadData()
})

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    auditList.value = []
    loading.value = false
  }, 500)
}
</script>

<style scoped>
.audit-list {
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