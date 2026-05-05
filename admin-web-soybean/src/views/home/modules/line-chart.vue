<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useAppStore } from '@/store/modules/app';
import { useThemeStore } from '@/store/modules/theme';
import { useEcharts } from '@/hooks/common/echarts';

defineOptions({
  name: 'LineChart'
});

const appStore = useAppStore();
const themeStore = useThemeStore();

const { domRef, updateOptions } = useEcharts(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: themeStore.darkMode ? '#1f1f1f' : '#ffffff',
    borderColor: 'var(--color-border)',
    textStyle: { color: 'var(--color-text-primary)', fontSize: 12 },
    axisPointer: {
      type: 'cross',
      crossStyle: { color: 'var(--color-primary)' },
      lineStyle: { color: 'var(--color-primary)', width: 1, type: 'dashed' }
    },
    padding: [12, 16]
  },
  grid: {
    left: '24px',
    right: '24px',
    bottom: '24px',
    top: '24px',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    axisLine: { lineStyle: { color: 'var(--color-divider)' } },
    axisTick: { show: false },
    axisLabel: { color: 'var(--color-text-placeholder)', fontSize: 11, margin: 12 },
    data: ['04-05', '04-08', '04-11', '04-14', '04-17', '04-20', '04-23', '04-26', '04-29', '05-02', '05-05']
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: 'var(--color-divider)', type: 'dashed' } },
    axisLabel: { color: 'var(--color-text-placeholder)', fontSize: 11 },
    max: 4,
    interval: 1
  },
  series: [
    {
      name: '勘察任务',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: { width: 2, color: '#1677ff' },
      itemStyle: { color: '#1677ff' },
      areaStyle: {
        opacity: 0.08,
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#1677ff' },
            { offset: 1, color: 'transparent' }
          ]
        }
      },
      data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    }
  ]
}));

async function fetchData() {
  await new Promise(resolve => setTimeout(resolve, 500));
}

watch(
  () => themeStore.darkMode,
  () => {
    updateOptions(opts => {
      opts.tooltip.backgroundColor = themeStore.darkMode ? '#1f1f1f' : '#ffffff';
      return opts;
    });
  }
);

onMounted(() => {
  fetchData().then(() => {
    updateOptions();
  });
});
</script>

<template>
  <div class="bg-[var(--bg-card)] rd-[8px] p-24px shadow-sm">
    <div class="flex justify-between items-center mb-24px">
      <div>
        <h3 class="text-16px font-500 text-[var(--color-text-primary)]">勘查任务趋势图</h3>
        <p class="text-13px text-[var(--color-text-secondary)] mt-4px">最近30天全平台数据采集频率分析</p>
      </div>
      <div class="flex gap-4px bg-[var(--bg-page)] p-4px rd-[6px]">
        <AButton size="small" type="text" class="px-16px! text-13px! rd-[4px]! bg-[var(--bg-card)] shadow-sm! text-[var(--color-primary)]! font-500!">月度</AButton>
        <AButton size="small" type="text" class="px-16px! text-13px! rd-[4px]! text-[var(--color-text-secondary)] font-400!">季度</AButton>
        <AButton size="small" type="text" class="px-16px! text-13px! rd-[4px]! text-[var(--color-text-secondary)] font-400!">年度</AButton>
      </div>
    </div>
    <div ref="domRef" class="h-300px w-full"></div>
  </div>
</template>

<style scoped></style>
