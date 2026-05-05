<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';

defineOptions({ name: 'HeaderBanner' });

const router = useRouter();

const userName = computed(() => '管理员');

const stats = computed(() => ({
  pendingAudit: 12,
  activeProjects: 3,
  systemStatus: '正常'
}));

const handleCreateProject = () => {
  router.push('/project/list');
};

const handleImportPoints = () => {
  router.push('/point/list');
};
</script>

<template>
  <div class="relative rounded-2xl overflow-hidden mb-28px" style="background: linear-gradient(135deg, rgba(22,119,255,0.12), rgba(139,92,246,0.08)); border: 1px solid rgba(22,119,255,0.2);">
    <!-- Background decoration -->
    <div class="absolute top--50% right--10% w-300px h-300px rounded-full" style="background: radial-gradient(circle, rgba(22,119,255,0.15) 0%, transparent 60%); pointer-events: none;"></div>
    
    <div class="relative z-1 px-36px py-32px flex items-center justify-between">
      <div>
        <h2 class="text-24px font-700 text-[var(--color-text-primary)] mb-8px" style="letter-spacing: -0.5px;">
          👋 欢迎回来，{{ userName }}
        </h2>
        <p class="text-14px text-[var(--color-text-secondary)] max-w-500px" style="line-height: 1.6;">
          今日有 {{ stats.pendingAudit }} 条数据待审核，{{ stats.activeProjects }} 个项目正在进行中。系统运行{{ stats.systemStatus }}，所有服务状态良好。
        </p>
      </div>
      <div class="flex gap-12px">
        <a-button type="primary" size="large" @click="handleCreateProject" class="px-24px h-44px text-14px font-600">
          <template #icon><span class="i-material-symbols:add-circle-outline-rounded"></span></template>
          新建项目
        </a-button>
        <a-button size="large" @click="handleImportPoints" class="px-24px h-44px text-14px font-600">
          <template #icon><span class="i-material-symbols:upload-file-outline-rounded"></span></template>
          导入点位
        </a-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
