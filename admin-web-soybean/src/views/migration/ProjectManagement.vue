<template>
  <div class="project-management">
    <a-page-header title="项目管理" style="padding:16px 24px" />
    <a-card style="margin:16px">
      <div style="display:flex;gap:12px;margin-bottom:12px;align-items:center">
        <a-input-search v-model:value="keyword" placeholder="搜索项目" style="width:320px" @search="onSearch" />
        <a-button type="primary" @click="openCreate">新建项目</a-button>
      </div>

      <a-table :data-source="projects" :loading="loading" rowKey="id" :pagination="pagination" @change="handleTableChange">
        <a-table-column title="项目名称" dataIndex="projectName" />
        <a-table-column title="项目编号" dataIndex="projectCode" />
        <a-table-column title="负责人" dataIndex="manager" />
        <a-table-column title="状态" dataIndex="status" />
        <a-table-column title="操作">
          <template #default="{ record }">
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="doDelete(record.id)">删除</a-button>
          </template>
        </a-table-column>
      </a-table>

      <a-modal v-model:visible="showModal" title="项目" @ok="submit" :okText="editing ? '保存' : '创建'">
        <a-form layout="vertical">
          <a-form-item label="项目名称">
            <a-input v-model:value="form.projectName" />
          </a-form-item>
          <a-form-item label="项目编号">
            <a-input v-model:value="form.projectCode" />
          </a-form-item>
          <a-form-item label="负责人">
            <a-input v-model:value="form.manager" />
          </a-form-item>
        </a-form>
      </a-modal>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProjectPage, createProjectV2, updateProjectV2, deleteProjectV2 } from '@/api/project'
import { message } from 'ant-design-vue'

const projects = ref([])
const loading = ref(false)
const keyword = ref('')
const pagination = ref({ current: 1, pageSize: 10, total: 0 })

const showModal = ref(false)
const editing = ref(null)
const form = ref({ projectName: '', projectCode: '', manager: '' })

async function load(page = 1, pageSize = 10) {
  try {
    loading.value = true
    const params = { keyword: keyword.value, pageNum: page, pageSize }
    const res = await getProjectPage(params)
    const pageData = res?.data || res || { records: [], total: 0, current: page }
    projects.value = pageData.records || []
    pagination.value.current = pageData.current || page
    pagination.value.pageSize = pageData.size || pageSize
    pagination.value.total = pageData.total || 0
  } catch (err) {
    message.error('加载项目失败')
    console.error(err)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editing.value = null
  form.value = { projectName: '', projectCode: '', manager: '' }
  showModal.value = true
}

const openEdit = (rec) => {
  editing.value = rec.id
  form.value = { ...rec }
  showModal.value = true
}

const submit = async () => {
  try {
    if (editing.value) {
      await updateProjectV2(editing.value, form.value)
      message.success('更新成功')
    } else {
      await createProjectV2(form.value)
      message.success('创建成功')
    }
    showModal.value = false
    await load(pagination.value.current, pagination.value.pageSize)
  } catch (err) {
    console.error(err)
    message.error('保存失败')
  }
}

const doDelete = async (id) => {
  if (!window.confirm('确定删除该项目吗？')) return
  try {
    await deleteProjectV2(id)
    message.success('删除成功')
    await load(pagination.value.current, pagination.value.pageSize)
  } catch (err) {
    console.error(err)
    message.error('删除失败')
  }
}

function onSearch(val) {
  load(1, pagination.value.pageSize)
}

onMounted(() => load(1, pagination.value.pageSize))
</script>

<style scoped>
.project-management { padding-bottom:40px }
</style>
