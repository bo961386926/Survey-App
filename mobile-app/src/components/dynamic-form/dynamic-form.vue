/**
 * 动态表单组件
 * 根据模板配置动态渲染表单字段
 * 与 PC 设计器 FieldSchema 兼容
 */
<template>
  <view class="dynamic-form">
    <view
      v-for="(field, index) in visibleFields"
      :key="field.id || index"
      class="form-item"
      :class="{ required: field.required }"
    >
      <!-- 字段标签 -->
      <view class="form-label">
        <text v-if="field.required" class="required-mark">*</text>
        <text class="label-text">{{ field.label }}</text>
      </view>

      <!-- Input -->
      <input v-if="field.type === 'input'"
        class="form-input"
        v-model="formData[field.id]"
        :placeholder="field.placeholder || '请输入'"
        :maxlength="field.validation?.maxLength || 100"
        :disabled="field.disabled || readonly"
      />

      <!-- Number -->
      <input v-else-if="field.type === 'number'"
        class="form-input"
        type="digit"
        v-model="formData[field.id]"
        :placeholder="field.placeholder || '请输入数字'"
        :disabled="field.disabled || readonly"
      />

      <!-- Textarea -->
      <textarea v-else-if="field.type === 'textarea'"
        class="form-textarea"
        v-model="formData[field.id]"
        :placeholder="field.placeholder || '请输入内容'"
        :maxlength="field.validation?.maxLength || 500"
        :disabled="field.disabled || readonly"
      />

      <!-- Select -->
      <picker v-else-if="field.type === 'select'"
        :range="getOptions(field)"
        range-key="label"
        :disabled="field.disabled || readonly"
        @change="onSelectChange($event, field)"
      >
        <view class="form-picker">
          <text :class="{ placeholder: !formData[field.id] }">
            {{ getSelectLabel(field) || field.placeholder || '请选择' }}
          </text>
        </view>
      </picker>

      <!-- Radio -->
      <view v-else-if="field.type === 'radio'" class="radio-group">
        <label v-for="option in getOptions(field)" :key="option.value" class="radio-item">
          <radio :value="option.value"
            :checked="formData[field.id] === option.value"
            :disabled="field.disabled || readonly"
            @change="formData[field.id] = option.value; clearError(field.id)"
          />
          <text>{{ option.label }}</text>
        </label>
      </view>

      <!-- Checkbox -->
      <view v-else-if="field.type === 'checkbox'" class="checkbox-group">
        <label v-for="option in getOptions(field)" :key="option.value" class="checkbox-item">
          <checkbox :value="option.value"
            :checked="isChecked(field, option.value)"
            :disabled="field.disabled || readonly"
            @change="toggleCheckbox(field, option.value)"
          />
          <text>{{ option.label }}</text>
        </label>
      </view>

      <!-- Switch -->
      <switch v-else-if="field.type === 'switch'"
        :checked="!!formData[field.id]"
        :disabled="field.disabled || readonly"
        @change="formData[field.id] = !!$event.detail.value"
      />

      <!-- Date -->
      <picker v-else-if="field.type === 'date'"
        mode="date"
        :disabled="field.disabled || readonly"
        @change="formData[field.id] = $event.detail.value; clearError(field.id)"
      >
        <view class="form-picker">
          <text :class="{ placeholder: !formData[field.id] }">
            {{ formData[field.id] || '请选择日期' }}
          </text>
        </view>
      </picker>

      <!-- Image -->
      <image-uploader v-else-if="field.type === 'image'"
        v-model="formData[field.id]"
        :max-count="field.imageConfig?.maxCount || 9"
        :camera-only="field.imageConfig?.cameraOnly || false"
      />

      <!-- Location -->
      <location-picker v-else-if="field.type === 'location'"
        v-model="formData[field.id]"
      />

      <!-- Divider -->
      <view v-else-if="field.type === 'divider'" class="form-divider"></view>

      <!-- 错误提示 -->
      <view v-if="errors[field.id]" class="error-tip">
        <text>{{ errors[field.id] }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import ImageUploader from '@/components/image-uploader/image-uploader.vue'
import LocationPicker from '@/components/location-picker/location-picker.vue'

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
  fields: { type: Array, default: () => [] },
  readonly: { type: Boolean, default: false },
  dictData: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'validate'])

const formData = reactive({ ...props.modelValue })
const errors = reactive({})

