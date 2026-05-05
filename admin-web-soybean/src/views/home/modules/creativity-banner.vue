<script setup lang="ts">
defineOptions({ name: 'CloudSync' });

const syncStatus = {
  status: '正常',
  lastSync: '2 分钟前',
  nextSync: '即将同步',
  progress: 100
};

const syncItems = [
  { name: '项目数据', status: '已同步', icon: 'i-material-symbols:folder-sync-rounded' },
  { name: '点位信息', status: '已同步', icon: 'i-material-symbols:location-sync-rounded' },
  { name: '审核记录', status: '已同步', icon: 'i-material-symbols:fact-check-sync-rounded' }
];
</script>

<template>
  <div class="bg-card rd-20px border border-border p-28px h-full relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 opacity-5 pointer-events-none" style="background-image: radial-gradient(var(--color-primary) 1px, transparent 1px); background-size: 20px 20px;"></div>
    
    <div class="relative z-10">
      <!-- 头部状态 -->
      <div class="flex items-center justify-between mb-24px">
        <div class="flex items-center gap-16px">
          <div class="size-56px bg-success/15 rd-full flex-center border-2 border-success/20 shadow-lg shadow-success/10">
            <div class="i-material-symbols:cloud-done-rounded text-28px text-success"></div>
          </div>
          <div>
            <h4 class="text-16px font-700 text-primary-text mb-4px">云端同步</h4>
            <div class="flex items-center gap-8px">
              <div class="w-8px h-8px rd-full bg-success animate-pulse"></div>
              <span class="text-13px text-success font-600">{{ syncStatus.status }}</span>
            </div>
          </div>
        </div>
        <div class="text-right">
          <p class="text-12px text-placeholder font-500">最后同步</p>
          <p class="text-14px text-secondary font-600">{{ syncStatus.lastSync }}</p>
        </div>
      </div>
      
      <!-- 同步项目列表 -->
      <div class="space-y-12px mb-24px">
        <div v-for="item in syncItems" :key="item.name" class="flex items-center justify-between p-12px rd-12px bg-page/50 border border-border/50">
          <div class="flex items-center gap-12px">
            <div :class="[item.icon, 'text-18px text-primary']"></div>
            <span class="text-14px font-500 text-secondary">{{ item.name }}</span>
          </div>
          <div class="flex items-center gap-8px">
            <div class="i-material-symbols:check-circle-rounded text-success text-16px"></div>
            <span class="text-12px text-success font-600">{{ item.status }}</span>
          </div>
        </div>
      </div>
      
      <!-- 同步进度 -->
      <div class="mb-24px">
        <div class="flex justify-between items-center mb-8px">
          <span class="text-12px text-secondary font-500">同步进度</span>
          <span class="text-12px text-success font-700">{{ syncStatus.progress }}%</span>
        </div>
        <div class="h-6px w-full bg-page rd-full overflow-hidden">
          <div class="h-full bg-success rd-full transition-all duration-500" :style="{ width: `${syncStatus.progress}%` }"></div>
        </div>
      </div>
      
      <!-- 底部操作 -->
      <div class="flex gap-12px">
        <AButton size="small" class="flex-1 rd-12px! text-13px! font-600! flex items-center justify-center gap-8px border border-primary text-primary! hover:bg-primary/10!">
          <template #icon>
            <div class="i-material-symbols:sync-rounded text-16px"></div>
          </template>
          立即同步
        </AButton>
        <AButton size="small" type="text" class="text-13px! font-600! text-primary! flex items-center gap-6px">
          <template #icon>
            <div class="i-material-symbols:history-rounded text-16px"></div>
          </template>
          查看日志
        </AButton>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
