<template>
  <div class="dashboard">
    <h2>数据统计</h2>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <el-icon class="icon" color="#409EFF"><Document /></el-icon>
            <div class="info">
              <div class="value">0</div>
              <div class="label">项目总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <el-icon class="icon" color="#67C23A"><Location /></el-icon>
            <div class="info">
              <div class="value">0</div>
              <div class="label">点位总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <el-icon class="icon" color="#E6A23C"><List /></el-icon>
            <div class="info">
              <div class="value">0</div>
              <div class="label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <el-icon class="icon" color="#F56C6C"><Check /></el-icon>
            <div class="info">
              <div class="value">0</div>
              <div class="label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div>最近项目</div>
          </template>
          <el-table :data="projects" style="width: 100%">
            <el-table-column prop="projectName" label="项目名称" />
            <el-table-column prop="projectCode" label="项目编号" />
            <el-table-column prop="manager" label="负责人" />
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProjectList } from '@/api/project'

const projects = ref([])

onMounted(() => {
  loadProjects()
})

const loadProjects = async () => {
  try {
    const res = await getProjectList()
    projects.value = res.data
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.icon {
  font-size: 32px;
}

.info .value {
  font-size: 24px;
  font-weight: bold;
}

.info .label {
  color: #909399;
}
</style>
