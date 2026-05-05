<script setup lang="ts">
defineOptions({ name: 'SurveyCoverage' });

const coverages = [
  { name: '已完成勘察', percent: 78, color: 'bg-success', count: 6926, icon: 'i-material-symbols:check-circle-rounded' },
  { name: '待审核', percent: 12, color: 'bg-warning', count: 1068, icon: 'i-material-symbols:pending-rounded' },
  { name: '进行中', percent: 8, color: 'bg-primary', count: 712, icon: 'i-material-symbols:progress-activity-rounded' },
  { name: '未开始', percent: 2, color: 'bg-secondary', count: 196, icon: 'i-material-symbols:circle-outline' }
];

const totalCount = coverages.reduce((sum, item) => sum + item.count, 0);
</script>

<template>
  <div class="bg-card rd-20px border border-border p-28px h-full relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="absolute right-0 bottom-0 w-100px h-100px opacity-5 pointer-events-none">
      <div class="i-material-symbols:donut-large-rounded text-100px text-primary"></div>
    </div>
    
    <div class="flex-y-center justify-between mb-28px relative z-10">
      <div>
        <h4 class="font-700 text-18px text-primary-text">勘察覆盖率</h4>
        <p class="text-13px text-secondary mt-6px font-500">各状态点位分布统计</p>
      </div>
      <div class="flex items-center gap-8px bg-primary/10 px-16px py-10px rd-12px">
        <div class="i-material-symbols:location-on-rounded text-primary text-18px"></div>
        <span class="text-14px font-700 text-primary">{{ totalCount.toLocaleString() }}</span>
        <span class="text-12px text-secondary font-500">个点位</span>
      </div>
    </div>
    
    <div class="space-y-20px relative z-10">
      <div v-for="item in coverages" :key="item.name" class="group">
        <div class="flex justify-between items-center mb-10px">
          <div class="flex items-center gap-12px">
            <div :class="[item.color, 'w-10px h-10px rd-full opacity-80']"></div>
            <div :class="[item.icon, 'text-16px text-secondary group-hover:text-primary transition-colors']"></div>
            <span class="font-600 text-14px text-secondary group-hover:text-primary-text transition-colors">{{ item.name }}</span>
          </div>
          <div class="flex items-center gap-12px">
            <span class="font-700 text-16px text-primary-text">{{ item.count.toLocaleString() }}</span>
            <span class="text-12px text-placeholder font-500">个</span>
            <span :class="[item.color.replace('bg-', 'text-'), 'text-14px font-700']">{{ item.percent }}%</span>
          </div>
        </div>
        <div class="h-8px w-full bg-page rd-full overflow-hidden shadow-inner">
          <div 
            :class="['h-full rd-full transition-all duration-700 ease-out group-hover:opacity-100', item.color]" 
            :style="{ width: `${item.percent}%`, opacity: 0.85 }"
          ></div>
        </div>
      </div>
    </div>
    
    <!-- 底部统计 -->
    <div class="mt-28px pt-20px border-t border-divider flex justify-between items-center relative z-10">
      <div class="flex items-center gap-8px">
        <div class="i-material-symbols:info-outline-rounded text-secondary text-16px"></div>
        <span class="text-12px text-placeholder font-500">数据更新于 10 分钟前</span>
      </div>
      <AButton size="small" type="text" class="text-13px! font-600! text-primary! flex items-center gap-6px">
        <template #icon>
          <div class="i-material-symbols:visibility-outline-rounded text-16px"></div>
        </template>
        查看详情
      </AButton>
    </div>
  </div>
</template>

<style scoped></style>
