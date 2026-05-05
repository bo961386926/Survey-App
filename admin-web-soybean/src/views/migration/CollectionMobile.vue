<template>
  <div class="collection-mobile">
    <a-page-header title="项目概览" style="padding:16px 24px" />

    <a-row :gutter="16" style="padding:0 16px">
      <a-col :xs="24" :sm="12" :md="8" v-for="item in projects" :key="item.id">
        <a-card :title="item.projectName" style="margin-bottom:16px">
          <p>编号: {{ item.projectCode }}</p>
          <p>负责人: {{ item.manager }}</p>
          <p>状态: <a-tag :color="item.status === 1 ? 'green' : 'red'">{{ item.status === 1 ? '正常' : '禁用' }}</a-tag></p>
          <div style="display:flex;gap:8px;margin-top:12px">
            <a-button size="small" @click="edit(item)">编辑</a-button>
            <a-button size="small" danger @click="del(item.id)">删除</a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-back-top />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProjectList, deleteProject } from '@/legacy/api/project'
import { message, Modal } from 'ant-design-vue'

const projects = ref([])

async function load() {
  try {
    const res = await getProjectList()
    // legacy request wrapper may return data or {data:...}
    projects.value = res?.data || res || []
  } catch (err) {
    console.error(err)
    message.error('加载项目失败')
  }
}

function edit(item) {
  message.info(`编辑 ${item.projectName}`)
}

function del(id) {
  Modal.confirm({
    title: '确认',
    content: '确定删除该项目吗？',
    onOk: async () => {
      try {
        await deleteProject(id)
        message.success('删除成功')
        load()
      } catch (err) {
        message.error('删除失败')
      }
    }
  })
}

onMounted(load)
</script>

<style scoped>
.collection-mobile {
  padding-bottom: 40px;
}
</style>
