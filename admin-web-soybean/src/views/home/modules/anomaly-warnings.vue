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
  <div class="warning-card rounded-xl overflow-hidden h-full flex flex-col" v-mouse-glow="{ color: '245,158,11', size: 200, intensity: 0.04 }">
    <!-- Header -->
    <div class="flex justify-between items-center px-24px py-16px header-divider">
      <div class="flex items-center gap-8px">
        <div class="warning-header-icon">
          <span class="i-material-symbols:crisis-alert-outline-rounded text-16px"></span>
        </div>
        <h3 class="text-15px font-600 text-[var(--color-text-primary)]">异常点位预警</h3>
      </div>
      <a class="view-all-link">查看全部</a>
    </div>

    <!-- Warning List -->
    <div class="flex-1 px-24px py-16px overflow-y-auto">
      <div class="flex flex-col gap-12px">
        <div
          v-for="item in warnings"
          :key="item.id"
          class="warning-item"
        >
          <!-- Warning Info -->
          <div class="warning-item-content">
            <!-- Warning Icon -->
            <div class="warning-icon-wrapper">
              <span class="i-material-symbols:location-on-outline-rounded text-18px"></span>
            </div>

            <!-- Warning Details -->
            <div class="flex-1 min-w-0">
              <div class="warning-name">{{ item.name }}</div>
              <div class="warning-project">
                {{ item.project }}
              </div>
            </div>
          </div>

          <!-- Status & Delay -->
          <div class="warning-status-area">
            <span class="status-tag">
              {{ item.status }}
            </span>
            <span class="delay-text">
              滞留 {{ item.delayDays }} 天
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.warning-card {
  background: var(--bg-card);
  border: 1px solid var(--color-divider);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.warning-card:hover {
  border-color: rgba(245, 158, 11, 0.2);
  box-shadow: 0 8px 32px rgba(245, 158, 11, 0.06), 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-divider {
  border-bottom: 1px solid var(--color-divider);
}

.warning-header-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 68, 68, 0.1);
  color: var(--color-danger);
  transition: all 0.25s ease;
}

.warning-card:hover .warning-header-icon {
  background: rgba(239, 68, 68, 0.15);
  transform: scale(1.05);
}

.view-all-link {
  font-size: 13px;
  color: var(--color-primary);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  position: relative;
}

.view-all-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: var(--color-primary);
  transition: width 0.3s ease;
}

.view-all-link:hover::after {
  width: 100%;
}

.warning-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--bg-page);
  border: 1px solid var(--color-divider);
}

.warning-item:hover {
  background: rgba(245, 158, 11, 0.04);
  border-color: rgba(245, 158, 11, 0.25);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.08);
}

.warning-item:active {
  transform: translateX(4px) scale(0.99);
  transition-duration: 0.1s;
}

.warning-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.warning-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(245, 158, 11, 0.1);
  color: var(--color-warning);
  transition: all 0.25s ease;
}

.warning-item:hover .warning-icon-wrapper {
  background: rgba(245, 158, 11, 0.15);
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.15);
}

.warning-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
  transition: color 0.2s ease;
}

.warning-item:hover .warning-name {
  color: var(--color-warning);
}

.warning-project {
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.warning-status-area {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  background: rgba(245, 158, 11, 0.1);
  color: var(--color-warning);
  transition: all 0.25s ease;
}

.warning-item:hover .status-tag {
  background: rgba(245, 158, 11, 0.15);
}

.delay-text {
  font-size: 12px;
  color: var(--color-danger);
  font-weight: 500;
}
</style>
