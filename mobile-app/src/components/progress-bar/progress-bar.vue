<template>
  <view class="progress-bar-container">
    <view class="progress-bar-track">
      <view 
        class="progress-bar-fill" 
        :style="{ 
          width: percentage + '%',
          background: gradientColor,
          height: height
        }"
      >
        <view v-if="showStripes" class="progress-stripes"></view>
      </view>
    </view>
    <text v-if="showText" class="progress-text">{{ percentage }}%</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 进度百分比 (0-100)
  percentage: {
    type: Number,
    default: 0,
    validator: value => value >= 0 && value <= 100
  },
  // 进度条颜色
  color: {
    type: String,
    default: '#3B82F6'
  },
  // 进度条高度
  height: {
    type: String,
    default: '8px'
  },
  // 是否显示文字
  showText: {
    type: Boolean,
    default: true
  },
  // 是否显示条纹动画
  showStripes: {
    type: Boolean,
    default: false
  }
})

// 渐变色
const gradientColor = computed(() => {
  return `linear-gradient(90deg, ${props.color}, ${adjustColor(props.color, 30)})`
})

/**
 * 调整颜色亮度
 */
function adjustColor(color, amount) {
  const hex = color.replace('#', '')
  const r = Math.min(255, parseInt(hex.substring(0, 2), 16) + amount)
  const g = Math.min(255, parseInt(hex.substring(2, 4), 16) + amount)
  const b = Math.min(255, parseInt(hex.substring(4, 6), 16) + amount)
  return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`
}
</script>

<style scoped>
.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.progress-bar-track {
  flex: 1;
  height: 8px;
  background-color: #E5E7EB;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s ease;
  position: relative;
  overflow: hidden;
}

.progress-stripes {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: linear-gradient(
    45deg,
    rgba(255, 255, 255, 0.15) 25%,
    transparent 25%,
    transparent 50%,
    rgba(255, 255, 255, 0.15) 50%,
    rgba(255, 255, 255, 0.15) 75%,
    transparent 75%,
    transparent
  );
  background-size: 1rem 1rem;
  animation: progress-stripes 1s linear infinite;
}

@keyframes progress-stripes {
  0% {
    background-position: 1rem 0;
  }
  100% {
    background-position: 0 0;
  }
}

.progress-text {
  font-size: 13px;
  color: #6B7280;
  font-weight: 500;
  min-width: 40px;
  text-align: right;
}
</style>
