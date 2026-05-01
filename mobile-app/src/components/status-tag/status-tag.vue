/**
 * 状态标签组件
 * 用于显示各种状态（点位状态、采集结果状态等）
 */
<template>
  <view class="status-tag" :class="[type, size]" :style="customStyle">
    <text class="status-text">{{ displayText }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 状态值
  status: {
    type: [String, Number],
    required: true
  },
  // 类型：point-点位状态, result-采集结果状态, project-项目状态
  type: {
    type: String,
    default: 'point'
  },
  // 尺寸：small, medium, large
  size: {
    type: String,
    default: 'small'
  },
  // 自定义样式
  customStyle: {
    type: String,
    default: ''
  }
})

// 点位状态映射
const pointStatusMap = {
  '待采集': { text: '待采集', color: '#909399', bg: '#F4F4F5' },
  '待分配': { text: '待分配', color: '#909399', bg: '#F4F4F5' },
  '草稿中': { text: '草稿中', color: '#E6A23C', bg: '#FDF6EC' },
  '待审核': { text: '待审核', color: '#409EFF', bg: '#ECF5FF' },
  '审核通过': { text: '已通过', color: '#67C23A', bg: '#F0F9EB' },
  '驳回待修改': { text: '已驳回', color: '#F56C6C', bg: '#FEF0F0' },
  '已归档': { text: '已归档', color: '#909399', bg: '#F4F4F5' },
  '作废': { text: '已作废', color: '#909399', bg: '#F4F4F5' }
}

// 采集结果状态映射
const resultStatusMap = {
  'draft': { text: '草稿', color: '#E6A23C', bg: '#FDF6EC' },
  'submitted': { text: '已提交', color: '#409EFF', bg: '#ECF5FF' },
  'pending': { text: '待审核', color: '#409EFF', bg: '#ECF5FF' },
  'approved': { text: '已通过', color: '#67C23A', bg: '#F0F9EB' },
  'rejected': { text: '已驳回', color: '#F56C6C', bg: '#FEF0F0' },
  'archived': { text: '已归档', color: '#909399', bg: '#F4F4F5' }
}

// 项目状态映射
const projectStatusMap = {
  'draft': { text: '草稿', color: '#909399', bg: '#F4F4F5' },
  'ongoing': { text: '进行中', color: '#409EFF', bg: '#ECF5FF' },
  'paused': { text: '已暂停', color: '#E6A23C', bg: '#FDF6EC' },
  'completed': { text: '已完成', color: '#67C23A', bg: '#F0F9EB' },
  'archived': { text: '已归档', color: '#909399', bg: '#F4F4F5' }
}

// 获取状态配置
const statusConfig = computed(() => {
  const map = props.type === 'point' ? pointStatusMap : 
              props.type === 'result' ? resultStatusMap : projectStatusMap
  
  return map[props.status] || { text: String(props.status), color: '#909399', bg: '#F4F4F5' }
})

// 显示文本
const displayText = computed(() => statusConfig.value.text)
</script>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  padding: 2px 8px;
  line-height: 1;
}

.status-text {
  font-weight: 500;
}

/* 尺寸 */
.small {
  padding: 2px 6px;
  font-size: 12px;
}

.medium {
  padding: 4px 10px;
  font-size: 14px;
}

.large {
  padding: 6px 14px;
  font-size: 16px;
}
</style>
