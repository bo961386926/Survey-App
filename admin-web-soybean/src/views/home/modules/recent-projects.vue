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
  <div class="rounded-xl overflow-hidden h-full flex flex-col" style="background: var(--bg-card); border: 1px solid var(--color-divider); box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
    <!-- Header -->
    <div class="flex justify-between items-center px-24px py-16px" style="border-bottom: 1px solid var(--color-divider);">
      <div class="flex items-center gap-8px">
        <div class="w-28px h-28px rounded-6px flex items-center justify-center" style="background: rgba(22,119,255,0.1);">
          <span class="i-material-symbols:folder-outline-rounded text-16px text-[var(--color-primary)]"></span>
        </div>
        <h3 class="text-15px font-600 text-[var(--color-text-primary)]">最近项目</h3>
      </div>
      <a @click="handleViewAll" class="text-13px text-[var(--color-primary)] cursor-pointer font-500 hover:underline">查看全部</a>
    </div>

    <!-- Project List -->
    <div class="flex-1 px-24px py-16px overflow-y-auto">
      <!-- Loading -->
      <div v-if="loading" class="flex flex-col gap-12px">
        <div v-for="i in 3" :key="i" class="flex items-center gap-12px p-14px rounded-lg" style="background: var(--bg-page);">
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
          class="flex items-center justify-between p-12px rounded-lg cursor-pointer transition-all hover:translate-x-2px"
          style="background: var(--bg-page); border: 1px solid var(--color-divider);"
          @click="handleProjectClick(item.id)"
        >
          <div class="flex items-center gap-10px flex-1 min-w-0">
            <!-- Project Icon -->
            <div class="w-32px h-32px rounded-6px flex items-center justify-center flex-shrink-0 overflow-hidden" style="background: #EFF6FF;">
              <svg class="w-16px h-16px" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
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
              <div class="text-13px font-medium text-[var(--color-text-primary)] truncate">{{ item.projectName }}</div>
              <div class="text-12px text-[var(--color-text-secondary)] truncate">
                {{ item.projectCode }} · {{ item.manager || '未指派' }}
              </div>
            </div>
          </div>

          <!-- Status Badge -->
          <span
            class="flex-shrink-0 px-8px py-3px rounded-4px text-11px font-medium"
            :class="getStatus(item.status).class"
            style="background: rgba(59,130,246,0.1);"
          >
            {{ getStatus(item.status).label }}
          </span>
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
</style>
