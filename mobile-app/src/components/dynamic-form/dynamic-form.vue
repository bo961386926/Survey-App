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

      <!-- Radio (supports sub-fields for text input) -->
      <view v-else-if="field.type === 'radio'" class="radio-group">
        <view v-for="option in getOptions(field)" :key="option.value" class="radio-option-wrapper">
          <label class="radio-item">
            <radio :value="option.value"
              :checked="formData[field.id] === option.value"
              :disabled="field.disabled || readonly"
              @change="onRadioChange(field, option)"
            />
            <text>{{ option.label }}</text>
          </label>
          <!-- Sub-fields: text inputs shown when this option is selected -->
          <view v-if="formData[field.id] === option.value && option.subFields && option.subFields.length > 0" class="radio-sub-fields">
            <view v-for="subField in option.subFields" :key="subField.id" class="radio-sub-field">
              <text class="sub-field-label">{{ subField.label }}</text>
              <input
                class="sub-field-input"
                v-model="formData[subField.id]"
                :type="subField.type === 'number' ? 'digit' : 'text'"
                :placeholder="subField.placeholder || '请输入'"
                :disabled="field.disabled || readonly"
              />
              <text v-if="subField.suffix" class="sub-field-suffix">{{ subField.suffix }}</text>
            </view>
          </view>
        </view>
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

      <!-- Location (Amap Integration) -->
      <location-picker v-else-if="field.type === 'location'"
        v-model="formData[field.id]"
        :map-zoom="(field as any).mapZoom || 15"
        @change="(val: any) => onLocationChange(val, field)"
      />

      <!-- Divider -->
      <view v-else-if="field.type === 'divider'" class="form-divider"></view>

      <!-- Grid Layout -->
      <view v-else-if="field.type === 'grid'" class="form-grid">
        <view
          v-for="(col, colIndex) in field.columns"
          :key="colIndex"
          :style="{ flexGrow: col.span, flexShrink: 1, flexBasis: '0%', minWidth: 0 }"
          class="grid-column"
        >
          <view
            v-for="(subField, subIndex) in col.fields"
            :key="subField.id || subIndex"
            v-show="isFieldVisible(subField)"
            class="grid-form-item"
            :class="{ required: subField.required }"
          >
            <!-- 字段标签 -->
            <view class="form-label" v-if="subField.type !== 'divider'">
              <text v-if="subField.required" class="required-mark">*</text>
              <text class="label-text">{{ subField.label }}</text>
            </view>

            <!-- Input -->
            <input v-if="subField.type === 'input'"
              class="form-input"
              v-model="formData[subField.id]"
              :placeholder="subField.placeholder || '请输入'"
              :maxlength="subField.validation?.maxLength || 100"
              :disabled="subField.disabled || readonly"
            />

            <!-- Number -->
            <input v-else-if="subField.type === 'number'"
              class="form-input"
              type="digit"
              v-model="formData[subField.id]"
              :placeholder="subField.placeholder || '请输入数字'"
              :disabled="subField.disabled || readonly"
            />

            <!-- Textarea -->
            <textarea v-else-if="subField.type === 'textarea'"
              class="form-textarea"
              v-model="formData[subField.id]"
              :placeholder="subField.placeholder || '请输入内容'"
              :maxlength="subField.validation?.maxLength || 500"
              :disabled="subField.disabled || readonly"
            />

            <!-- Select -->
            <picker v-else-if="subField.type === 'select'"
              :range="getOptions(subField)"
              range-key="label"
              :disabled="subField.disabled || readonly"
              @change="onSelectChange($event, subField)"
            >
              <view class="form-picker">
                <text :class="{ placeholder: !formData[subField.id] }">
                  {{ getSelectLabel(subField) || subField.placeholder || '请选择' }}
                </text>
              </view>
            </picker>

            <!-- Radio -->
            <view v-else-if="subField.type === 'radio'" class="radio-group">
              <view v-for="option in getOptions(subField)" :key="option.value" class="radio-option-wrapper">
                <label class="radio-item">
                  <radio :value="option.value"
                    :checked="formData[subField.id] === option.value"
                    :disabled="subField.disabled || readonly"
                    @change="onRadioChange(subField, option)"
                  />
                  <text>{{ option.label }}</text>
                </label>
                <view v-if="formData[subField.id] === option.value && option.subFields && option.subFields.length > 0" class="radio-sub-fields">
                  <view v-for="subSubField in option.subFields" :key="subSubField.id" class="radio-sub-field">
                    <text class="sub-field-label">{{ subSubField.label }}</text>
                    <input
                      class="sub-field-input"
                      v-model="formData[subSubField.id]"
                      :type="subSubField.type === 'number' ? 'digit' : 'text'"
                      :placeholder="subSubField.placeholder || '请输入'"
                      :disabled="subField.disabled || readonly"
                    />
                    <text v-if="subSubField.suffix" class="sub-field-suffix">{{ subSubField.suffix }}</text>
                  </view>
                </view>
              </view>
            </view>

            <!-- Checkbox -->
            <view v-else-if="subField.type === 'checkbox'" class="checkbox-group">
              <label v-for="option in getOptions(subField)" :key="option.value" class="checkbox-item">
                <checkbox :value="option.value"
                  :checked="isChecked(subField, option.value)"
                  :disabled="subField.disabled || readonly"
                  @change="toggleCheckbox(subField, option.value)"
                />
                <text>{{ option.label }}</text>
              </label>
            </view>

            <!-- Switch -->
            <switch v-else-if="subField.type === 'switch'"
              :checked="!!formData[subField.id]"
              :disabled="subField.disabled || readonly"
              @change="formData[subField.id] = !!$event.detail.value"
            />

            <!-- Date -->
            <picker v-else-if="subField.type === 'date'"
              mode="date"
              :disabled="subField.disabled || readonly"
              @change="formData[subField.id] = $event.detail.value; clearError(subField.id)"
            >
              <view class="form-picker">
                <text :class="{ placeholder: !formData[subField.id] }">
                  {{ formData[subField.id] || '请选择日期' }}
                </text>
              </view>
            </picker>

            <!-- Image -->
            <image-uploader v-else-if="subField.type === 'image'"
              v-model="formData[subField.id]"
              :max-count="subField.imageConfig?.maxCount || 9"
              :camera-only="subField.imageConfig?.cameraOnly || false"
            />

            <!-- Location -->
            <location-picker v-else-if="subField.type === 'location'"
              v-model="formData[subField.id]"
              :map-zoom="(subField as any).mapZoom || 15"
              @change="(val: any) => onLocationChange(val, subField)"
            />

            <!-- Divider -->
            <view v-else-if="subField.type === 'divider'" class="form-divider"></view>

            <view v-if="errors[subField.id]" class="error-tip">
              <text>{{ errors[subField.id] }}</text>
            </view>
          </view>
        </view>
      </view>

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

