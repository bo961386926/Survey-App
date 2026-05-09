/**
 * 定位选择器组件
 * 集成高德地图选点、GPS定位、人工纠偏、位置显示
 * 支持回填经纬度到关联的文本字段
 */
<template>
  <view class="location-picker">
    <!-- 位置信息展示 -->
    <view class="location-info">
      <view class="location-header">
        <text class="label">当前位置</text>
        <view class="status-badge" :style="{ backgroundColor: statusColor }">
          <text class="status-text">{{ statusText }}</text>
        </view>
      </view>

      <view class="location-coords">
        <text v-if="location.lng && location.lat">
          经度: {{ location.lng.toFixed(6) }}
        </text>
        <text v-if="location.lng && location.lat">
          纬度: {{ location.lat.toFixed(6) }}
        </text>
        <text v-if="location.accuracy">
          精度: ±{{ location.accuracy.toFixed(0) }}米
        </text>
        <text v-if="location.address" class="address-text">
          地址: {{ location.address }}
        </text>
      </view>

      <!-- 距离目标点位距离 -->
      <view v-if="targetLocation && distance !== null" class="distance-info">
        <text>距目标点位: {{ distance }}米</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="btn btn-primary" @click="getLocation" :loading="locating">
        {{ locating ? '定位中...' : '重新定位' }}
      </button>
      <button class="btn btn-secondary" @click="openAmapMapPicker">
        地图选点
      </button>
    </view>

    <!-- 纠偏提示 -->
    <view v-if="showCorrectionTip && distance !== null && distance > maxDistance" class="correction-tip">
      <text>您已偏离目标位置 ({{ distance }}米)，建议使用地图选点确认实际位置</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  getCurrentLocation,
  openAmapPicker,
  calculateDistance,
  getLocationStatus,
  formatCorrectionLog
} from '@/utils/location'

const props = defineProps({
  // 当前值
  modelValue: {
    type: Object,
    default: () => ({ lng: null, lat: null })
  },
  // 目标点位位置（用于计算距离）
  targetLocation: {
    type: Object,
    default: null
  },
  // 最大允许距离（米）
  maxDistance: {
    type: Number,
    default: 100
  },
  // 是否显示纠偏提示
  showCorrectionTip: {
    type: Boolean,
    default: true
  },
  // 地图默认缩放级别
  mapZoom: {
    type: Number,
    default: 15
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'correction'])

const location = ref({ ...props.modelValue })
const locating = ref(false)

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  if (newVal) location.value = { ...newVal }
}, { deep: true })

// 计算距离
const distance = computed(() => {
  if (!props.targetLocation || !location.value.lng || !location.value.lat) {
    return null
  }
  return calculateDistance(
    location.value.lng,
    location.value.lat,
    props.targetLocation.lng,
    props.targetLocation.lat
  )
})

// 状态文本
const statusText = computed(() => {
  if (!location.value.lng || !location.value.lat) {
    return '未定位'
  }
  const status = getLocationStatus(distance.value)
  return status.message
})

// 状态颜色
const statusColor = computed(() => {
  if (!location.value.lng || !location.value.lat) {
    return '#999'
  }
  const status = getLocationStatus(distance.value)
  return status.color
})

// 获取当前位置
async function getLocation() {
  locating.value = true
  try {
    const pos = await getCurrentLocation()
    location.value = pos
    emitChange()

    // 检查是否需要纠偏
    if (props.showCorrectionTip && distance.value > props.maxDistance) {
      uni.showToast({
        title: `已偏离目标位置${distance.value}米`,
        icon: 'none'
      })
    }
  } catch (error) {
    uni.showToast({
      title: '定位失败,请检查定位权限',
      icon: 'none'
    })
  } finally {
    locating.value = false
  }
}

// 打开高德地图选点
async function openAmapMapPicker() {
  try {
    const pos = await openAmapPicker({
      latitude: location.value.lat || props.targetLocation?.lat || 39.9042,
      longitude: location.value.lng || props.targetLocation?.lng || 116.4074,
      zoom: props.mapZoom
    })
    // 如果 resolve 成功了，直接使用结果
    if (pos && pos.lng && pos.lat) {
      location.value = pos
      emitChange()
      uni.showToast({ title: '位置已选择', icon: 'success' })
    }
  } catch (error) {
    console.error('地图选点失败:', error)
  }
}

// 处理纠偏
function handleCorrection() {
  const correctionData = formatCorrectionLog({
    pointId: props.targetLocation?.pointId,
    originalLng: props.targetLocation?.lng,
    originalLat: props.targetLocation?.lat,
    correctedLng: location.value.lng,
    correctedLat: location.value.lat
  })

  emit('correction', correctionData)
}

// 触发变化事件
function emitChange() {
  emit('update:modelValue', { ...location.value })
  emit('change', { ...location.value })
}

// 暴露方法给父组件
defineExpose({
  getLocation,
  openAmapMapPicker,
  getLocationData: () => ({ ...location.value })
})
</script>

<style scoped>
.location-picker {
  width: 100%;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
}

.location-info {
  margin-bottom: 16px;
}

.location-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
}

.status-text {
  font-size: 12px;
  color: #fff;
}

.location-coords {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.location-coords text {
  font-size: 13px;
  color: #666;
}

.address-text {
  color: #409EFF !important;
  font-size: 12px !important;
}

.distance-info {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #FFF7E6;
  border-radius: 4px;
}

.distance-info text {
  font-size: 13px;
  color: #E6A23C;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.btn {
  flex: 1;
  height: 40px;
  line-height: 40px;
  text-align: center;
  border-radius: 6px;
  font-size: 14px;
  border: none;
}

.btn-primary {
  background-color: #409EFF;
  color: #fff;
}

.btn-secondary {
  background-color: #F5F7FA;
  color: #606266;
  border: 1px solid #DCDFE6;
}

.btn-small {
  padding: 4px 12px;
  height: auto;
  font-size: 12px;
  background-color: #409EFF;
  color: #fff;
}

.correction-tip {
  margin-top: 12px;
  padding: 12px;
  background-color: #FEF0F0;
  border-radius: 6px;
}

.correction-tip text {
  font-size: 13px;
  color: #F56C6C;
}
</style>