// 联动规则引擎：计算可见字段
const visibleFields = computed(() => {
  return props.fields.filter(field => {
    if (!field.linkageRules || field.linkageRules.length === 0) return true
    // 所有条件满足（AND逻辑）才隐藏
    const shouldHide = field.linkageRules.some(rule => {
      const conditionsMet = rule.conditions.every(cond => {
        const triggerValue = formData[cond.fieldId]
        switch (cond.operator) {
          case 'eq': return triggerValue === cond.value
          case 'neq': return triggerValue !== cond.value
          case 'in': return Array.isArray(triggerValue) && triggerValue.includes(cond.value)
          case 'contains': return String(triggerValue || '').includes(String(cond.value))
          default: return false
        }
      })
      return conditionsMet && (rule.action === 'hide' || rule.action === 'clear')
    })
    return !shouldHide
  })
})

watch(formData, (newVal) => {
  emit('update:modelValue', { ...newVal })
}, { deep: true })

watch(() => props.modelValue, (newVal) => {
  Object.assign(formData, newVal)
}, { deep: true })

function getOptions(field) {
  if (field.options && field.options.length > 0) return field.options
  if (field.optionSource?.type === 'dict' && field.optionSource?.dictCode) {
    const dictData = props.dictData[field.optionSource.dictCode]
    if (dictData) return dictData
  }
  return []
}

function getSelectLabel(field) {
  const options = getOptions(field)
  const option = options.find(o => o.value === formData[field.id])
  return option ? option.label : ''
}

function isChecked(field, value) {
  const val = formData[field.id]
  return Array.isArray(val) ? val.includes(value) : false
}

function toggleCheckbox(field, value) {
  if (!Array.isArray(formData[field.id])) formData[field.id] = []
  const arr = formData[field.id]
  const idx = arr.indexOf(value)
  if (idx >= 0) arr.splice(idx, 1)
  else arr.push(value)
  clearError(field.id)
}

function onSelectChange(e, field) {
  const options = getOptions(field)
  formData[field.id] = options[e.detail.value]?.value
  clearError(field.id)
}

function clearError(key) { delete errors[key] }

// 表单验证
function validate() {
  let isValid = true
  for (const field of props.fields) {
    if (field.disabled) continue
    const value = formData[field.id]
    const v = field.validation

    // 必填校验
    if (field.required) {
      if (value === null || value === undefined || value === '' ||
          (Array.isArray(value) && value.length === 0)) {
        errors[field.id] = `${field.label}不能为空`
        isValid = false; continue
      }
    }

    if (value === undefined || value === null || value === '') continue

    // 数值范围校验
    if (field.type === 'number' && v) {
      const num = parseFloat(value)
      if (v.min !== undefined && num < v.min) { errors[field.id] = `不能小于${v.min}`; isValid = false }
      if (v.max !== undefined && num > v.max) { errors[field.id] = `不能大于${v.max}`; isValid = false }
    }

    // 格式校验
    if (v?.pattern && value) {
      const regex = new RegExp(v.pattern)
      if (!regex.test(value)) { errors[field.id] = '格式不正确'; isValid = false }
    }

    // 长度校验
    if (typeof value === 'string') {
      if (v?.minLength && value.length < v.minLength) { errors[field.id] = `不能少于${v.minLength}个字符`; isValid = false }
      if (v?.maxLength && value.length > v.maxLength) { errors[field.id] = `不能超过${v.maxLength}个字符`; isValid = false }
    }
  }
  emit('validate', { isValid, errors: { ...errors } })
  return isValid
}

function getFormData() { return { ...formData } }

defineExpose({ validate, getFormData })
</script>

<style scoped>
.dynamic-form { width: 100%; }
.form-item { margin-bottom: 20px; padding: 0 16px; }
.form-label { display: flex; align-items: center; margin-bottom: 8px; }
.required-mark { color: #F56C6C; margin-right: 4px; }
.label-text { font-size: 14px; color: #333; font-weight: 500; }
.form-input, .form-textarea, .form-picker {
  width: 100%; padding: 10px 12px;
  border: 1px solid #DCDFE6; border-radius: 6px;
  font-size: 14px; background-color: #fff; box-sizing: border-box;
}
.form-textarea { min-height: 80px; }
.form-picker { display: flex; align-items: center; min-height: 40px; }
.placeholder { color: #C0C4CC; }
.checkbox-group, .radio-group { display: flex; flex-wrap: wrap; gap: 12px; }
.checkbox-item, .radio-item { display: flex; align-items: center; gap: 6px; }
.checkbox-item text, .radio-item text { font-size: 14px; color: #333; }
.form-divider { height: 1px; background: #eee; margin: 12px 0; }
.error-tip { margin-top: 4px; }
.error-tip text { font-size: 12px; color: #F56C6C; }
</style>
