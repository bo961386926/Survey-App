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
  FolderOpenOutlined,
  WarningOutlined,
  CheckSquareOutlined,
  EnvironmentOutlined,
  ClockCircleOutlined,
  SendOutlined
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
const { hasPermission, canManageProject } = useAuth();

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
  { title: '项目名称', dataIndex: 'projectName', key: 'projectName', width: 240, ellipsis: true },
  { title: '项目编号', dataIndex: 'projectCode', key: 'projectCode', width: 160 },
  { title: '负责人', dataIndex: 'manager', key: 'manager', width: 100 },
  { title: '区域', dataIndex: 'region', key: 'region', width: 120 },
  { title: '进度', dataIndex: 'progress', key: 'progress', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '起止日期', key: 'dateRange', width: 180 },
  { title: '操作', key: 'action', width: 188, fixed: 'right' as const }
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

const getProgressPercent = (record: Api.Project.ProjectInfo) => {
  return Math.round((record.completedCount / (record.pointCount || 1)) * 100);
};

const handleCreateSuccess = () => {
  loadData();
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
    1: '进行中',
    2: '已暂停',
    3: '已完成',
    4: '已归档'
  };
  return textMap[Number(status)] || '未知';
};

const iconMap: Record<string, any> = {
  folder: FolderOutlined,
  trending: CheckSquareOutlined,
  warning: WarningOutlined,
  task: CheckCircleOutlined
};

const metrics = computed(() => [
  { title: '总项目', value: 1284, icon: iconMap.folder, color: 'var(--color-primary)' },
  { title: '进行中', value: 42, icon: iconMap.trending, color: 'var(--color-primary)' },
  { title: '逾期风险', value: 3, icon: iconMap.warning, color: 'var(--color-warning)' },
  { title: '本月完成', value: 156, icon: iconMap.task, color: 'var(--color-success)' }
]);

