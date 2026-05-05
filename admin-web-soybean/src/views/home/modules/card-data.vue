<script setup lang="ts">
import { computed } from 'vue';

defineOptions({
  name: 'CardData'
});

interface CardData {
  key: string;
  title: string;
  value: number | string;
  badge?: string;
  icon: string;
  colorClass: string;
  bgColorClass: string;
}

const cardData = computed<CardData[]>(() => [
  {
    key: 'totalProjects',
    title: '年度项目总数',
    value: 6,
    icon: 'i-material-symbols:folder-open-outline-rounded',
    colorClass: 'text-[var(--color-primary)]',
    bgColorClass: 'bg-[var(--color-primary)]',
    badge: '全部项目'
  },
  {
    key: 'activeProjects',
    title: '当前活跃项目',
    value: 6,
    icon: 'i-material-symbols:trending-up-outline-rounded',
    colorClass: 'text-[var(--color-primary)]',
    bgColorClass: 'bg-[var(--color-primary)]',
    badge: '进行中'
  },
  {
    key: 'completedResults',
    title: '已验收成果物',
    value: 5,
    icon: 'i-material-symbols:task-alt-outline-rounded',
    colorClass: 'text-[var(--color-success)]',
    bgColorClass: 'bg-[var(--color-success)]',
    badge: '完成率 56%'
  },
  {
    key: 'warningItems',
    title: '预警异常项',
    value: 0,
    icon: 'i-material-symbols:warning-outline-rounded',
    colorClass: 'text-[var(--color-warning)]',
    bgColorClass: 'bg-[var(--color-warning)]',
    badge: '待审核'
  }
]);
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-16px mb-24px">
    <div
      v-for="item in cardData"
      :key="item.key"
      class="relative rounded-lg p-20px transition-all"
      style="background: var(--bg-card); border: 1px solid var(--color-divider); box-shadow: 0 1px 3px rgba(0,0,0,0.05);"
    >
      <!-- Header with badge -->
      <div class="flex items-center justify-between mb-12px">
        <div class="text-13px text-[var(--color-text-secondary)]">{{ item.title }}</div>
        <div 
          v-if="item.badge"
          class="px-8px py-2px rounded-4px text-11px"
          :style="{
            background: item.badge.includes('进行中') ? 'rgba(22,119,255,0.1)' : 
                       item.badge.includes('完成率') ? 'rgba(16,185,129,0.1)' : 
                       item.badge.includes('待审核') ? 'rgba(245,158,11,0.1)' : 'var(--bg-page)',
            color: item.badge.includes('进行中') ? 'var(--color-primary)' : 
                   item.badge.includes('完成率') ? 'var(--color-success)' : 
                   item.badge.includes('待审核') ? 'var(--color-warning)' : 'var(--color-text-secondary)'
          }"
        >
          {{ item.badge }}
        </div>
      </div>
      
      <!-- Value -->
      <div class="text-28px font-700 text-[var(--color-text-primary)]" style="letter-spacing: -0.5px;">
        {{ item.value }}
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
