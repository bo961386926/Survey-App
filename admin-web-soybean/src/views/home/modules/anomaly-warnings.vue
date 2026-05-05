<script setup lang="ts">
import { computed } from 'vue';

defineOptions({
  name: 'AnomalyWarnings'
});

interface Warning {
  id: number;
  name: string;
  project: string;
  status: string;
  delayDays: number;
}

const warnings = computed<Warning[]>(() => [
  { id: 1, name: '建安路北排口', project: '亳州市城区入河排污口排查项目', status: '待审核', delayDays: 6 },
  { id: 2, name: '涡河上游A1排口', project: '涡河流域排污口专项勘查', status: '待审核', delayDays: 6 },
  { id: 3, name: '涡河中游B1排口', project: '涡河流域排污口专项勘查', status: '待审核', delayDays: 6 },
  { id: 4, name: '小涧镇排口', project: '蒙城县农村排污口普查', status: '待审核', delayDays: 6 },
  { id: 5, name: '阚疃镇排口', project: '利辛县河道排污口整治勘查', status: '待审核', delayDays: 6 }
]);
</script>

<template>
  <div class="rounded-xl overflow-hidden h-full flex flex-col" style="background: var(--bg-card); border: 1px solid var(--color-divider); box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
    <!-- Header -->
    <div class="flex justify-between items-center px-24px py-16px" style="border-bottom: 1px solid var(--color-divider);">
      <div class="flex items-center gap-8px">
        <div class="w-28px h-28px rounded-6px flex items-center justify-center" style="background: rgba(239,68,68,0.1);">
          <span class="i-material-symbols:crisis-alert-outline-rounded text-16px text-[var(--color-danger)]"></span>
        </div>
        <h3 class="text-15px font-600 text-[var(--color-text-primary)]">异常点位预警</h3>
      </div>
      <a class="text-13px text-[var(--color-primary)] cursor-pointer font-500 hover:underline">查看全部</a>
    </div>

    <!-- Warning List -->
    <div class="flex-1 px-24px py-16px overflow-y-auto">
      <div class="flex flex-col gap-12px">
        <div 
          v-for="item in warnings" 
          :key="item.id" 
          class="flex items-center justify-between p-14px rounded-lg"
          style="background: var(--bg-page); border: 1px solid var(--color-divider);"
        >
          <!-- Warning Info -->
          <div class="flex items-center gap-12px flex-1 min-w-0">
            <!-- Warning Icon -->
            <div 
              class="w-36px h-36px rounded-8px flex items-center justify-center text-18px flex-shrink-0"
              style="background: rgba(245,158,11,0.1);"
            >
              <span class="i-material-symbols:location-on-outline-rounded text-18px text-[var(--color-warning)]"></span>
            </div>
            
            <!-- Warning Details -->
            <div class="flex-1 min-w-0">
              <div class="text-14px font-600 text-[var(--color-text-primary)] mb-4px">{{ item.name }}</div>
              <div class="text-12px text-[var(--color-text-secondary)]">
                {{ item.project }}
              </div>
            </div>
          </div>
          
          <!-- Status & Delay -->
          <div class="flex flex-col items-end gap-4px flex-shrink-0">
            <span 
              class="px-8px py-2px rounded-4px text-11px font-500"
              style="background: rgba(245,158,11,0.1); color: var(--color-warning);"
            >
              {{ item.status }}
            </span>
            <span class="text-12px text-[var(--color-danger)] font-500">
              滞留 {{ item.delayDays }} 天
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
