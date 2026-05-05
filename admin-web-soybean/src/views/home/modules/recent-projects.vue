<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';

defineOptions({
  name: 'RecentProjects'
});

const router = useRouter();

interface Project {
  id: number;
  name: string;
  code: string;
  manager: string;
  status: string;
  statusClass: string;
  icon: string;
  iconColor: string;
}

const projects = computed<Project[]>(() => [
  { 
    id: 1, 
    name: '兰溪智慧排水项目', 
    code: 'lanx-20260102',
    manager: '杨丽萍',
    status: '进行中',
    statusClass: 'text-[var(--color-primary)]',
    icon: 'i-material-symbols:folder-outline-rounded',
    iconColor: 'var(--color-primary)'
  },
  { 
    id: 2, 
    name: '亳州市城区入河排污口排查项目', 
    code: 'BZ-2024-001',
    manager: '张建国',
    status: '进行中',
    statusClass: 'text-[var(--color-primary)]',
    icon: 'i-material-symbols:folder-outline-rounded',
    iconColor: 'var(--color-primary)'
  },
  { 
    id: 3, 
    name: '涡河流域排污口专项勘查', 
    code: 'WZ-2024-002',
    manager: '李秀英',
    status: '进行中',
    statusClass: 'text-[var(--color-primary)]',
    icon: 'i-material-symbols:folder-outline-rounded',
    iconColor: 'var(--color-primary)'
  },
  { 
    id: 4, 
    name: '蒙城县农村排污口普查', 
    code: 'MC-2024-003',
    manager: '王志强',
    status: '进行中',
    statusClass: 'text-[var(--color-primary)]',
    icon: 'i-material-symbols:folder-outline-rounded',
    iconColor: 'var(--color-primary)'
  },
  { 
    id: 5, 
    name: '利辛县河道排污口整治勘查', 
    code: 'LX-2024-004',
    manager: '赵敏',
    status: '进行中',
    statusClass: 'text-[var(--color-primary)]',
    icon: 'i-material-symbols:folder-outline-rounded',
    iconColor: 'var(--color-primary)'
  }
]);

const handleViewAll = () => {
  router.push('/project/list');
};
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
      <div class="flex flex-col gap-12px">
        <div 
          v-for="item in projects" 
          :key="item.id" 
          class="flex items-center justify-between p-14px rounded-lg cursor-pointer transition-all hover:translate-x-2px"
          style="background: var(--bg-page); border: 1px solid var(--color-divider);"
        >
          <!-- Project Info -->
          <div class="flex items-center gap-12px flex-1 min-w-0">
            <!-- Project Icon -->
            <div 
              class="w-36px h-36px rounded-8px flex items-center justify-center text-18px flex-shrink-0"
              :style="{ background: `rgba(22,119,255,0.1)` }"
            >
              <span :style="{ color: item.iconColor }" :class="item.icon"></span>
            </div>
            
            <!-- Project Details -->
            <div class="flex-1 min-w-0">
              <div class="text-14px font-600 text-[var(--color-text-primary)] mb-4px">{{ item.name }}</div>
              <div class="text-12px text-[var(--color-text-secondary)]">
                {{ item.code }} · {{ item.manager }}
              </div>
            </div>
          </div>
          
          <!-- Status Badge -->
          <div class="flex-shrink-0">
            <span 
              class="px-10px py-4px rounded-4px text-12px font-500"
              :class="item.statusClass"
              :style="{ background: 'rgba(22,119,255,0.1)' }"
            >
              {{ item.status }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
