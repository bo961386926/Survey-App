/**
 * 动态表单组件
 * 根据模板配置动态渲染表单字段
 * 支持：Input、Number、Select、MultiSelect、Radio、Checkbox、Switch、Date、Image、Location、Textarea
 */
<template>
  <view class="dynamic-form">
    <view 
      v-for="(field, index) in fields" 
      :key="field.id || index" 
      class="form-item"
      :class="{ 'required': field.required, 'hidden': isHidden(field) }"
    >
      <!-- 字段标签 -->
      <view class="form-label">
        <text v-if="field.required" class="required-mark">*</text>
        <text class="label-text">{{ field.label }}</text>
      </view>
      
      <!-- Input -->
      <input 
        v-if="field.type === 'input'"
        class="form-input"
        v-model="formData[field.key]"
        :placeholder="field.placeholder || '请输入'"
        :maxlength="field.maxlength || 100"
        :disabled="field.disabled || readonly"
      />
      
      <!-- Number -->
      <input 
        v-else-if="field.type === 'number'"
        class="form-input"
        type="digit"
        v-model="formData[field.key]"
        :placeholder="field.placeholder || '请输入数字'"
        :disabled="field.disabled || readonly"
      />
      
      <!-- Textarea -->
      <textarea 
        v-else-if="field.type === 'textarea'"
        class="form-textarea"
        v-model="formData[field.key]"
        :placeholder="field.placeholder || '请输入内容'"
        :maxlength="field.maxlength || 500"
        :disabled="field.disabled || readonly"
      />
      
      <!-- Select -->
      <picker 
        v-else-if="field.type === 'select'"
        :range="getOptions(field)"
        range-key="label"
        :disabled="field.disabled || readonly"
        @change="onSelectChange($event, field)"
      >
        <view class="form-picker">
          <text :class="{ 'placeholder': !formData[field.key] }">
            {{ getSelectLabel(field) || field.placeholder || '请选择' }}
          </text>
        </view>
      </picker>
      
      <!-- MultiSelect -->
      <view v-else-if="field.type === 'multiselect'" class="checkbox-group">
        <label 
          v-for="option in getOptions(field)" 
          :key="option.value"
          class="checkbox-item"
        >
          <checkbox 
            :value="option.value" 
            :checked="isChecked(field, option.value)"
            :disabled="field.disabled || readonly"
            @change="onCheckboxChange($event, field, option.value)"
          />
          <text>{{ option.label }}</text>
        </label>
      </view>
      
      <!-- Radio -->
      <view v-else-if="field.type === 'radio'" class="radio-group">
        <label 
          v-for="option in getOptions(field)" 
          :key="option.value"
          class="radio-item"
        >
          <radio 
            :value="option.value" 
            :checked="formData[field.key] === option.value"
            :disabled="field.disabled || readonly"
            @change="onRadioChange($event, field, option.value)"
          />
          <text>{{ option.label }}</text>
        </label>
      </view>
      
      <!-- Checkbox (单个开关) -->
      <switch 
        v-else-if="field.type === 'switch'"
        :checked="!!formData[field.key]"
        :disabled="field.disabled || readonly"
        @change="onSwitchChange($event, field)"
      />
      
      <!-- Date -->
      <picker 
        v-else-if="field.type === 'date'"
        mode="date"
        :disabled="field.disabled || readonly"
        @change="onDateChange($event, field)"
      >
        <view class="form-picker">
          <text :class="{ 'placeholder': !formData[field.key] }">
            {{ formData[field.key] || '请选择日期' }}
          </text>
        </view>
      </picker>
      
      <!-- Image -->
      <image-uploader
        v-else-if="field.type === 'image'"
        v-model="formData[field.key]"
        :max-count="field.maxCount || 9"
        :camera-only="field.cameraOnly || false"
        :tip-text="field.tipText"
      />
      
      <!-- Location -->
      <location-picker
        v-else-if="field.type === 'location'"
        v-model="formData[field.key]"
        :target-location="field.targetLocation"
        :max-distance="field.maxDistance || 100"
      />
      
      <!-- 错误提示 -->
      <view v-if="errors[field.key]" class="error-tip">
        <text>{{ errors[field.key] }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import ImageUploader from '@/components/image-uploader/image-uploader.vue'
import LocationPicker from '@/components/location-picker/location-picker.vue'

const props = defineProps({
  // 表单数据
  modelValue: {
    type: Object,
    default: () => ({})
  },
  // 字段配置
  fields: {
    type: Array,
    default: () => []
  },
  // 是否只读
  readonly: {
    type: Boolean,
    default: false
  },
  // 字典数据缓存
  dictData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'validate'])

const formData = reactive({ ...props.modelValue })
const errors = reactive({})

// 监听表单数据变化
watch(formData, (newVal) => {
  emit('update:modelValue', { ...newVal })
}, { deep: true })

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  Object.assign(formData, newVal)
}, { deep: true })

