<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { fetchGetProjectList } from '@/service/api';

defineOptions({
  name: 'RecentProjects'
});

const router = useRouter();
const loading = ref(false);
const projectList = ref<Api.Project.ProjectInfo[]>([]);

// 取最新的5条进行中项目
const projects = computed(() => projectList.value.slice(0, 5));

const statusMap: Record<number, { label: string; class: string }> = {
  0: { label: '草稿', class: 'text-info' },
  1: { label: '进行中', class: 'text-primary' },
  2: { label: '已暂停', class: 'text-warning' },
  3: { label: '已完成', class: 'text-success' },
  4: { label: '已归档', class: 'text-secondary' }
};

const getStatus = (status: number) => statusMap[status] || { label: '未知', class: 'text-secondary' };

const loadProjects = async () => {
  loading.value = true;
  try {
    const { data } = await fetchGetProjectList({ current: 1, size: 5 });
    if (data?.records) {
      projectList.value = data.records;
    }
  } catch (e) {
    console.error('Failed to load recent projects', e);
  } finally {
    loading.value = false;
  }
};

const handleViewAll = () => {
  router.push('/project');
};

const handleProjectClick = (id: number | string) => {
  router.push(`/project/detail/${id}`);
};

onMounted(() => {
  loadProjects();
});
</script>

<template>
  <div class="glass-card rounded-lg overflow-hidden h-full flex flex-col" v-mouse-glow="{ color: '22,119,255', size: 200, intensity: 0.04 }">
    <!-- Header -->
    <div class="flex justify-between items-center px-24px py-16px header-divider">
      <div class="flex items-center gap-8px">
        <div class="w-28px h-28px rounded-6px flex items-center justify-center header-icon-wrapper">
          <span class="i-material-symbols:folder-outline-rounded text-16px text-[var(--color-primary)]"></span>
        </div>
        <h3 class="text-15px font-600 text-[var(--color-text-primary)]">最近项目</h3>
      </div>
      <a @click="handleViewAll" class="text-13px text-[var(--color-primary)] cursor-pointer font-500 hover-underline-wrapper">查看全部</a>
    </div>

    <!-- Project List -->
    <div class="flex-1 px-24px py-16px overflow-y-auto">
      <!-- Loading -->
      <div v-if="loading" class="flex flex-col gap-12px">
        <div v-for="i in 3" :key="i" class="flex items-center gap-12px p-14px rounded-lg skeleton-item">
          <div class="w-36px h-36px rounded-8px bg-[var(--bg-card-alt)] animate-pulse flex-shrink-0"></div>
          <div class="flex-1">
            <div class="h-14px w-3/4 bg-[var(--bg-card-alt)] rounded animate-pulse mb-6px"></div>
            <div class="h-12px w-1/2 bg-[var(--bg-card-alt)] rounded animate-pulse"></div>
          </div>
        </div>
      </div>

      <!-- Data -->
      <div v-else class="flex flex-col gap-10px">
        <div
          v-for="item in projects"
          :key="item.id"
          class="project-item group"
          @click="handleProjectClick(item.id)"
        >
          <div class="flex items-center justify-between p-12px rounded-lg">
            <div class="flex items-center gap-10px flex-1 min-w-0">
              <!-- Project Icon -->
              <div class="project-icon-wrapper">
                <svg class="project-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="10" stroke="#3B82F6" stroke-width="1.5" stroke-dasharray="3 2" opacity="0.4"/>
                  <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" fill="#3B82F6" opacity="0.2" stroke="#3B82F6" stroke-width="1.5" stroke-linejoin="round"/>
                  <circle cx="12" cy="9" r="2.5" fill="#3B82F6"/>
                  <line x1="8" y1="9" x2="10.2" y2="9" stroke="#3B82F6" stroke-width="1.5" stroke-linecap="round"/>
                  <line x1="13.8" y1="9" x2="16" y2="9" stroke="#3B82F6" stroke-width="1.5" stroke-linecap="round"/>
                  <line x1="12" y1="6.5" x2="12" y2="8" stroke="#3B82F6" stroke-width="1.5" stroke-linecap="round"/>
                  <line x1="12" y1="10" x2="12" y2="11.5" stroke="#3B82F6" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </div>

              <!-- Project Details -->
              <div class="flex-1 min-w-0">
                <div class="project-name">{{ item.projectName }}</div>
                <div class="project-code">
                  {{ item.projectCode }} · {{ item.manager || '未指派' }}
                </div>
              </div>
            </div>

            <!-- Status Badge + Arrow Indicator -->
            <div class="flex items-center gap-8px">
              <span
                class="status-badge"
                :class="getStatus(Number(item.status)).class"
              >
                {{ getStatus(Number(item.status)).label }}
              </span>
              <!-- Hover 箭头指示器 -->
              <div class="arrow-indicator">
                <span class="i-material-symbols:arrow-forward-rounded text-16px"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty state -->
        <div v-if="projects.length === 0" class="py-16px text-center">
          <div class="i-material-symbols:folder-open-outline text-32px mx-auto mb-8px text-[var(--color-text-placeholder)]"></div>
          <p class="text-13px text-[var(--color-text-secondary)]">暂无项目</p>
          <a-button type="primary" size="small" class="mt-8px" @click="handleViewAll">创建项目</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card:hover {
  border-color: rgba(22, 119, 255, 0.25);
  box-shadow: 0 8px 32px rgba(22, 119, 255, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-divider {
  border-bottom: 1px solid var(--color-divider);
}

.header-icon-wrapper {
  background: rgba(22, 119, 255, 0.1);
  transition: all 0.3s ease;
}

.glass-card:hover .header-icon-wrapper {
  background: rgba(22, 119, 255, 0.15);
  transform: scale(1.05);
}

.hover-underline-wrapper {
  position: relative;
  transition: all 0.2s ease;
}

.hover-underline-wrapper::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: var(--color-primary);
  transition: width 0.3s ease;
}

.hover-underline-wrapper:hover::after {
  width: 100%;
}

.project-item {
  cursor: pointer;
}

.project-item > div {
  background: rgba(247, 248, 250, 0.5);
  border: 1px solid var(--color-divider);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.project-item:hover > div {
  background: rgba(22, 119, 255, 0.04);
  border-color: rgba(22, 119, 255, 0.2);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.06);
}

.project-item:active > div {
  transform: translateX(4px) scale(0.99);
  transition-duration: 0.1s;
}

.project-icon-wrapper {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  background: #EFF6FF;
  transition: all 0.25s ease;
}

.project-item:hover .project-icon-wrapper {
  background: rgba(22, 119, 255, 0.1);
  transform: scale(1.05);
}

.project-svg {
  width: 16px;
  height: 16px;
}

.project-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s ease;
}

.project-item:hover .project-name {
  color: var(--color-primary);
}

.project-code {
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-badge {
  flex-shrink: 0;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  background: rgba(59, 130, 246, 0.1);
  transition: all 0.25s ease;
}

.project-item:hover .status-badge {
  background: rgba(59, 130, 246, 0.15);
}

.arrow-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  color: var(--color-primary);
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(22, 119, 255, 0.08);
}

.project-item:hover .arrow-indicator {
  opacity: 1;
  transform: translateX(0);
}
</style>
