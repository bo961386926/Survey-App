<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { useThemeStore } from '@/store/modules/theme';
import { useEcharts } from '@/hooks/common/echarts';

defineOptions({ name: 'Dashboard' });

const themeStore = useThemeStore();
const selectedTimeRange = ref('月度');
const timeRanges = ['月度', '季度', '年度'];

// Chart options with the hook
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
  legend: {
    show: false
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
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: {
      color: 'rgba(0,0,0,0.45)',
      fontSize: 11,
      formatter: (value: string) => {
        if (['01 MAY', '10 MAY', '20 MAY', '30 MAY', 'Q1', 'Q2', 'Q3', 'Q4', '2023', '2024', '2025', '2026'].includes(value)) {
          return value;
        }
        return '';
      }
    },
    data: ['01 MAY', '05 MAY', '10 MAY', '15 MAY', '20 MAY', '25 MAY', '30 MAY', '']
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: 'rgba(226, 232, 240, 0.6)', type: 'dashed' } },
    axisLabel: { show: false },
    axisLine: { show: false },
    axisTick: { show: false }
  },
  series: [
    {
      name: '本月采集频率',
      type: 'line',
      smooth: true,
      showSymbol: false,
      lineStyle: { width: 4, color: '#1677ff' },
      itemStyle: { color: '#1677ff' },
      areaStyle: {
        opacity: 0.1,
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(22, 119, 255, 0.3)' },
            { offset: 1, color: 'rgba(22, 119, 255, 0)' }
          ]
        }
      },
      data: [150, 220, 200, 480, 300, 680, 842, 450]
    },
    {
      name: '历史均值',
      type: 'line',
      smooth: true,
      showSymbol: false,
      lineStyle: { width: 3, color: '#a0cfff', type: 'dashed' },
      itemStyle: { color: '#a0cfff' },
      data: [100, 150, 130, 320, 210, 400, 310, 280]
    }
  ]
}));

// Metric card data
const metricCards = ref([
  {
    title: '年度项目总数',
    value: '1,284',
    badge: '+12.5%',
    badgeType: 'success',
    icon: 'material-symbols:bar-chart-rounded'
  },
  {
    title: '当前活跃项目',
    value: '42',
    badge: '进行中',
    badgeType: 'primary',
    icon: 'material-symbols:timelapse-outline-rounded'
  },
  {
    title: '已验收成果物',
    value: '8,902',
    badge: '完成率 94%',
    badgeType: 'info',
    icon: 'material-symbols:check-circle-outline-rounded'
  },
  {
    title: '预警异常项',
    value: '07',
    badge: '待审核',
    badgeType: 'warning',
    icon: 'material-symbols:warning-outline-rounded'
  }
]);

// Coverage data
const coverageItems = ref([
  { label: '地质稳定性监测', percent: 78, color: '#1677ff' },
  { label: '地下管线探测', percent: 45, color: '#475569' },
  { label: '地形测绘绘图', percent: 92, color: '#10b981' }
]);

// Watch selected time range to update chart options dynamically
watch(selectedTimeRange, (val) => {
  let xData = [];
  let seriesData1 = [];
  let seriesData2 = [];
  if (val === '月度') {
    xData = ['01 MAY', '05 MAY', '10 MAY', '15 MAY', '20 MAY', '25 MAY', '30 MAY', ''];
    seriesData1 = [150, 220, 200, 480, 300, 680, 842, 450];
    seriesData2 = [100, 150, 130, 320, 210, 400, 310, 280];
  } else if (val === '季度') {
    xData = ['Q1', '', 'Q2', '', 'Q3', '', 'Q4', ''];
    seriesData1 = [300, 520, 450, 800, 600, 950, 1100, 850];
    seriesData2 = [250, 350, 380, 600, 500, 700, 800, 650];
  } else {
    xData = ['2023', '', '2024', '', '2025', '', '2026', ''];
    seriesData1 = [1200, 1500, 1800, 2500, 2200, 3100, 4200, 3600];
    seriesData2 = [1000, 1200, 1400, 2000, 1800, 2500, 3000, 2800];
  }
  updateOptions(opts => {
    opts.xAxis.data = xData;
    opts.series[0].data = seriesData1;
    opts.series[1].data = seriesData2;
    return opts;
  });
});

// Watch dark mode to update theme if needed
watch(
  () => themeStore.darkMode,
  () => {
    updateOptions(opts => {
      opts.tooltip.backgroundColor = themeStore.darkMode ? '#1e1e1e' : 'rgba(255, 255, 255, 0.96)';
      return opts;
    });
  }
);