const statusOptions = [
  { label: '全部', value: undefined as string | undefined, dotClass: 'bg-[var(--color-text-disabled)]' },
  { label: '草稿', value: '0', dotClass: 'bg-[var(--color-info)]' },
  { label: '进行中', value: '1', dotClass: 'bg-[var(--color-primary)]' },
  { label: '已暂停', value: '2', dotClass: 'bg-[var(--color-warning)]' },
  { label: '已完成', value: '3', dotClass: 'bg-[var(--color-success)]' },
  { label: '已归档', value: '4', dotClass: 'bg-[var(--color-secondary)]' }
];

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="h-full flex-col gap-24px p-24px overflow-y-auto project-list-page">
    <!-- Page Header -->
    <div class="flex justify-between items-center page-header">
      <div class="flex items-center gap-12px">
        <div class="page-header-icon">
          <FolderOutlined class="text-20px" />
        </div>
        <div>
          <h1 class="text-20px font-700 text-[var(--color-text-primary)]">勘察项目管理</h1>
          <p class="text-12px text-[var(--color-text-secondary)] mt-2px">管理和追踪所有勘察项目进度</p>
        </div>
      </div>
      <AButton v-if="hasPermission('project:create')" type="primary" class="create-btn" @click="handleCreateProject">
        <PlusOutlined class="text-16px flex-shrink-0" />
        <span>新建项目</span>
      </AButton>
    </div>

    <!-- Stats Bar -->
    <div class="metrics-grid">
      <div
        v-for="item in metrics"
        :key="item.title"
        class="metric-card group"
      >
        <div class="flex items-center justify-between mb-8px">
          <span class="text-12px text-[var(--color-text-secondary)]">{{ item.title }}</span>
          <div class="metric-icon-wrapper" :style="{ color: item.color }">
            <component :is="item.icon" class="metric-icon" />
          </div>
        </div>
        <div class="metric-value">{{ item.value.toLocaleString() }}</div>
        <!-- 底部装饰线 -->
        <div class="metric-line" :style="{ background: `linear-gradient(90deg, transparent, ${item.color}40, transparent)` }"></div>
      </div>
    </div>

    <!-- Table Section -->
    <div class="table-card" v-mouse-glow="{ color: '22,119,255', size: 300, intensity: 0.04 }">
      <!-- Filter Bar -->
      <div class="filter-bar">
        <div class="w-180px">
          <AInput v-model:value="searchForm.keyword" placeholder="搜索项目名称或编号..." class="w-full h-32px" @pressEnter="handleSearch" allow-clear>
            <template #prefix>
              <SearchOutlined class="text-14px text-[var(--color-text-secondary)]" />
            </template>
          </AInput>
        </div>

        <!-- Status Filter Toggle Buttons -->
        <div class="flex gap-8px">
          <div 
            v-for="status in statusOptions" 
            :key="status.label"
            class="px-12px h-32px rd-4px flex items-center cursor-pointer transition-all border"
            :class="searchForm.status === status.value ? 'bg-[var(--color-primary)] border-[var(--color-primary)] text-white' : 'bg-[var(--bg-card-alt)] border-[var(--color-border)] text-[var(--color-text-secondary)] hover:border-[var(--color-primary)]'"
            @click="searchForm.status = status.value; handleSearch()"
          >
            <div v-if="status.value" class="w-6px h-6px rd-full mr-8px" :class="searchForm.status === status.value ? 'bg-white' : status.dotClass"></div>
            <span class="text-13px">{{ status.label }}</span>
          </div>
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
        :scroll="{ y: 'calc(100vh - 430px)' }"
      >
        <template #bodyCell="{ column, record }">
          <!-- Project Name -->
          <template v-if="column.key === 'projectName'">
            <div class="project-cell" @click="handleViewProject(record.id)">
              <div class="project-icon-wrapper">
                <EnvironmentOutlined class="text-16px text-[var(--color-primary)]" />
              </div>
              <span class="project-name">{{ record.projectName }}</span>
            </div>
          </template>

          <!-- Project Code -->
          <template v-if="column.key === 'projectCode'">
            <span class="font-mono text-12px text-[var(--color-text-secondary)]">{{ record.projectCode || '--' }}</span>
          </template>

          <!-- Region -->
          <template v-if="column.key === 'region'">
            <span class="text-13px text-[var(--color-text-secondary)]">{{ record.region || '--' }}</span>
          </template>

          <!-- Manager -->
          <template v-if="column.key === 'manager'">
            <span class="manager-text">{{ record.manager || '未指派' }}</span>
          </template>

          <!-- Progress -->
          <template v-if="column.key === 'progress'">
            <div class="progress-wrapper">
              <AProgress :percent="getProgressPercent(record)" size="small" :show-info="false" :stroke-width="6" class="progress-bar" />
              <span class="progress-text">{{ getProgressPercent(record) }}%</span>
            </div>
          </template>

          <!-- Status -->
          <template v-if="column.key === 'status'">
            <span
              class="status-tag"
              :class="getStatusClass(record.status)"
            >
              <span v-if="record.status == 1" class="status-dot status-dot--pulse"></span>
              <span v-else class="status-dot"></span>
              {{ getStatusText(record.status) }}
            </span>
          </template>

          <!-- Date Range -->
          <template v-if="column.key === 'dateRange'">
            <div class="flex flex-col gap-2px">
              <span class="text-12px text-[var(--color-text-secondary)]">{{ record.startDate || '--' }}</span>
              <span class="text-11px text-[var(--color-text-placeholder)]">至 {{ record.endDate || '--' }}</span>
            </div>
          </template>

          <!-- Action -->
          <template v-if="column.key === 'action'">
            <!-- 无编辑权限：仅查看 -->
            <div v-if="!hasPermission('project:edit')" class="action-group">
              <ATooltip title="查看详情">
                <AButton type="text" size="small" class="action-btn" @click="handleViewProject(record.id)">
                  <ArrowRightOutlined class="text-15px" />
                </AButton>
              </ATooltip>
            </div>
            <!-- 管理员：编辑 + 状态操作 + 删除 -->
            <div v-else class="action-group">
              <ATooltip title="查看详情">
                <AButton type="text" size="small" class="action-btn" @click="handleViewProject(record.id)">
                  <ArrowRightOutlined class="text-15px" />
                </AButton>
              </ATooltip>
              <ATooltip title="编辑项目">
                <AButton type="text" size="small" class="action-btn" @click="handleEditProject(record)">
                  <EditOutlined class="text-15px" />
                </AButton>
              </ATooltip>
              <!-- 状态操作：草稿→开始，进行中→暂停/完成，已暂停→恢复，已完成→归档 -->
              <ATooltip v-if="record.status == 0" title="开始项目">
                <AButton type="text" size="small" class="action-btn action-btn--success" @click="handleUpdateStatus(record, 1)">
                  <PlayCircleOutlined class="text-15px" />
                </AButton>
              </ATooltip>
              <template v-else-if="record.status == 1">
                <ATooltip title="暂停项目">
                  <AButton type="text" size="small" class="action-btn action-btn--warning" @click="handleUpdateStatus(record, 2)">
                    <PauseCircleOutlined class="text-15px" />
                  </AButton>
                </ATooltip>
                <ATooltip title="完成项目">
                  <AButton type="text" size="small" class="action-btn action-btn--success" @click="handleUpdateStatus(record, 3)">
                    <CheckCircleOutlined class="text-15px" />
                  </AButton>
                </ATooltip>
              </template>
              <ATooltip v-else-if="record.status == 2" title="恢复项目">
                <AButton type="text" size="small" class="action-btn action-btn--success" @click="handleUpdateStatus(record, 1)">
                  <PlayCircleOutlined class="text-15px" />
                </AButton>
              </ATooltip>
              <ATooltip v-else-if="record.status == 3" title="归档项目">
                <AButton type="text" size="small" class="action-btn action-btn--secondary" @click="handleUpdateStatus(record, 4)">
                  <StopOutlined class="text-15px" />
                </AButton>
              </ATooltip>
              <ATooltip title="删除项目">
                <AButton type="text" size="small" class="action-btn action-btn--danger" @click="handleDeleteProject(record)">
                  <DeleteOutlined class="text-15px" />
                </AButton>
              </ATooltip>
            </div>
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

