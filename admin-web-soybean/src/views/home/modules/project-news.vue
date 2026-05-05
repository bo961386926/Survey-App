<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';

defineOptions({
  name: 'ProjectNews'
});

const router = useRouter();

interface ActivityItem {
  id: number;
  type: string;
  title: string;
  description: string;
  time: string;
  dotColor: string;
}

const activities = computed<ActivityItem[]>(() => [
  {
    id: 1,
    type: 'create',
    title: '新建',
    description: '张管理 在project「A1标段排污口勘查项目」进行了新建',
    time: '6天前',
    dotColor: 'var(--color-primary)'
  },
  {
    id: 2,
    type: 'submit',
    title: '提交',
    description: '刘晨 在survey「西闸口 001」进行了提交',
    time: '6天前',
    dotColor: 'var(--color-primary)'
  },
  {
    id: 3,
    type: 'update',
    title: '更新',
    description: '王审核 在survey「东排口 003」进行了更新',
    time: '6天前',
    dotColor: 'var(--color-success)'
  },
  {
    id: 4,
    type: 'update',
    title: '更新',
    description: '张管理 在template「排污口标准模板 v2」进行了更新',
    time: '6天前',
    dotColor: 'var(--color-success)'
  }
]);

const quickActions = [
  { 
    name: '新建项目', 
    icon: 'i-material-symbols:add-circle-outline-rounded', 
    color: 'var(--color-primary)',
    route: '/project/list'
  },
  { 
    name: '导入点位', 
    icon: 'i-material-symbols:upload-file-outline-rounded', 
    color: 'var(--color-success)',
    route: '/point/list'
  },
  { 
    name: '审核数据', 
    icon: 'i-material-symbols:verified-user-outline-rounded', 
    color: 'var(--color-warning)',
    route: '/audit/list'
  },
  { 
    name: '导出报告', 
    icon: 'i-material-symbols:download-outline-rounded', 
    color: 'var(--color-info)',
    route: '/export/list'
  }
];

const handleAction = (route: string) => {
  router.push(route);
};
</script>

<template>
  <div class="rounded-xl overflow-hidden h-full flex flex-col" style="background: var(--bg-card); border: 1px solid var(--color-divider); box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
    <!-- Header -->
    <div class="flex justify-between items-center px-24px py-16px" style="border-bottom: 1px solid var(--color-divider);">
      <h3 class="text-15px font-600 text-[var(--color-text-primary)]">最近活动</h3>
      <div class="flex items-center gap-12px">
        <span class="i-material-symbols:history-outline-rounded text-18px text-[var(--color-text-placeholder)]"></span>
        <span class="i-material-symbols:view-module-outline-rounded text-18px text-[var(--color-text-placeholder)]"></span>
      </div>
    </div>

    <!-- Activity List -->
    <div class="flex-1 px-24px py-16px overflow-y-auto">
      <div class="flex flex-col gap-16px">
        <div 
          v-for="item in activities" 
          :key="item.id" 
          class="flex gap-12px"
        >
          <!-- Activity Icon -->
          <div 
            class="w-32px h-32px rounded-full flex items-center justify-center flex-shrink-0"
            :style="{
              background: item.type === 'create' ? 'rgba(22,119,255,0.1)' : 
                         item.type === 'submit' ? 'rgba(22,119,255,0.1)' : 
                         'rgba(16,185,129,0.1)',
              color: item.type === 'create' || item.type === 'submit' ? 'var(--color-primary)' : 'var(--color-success)'
            }"
          >
            <span :class="item.type === 'create' ? 'i-material-symbols:add-circle-outline-rounded' : 
                          item.type === 'submit' ? 'i-material-symbols:cloud-upload-outline-rounded' : 
                          'i-material-symbols:edit-outline-rounded'" class="text-16px"></span>
          </div>
          
          <!-- Activity Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-start justify-between gap-8px mb-4px">
              <div class="text-14px font-600 text-[var(--color-text-primary)]">{{ item.title }}</div>
              <div class="text-12px text-[var(--color-text-placeholder)] flex-shrink-0">{{ item.time }}</div>
            </div>
            <div class="text-13px text-[var(--color-text-secondary)]" style="line-height: 1.5;">
              {{ item.description }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer: View All -->
    <div class="px-24px py-12px" style="border-top: 1px solid var(--color-divider);">
      <a class="text-13px text-[var(--color-primary)] cursor-pointer font-500 hover:underline block text-center">
        查看全量历史活动
      </a>
    </div>
  </div>
</template>

<style scoped>
</style>