// Click Handlers
const handleViewLogs = () => {
  message.info('正在获取云端数据同步日志...');
};

const handleViewAllActivities = () => {
  message.info('正在加载全量历史活动流...');
};

onMounted(() => {
  setTimeout(() => {
    updateOptions();
  }, 600);
});
</script>

<template>
  <div class="h-full flex-col overflow-y-auto custom-scrollbar dashboard-page bg-[#f8fafc] dark:bg-[#0f172a] relative">
    <div class="p-24px relative z-1 flex flex-col gap-24px">
      <!-- Welcome Header -->
      <div class="page-header flex flex-col gap-4px">
        <h1 class="text-22px font-700 text-[#1e293b] dark:text-[#f8fafc]">项目概览工作台</h1>
        <p class="text-13px text-[#64748b] dark:text-[#94a3b8]">欢迎回来，这是您当前的勘察数字化管理中心。</p>
      </div>

      <!-- Core Metrics Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-20px">
        <div
          v-for="card in metricCards"
          :key="card.title"
          class="metric-card bg-white dark:bg-[#1e293b] border border-[#e2e8f0] dark:border-[#334155] rounded-16px p-20px shadow-sm transition-all duration-300 hover:shadow-md hover:-translate-y-2px"
        >
          <div class="flex items-center justify-between mb-16px">
            <div class="icon-box w-40px h-40px rounded-10px bg-[#f1f5f9] dark:bg-[#334155] flex-center">
              <SvgIcon :icon="card.icon" class="text-22px text-[#1677ff]" />
            </div>
            <span
              class="px-8px py-2px rounded-full text-11px font-600"
              :class="{
                'bg-[#e6f4ff] text-[#1677ff]': card.badgeType === 'primary',
                'bg-[#f6ffed] text-[#52c41a]': card.badgeType === 'success',
                'bg-[#fffbe6] text-[#faad14]': card.badgeType === 'warning',
                'bg-[#f0f5ff] text-[#2f54eb]': card.badgeType === 'info'
              }"
            >
              {{ card.badge }}
            </span>
          </div>
          <div class="text-13px text-[#64748b] dark:text-[#94a3b8] mb-4px font-500">{{ card.title }}</div>
          <div class="text-28px font-700 text-[#0f172a] dark:text-[#f8fafc] tracking-tight leading-none">
            {{ card.value }}
          </div>
        </div>
      </div>

      <!-- Main Layout Grid (2/3 & 1/3) -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-24px">
        <!-- Left Side: Chart & Bottom Cards (2/3 width) -->
        <div class="lg:col-span-2 flex flex-col gap-24px">
          <!-- Trend Chart Card -->
          <div class="bg-white dark:bg-[#1e293b] border border-[#e2e8f0] dark:border-[#334155] rounded-16px shadow-sm">
            <div class="flex justify-between items-center px-24px py-20px border-b border-[#f1f5f9] dark:border-[#334155]">
              <div>
                <h3 class="text-15px font-700 text-[#1e293b] dark:text-[#f8fafc]">勘察任务趋势图</h3>
                <p class="text-12px text-[#64748b] dark:text-[#94a3b8] mt-4px font-400">最近30天全平台数据采集频率分析</p>
              </div>
              <div class="tab-group flex gap-2px bg-[#f1f5f9] dark:bg-[#334155] p-2px rounded-6px">
                <button
                  v-for="range in timeRanges"
                  :key="range"
                  class="px-12px py-4px text-12px rounded-4px font-500 transition-all cursor-pointer border-none"
                  :class="selectedTimeRange === range ? 'bg-white dark:bg-[#1e293b] text-[#1677ff] shadow-sm' : 'text-[#64748b] dark:text-[#94a3b8] bg-transparent'"
                  @click="selectedTimeRange = range"
                >
                  {{ range }}
                </button>
              </div>
            </div>
            <div class="p-20px">
              <div ref="domRef" class="h-320px w-full"></div>
            </div>
          </div>

          <!-- Bottom Grid: Coverage & Sync -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-24px">
            <!-- Coverage Card -->
            <div class="bg-white dark:bg-[#1e293b] border border-[#e2e8f0] dark:border-[#334155] rounded-16px p-24px shadow-sm flex flex-col justify-between">
              <div class="flex items-center gap-8px mb-20px">
                <span class="i-material-symbols:diamond-outline-rounded text-18px text-[#1677ff]"></span>
                <h3 class="text-15px font-700 text-[#1e293b] dark:text-[#f8fafc]">实地勘察覆盖分布</h3>
              </div>
              <div class="flex flex-col gap-16px">
                <div v-for="item in coverageItems" :key="item.label" class="flex flex-col gap-6px">
                  <div class="flex justify-between items-center">
                    <span class="text-13px text-[#475569] dark:text-[#94a3b8] font-500">{{ item.label }}</span>
                    <span class="text-12px font-700 text-[#0f172a] dark:text-[#f8fafc]">{{ item.percent }}%</span>
                  </div>
                  <AProgress :percent="item.percent" :show-info="false" :stroke-color="item.color" :stroke-width="6" class="m-0!" />
                </div>
              </div>
            </div>

            <!-- Cloud Sync Card -->
            <div class="bg-white dark:bg-[#1e293b] border border-[#e2e8f0] dark:border-[#334155] rounded-16px p-24px shadow-sm flex flex-col items-center justify-between">
              <div class="flex flex-col items-center py-10px">
                <div class="w-56px h-56px rounded-full bg-[#e6f4ff] dark:bg-[#1e3a5f] flex-center mb-16px">
                  <span class="i-material-symbols:cloud-done-outline-rounded text-28px text-[#1677ff]"></span>
                </div>
                <h3 class="text-16px font-700 text-[#1e293b] dark:text-[#f8fafc] mb-6px">云端同步正常</h3>
                <p class="text-12px text-[#64748b] dark:text-[#94a3b8]">最后同步于 2分钟前</p>
              </div>
              <AButton type="text" class="w-full bg-[#f0f5ff] hover:bg-[#e6f4ff]! text-[#1677ff] dark:bg-[#1e293b] dark:hover:bg-[#334155]! border-none! h-36px rounded-8px font-600 transition-colors" @click="handleViewLogs">
                查看详细日志
              </AButton>
            </div>
          </div>
        </div>

        <!-- Right Side: Recent Activity (1/3 width) -->
        <div class="lg:col-span-1">
          <div class="bg-white dark:bg-[#1e293b] border border-[#e2e8f0] dark:border-[#334155] rounded-16px shadow-sm h-full flex flex-col justify-between">
            <div class="flex justify-between items-center px-24px py-20px border-b border-[#f1f5f9] dark:border-[#334155]">
              <h3 class="text-15px font-700 text-[#1e293b] dark:text-[#f8fafc]">最近活动流</h3>
              <span class="i-material-symbols:history-toggle-off-rounded text-18px text-[#64748b] dark:text-[#94a3b8] cursor-pointer"></span>
            </div>

            <!-- Timeline Body -->
            <div class="p-24px flex-1 flex flex-col gap-24px">
              <!-- Item 1: Upload Complete -->
              <div class="flex gap-16px relative timeline-item pb-4px">
                <div class="flex flex-col items-center">
                  <div class="w-32px h-32px rounded-full bg-[#e6f4ff] flex-center z-2">
                    <span class="i-material-symbols:description-outline-rounded text-16px text-[#1677ff]"></span>
                  </div>
                  <div class="w-2px bg-[#f1f5f9] dark:bg-[#334155] flex-1 my-4px"></div>
                </div>
                <div class="flex-1">
                  <div class="flex justify-between items-center mb-6px">
                    <span class="text-14px font-700 text-[#1e293b] dark:text-[#f8fafc]">成果物上传完成</span>
                    <span class="text-12px text-[#94a3b8]">12:45</span>
                  </div>
                  <p class="text-13px text-[#64748b] dark:text-[#94a3b8] mb-10px">
                    项目 #PJ-88293 的地质报告已由 <span class="bg-[#e6f4ff] text-[#1677ff] dark:bg-[#1e3a5f] px-6px py-2px rounded-4px text-12px font-500 mx-2px">李明</span> 提交系统.
                  </p>
                  <!-- Attachment card -->
                  <div class="flex items-center justify-between p-10px bg-[#f8fafc] dark:bg-[#0f172a] border border-[#e2e8f0] dark:border-[#334155] rounded-8px">
                    <div class="flex items-center gap-8px">
                      <span class="i-material-symbols:picture-as-pdf-outline-rounded text-20px text-[#ef4444]"></span>
                      <span class="text-12px font-500 text-[#475569] dark:text-[#cbd5e1] max-w-150px truncate">Survey_Report_V2.pdf</span>
                    </div>
                    <span class="i-material-symbols:download-rounded text-16px text-[#64748b] dark:text-[#94a3b8] cursor-pointer hover:text-[#1677ff] transition-colors"></span>
                  </div>
                </div>
              </div>

              <!-- Item 2: Audit Approved -->
              <div class="flex gap-16px relative timeline-item pb-4px">
                <div class="flex flex-col items-center">
                  <div class="w-32px h-32px rounded-full bg-[#f6ffed] flex-center z-2">
                    <span class="i-material-symbols:verified-user-outline-rounded text-16px text-[#52c41a]"></span>
                  </div>
                  <div class="w-2px bg-[#f1f5f9] dark:bg-[#334155] flex-1 my-4px"></div>
                </div>
                <div class="flex-1">
                  <div class="flex justify-between items-center mb-6px">
                    <span class="text-14px font-700 text-[#1e293b] dark:text-[#f8fafc]">审核通过</span>
                    <span class="text-12px text-[#94a3b8]">10:20</span>
                  </div>
                  <p class="text-13px text-[#64748b] dark:text-[#94a3b8]">
                    上海浦东新区基建项目已通过系统预审，准备下发实地指引。
                  </p>
                </div>
              </div>

              <!-- Item 3: New Member Join -->
              <div class="flex gap-16px relative timeline-item pb-4px">
                <div class="flex flex-col items-center">
                  <div class="w-32px h-32px rounded-full bg-[#fff7e6] flex-center z-2">
                    <span class="i-material-symbols:person-add-outline-rounded text-16px text-[#fa8c16]"></span>
                  </div>
                  <div class="w-2px bg-[#f1f5f9] dark:bg-[#334155] flex-1 my-4px"></div>
                </div>
                <div class="flex-1">
                  <div class="flex justify-between items-center mb-6px">
                    <span class="text-14px font-700 text-[#1e293b] dark:text-[#f8fafc]">新成员加入</span>
                    <span class="text-12px text-[#94a3b8]">昨天</span>
                  </div>
                  <p class="text-13px text-[#64748b] dark:text-[#94a3b8] mb-10px">
                    张晓华 加入了 <span class="bg-[#f5f5f5] text-[#595959] dark:bg-[#262626] dark:text-[#bfbfbf] px-6px py-2px rounded-4px text-12px font-500 mx-2px">地勘一组</span> 团队.
                  </p>
                  <!-- Avatars overlapping -->
                  <div class="flex items-center -space-x-8px">
                    <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=80&q=80" alt="avatar" class="w-24px h-24px rounded-full border-2 border-white dark:border-[#1e293b] object-cover" />
                    <img src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=80&q=80" alt="avatar" class="w-24px h-24px rounded-full border-2 border-white dark:border-[#1e293b] object-cover" />
                    <div class="w-24px h-24px rounded-full bg-[#1677ff] text-white flex-center text-9px border-2 border-white dark:border-[#1e293b] font-700">+4</div>
                  </div>
                </div>
              </div>

              <!-- Item 4: System Update -->
              <div class="flex gap-16px relative timeline-item">
                <div class="flex flex-col items-center">
                  <div class="w-32px h-32px rounded-full bg-[#f9f0ff] flex-center z-2">
                    <span class="i-material-symbols:settings-outline-rounded text-16px text-[#722ed1]"></span>
                  </div>
                </div>
                <div class="flex-1">
                  <div class="flex justify-between items-center mb-6px">
                    <span class="text-14px font-700 text-[#1e293b] dark:text-[#f8fafc]">系统更新</span>
                    <span class="text-12px text-[#94a3b8]">昨天</span>
                  </div>
                  <p class="text-13px text-[#64748b] dark:text-[#94a3b8]">
                    平台算法引擎升级至 V3.4.1，优化了坐标纠偏精度。
                  </p>
                </div>
              </div>
            </div>

            <!-- View All Button -->
            <div class="p-16px border-t border-[#f1f5f9] dark:border-[#334155]">
              <button class="w-full py-8px border border-dashed border-[#e2e8f0] dark:border-[#334155] rounded-8px text-13px font-600 text-[#64748b] dark:text-[#94a3b8] hover:text-[#1677ff] hover:border-[#1677ff] dark:hover:text-[#1677ff] dark:hover:border-[#1677ff] transition-all bg-transparent cursor-pointer" @click="handleViewAllActivities">
                查看全量历史活动
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--color-divider);
  border-radius: 10px;
}

/* Glass effect background */
.glass-bg-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  pointer-events: none;
  background: radial-gradient(circle at 10% 10%, rgba(22, 119, 255, 0.04) 0%, transparent 40%),
              radial-gradient(circle at 90% 80%, rgba(16, 185, 129, 0.03) 0%, transparent 40%);
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
