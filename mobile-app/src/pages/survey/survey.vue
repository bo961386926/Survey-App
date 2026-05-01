/**
 * 勘察填报页面
 * 根据模板动态渲染表单，支持草稿保存、提交审核
 */
<template>
  <view class="survey-container">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="title">{{ pageTitle }}</text>
      <view v-if="pointInfo" class="point-info">
        <text class="point-name">{{ pointInfo.name }}</text>
        <status-tag :status="pointInfo.status" type="point" />
      </view>
    </view>
    
    <!-- 表单内容 -->
    <scroll-view scroll-y class="form-scroll">
      <dynamic-form
        ref="formRef"
        v-model="formData"
        :fields="formFields"
        :readonly="mode === 'view'"
        :dict-data="dictData"
      />
    </scroll-view>
    
    <!-- 底部操作按钮 -->
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
import StatusTag from '@/components/status-tag/status-tag.vue'

const formRef = ref(null)
const pointId = ref(null)
const mode = ref('new') // new | draft | modify | view
const pointInfo = ref(null)
const template = ref(null)
const formData = reactive({})
const formFields = ref([])
const dictData = reactive({})

// 页面标题
const pageTitle = computed(() => {
  const titles = {
    new: '新建勘察',
    draft: '继续填报',
    modify: '修改重提',
    view: '查看结果'
  }
  return titles[mode.value] || '勘察填报'
})

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  pointId.value = currentPage.options.id
  mode.value = currentPage.options.mode || 'new'
  
  if (pointId.value) {
    loadPointInfo()
    loadTemplate()
    loadDictData()
    loadFormData()
  }
})

// 加载点位信息
async function loadPointInfo() {
  try {
    pointInfo.value = await pointApi.getDetail(pointId.value)
  } catch (error) {
    console.error('加载点位信息失败:', error)
  }
}

// 加载模板
async function loadTemplate() {
  try {
    // 根据排口类型获取模板
    const outfallType = pointInfo.value?.outfallType
    if (outfallType) {
      template.value = await templateApi.getByOutfallType(outfallType)
      formFields.value = template.value?.fields || []
    }
  } catch (error) {
    console.error('加载模板失败:', error)
    uni.showToast({ title: '加载模板失败', icon: 'none' })
  }
}

// 加载字典数据
async function loadDictData() {
  try {
    // 收集所有需要字典数据的字段
    const dictTypes = formFields.value
      .filter(f => f.dictType)
      .map(f => f.dictType)
    
    // 去重
    const uniqueDictTypes = [...new Set(dictTypes)]
    
    // 加载字典数据
    for (const dictType of uniqueDictTypes) {
      try {
        const data = await dictApi.getData(dictType)
        dictData[dictType] = data || []
      } catch (e) {
        dictData[dictType] = []
      }
    }
  } catch (error) {
    console.error('加载字典数据失败:', error)
  }
}

// 加载表单数据
async function loadFormData() {
  try {
    if (mode.value === 'draft') {
      // 从本地草稿加载
      const draft = getDraft(pointId.value)
      if (draft) {
        Object.assign(formData, draft.data)
      }
    } else if (mode.value === 'modify' || mode.value === 'view') {
      // 从服务器加载已有结果
      const result = await resultApi.getDetail(pointId.value)
      if (result) {
        Object.assign(formData, result.formData || {})
      }
    }
  } catch (error) {
    console.error('加载表单数据失败:', error)
  }
}

// 保存草稿
async function saveDraft() {
  const data = formRef.value?.getFormData() || { ...formData }
  
  try {
    // 保存到本地草稿
    saveDraftUtil(pointId.value, data)
    
    // 同时保存到服务器
    await resultApi.saveDraft({
      pointId: pointId.value,
      formData: data
    })
    
    uni.showToast({ title: '草稿已保存', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

// 提交审核
async function submitSurvey() {
  // 表单验证
  const isValid = formRef.value?.validate()
  if (!isValid) {
    uni.showToast({ title: '请完善必填项', icon: 'none' })
    return
  }
  
  uni.showModal({
    title: '确认提交',
    content: '提交后将进入审核流程，是否继续？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const data = formRef.value?.getFormData() || { ...formData }
          
          await resultApi.submit({
            pointId: pointId.value,
            formData: data
          })
          
          // 清除本地草稿
          clearDraft(pointId.value)
          
          uni.showToast({ title: '提交成功', icon: 'success' })
          
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          uni.showToast({ title: error.message || '提交失败', icon: 'none' })
        }
      }
    }
  })
}

// 返回
function goBack() {
  uni.navigateBack()
}
</script>

<style scoped>
.survey-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F5F7FA;
}

.page-header {
  background-color: #fff;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.point-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.point-name {
  font-size: 14px;
  color: #666;
}

.form-scroll {
  flex: 1;
  padding-bottom: 80px;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background-color: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.btn {
  flex: 1;
  height: 44px;
  line-height: 44px;
  font-size: 16px;
  border: none;
  border-radius: 8px;
}

.btn-draft {
  background-color: #f5f5f5;
  color: #666;
}

.btn-submit {
  background-color: #409EFF;
  color: #fff;
}

.btn-default {
  background-color: #f5f5f5;
  color: #666;
}
</style>
