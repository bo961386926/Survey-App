<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Modal, message } from 'ant-design-vue';
import {
  FolderOutlined,
  PlusOutlined,
  SearchOutlined,
  ArrowRightOutlined,
  EditOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  CheckCircleOutlined,
  StopOutlined,
  DeleteOutlined,
  EnvironmentOutlined
} from '@ant-design/icons-vue';
import {
  fetchGetProjectList,
  fetchDeleteProject,
  fetchUpdateProjectStatus
} from '@/service/api';
import ProjectCreateModal from '../modules/create-modal.vue';
import { useAuth } from '@/hooks/common/auth';

defineOptions({ name: 'ProjectList' });

const router = useRouter();
const { hasPermission } = useAuth();

// Search Filter
const searchForm = ref({
  keyword: '',
  status: undefined as string | undefined
});

// Create/Edit modal visibility
const modalVisible = ref(false);
const editingProject = ref<Api.Project.ProjectInfo | null>(null);

// Table Data & Pagination
const loading = ref(false);
const projectList = ref<Api.Project.ProjectInfo[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
});

const columns = computed(() => [
  { title: '项目编号', dataIndex: 'projectCode', key: 'projectCode', width: 140 },
  { title: '项目名称 / 勘察区域', key: 'projectNameRegion', width: 280 },
  { title: '负责人', dataIndex: 'manager', key: 'manager', width: 120 },
  { title: '勘察进度', key: 'progress', width: 180 },
  { title: '当前状态', key: 'status', width: 120 },
  { title: '更新时间', key: 'updateTime', width: 160 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' as const }
]);

const loadData = async () => {
  loading.value = true;
  try {
    const { data, error } = await fetchGetProjectList({
      current: pagination.value.current,
      size: pagination.value.pageSize,
      keyword: searchForm.value.keyword,
      status: searchForm.value.status
    });

    if (!error && data) {
      projectList.value = (data.records || []).map((r: any) => ({ ...r, id: String(r.id) }));
      pagination.value.total = data.total || 0;
    }
  } catch (err) {
    console.error('Failed to load projects', err);
  } finally {
    loading.value = false;
  }
};

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadData();
};

const handleSearch = () => {
  pagination.value.current = 1;
  loadData();
};

const handleViewProject = (id: string | number) => {
  router.push(`/project/detail/${String(id)}`);
};

const handleCreateProject = () => {
  editingProject.value = null;
  modalVisible.value = true;
};

const handleEditProject = (record: Api.Project.ProjectInfo) => {
  editingProject.value = record;
  modalVisible.value = true;
};

