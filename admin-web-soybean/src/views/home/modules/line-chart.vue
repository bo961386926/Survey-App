<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useThemeStore } from '@/store/modules/theme';
import { useEcharts } from '@/hooks/common/echarts';
import { fetchGetOperationLogTrend } from '@/service/api/system-manage';

defineOptions({
  name: 'LineChart'
});

const themeStore = useThemeStore();
const selectedTimeRange = ref('月度');

const timeRanges = ['月度', '季度', '年度'];

const { domRef, updateOptions } = useEcharts(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.96)',
    borderColor: 'var(--color-divider)',
    borderWidth: 1,
    textStyle: { color: 'var(--color-text-primary)', fontSize: 12 },
    axisPointer: {
      type: 'line',
      lineStyle: { color: 'var(--color-primary)', width: 1, type: 'dashed' }
    },
    padding: [10, 14],
    extraCssText: 'box-shadow: 0 2px 12px rgba(0,0,0,0.08); border-radius: 6px;'
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
    axisLabel: { color: 'var(--color-text-secondary)', fontSize: 12, margin: 10 },
    data: ['04-05', '04-08', '04-11', '04-14', '04-17', '04-20', '04-23', '04-26', '04-29', '05-02', '05-05']
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: 'var(--color-divider)', type: 'dashed' } },
    axisLabel: { color: 'var(--color-text-secondary)', fontSize: 12 },
    max: 4,
    interval: 1
  },
  series: [
    {
      name: '勘察任务',
      type: 'line',
      smooth: true,
      symbol: 'emptyCircle',
      symbolSize: 6,
      lineStyle: { width: 2, color: 'var(--color-primary)' },
      itemStyle: { color: 'var(--color-primary)', borderWidth: 2 },
      emphasis: {
        itemStyle: {
          color: 'var(--color-primary)',
          borderColor: '#fff',
          borderWidth: 3,
          shadowColor: 'rgba(22, 119, 255, 0.3)',
          shadowBlur: 8
        }
      },
      areaStyle: {
        opacity: 0.1,
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(22, 119, 255, 0.2)' },
            { offset: 1, color: 'rgba(22, 119, 255, 0)' }
          ]
        }
      },
      data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    }
  ]
}));

async function fetchData() {
  try {
    const endDate = new Date().toISOString().split('T')[0];
    const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    const { data } = await fetchGetOperationLogTrend({ startDate, endDate });
    if (data && typeof data === 'object') {
      const dates = Object.keys(data).sort();
      const values = dates.map(d => data[d]);
      const maxVal = Math.max(...values, 1);
      updateOptions(opts => {
        opts.xAxis.data = dates.map(d => d.slice(5));
        opts.series[0].data = values;
        opts.yAxis.max = Math.ceil(maxVal * 1.2);
        opts.yAxis.interval = Math.max(1, Math.ceil(maxVal / 4));
        return opts;
      });
    }
  } catch (e) {
    console.error('Failed to load chart data', e);
  }
}

watch(
  () => themeStore.darkMode,
  () => {
    updateOptions(opts => {
      opts.tooltip.backgroundColor = 'rgba(255, 255, 255, 0.96)';
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
  <div class="chart-card rounded-lg overflow-hidden" v-mouse-glow="{ color: '22,119,255', size: 250, intensity: 0.04 }">
    <!-- Header -->
    <div class="flex justify-between items-center px-24px py-16px header-divider">
      <div>
        <h3 class="text-15px font-600 text-[var(--color-text-primary)]">勘查任务趋势图</h3>
        <p class="text-12px text-[var(--color-text-placeholder)] mt-4px">最近30天全平台数据采集频率分析</p>
      </div>
      <div class="tab-group">
        <button
          v-for="range in timeRanges"
          :key="range"
          class="tab-btn"
          :class="{ 'tab-btn--active': selectedTimeRange === range }"
          @click="selectedTimeRange = range"
        >
          {{ range }}
        </button>
      </div>
    </div>
    <!-- Chart -->
    <div ref="domRef" class="h-300px w-full"></div>
  </div>
</template>

<style scoped>
.chart-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart-card:hover {
  border-color: rgba(22, 119, 255, 0.25);
  box-shadow: 0 8px 32px rgba(22, 119, 255, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-divider {
  border-bottom: 1px solid var(--color-divider);
}

/* Tab Group Styles */
.tab-group {
  display: flex;
  gap: 2px;
  background: rgba(247, 248, 250, 0.6);
  border-radius: 6px;
  padding: 2px;
}

.tab-btn {
  padding: 4px 12px;
  font-size: 12px;
  border-radius: 4px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  background: transparent;
  color: var(--color-text-secondary);
  font-weight: 400;
  border: none;
  outline: none;
  position: relative;
  overflow: hidden;
}

.tab-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--color-primary);
  opacity: 0;
  border-radius: 4px;
  transition: opacity 0.25s ease;
}

.tab-btn:hover:not(.tab-btn--active)::before {
  opacity: 0.08;
}

.tab-btn:hover:not(.tab-btn--active) {
  transform: scale(1.02);
}

.tab-btn:active {
  transform: scale(0.96);
  transition-duration: 0.1s;
}

.tab-btn--active {
  background: var(--color-primary);
  color: #fff;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(22, 119, 255, 0.2);
}

.tab-btn--active:active {
  transform: scale(0.98);
}
</style>
