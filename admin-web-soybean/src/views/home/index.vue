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
  <div class="h-full flex-col overflow-y-auto custom-scrollbar" style="background: var(--bg-page);">
    <div class="p-24px">
      <!-- Welcome Header -->
      <div class="mb-24px">
        <h1 class="text-20px font-700 text-[var(--color-text-primary)] mb-4px">项目概览工作台</h1>
        <p class="text-13px text-[var(--color-text-secondary)]">欢迎回来，这是您当前的勘察数字化管理中心。</p>
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
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--color-divider);
  border-radius: 10px;
}
</style>
