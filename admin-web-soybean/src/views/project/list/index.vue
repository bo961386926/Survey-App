<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Modal, message } from 'ant-design-vue';
import { 
  fetchGetProjectList, 
  fetchDeleteProject, 
  fetchUpdateProjectStatus 
} from '@/service/api';
import ProjectCreateModal from '../modules/create-modal.vue';
import { useAuth } from '@/hooks/common/auth';

defineOptions({ name: 'ProjectList' });

const router = useRouter();
const { isAdmin, canManageProject } = useAuth();

// Search Filter
const searchForm = ref({
  keyword: '',
  status: undefined
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
  { title: '项目名称', dataIndex: 'projectName', key: 'projectName', width: 260, ellipsis: true },
  { title: '负责人', dataIndex: 'manager', key: 'manager', width: 100 },
  { title: '进度', dataIndex: 'progress', key: 'progress', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 96 },
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
      projectList.value = data.records || [];
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
  router.push(`/project/detail/${id}`);
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

const metrics = computed(() => [
  { title: '总项目', value: 1284 },
  { title: '进行中', value: 42 },
  { title: '逾期风险', value: 3 },
  { title: '本月完成', value: 156 }
]);

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="h-full flex-col gap-24px p-24px overflow-y-auto">
    <!-- Page Header -->
    <div class="flex justify-between items-center">
      <h1 class="text-20px font-700 text-primary-text">勘察项目管理</h1>
      <AButton v-if="isAdmin" type="primary" class="h-36px! px-16px! rd-8px! font-medium! flex items-center gap-6px" @click="handleCreateProject">
        <div class="i-material-symbols:add-rounded text-16px flex-shrink-0"></div>
        <span>新建项目</span>
      </AButton>
    </div>

    <!-- Stats Bar -->
    <div class="bg-card px-24px py-12px rd-8px border border-border flex items-center gap-0 flex-shrink-0">
      <div v-for="(item, idx) in metrics" :key="item.title" class="flex items-center gap-10px">
        <span class="text-22px font-900 text-primary-text leading-none">{{ item.value.toLocaleString() }}</span>
        <span class="text-12px text-secondary whitespace-nowrap">{{ item.title }}</span>
        <div v-if="idx < metrics.length - 1" class="w-1px h-16px bg-divider mx-22px"></div>
      </div>
    </div>

    <!-- Table Section -->
    <div class="bg-card rd-12px border border-border overflow-visible flex-1 flex flex-col">
      <!-- Filter Bar -->
      <div class="px-24px py-10px border-b border-divider flex-y-center gap-12px">
        <AInput v-model:value="searchForm.keyword" placeholder="搜索项目名称或编号..." class="w-240px!" @pressEnter="handleSearch" allow-clear>
          <template #prefix>
            <div class="i-material-symbols:search text-placeholder text-14px"></div>
          </template>
        </AInput>
        <ASelect v-model:value="searchForm.status" placeholder="状态" class="w-110px!" allow-clear @change="handleSearch">
          <ASelectOption value="0">草稿</ASelectOption>
          <ASelectOption value="1">进行中</ASelectOption>
          <ASelectOption value="2">已暂停</ASelectOption>
          <ASelectOption value="3">已完成</ASelectOption>
          <ASelectOption value="4">已归档</ASelectOption>
        </ASelect>
      </div>

      <!-- Table -->
      <ATable
        :dataSource="projectList"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="id"
        class="flex-1"
      >
        <template #bodyCell="{ column, record }">
          <!-- Project Name -->
          <template v-if="column.key === 'projectName'">
            <div class="flex-y-center gap-10px cursor-pointer" @click="handleViewProject(record.id)">
              <div class="size-32px rd-6px bg-[#EFF6FF] flex items-center justify-center shrink-0 overflow-hidden">
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
              <div class="overflow-hidden">
                <p class="text-13px font-medium text-primary-text truncate group-hover:text-primary">{{ record.projectName }}</p>
                <p class="text-12px text-placeholder truncate leading-tight">{{ record.projectCode }}</p>
              </div>
            </div>
          </template>

          <!-- Manager -->
          <template v-if="column.key === 'manager'">
            <span class="text-13px text-secondary">{{ record.manager || '未指派' }}</span>
          </template>

          <!-- Progress -->
          <template v-if="column.key === 'progress'">
            <div class="flex-y-center gap-8px w-130px">
              <AProgress :percent="getProgressPercent(record)" size="small" :show-info="false" :stroke-width="6" class="flex-1" />
              <span class="text-11px text-secondary w-32px text-right">{{ getProgressPercent(record) }}%</span>
            </div>
          </template>

          <!-- Status -->
          <template v-if="column.key === 'status'">
            <span
              :class="[
                'px-10px py-3px rd-full text-11px font-bold inline-flex items-center gap-5px whitespace-nowrap',
                record.status == 1 ? 'bg-primary/10 text-primary' :
                record.status == 2 ? 'bg-warning/10 text-warning' :
                record.status == 3 ? 'bg-success/10 text-success' :
                record.status == 4 ? 'bg-secondary/10 text-secondary' : 'bg-info/10 text-info'
              ]"
            >
              <span v-if="record.status == 1" class="size-6px rd-full bg-primary animate-pulse flex-shrink-0"></span>
              <span v-else class="size-6px rd-full bg-current flex-shrink-0"></span>
              {{
                record.status == 0 ? '草稿' :
                record.status == 1 ? '进行中' :
                record.status == 2 ? '已暂停' :
                record.status == 3 ? '已完成' :
                record.status == 4 ? '已归档' : '未知'
              }}
            </span>
          </template>

          <!-- Action -->
          <template v-if="column.key === 'action'">
            <!-- 非管理员：仅查看 -->
            <div v-if="!isAdmin" class="flex-y-center justify-end gap-4px">
              <ATooltip title="查看详情">
                <AButton type="text" size="small" class="!p-0 !h-auto !w-22px text-secondary!" @click="handleViewProject(record.id)">
                  <div class="i-material-symbols:arrow-forward-rounded text-15px"></div>
                </AButton>
              </ATooltip>
            </div>
            <!-- 管理员：编辑 + 状态操作 + 删除 -->
            <div v-else class="flex-y-center justify-end gap-4px">
              <ATooltip title="查看详情">
                <AButton type="text" size="small" class="!p-0 !h-auto !w-22px text-secondary!" @click="handleViewProject(record.id)">
                  <div class="i-material-symbols:arrow-forward-rounded text-15px"></div>
                </AButton>
              </ATooltip>
              <ATooltip title="编辑项目">
                <AButton type="text" size="small" class="!p-0 !h-auto !w-22px text-secondary!" @click="handleEditProject(record)">
                  <div class="i-material-symbols:edit-outline-rounded text-15px"></div>
                </AButton>
              </ATooltip>
              <!-- 状态操作：草稿→开始，进行中→暂停/完成，已暂停→恢复，已完成→归档 -->
              <ATooltip v-if="record.status == 0" title="开始项目">
                <AButton type="text" size="small" class="!p-0 !h-auto !w-22px text-success!" @click="handleUpdateStatus(record, 1)">
                  <div class="i-material-symbols:play-arrow-rounded text-15px"></div>
                </AButton>
              </ATooltip>
              <template v-else-if="record.status == 1">
                <ATooltip title="暂停项目">
                  <AButton type="text" size="small" class="!p-0 !h-auto !w-22px text-warning!" @click="handleUpdateStatus(record, 2)">
                    <div class="i-material-symbols:pause-rounded text-15px"></div>
                  </AButton>
                </ATooltip>
                <ATooltip title="完成项目">
                  <AButton type="text" size="small" class="!p-0 !h-auto !w-22px text-success!" @click="handleUpdateStatus(record, 3)">
                    <div class="i-material-symbols:check-circle-rounded text-15px"></div>
                  </AButton>
                </ATooltip>
              </template>
              <ATooltip v-else-if="record.status == 2" title="恢复项目">
                <AButton type="text" size="small" class="!p-0 !h-auto !w-22px text-success!" @click="handleUpdateStatus(record, 1)">
                  <div class="i-material-symbols:play-arrow-rounded text-15px"></div>
                </AButton>
              </ATooltip>
              <ATooltip v-else-if="record.status == 3" title="归档项目">
                <AButton type="text" size="small" class="!p-0 !h-auto !w-22px text-secondary!" @click="handleUpdateStatus(record, 4)">
                  <div class="i-material-symbols:archive-rounded text-15px"></div>
                </AButton>
              </ATooltip>
              <ATooltip title="删除项目">
                <AButton type="text" size="small" class="!p-0 !h-auto !w-22px text-error!" @click="handleDeleteProject(record)">
                  <div class="i-material-symbols:delete-rounded text-15px"></div>
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
:deep(.ant-table-wrapper) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

:deep(.ant-table) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

:deep(.ant-table-container) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

:deep(.ant-table-thead > tr > th) {
  background-color: var(--bg-card-alt) !important;
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 600;
  padding: 12px 20px !important;
  border-bottom: 1px solid var(--color-divider) !important;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 12px 20px !important;
  border-bottom: 1px solid var(--color-divider) !important;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background-color: var(--bg-hover) !important;
}

:deep(.ant-table-tbody > tr) {
  cursor: pointer;
}

:deep(.ant-table-cell-fix-right) {
  background-color: var(--bg-card) !important;
}

:deep(.ant-table-pagination) {
  padding: 12px 20px !important;
  margin: 0 !important;
  border-top: 1px solid var(--color-divider);
  flex-shrink: 0;
}
</style>
