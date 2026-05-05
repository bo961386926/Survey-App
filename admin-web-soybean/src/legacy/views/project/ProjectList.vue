<template>
  <div class="project-list">
    <div class="card">
      <div class="card-header">
        <span>项目列表</span>
        <button @click="handleAdd">新建项目</button>
      </div>

      <table class="simple-table">
        <thead>
          <tr><th>项目名称</th><th>项目编号</th><th>负责人</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="p in projects" :key="p.id">
            <td>{{ p.projectName }}</td>
            <td>{{ p.projectCode }}</td>
            <td>{{ p.manager }}</td>
            <td>{{ p.status === 1 ? '正常' : '禁用' }}</td>
            <td>
              <button @click="handleEdit(p)">编辑</button>
              <button @click="handleDelete(p.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="dialogVisible" class="modal">
      <div class="modal-content">
        <h3>{{ dialogTitle }}</h3>
        <div class="form-item">
          <label>项目名称</label>
          <input v-model="form.projectName" />
        </div>
        <div class="form-item">
          <label>项目编号</label>
          <input v-model="form.projectCode" />
        </div>
        <div class="form-item">
          <label>负责人</label>
          <input v-model="form.manager" />
        </div>
        <div class="modal-actions">
          <button @click="dialogVisible = false">取消</button>
          <button @click="handleSubmit">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getProjectList,
  createProject,
  updateProject,
  deleteProject
} from '@/api/project'

const projects = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建项目')
const form = ref({
  id: null,
  projectName: '',
  projectCode: '',
  manager: '',
  status: 1
})

onMounted(() => {
  loadProjects()
})

const loadProjects = async () => {
  try {
    const res = await getProjectList()
    projects.value = res.data
  } catch (error) {
    window.alert('加载项目列表失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新建项目'
  form.value = {
    id: null,
    projectName: '',
    projectCode: '',
    manager: '',
    status: 1
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑项目'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (form.value.id) {
      await updateProject(form.value)
    } else {
      await createProject(form.value)
    }
    window.alert('操作成功')
    dialogVisible.value = false
    loadProjects()
  } catch (error) {
    window.alert('操作失败')
  }
}

const handleDelete = async (id) => {
  if (!window.confirm('确定删除该项目吗？')) return
  try {
    await deleteProject(id)
    window.alert('删除成功')
    loadProjects()
  } catch (error) {
    window.alert('删除失败')
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