const handleDeleteProject = (record: Api.Project.ProjectInfo) => {
  Modal.confirm({
    title: '确认删除项目？',
    content: `删除项目「${record.projectName}」后，关联的所有勘察数据及点位信息将不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      const { error } = await fetchDeleteProject(record.id);
      if (!error) {
        message.success('项目已成功删除');
        loadData();
      }
    }
  });
};

const handleUpdateStatus = async (record: Api.Project.ProjectInfo, status: number) => {
  const { error } = await fetchUpdateProjectStatus(record.id, status);
  if (!error) {
    message.success('状态更新成功');
    loadData();
  }
};

const getProgressPercent = (record: any) => {
  return Math.round((record.completedCount / (record.pointCount || 1)) * 100);
};

const handleCreateSuccess = () => {
  loadData();
};

// Initials and Color Helpers for Avatar
const getInitials = (name: string) => {
  if (!name) return '??';
  if (/[\u4e00-\u9fa5]/.test(name)) {
    return name.length > 2 ? name.substring(name.length - 2) : name;
  }
  const parts = name.split(' ');
  if (parts.length > 1) {
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }
  return name.substring(0, 2).toUpperCase();
};

const getAvatarBgColor = (name: string) => {
  if (!name) return '#a0aec0';
  const colors = ['#1677ff', '#52c41a', '#faad14', '#13c2c2', '#722ed1', '#eb2f96'];
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colors[Math.abs(hash) % colors.length];
};

const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return ['--', ''];
  const parts = dateTimeStr.split(' ');
  if (parts.length === 2) {
    return [parts[0], parts[1]];
  }
  try {
    const d = new Date(dateTimeStr);
    if (isNaN(d.getTime())) return [dateTimeStr, ''];
    const date = d.toISOString().split('T')[0];
    const time = d.toTimeString().split(' ')[0];
    return [date, time];
  } catch (e) {
    return [dateTimeStr, ''];
  }
};

// 状态映射辅助函数
const getStatusClass = (status: number | string) => {
  const statusMap: Record<number, string> = {
    0: 'status-tag--draft',
    1: 'status-tag--active',
    2: 'status-tag--paused',
    3: 'status-tag--completed',
    4: 'status-tag--archived'
  };
  return statusMap[Number(status)] || 'status-tag--draft';
};

const getStatusText = (status: number | string) => {
  const textMap: Record<number, string> = {
    0: '草稿',
    1: '勘察中',
    2: '待审核',
    3: '已完成',
    4: '已归档'
  };
  return textMap[Number(status)] || '未知';
};

const metrics = computed(() => [
  { title: '本年度活跃项目', value: 248, badge: '+12.5%', icon: 'material-symbols:folder-open-outline-rounded', color: '#1677ff' },
  { title: '进行中项目', value: 156, icon: 'material-symbols:play-circle-outline-rounded', color: '#52c41a' },
  { title: '已延期项目', value: 12, icon: 'material-symbols:warning-outline-rounded', color: '#faad14' },
  { title: '系统健康度', value: '94%', icon: 'material-symbols:health-and-safety-outline-rounded', color: '#722ed1', subtitle: '资源分配与并发请求状态良好' }
]);

const filterTabs = [
  { label: '全部', value: undefined as string | undefined },
  { label: '勘察中', value: '1' },
  { label: '待审核', value: '2' }
];

const handleAdvancedSearch = () => {
  message.info('高级筛选功能开发中...');
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="h-full flex-col gap-24px p-24px overflow-y-auto project-list-page">
    <!-- Page Header -->
    <div class="flex justify-between items-center page-header mb-16px">
      <div class="flex items-center gap-12px">
        <div class="page-header-icon">
          <FolderOutlined class="text-20px" />
        </div>
        <div>
          <h1 class="text-20px font-700 text-[var(--color-text-primary)]">勘察项目管理</h1>
          <p class="text-12px text-[var(--color-text-secondary)] mt-2px">管理和追踪所有勘察项目进度</p>
        </div>
      </div>
    </div>

    <!-- Stats Bar -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-16px mb-20px">
      <!-- Card 1-4: White glass cards -->
      <div
        v-for="item in metrics"
        :key="item.title"
        class="metric-card bg-white/75 dark:bg-[#1e293b]/75 border border-white/40 dark:border-white/10 rounded-12px p-16px backdrop-blur-md shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-2px flex flex-col justify-between min-h-100px"
      >
        <div class="flex items-center justify-between mb-12px">
          <span class="text-13px text-[var(--color-text-secondary)] font-500">{{ item.title }}</span>
          <div class="flex items-center gap-6px">
            <span
              v-if="item.badge"
              class="px-8px py-2px rounded-full text-10px font-600 bg-emerald-50 text-emerald-600 dark:bg-emerald-950/30 dark:text-emerald-400"
            >
              {{ item.badge }}
            </span>
            <div class="w-32px h-32px rounded-8px flex items-center justify-center" :style="{ backgroundColor: item.color + '15', color: item.color }">
              <SvgIcon :icon="item.icon" class="text-18px" />
            </div>
          </div>
        </div>
        <div>
          <div class="text-24px font-700 text-[var(--color-text-primary)] tracking-tight leading-none">
            {{ typeof item.value === 'number' ? item.value.toLocaleString() : item.value }}
          </div>
          <div v-if="item.subtitle" class="text-11px text-[var(--color-text-placeholder)] mt-6px">
            {{ item.subtitle }}
          </div>
        </div>
      </div>
    </div>

    <!-- Table Section -->
    <div class="table-card" v-mouse-glow="{ color: '22,119,255', size: 300, intensity: 0.04 }">
      <!-- Filter Bar -->
      <div class="flex justify-between items-center px-20px py-14px border-b border-[var(--color-divider)]">
        <div class="flex items-center gap-16px">
          <h3 class="text-15px font-700 text-[var(--color-text-primary)] m-0">项目列表数据</h3>
          <!-- Tabs -->
          <div class="flex gap-4px bg-gray-100 dark:bg-zinc-800 p-2px rd-6px">
            <button
              v-for="opt in filterTabs"
              :key="opt.label"
              class="px-14px h-28px rd-4px flex items-center cursor-pointer transition-all border-none text-12px font-500"
              :class="searchForm.status === opt.value ? 'bg-white dark:bg-zinc-700 text-[var(--color-primary)] shadow-sm' : 'bg-transparent text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]'"
              @click="searchForm.status = opt.value; handleSearch()"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>

        <div class="flex items-center gap-12px">
          <AInput v-model:value="searchForm.keyword" placeholder="搜索项目名称或编号..." class="w-200px h-32px" @pressEnter="handleSearch" allow-clear>
            <template #prefix>
              <SearchOutlined class="text-13px text-[var(--color-text-secondary)]" />
            </template>
          </AInput>
          
          <AButton class="h-32px px-12px flex items-center gap-6px" @click="handleAdvancedSearch">
            <template #icon>
              <SvgIcon icon="material-symbols:filter-alt-outline-rounded" class="text-14px" />
            </template>
            <span>高级筛选</span>
          </AButton>

          <AButton v-if="hasPermission('project:create')" type="primary" class="h-32px px-14px flex items-center gap-6px" @click="handleCreateProject">
            <template #icon>
              <PlusOutlined />
            </template>
            <span>新建项目</span>
          </AButton>
        </div>
      </div>

      <!-- Table -->
      <ATable
        :dataSource="projectList"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="id"
        class="project-table"
        :scroll="{ x: 1100, y: 'calc(100vh - 430px)' }"
      >
        <template #bodyCell="{ column, record }">
          <!-- Project Code -->
          <template v-if="column.key === 'projectCode'">
            <span class="font-mono font-600 text-13px text-[var(--color-text-primary)]">{{ record.projectCode || '--' }}</span>
          </template>

          <!-- Project Name & Region -->
          <template v-if="column.key === 'projectNameRegion'">
            <div class="flex flex-col gap-4px py-4px">
              <span class="text-14px font-600 text-[var(--color-text-primary)] hover:text-[var(--color-primary)] transition-colors cursor-pointer" @click="handleViewProject(record.id)">
                {{ record.projectName }}
              </span>
              <span class="text-12px text-[var(--color-text-secondary)] flex items-center gap-4px">
                <SvgIcon icon="material-symbols:location-on-outline-rounded" class="text-14px text-gray-400" />
                {{ record.region || '未设定区域' }}
              </span>
            </div>
          </template>

          <!-- Manager -->
          <template v-if="column.key === 'manager'">
            <div class="flex items-center gap-8px">
              <div 
                class="w-24px h-24px rd-full flex items-center justify-center text-10px font-600 text-white select-none"
                :style="{ backgroundColor: getAvatarBgColor(record.manager) }"
              >
                {{ getInitials(record.manager) }}
              </div>
              <span class="text-13px font-500 text-[var(--color-text-primary)]">{{ record.manager || '未指派' }}</span>
            </div>
          </template>

          <!-- Progress -->
          <template v-if="column.key === 'progress'">
            <div class="flex flex-col gap-4px w-full max-w-160px">
              <div class="flex justify-between items-center text-11px text-[var(--color-text-secondary)]">
                <span>{{ getProgressPercent(record) }}%</span>
                <span class="font-mono">{{ record.completedCount || 0 }}/{{ record.pointCount || 0 }}</span>
              </div>
              <AProgress 
                :percent="getProgressPercent(record)" 
                size="small" 
                :show-info="false" 
                :stroke-width="5" 
                :stroke-color="record.status == 2 ? '#ff7d00' : '#1677ff'" 
                class="m-0!" 
              />
            </div>
          </template>

          <!-- Status -->
          <template v-if="column.key === 'status'">
            <span
              class="status-tag"
              :class="getStatusClass(record.status)"
            >
              <span class="status-dot" :class="record.status == 1 ? 'status-dot--pulse' : ''"></span>
              {{ getStatusText(record.status) }}
            </span>
          </template>

          <!-- Update Time -->
          <template v-if="column.key === 'updateTime'">
            <div class="flex flex-col gap-2px">
              <span class="text-13px font-500 text-[var(--color-text-primary)]">{{ formatDateTime(record.updateTime)[0] }}</span>
              <span class="text-11px text-[var(--color-text-placeholder)]">{{ formatDateTime(record.updateTime)[1] }}</span>
            </div>
          </template>

          <!-- Action -->
          <template v-if="column.key === 'action'">
            <AButton type="link" size="small" class="p-0! text-13px font-500 flex items-center gap-2px" @click="handleViewProject(record.id)">
              <span>详情</span>
              <ArrowRightOutlined class="text-11px" />
            </AButton>
          </template>
        </template>
      </ATable>
    </div>

    <!-- Project Modal (Create/Edit) -->
    <ProjectCreateModal
      v-model:visible="modalVisible"
      :editData="editingProject"
      @success="handleCreateSuccess"
    />
  </div>
</template>

<style scoped>
/* ===== 页面整体 ===== */
.project-list-page {
  --bg-card: rgba(255, 255, 255, 0.65);
  --bg-card-alt: rgba(247, 248, 250, 0.6);
  --color-divider: #E5E6EB;
  --color-text-primary: #1D2129;
  --color-text-secondary: #4E5969;
  --color-text-placeholder: #86909C;
  --color-primary: #1677FF;
  --color-success: #00B42A;
  --color-warning: #FF7D00;
  --color-danger: #F53F3F;
  --color-info: #0FC6C2;
  --color-secondary: #86909C;
}

/* ===== 页面头部 ===== */
.page-header {
  padding: 8px 12px;
  margin: -8px -12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.page-header:hover {
  background: rgba(22, 119, 255, 0.03);
}

.page-header-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(22, 119, 255, 0.1);
  color: var(--color-primary);
  transition: all 0.25s ease;
}

.page-header:hover .page-header-icon {
  background: rgba(22, 119, 255, 0.15);
  transform: scale(1.05);
}

/* ===== 指标卡片 ===== */
.metric-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.metric-card--gradient {
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

/* ===== 表格卡片 ===== */
.table-card {
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.table-card:hover {
  border-color: rgba(22, 119, 255, 0.25);
  box-shadow: 0 8px 32px rgba(22, 119, 255, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* ===== 表格样式 ===== */
.project-table {
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
}

.project-table :deep(.ant-table-thead > tr > th) {
  background-color: rgba(247, 248, 250, 0.8) !important;
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 600;
  padding: 12px 20px !important;
  border-bottom: 1px solid var(--color-divider) !important;
}

.project-table :deep(.ant-table-tbody > tr > td) {
  padding: 12px 20px !important;
  border-bottom: 1px solid var(--color-divider) !important;
  transition: all 0.2s ease;
}

.project-table :deep(.ant-table-tbody > tr) {
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.project-table :deep(.ant-table-tbody > tr:hover) {
  background: rgba(22, 119, 255, 0.03);
}

.project-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: transparent !important;
}

.project-table :deep(.ant-table-cell-fix-right) {
  background-color: var(--bg-card) !important;
}

.project-table :deep(.ant-table-pagination) {
  padding: 12px 20px !important;
  margin: 0 !important;
  border-top: 1px solid var(--color-divider);
  flex-shrink: 0;
}

/* ===== 状态标签 ===== */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  transition: all 0.25s ease;
}

.status-tag:hover {
  transform: scale(1.05);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-dot--pulse {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.status-tag--draft {
  background: #f3f4f6;
  color: #4b5563;
}
.status-tag--draft .status-dot {
  background: #9ca3af;
}

.status-tag--active {
  background: #e0f2fe;
  color: #0284c7;
}
.status-tag--active .status-dot {
  background: #0ea5e9;
}

.status-tag--paused {
  background: #ffedd5;
  color: #ea580c;
}
.status-tag--paused .status-dot {
  background: #f97316;
}

.status-tag--completed {
  background: #dcfce7;
  color: #16a34a;
}
.status-tag--completed .status-dot {
  background: #22c55e;
}

.status-tag--archived {
  background: #f1f5f9;
  color: #64748b;
}
.status-tag--archived .status-dot {
  background: #94a3b8;
}
</style>
