<template>
  <div class="template-list">
    <div class="card">
      <div class="card-header">
        <span>模板列表</span>
        <button @click="handleAdd">新建模板</button>
      </div>

      <table class="simple-table">
        <thead>
          <tr><th>模板名称</th><th>创建时间</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="t in templates" :key="t.id">
            <td>{{ t.templateName }}</td>
            <td>{{ t.createTime }}</td>
            <td>
              <button @click="handleEdit(t)">编辑</button>
              <button @click="handlePreview(t)">预览</button>
              <button @click="handleDelete(t.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="dialogVisible" class="modal">
      <div class="modal-content">
        <h3>{{ dialogTitle }}</h3>
        <div class="form-item">
          <label>模板名称</label>
          <input v-model="form.templateName" />
        </div>
        <div class="form-item">
          <label>字段配置 (JSON)</label>
          <textarea v-model="form.fieldsJson" rows="8" placeholder='[{"label":"排口名称","type":"input","required":true}]'></textarea>
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
    window.alert('加载模板列表失败')
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
    window.alert(`模板包含 ${fields.length} 个字段`)
  } catch (error) {
    window.alert('模板配置格式错误')
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
    window.alert('操作成功')
    dialogVisible.value = false
    loadTemplates()
  } catch (error) {
    if (error instanceof SyntaxError) {
      window.alert('字段配置格式错误，请检查 JSON 格式')
    } else {
      window.alert('操作失败')
    }
  }
}

const handleDelete = async (id) => {
  if (!window.confirm('确定删除该模板吗？')) return
  try {
    await deleteTemplate(id)
    window.alert('删除成功')
    loadTemplates()
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
