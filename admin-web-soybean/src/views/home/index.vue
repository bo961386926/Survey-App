<script setup lang="ts">
import HeaderBanner from './modules/header-banner.vue';
import CardData from './modules/card-data.vue';
import LineChart from './modules/line-chart.vue';
import ProjectNews from './modules/project-news.vue';
import RecentProjects from './modules/recent-projects.vue';
import AnomalyWarnings from './modules/anomaly-warnings.vue';

defineOptions({ name: 'Home' });
</script>

<template>
  <div class="h-full flex-col overflow-y-auto custom-scrollbar home-page">
    <!-- 毛玻璃背景层 -->
    <div class="glass-bg-layer"></div>

    <div class="p-24px relative z-1">
      <!-- Welcome Header -->
      <div class="mb-24px transition-all duration-300 page-header">
        <h1 class="text-20px font-700 text-[var(--color-text-primary)] mb-4px header-title">项目概览工作台</h1>
        <p class="text-13px text-[var(--color-text-secondary)] header-desc">欢迎回来，这是您当前的勘察数字化管理中心。</p>
      </div>

      <!-- Core Metrics -->
      <CardData />

      <!-- Main Content Grid: Chart + Activity -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-24px mb-24px">
        <!-- Left Column: Line Chart (2/3 width) -->
        <div class="lg:col-span-2">
          <LineChart />
        </div>

        <!-- Right Column: Recent Activity (1/3 width) -->
        <div class="lg:col-span-1">
          <ProjectNews />
        </div>
      </div>

      <!-- Bottom Grid: Recent Projects + Anomaly Warnings -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-24px">
        <!-- Recent Projects -->
        <RecentProjects />

        <!-- Anomaly Warnings -->
        <AnomalyWarnings />
      </div>
    </div>
  </div>
</template>

<style scoped>
.glass-bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  background: linear-gradient(135deg,
    rgba(22, 119, 255, 0.08) 0%,
    rgba(22, 119, 255, 0) 50%,
    rgba(16, 185, 129, 0.05) 100%);
  pointer-events: none;
}

.glass-bg-layer::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 20%,
    rgba(22, 119, 255, 0.12) 0%,
    transparent 40%),
    radial-gradient(circle at 80% 60%,
    rgba(16, 185, 129, 0.08) 0%,
    transparent 40%);
  animation: float-bg 20s ease-in-out infinite;
}

@keyframes float-bg {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-2%, -2%); }
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--color-divider);
  border-radius: 10px;
  transition: background-color 0.2s ease;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-placeholder);
}

/* Header Hover Effects */
.page-header {
  cursor: default;
  padding: 8px 12px;
  margin: -8px -12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.page-header:hover {
  background: rgba(22, 119, 255, 0.03);
}

.header-title {
  position: relative;
  display: inline-block;
}

.header-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--color-primary), transparent);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-header:hover .header-title::after {
  width: 100%;
}

.header-desc {
  transition: all 0.3s ease;
}

.page-header:hover .header-desc {
  color: var(--color-text-primary);
}

/* Grid Item Hover Effects */
:deep(.grid > div) {
  transition: all 0.3s ease;
}
</style>
