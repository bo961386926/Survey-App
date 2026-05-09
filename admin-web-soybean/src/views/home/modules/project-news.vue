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
  <div class="news-card rounded-xl overflow-hidden h-full flex flex-col" v-mouse-glow="{ color: '22,119,255', size: 200, intensity: 0.04 }">
    <!-- Header -->
    <div class="flex justify-between items-center px-24px py-16px header-divider">
      <h3 class="text-15px font-600 text-[var(--color-text-primary)]">最近活动</h3>
      <div class="flex items-center gap-12px">
        <span class="header-icon i-material-symbols:history-outline-rounded text-18px text-[var(--color-text-placeholder)]"></span>
        <span class="header-icon i-material-symbols:view-module-outline-rounded text-18px text-[var(--color-text-placeholder)]"></span>
      </div>
    </div>

    <!-- Activity List -->
    <div class="flex-1 px-24px py-16px overflow-y-auto">
      <div class="flex flex-col gap-16px">
        <div
          v-for="item in activities"
          :key="item.id"
          class="activity-item"
        >
          <!-- Activity Icon -->
          <div class="activity-icon-wrapper"
               :style="{
                 background: item.type === 'create' ? 'rgba(22,119,255,0.1)' :
                            item.type === 'submit' ? 'rgba(22,119,255,0.1)' :
                            'rgba(16,185,129,0.1)',
                 color: item.type === 'create' || item.type === 'submit' ? 'var(--color-primary)' : 'var(--color-success)'
               }">
            <span :class="item.type === 'create' ? 'i-material-symbols:add-circle-outline-rounded' :
                          item.type === 'submit' ? 'i-material-symbols:cloud-upload-outline-rounded' :
                          'i-material-symbols:edit-outline-rounded'" class="activity-icon"></span>
          </div>

          <!-- Activity Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-start justify-between gap-8px mb-4px">
              <div class="activity-title">{{ item.title }}</div>
              <div class="activity-time">{{ item.time }}</div>
            </div>
            <div class="activity-desc">
              {{ item.description }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer: View All -->
    <div class="px-24px py-12px footer-divider">
      <a class="view-all-link">
        查看全量历史活动
      </a>
    </div>
  </div>
</template>

<style scoped>
.news-card {
  background: var(--bg-card);
  border: 1px solid var(--color-divider);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.news-card:hover {
  border-color: rgba(22, 119, 255, 0.25);
  box-shadow: 0 8px 32px rgba(22, 119, 255, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-divider {
  border-bottom: 1px solid var(--color-divider);
}

.footer-divider {
  border-top: 1px solid var(--color-divider);
}

.header-icon {
  transition: all 0.2s ease;
  cursor: pointer;
  border-radius: 4px;
  padding: 2px;
}

.header-icon:hover {
  color: var(--color-primary);
  background: rgba(22, 119, 255, 0.08);
  transform: scale(1.1);
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 8px;
  border-radius: 6px;
  margin: -8px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.activity-item:hover {
  background: rgba(22, 119, 255, 0.04);
}

.activity-item:active {
  background: rgba(22, 119, 255, 0.08);
  transition-duration: 0.1s;
}

.activity-icon-wrapper {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.25s ease;
}

.activity-item:hover .activity-icon-wrapper {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.15);
}

.activity-icon {
  font-size: 16px;
}

.activity-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  transition: color 0.2s ease;
}

.activity-item:hover .activity-title {
  color: var(--color-primary);
}

.activity-time {
  font-size: 12px;
  color: var(--color-text-placeholder);
  flex-shrink: 0;
}

.activity-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.view-all-link {
  font-size: 13px;
  color: var(--color-primary);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  display: block;
  text-align: center;
  position: relative;
}

.view-all-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  width: 0;
  height: 1px;
  background: var(--color-primary);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.view-all-link:hover::after {
  width: 100%;
}

.view-all-link:hover {
  transform: translateX(2px);
}
</style>