// 获取选项
function getOptions(field) {
  if (field.options) {
    return field.options
  }
  if (field.dictType && props.dictData[field.dictType]) {
    return props.dictData[field.dictType]
  }
  return []
}

// 获取Select显示文本
function getSelectLabel(field) {
  const options = getOptions(field)
  const option = options.find(o => o.value === formData[field.key])
  return option ? option.label : ''
}

// 检查是否选中
function isChecked(field, value) {
  const val = formData[field.key]
  if (Array.isArray(val)) {
    return val.includes(value)
  }
  return false
}

// 判断字段是否隐藏
function isHidden(field) {
  if (field.hiddenRule) {
    const { key, value } = field.hiddenRule
    return formData[key] !== value
  }
  return false
}

// Select变化
function onSelectChange(e, field) {
  const options = getOptions(field)
  formData[field.key] = options[e.detail.value]?.value
  clearError(field.key)
}

// Checkbox变化
function onCheckboxChange(e, field, value) {
  if (!Array.isArray(formData[field.key])) {
    formData[field.key] = []
  }
  if (e.detail.value.length > 0) {
    if (!formData[field.key].includes(value)) {
      formData[field.key].push(value)
    }
  } else {
    formData[field.key] = formData[field.key].filter(v => v !== value)
  }
  clearError(field.key)
}

// Radio变化
function onRadioChange(e, field, value) {
  formData[field.key] = value
  clearError(field.key)
}

// Switch变化
function onSwitchChange(e, field) {
  formData[field.key] = e.detail.value
  clearError(field.key)
}

// Date变化
function onDateChange(e, field) {
  formData[field.key] = e.detail.value
  clearError(field.key)
}

// 清除错误
function clearError(key) {
  delete errors[key]
}

// 表单验证
function validate() {
  errors = {}
  let isValid = true
  
  for (const field of props.fields) {
    if (isHidden(field)) continue
    if (field.disabled) continue
    
    const value = formData[field.key]
    
    // 必填校验
    if (field.required) {
      if (value === null || value === undefined || value === '' || 
          (Array.isArray(value) && value.length === 0)) {
        errors[field.key] = field.errorMessage || `${field.label}不能为空`
        isValid = false
        continue
      }
    }
    
    // 数值范围校验
    if (field.type === 'number' && value) {
      const num = parseFloat(value)
      if (field.min !== undefined && num < field.min) {
        errors[field.key] = `不能小于${field.min}`
        isValid = false
      }
      if (field.max !== undefined && num > field.max) {
        errors[field.key] = `不能大于${field.max}`
        isValid = false
      }
    }
    
    // 格式校验
    if (field.pattern && value) {
      const regex = new RegExp(field.pattern)
      if (!regex.test(value)) {
        errors[field.key] = field.errorMessage || '格式不正确'
        isValid = false
      }
    }
  }
  
  emit('validate', { isValid, errors })
  return isValid
}

// 获取表单数据
function getFormData() {
  return { ...formData }
}

// 暴露方法
defineExpose({
  validate,
  getFormData
})
</script>

<style scoped>
.dynamic-form {
  width: 100%;
}

.form-item {
  margin-bottom: 20px;
  padding: 0 16px;
}

.form-item.hidden {
  display: none;
}

.form-label {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.required-mark {
  color: #F56C6C;
  margin-right: 4px;
}

.label-text {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.form-input,
.form-textarea,
.form-picker {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #DCDFE6;
  border-radius: 6px;
  font-size: 14px;
  background-color: #fff;
  box-sizing: border-box;
}

.form-textarea {
  min-height: 80px;
}

.form-picker {
  display: flex;
  align-items: center;
  min-height: 40px;
}

.placeholder {
  color: #C0C4CC;
}

.checkbox-group,
.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.checkbox-item,
.radio-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.checkbox-item text,
.radio-item text {
  font-size: 14px;
  color: #333;
}

.error-tip {
  margin-top: 4px;
}

.error-tip text {
  font-size: 12px;
  color: #F56C6C;
}
</style>
