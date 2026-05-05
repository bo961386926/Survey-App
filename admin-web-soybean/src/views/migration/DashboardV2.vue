<template>
  <div class="dashboard-v2">
    <a-page-header title="控制台 V2" style="padding:16px 24px" />
    <a-row :gutter="16" style="padding:0 16px">
      <a-col :xs="24" :md="12">
        <a-card title="今日统计" style="margin-bottom:16px">
          <div class="stats-grid">
            <div v-for="(v,k) in summary" :key="k" class="stat-item">
              <div class="num">{{ v }}</div>
              <div class="name">{{ k }}</div>
            </div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :md="12">
        <a-card title="待办事项" style="margin-bottom:16px">
          <a-list :data-source="todos">
            <template #renderItem="{ item }">
              <a-list-item>{{ item.title }}</a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProjectList } from '@/legacy/api/project'
import { message } from 'ant-design-vue'

const summary = ref({ Projects: 0, Points: 0, Pending: 0, Passed: 0 })
const todos = ref([])

async function load() {
  try {
    const res = await getProjectList()
    const data = res?.data || res || []
    summary.value.Projects = data.length
    summary.value.Points = data.reduce((s, d) => s + (d.pointCount || 0), 0)
    summary.value.Pending = data.filter(d => d.status === 0).length
    summary.value.Passed = data.filter(d => d.status === 1).length
    todos.value = data.slice(0, 5).map(d => ({ title: d.projectName }))
  } catch (err) {
    message.error('加载统计失败')
  }
}

onMounted(load)
</script>

<style scoped>
.stats-grid { display:flex; gap:12px }
.stat-item { flex:1; text-align:center }
.num { font-size:20px; font-weight:700 }
.name { color:#888 }
</style>
