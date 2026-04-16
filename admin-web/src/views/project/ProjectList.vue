<template>
  <div class="project-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目列表</span>
          <el-button type="primary" @click="handleAdd">新建项目</el-button>
        </div>
      </template>

      <el-table :data="projects" style="width: 100%">
        <el-table-column prop="projectName" label="项目名称" />
        <el-table-column prop="projectCode" label="项目编号" />
        <el-table-column prop="manager" label="负责人" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="项目名称" required>
          <el-input v-model="form.projectName" />
        </el-form-item>
        <el-form-item label="项目编号">
          <el-input v-model="form.projectCode" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.manager" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
    ElMessage.error('加载项目列表失败')
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
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadProjects()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该项目吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProject(id)
      ElMessage.success('删除成功')
      loadProjects()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
