<template>
  <div class="dashboard-analytics">
    <a-page-header title="分析面板" style="padding:16px 24px" />

    <a-row :gutter="16" style="padding:0 16px">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <div class="stat">
            <div class="value">{{ stats.projects }}</div>
            <div class="label">项目总数</div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <div class="stat">
            <div class="value">{{ stats.points }}</div>
            <div class="label">点位总数</div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <div class="stat">
            <div class="value">{{ stats.pending }}</div>
            <div class="label">待审核</div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card>
          <div class="stat">
            <div class="value">{{ stats.passed }}</div>
            <div class="label">已通过</div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="最近项目" style="margin:16px">
      <a-table :data-source="recent" :columns="columns" rowKey="id" />
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProjectList } from '@/legacy/api/project'
import { message } from 'ant-design-vue'

const stats = ref({ projects: 0, points: 0, pending: 0, passed: 0 })
const recent = ref([])

const columns = [
  { title: '项目名称', dataIndex: 'projectName', key: 'projectName' },
  { title: '项目编号', dataIndex: 'projectCode', key: 'projectCode' },
  { title: '负责人', dataIndex: 'manager', key: 'manager' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' }
]

async function load() {
  try {
    const res = await getProjectList()
    const data = res?.data || res || []
    recent.value = data.slice(0, 5)
    stats.value.projects = data.length
    // points/pending/passed are placeholders until backend provides
    stats.value.points = data.reduce((acc, cur) => acc + (cur.pointCount || 0), 0)
    stats.value.pending = data.filter(d => d.status === 0).length
    stats.value.passed = data.filter(d => d.status === 1).length
  } catch (err) {
    console.error(err)
    message.error('无法加载数据')
  }
}

onMounted(load)
</script>

<style scoped>
.stat { text-align:center }
.value { font-size:24px; font-weight:700 }
.label { color:#888 }
</style>
