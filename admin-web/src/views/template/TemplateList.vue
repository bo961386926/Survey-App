<template>
  <div class="template-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>模板列表</span>
          <el-button type="primary" @click="handleAdd">新建模板</el-button>
        </div>
      </template>

      <el-table :data="templates" style="width: 100%">
        <el-table-column prop="templateName" label="模板名称" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="info" @click="handlePreview(row)">预览</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="模板名称" required>
          <el-input v-model="form.templateName" />
        </el-form-item>
        <el-form-item label="字段配置" required>
          <el-input
            v-model="form.fieldsJson"
            type="textarea"
            :rows="10"
            placeholder='[{"label":"排口名称","type":"input","required":true}]'
          />
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
  getTemplateList,
  createTemplate,
  updateTemplate,
  deleteTemplate
} from '@/api/template'

const templates = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建模板')
const form = ref({
  id: null,
  templateName: '',
  fieldsJson: ''
})

onMounted(() => {
  loadTemplates()
})

const loadTemplates = async () => {
  try {
    const res = await getTemplateList()
    templates.value = res.data
  } catch (error) {
    ElMessage.error('加载模板列表失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新建模板'
  form.value = {
    id: null,
    templateName: '',
    fieldsJson: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑模板'
  form.value = { ...row }
  dialogVisible.value = true
}

const handlePreview = (row) => {
  try {
    const fields = JSON.parse(row.fieldsJson)
    ElMessage.info(`模板包含 ${fields.length} 个字段`)
  } catch (error) {
    ElMessage.error('模板配置格式错误')
  }
}

const handleSubmit = async () => {
  try {
    JSON.parse(form.value.fieldsJson) // 验证 JSON 格式
    if (form.value.id) {
      await updateTemplate(form.value)
    } else {
      await createTemplate(form.value)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadTemplates()
  } catch (error) {
    if (error instanceof SyntaxError) {
      ElMessage.error('字段配置格式错误，请检查 JSON 格式')
    } else {
      ElMessage.error('操作失败')
    }
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该模板吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTemplate(id)
      ElMessage.success('删除成功')
      loadTemplates()
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
