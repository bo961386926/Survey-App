<template>
  <div class="point-list">
    <a-page-header title="点位管理" style="padding:16px 24px" />
    <a-card style="margin:16px">
      <div style="display:flex;gap:12px;margin-bottom:12px;align-items:center">
        <a-select v-model:value="selectedProject" placeholder="选择项目" style="width:240px">
          <a-select-option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</a-select-option>
        </a-select>
        <a-button type="primary" @click="openCreate">新建点位</a-button>
      </div>

      <a-table :data-source="points" :loading="loading" rowKey="id" :pagination="pagination" @change="handleTableChange">
        <a-table-column title="点位名称" dataIndex="pointName" />
        <a-table-column title="点位编号" dataIndex="pointCode" />
        <a-table-column title="所属项目" dataIndex="projectName" />
        <a-table-column title="经度" dataIndex="lng" />
        <a-table-column title="纬度" dataIndex="lat" />
        <a-table-column title="操作">
          <template #default="{ record }">
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="doDelete(record.id)">删除</a-button>
          </template>
        </a-table-column>
      </a-table>

      <a-modal v-model:visible="showModal" title="点位" @ok="submit" :okText="editing ? '保存' : '创建'">
        <a-form layout="vertical">
          <a-form-item label="点位名称">
            <a-input v-model:value="form.pointName" />
          </a-form-item>
          <a-form-item label="点位编号">
            <a-input v-model:value="form.pointCode" />
          </a-form-item>
          <a-form-item label="所属项目">
            <a-select v-model:value="form.projectId" placeholder="选择项目">
              <a-select-option v-for="p in projects" :key="p.id" :value="p.id">{{ p.projectName }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="经度">
            <a-input v-model:value="form.lng" />
          </a-form-item>
          <a-form-item label="纬度">
            <a-input v-model:value="form.lat" />
          </a-form-item>
        </a-form>
      </a-modal>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  getPointList,
  getPointPage,
  createPoint,
  updatePoint,
  deletePoint
} from '@/api/point'
import { getProjectList } from '@/api/project'



const projects = ref([])
const selectedProject = ref(null)

const points = ref([])
const loading = ref(false)

const pagination = ref({ current: 1, pageSize: 10, total: 0 })

const showModal = ref(false)
const editing = ref(null)
const form = ref({ pointName: '', pointCode: '', lng: '', lat: '', projectId: null })

async function load(page = 1, pageSize = 10) {
  try {
    loading.value = true
    const params = { projectId: selectedProject.value, pageNum: page, pageSize }
    const res = await getPointPage(params)
    const pageData = res?.data || res || { records: [], total: 0, current: page }
    points.value = pageData.records || []
    pagination.value.current = pageData.current || page
    pagination.value.pageSize = pageData.size || pageSize
    pagination.value.total = pageData.total || 0
  } catch (err) {
    message.error('加载点位失败')
    console.error('load points error', err)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editing.value = null
  form.value = { pointName: '', pointCode: '', lng: '', lat: '', projectId: null }
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
      await updatePoint(editing.value, form.value)
      message.success('更新成功')
    } else {
      await createPoint(form.value)
      message.success('创建成功')
    }
    showModal.value = false
    await load()
  } catch (err) {
    console.error(err)
    message.error('保存失败')
  }
}

const doDelete = async (id) => {
  if (!window.confirm('确定删除该点位吗？')) return
  try {
    await deletePoint(id)
    message.success('删除成功')
    await load()
  } catch (err) {
    console.error(err)
    message.error('删除失败')
  }
}



async function loadProjects() {
  try {
    const res = await getProjectList()
    const data = res?.data || res || []
    projects.value = data
  } catch (err) {
    console.error('load projects error', err)
  }
}

onMounted(() => {
  load(1, pagination.value.pageSize)
  loadProjects()
})

function handleTableChange(pag) {
  const cur = (pag && pag.current) || pagination.value.current
  const size = (pag && pag.pageSize) || pagination.value.pageSize
  load(cur, size)
}
</script>

<style scoped>
.point-list { padding-bottom:40px }
</style>