// Handle location field change - auto-fill lat/lng to linked text fields
function onLocationChange(value, field) {
  if (!value || !value.lng || !value.lat) return

  // Auto-fill longitude to linked field
  const lngFieldId = field.autoFillLngFieldId
  if (lngFieldId && formData[lngFieldId] !== undefined) {
    formData[lngFieldId] = String(value.lng.toFixed(6))
  }

  // Auto-fill latitude to linked field
  const latFieldId = field.autoFillLatFieldId
  if (latFieldId && formData[latFieldId] !== undefined) {
    formData[latFieldId] = String(value.lat.toFixed(6))
  }
}

// 联动规则引擎：判断字段是否可见
function isFieldVisible(field) {
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
}

const visibleFields = computed(() => {
  return props.fields.filter(field => isFieldVisible(field))
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

function onRadioChange(field, option) {
  formData[field.id] = option.value
  clearError(field.id)

  // Clear sub-field data from previously selected option
  const options = getOptions(field)
  for (const opt of options) {
    if (opt.subFields && opt.value !== option.value) {
      for (const subField of opt.subFields) {
        delete formData[subField.id]
      }
    }
  }
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
.form-grid { display: flex; flex-direction: row; gap: 8px; width: 100%; box-sizing: border-box; padding: 0 16px; margin-bottom: 20px; }
.grid-column { display: flex; flex-direction: column; min-width: 0; width: 0; }
.grid-form-item { margin-bottom: 12px; }
.form-label { display: flex; align-items: center; margin-bottom: 8px; }
.required-mark { color: #F56C6C; margin-right: 4px; }
.label-text { font-size: 14px; color: #333; font-weight: 500; }
.form-input, .form-textarea, .form-picker {
  width: 100%; min-width: 0; padding: 10px 12px;
  border: 1px solid #DCDFE6; border-radius: 6px;
  font-size: 14px; background-color: #fff; box-sizing: border-box;
}
.form-textarea { min-height: 80px; }
.form-picker { display: flex; align-items: center; min-height: 40px; }
.placeholder { color: #C0C4CC; }
.checkbox-group, .radio-group { display: flex; flex-wrap: wrap; gap: 12px; }
.checkbox-item, .radio-item { display: flex; align-items: center; gap: 6px; }
.checkbox-item text, .radio-item text { font-size: 14px; color: #333; }
.radio-option-wrapper { width: 100%; }
.radio-sub-fields { margin-left: 24px; margin-top: 8px; display: flex; flex-direction: column; gap: 8px; }
.radio-sub-field { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.sub-field-label { font-size: 14px; color: #333; white-space: nowrap; }
.sub-field-input { flex: 1; padding: 6px 10px; border: 1px solid #DCDFE6; border-radius: 4px; font-size: 14px; min-width: 60px; max-width: 150px; background-color: #fff; }
.sub-field-suffix { font-size: 14px; color: #333; white-space: nowrap; }
.form-divider { height: 1px; background: #eee; margin: 12px 0; }
.error-tip { margin-top: 4px; }
.error-tip text { font-size: 12px; color: #F56C6C; }
</style>
