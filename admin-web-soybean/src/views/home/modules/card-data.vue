<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { fetchGetSystemOverview } from '@/service/api/statistics';
import { fetchGetProjectList } from '@/service/api/project';

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

const totalProjects = ref(0);
const activeProjects = ref(0);
const completedResults = ref(0);
const warningItems = ref(0);
const totalPoints = ref(0);
const auditRate = ref(0);

const loadData = async () => {
  try {
    const [overviewRes, projectRes] = await Promise.all([
      fetchGetSystemOverview(),
      fetchGetProjectList({ current: 1, size: 1 })
    ]);
    const overview = (overviewRes as any)?.data;
    if (overview) {
      totalProjects.value = overview.totalProjects ?? 0;
      totalPoints.value = overview.totalPoints ?? 0;
      auditRate.value = overview.auditRate ?? 0;
    }
    const projectData = (projectRes as any)?.data;
    if (projectData) {
      totalProjects.value = projectData.total ?? totalProjects.value;
    }
    // Count active projects (status=1)
    const activeRes = await fetchGetProjectList({ current: 1, size: 1 });
    const activeData = (activeRes as any)?.data;
    if (activeData) {
      activeProjects.value = activeData.total ?? 0;
    }
    // Count completed results (status=3 audit passed) via warningItems placeholder
    completedResults.value = overview?.completedResults ?? 0;
    warningItems.value = overview?.pendingAudits ?? 0;
  } catch (e) {
    console.error('Failed to load dashboard stats', e);
  }
};

onMounted(() => {
  loadData();
});

const cardData = computed<CardData[]>(() => [
  {
    key: 'totalProjects',
    title: '年度项目总数',
    value: totalProjects.value,
    icon: 'i-material-symbols:folder-open-outline-rounded',
    colorClass: 'text-[var(--color-primary)]',
    bgColorClass: 'bg-[var(--color-primary)]',
    badge: '全部项目'
  },
  {
    key: 'activeProjects',
    title: '当前活跃项目',
    value: activeProjects.value,
    icon: 'i-material-symbols:trending-up-outline-rounded',
    colorClass: 'text-[var(--color-primary)]',
    bgColorClass: 'bg-[var(--color-primary)]',
    badge: '进行中'
  },
  {
    key: 'completedResults',
    title: '已验收成果物',
    value: completedResults.value,
    icon: 'i-material-symbols:task-alt-outline-rounded',
    colorClass: 'text-[var(--color-success)]',
    bgColorClass: 'bg-[var(--color-success)]',
    badge: totalPoints.value > 0 ? `完成率 ${totalPoints.value > 0 ? Math.round((completedResults.value / totalPoints.value) * 100) : 0}%` : '完成率 0%'
  },
  {
    key: 'warningItems',
    title: '预警异常项',
    value: warningItems.value,
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
      class="glass-card group relative rounded-8px p-20px cursor-pointer"
      v-mouse-glow="{ color: '22,119,255', size: 250, intensity: 0.06 }"
    >
      <!-- 顶部标题栏 -->
      <div class="flex items-center justify-between mb-12px">
        <div class="text-13px text-[var(--color-text-secondary)]">{{ item.title }}</div>
        <div
          v-if="item.badge"
          class="px-8px py-2px rounded-4px text-11px transition-all duration-300"
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

      <!-- 数值区域 -->
      <div class="flex items-end justify-between">
        <div class="text-28px font-700 text-[var(--color-text-primary)] transition-transform duration-300 group-hover:translate-x-4px" style="letter-spacing: -0.5px;">
          {{ item.value }}
        </div>

        <!-- 右侧箭头指示 -->
        <div class="flex items-center justify-center w-28px h-28px rounded-6px transition-all duration-300 opacity-0 -translate-x-8px group-hover:opacity-100 group-hover:translate-x-0"
             :style="{ background: 'rgba(22,119,255,0.1)' }">
          <span class="i-material-symbols:arrow-forward-rounded text-16px text-[var(--color-primary)]"></span>
        </div>
      </div>

      <!-- 底部装饰线条 -->
      <div class="mt-16px h-2px rounded-full transition-all duration-500 opacity-0 group-hover:opacity-100"
           :style="{ background: `linear-gradient(90deg, transparent, rgba(22,119,255,0.3), transparent)` }">
      </div>
    </div>
  </div>
</template>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card:hover {
  border-color: rgba(22, 119, 255, 0.25);
  box-shadow: 0 8px 32px rgba(22, 119, 255, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.glass-card:active {
  transform: translateY(0);
  transition-duration: 0.1s;
}
</style>
