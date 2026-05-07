/**
 * 勘察填报页面
 * 根据模板动态渲染表单，支持草稿保存、提交审核
 */
<template>
  <view class="survey-container">
    <view class="page-header">
      <text class="title">{{ pageTitle }}</text>
      <view v-if="pointInfo" class="point-info">
        <text class="point-name">{{ pointInfo.name }}</text>
      </view>
    </view>
    <scroll-view scroll-y class="form-scroll">
      <dynamic-form
        ref="formRef"
        v-model="formData"
        :fields="formFields"
        :readonly="mode === 'view'"
        :dict-data="dictData"
      />
    </scroll-view>
    <view v-if="mode !== 'view'" class="bottom-actions">
      <button class="btn btn-draft" @click="saveDraft">保存草稿</button>
      <button class="btn btn-submit" @click="submitSurvey">提交审核</button>
    </view>
    <view v-else class="bottom-actions">
      <button class="btn btn-default" @click="goBack">返回</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { pointApi, templateApi, resultApi, dictApi } from '@/utils/api'
import { saveDraft as saveDraftUtil, getDraft, clearDraft } from '@/utils/draft'
import DynamicForm from '@/components/dynamic-form/dynamic-form.vue'

const formRef = ref(null)
const pointId = ref(null)
const mode = ref('new')
const pointInfo = ref(null)
const template = ref(null)
const formData = reactive({})
const formFields = ref([])
const dictData = reactive({})

const pageTitle = computed(() => ({
  new: '新建勘察', draft: '继续填报', modify: '修改重提', view: '查看结果'
})[mode.value] || '勘察填报')

onMounted(() => {
  const pages = getCurrentPages()
  const cp = pages[pages.length - 1]
  pointId.value = cp.options.id
  mode.value = cp.options.mode || 'new'
  if (pointId.value) { loadPointInfo(); loadTemplate(); loadFormData() }
})

async function loadPointInfo() {
  try { pointInfo.value = await pointApi.getDetail(pointId.value) } catch (e) { console.error(e) }
}

// 加载模板 - 兼容设计器 FieldSchema JSON
async function loadTemplate() {
  try {
    const outfallType = pointInfo.value?.outfallType
    if (!outfallType) return
    const res = await templateApi.getByOutfallType(outfallType)
    template.value = res
    const raw = res?.fieldsJson || res?.fields || '[]'
    formFields.value = typeof raw === 'string' ? JSON.parse(raw) : raw
    // 加载字典数据
    loadDictData()
  } catch (error) {
    console.error('加载模板失败:', error)
    uni.showToast({ title: '加载模板失败', icon: 'none' })
  }
}

async function loadDictData() {
  const dictCodes = [...new Set(formFields.value
    .filter(f => f.optionSource?.type === 'dict' && f.optionSource?.dictCode)
    .map(f => f.optionSource.dictCode))]
  for (const code of dictCodes) {
    try { dictData[code] = await dictApi.getData(code) || [] } catch { dictData[code] = [] }
  }
}

async function loadFormData() {
  try {
    if (mode.value === 'draft') {
      const draft = getDraft(pointId.value)
      if (draft) Object.assign(formData, draft.data)
    } else if (mode.value === 'modify' || mode.value === 'view') {
      const result = await resultApi.getDetail(pointId.value)
      if (result) Object.assign(formData, result.formData || {})
    }
  } catch (e) { console.error(e) }
}

async function saveDraft() {
  const data = formRef.value?.getFormData() || { ...formData }
  try {
    saveDraftUtil(pointId.value, data)
    await resultApi.saveDraft({ pointId: pointId.value, formData: data })
    uni.showToast({ title: '草稿已保存', icon: 'success' })
  } catch { uni.showToast({ title: '保存失败', icon: 'none' }) }
}

async function submitSurvey() {
  const isValid = formRef.value?.validate()
  if (!isValid) { uni.showToast({ title: '请完善必填项', icon: 'none' }); return }
  uni.showModal({
    title: '确认提交',
    content: '提交后将进入审核流程，是否继续？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        const data = formRef.value?.getFormData() || { ...formData }
        await resultApi.submit({ pointId: pointId.value, formData: data })
        clearDraft(pointId.value)
        uni.showToast({ title: '提交成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1500)
      } catch (error) { uni.showToast({ title: error.message || '提交失败', icon: 'none' }) }
    }
  })
}

function goBack() { uni.navigateBack() }
</script>

<style scoped>
.survey-container { height: 100vh; display: flex; flex-direction: column; background-color: #F5F7FA; }
.page-header { background-color: #fff; padding: 16px; border-bottom: 1px solid #eee; }
.title { font-size: 18px; font-weight: 500; color: #333; }
.point-info { margin-top: 8px; }
.point-name { font-size: 14px; color: #666; }
.form-scroll { flex: 1; padding-bottom: 80px; }
.bottom-actions { position: fixed; bottom: 0; left: 0; right: 0; display: flex; gap: 12px; padding: 12px 16px; background-color: #fff; }
.btn { flex: 1; height: 44px; line-height: 44px; font-size: 16px; border: none; border-radius: 8px; }
.btn-draft { background-color: #f5f5f5; color: #666; }
.btn-submit { background-color: #409EFF; color: #fff; }
.btn-default { background-color: #f5f5f5; color: #666; }
</style>