.create-btn {
  height: 36px !important;
  padding: 0 16px !important;
  border-radius: 6px !important;
  font-weight: 500 !important;
  display: flex !important;
  align-items: center !important;
  gap: 6px !important;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.create-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.create-btn:active {
  transform: translateY(0);
}

/* ===== 指标卡片 ===== */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.metric-card {
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  padding: 16px 20px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.metric-card:hover {
  border-color: rgba(22, 119, 255, 0.25);
  box-shadow: 0 8px 32px rgba(22, 119, 255, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.metric-card:active {
  transform: translateY(0);
  transition-duration: 0.1s;
}

.metric-icon-wrapper {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(22, 119, 255, 0.1);
  transition: all 0.25s ease;
}

.metric-card:hover .metric-icon-wrapper {
  transform: scale(1.1);
}

.metric-icon {
  font-size: 16px;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: -0.5px;
  transition: transform 0.3s ease;
}

.metric-card:hover .metric-value {
  transform: translateX(4px);
}

.metric-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.metric-card:hover .metric-line {
  opacity: 1;
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

/* ===== 筛选栏 ===== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--color-border);
}

.search-input {
  width: 180px;
}

.search-input :deep(.ant-input) {
  border-radius: 4px;
  height: 32px;
}

/* ===== 表格样式 ===== */
.project-table {
  flex: 1;
  min-height: 0;
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
  padding: 14px 20px !important;
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

/* ===== 项目单元格 ===== */
.project-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.project-cell:hover .project-name {
  color: var(--color-primary);
}

.project-icon-wrapper {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: #EFF6FF;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  transition: all 0.25s ease;
}

.project-cell:hover .project-icon-wrapper {
  background: rgba(22, 119, 255, 0.1);
  transform: scale(1.05);
}

.project-svg {
  width: 16px;
  height: 16px;
}

.project-info {
  overflow: hidden;
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

.project-code {
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 负责人 ===== */
.manager-text {
  font-size: 13px;
  color: var(--color-text-secondary);
}

/* ===== 进度条 ===== */
.progress-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 130px;
}

.progress-bar {
  flex: 1;
}

.progress-text {
  font-size: 11px;
  color: var(--color-text-secondary);
  width: 32px;
  text-align: right;
}

/* ===== 状态标签 ===== */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 100px;
  font-size: 11px;
  font-weight: 600;
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
  50% { opacity: 0.5; }
}

.status-tag--draft {
  background: rgba(134, 144, 156, 0.1);
  color: var(--color-info);
}

.status-tag--active {
  background: rgba(22, 119, 255, 0.1);
  color: var(--color-primary);
}

.status-tag--paused {
  background: rgba(255, 125, 0, 0.1);
  color: var(--color-warning);
}

.status-tag--completed {
  background: rgba(0, 180, 42, 0.1);
  color: var(--color-success);
}

.status-tag--archived {
  background: rgba(134, 144, 156, 0.1);
  color: var(--color-secondary);
}

/* ===== 操作按钮组 ===== */
.action-group {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}

.action-btn {
  padding: 0 !important;
  height: auto !important;
  width: 28px !important;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-btn:hover {
  transform: scale(1.1);
  background: rgba(22, 119, 255, 0.08) !important;
}

.action-btn:active {
  transform: scale(0.95);
}

.action-btn--success:hover {
  background: rgba(0, 180, 42, 0.08) !important;
}

.action-btn--warning:hover {
  background: rgba(255, 125, 0, 0.08) !important;
}

.action-btn--danger:hover {
  background: rgba(245, 63, 63, 0.08) !important;
}

.action-btn--secondary:hover {
  background: rgba(134, 144, 156, 0.08) !important;
}
</style>
