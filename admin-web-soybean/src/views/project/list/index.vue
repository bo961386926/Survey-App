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

defineOptions({ name: 'ProjectList' });

const router = useRouter();

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

const columns = [
  { title: '项目编号', dataIndex: 'projectCode', key: 'projectCode', width: 140 },
  { title: '勘察项目名称', dataIndex: 'projectName', key: 'projectName', ellipsis: true },
  { title: '负责人', dataIndex: 'manager', key: 'manager', width: 140 },
  { title: '勘察进度', dataIndex: 'progress', key: 'progress', width: 220 },
  { title: '当前状态', dataIndex: 'status', key: 'status', width: 150 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const, align: 'right' as const }
];

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

const handleReset = () => {
  searchForm.value = { keyword: '', status: undefined };
  handleSearch();
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

const handleCreateSuccess = () => {
  loadData();
};

const metrics = computed(() => [
  { title: '总勘察项目量', value: 1284, trend: '+12%', icon: 'i-material-symbols:inventory-rounded', color: 'text-primary' },
  { title: '进行中勘察', value: 42, trend: '运行中', icon: 'i-material-symbols:engineering-rounded', color: 'text-info' },
  { title: '逾期/风险项', value: 3, trend: '待处理', icon: 'i-material-symbols:error-rounded', color: 'text-error' },
  { title: '已完成勘察', value: 156, trend: '本月累计', icon: 'i-material-symbols:check-circle-rounded', color: 'text-success' }
]);

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="h-full flex-col gap-24px p-24px overflow-y-auto">
    <!-- Page Header -->
    <div class="flex justify-between items-end">
      <div>
        <h1 class="text-24px font-700 text-primary-text mb-8px">勘察项目管理</h1>
        <p class="text-14px text-secondary">实时跟踪全国范围内的地质勘察项目进度与质量合规情况</p>
      </div>
      <AButton type="primary" class="h-40px! rd-8px! font-bold! flex-center gap-8px shadow-lg! shadow-primary/20!" @click="handleCreateProject">
        <div class="i-material-symbols:add-rounded text-18px"></div>
        <span>创建新勘察项目</span>
      </AButton>
    </div>

    <!-- Metric Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-24px">
      <div v-for="item in metrics" :key="item.title" class="bg-card p-24px rd-12px border border-border flex flex-col gap-16px">
        <div class="flex justify-between items-start">
          <div :class="[item.icon, item.color, 'text-24px bg-page p-8px rd-8px']"></div>
          <span class="text-11px font-bold px-8px py-4px rd-4px bg-page border border-border">{{ item.trend }}</span>
        </div>
        <div>
          <p class="text-12px text-secondary uppercase font-600 mb-4px">{{ item.title }}</p>
          <p class="text-28px font-900 text-primary-text">{{ item.value.toLocaleString() }}</p>
        </div>
      </div>
    </div>

    <!-- Table Section -->
    <div class="bg-card rd-12px border border-border overflow-hidden flex-1 flex flex-col">
      <!-- Filter Bar -->
      <div class="px-24px py-16px border-b border-divider flex-y-center justify-between">
        <div class="flex-y-center gap-16px">
          <AInput v-model:value="searchForm.keyword" placeholder="搜索项目、负责人或编号..." class="w-320px! rd-8px!" @pressEnter="handleSearch">
            <template #prefix>
              <div class="i-material-symbols:search text-placeholder"></div>
            </template>
          </AInput>
          <ASelect v-model:value="searchForm.status" placeholder="项目状态" class="w-140px!" allow-clear @change="handleSearch">
            <ASelectOption value="0">草稿</ASelectOption>
            <ASelectOption value="1">进行中</ASelectOption>
            <ASelectOption value="2">已暂停</ASelectOption>
            <ASelectOption value="3">已完成</ASelectOption>
            <ASelectOption value="4">已归档</ASelectOption>
          </ASelect>
          <AButton class="rd-8px!" @click="handleReset">重置</AButton>
        </div>
        <div class="flex gap-12px">
          <AButton type="text" class="p-8px! rd-8px!">
            <div class="i-material-symbols:download-rounded text-20px text-secondary"></div>
          </AButton>
          <AButton type="text" class="p-8px! rd-8px!">
            <div class="i-material-symbols:print-rounded text-20px text-secondary"></div>
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
        class="flex-1"
      >
        <template #bodyCell="{ column, record }">
          <!-- Project Name -->
          <template v-if="column.key === 'projectName'">
            <div class="flex-y-center gap-12px">
              <div class="size-36px rd-8px bg-primary/10 flex-center shrink-0">
                <div class="i-material-symbols:landscape-rounded text-primary text-18px"></div>
              </div>
              <div class="overflow-hidden">
                <p class="text-14px font-bold text-primary-text truncate">{{ record.projectName }}</p>
                <p class="text-11px text-placeholder truncate">更新于 2小时前</p>
              </div>
            </div>
          </template>

          <!-- Manager -->
          <template v-if="column.key === 'manager'">
            <div class="flex-y-center gap-8px">
              <div class="size-24px rd-full bg-divider flex-center overflow-hidden">
                <div class="i-material-symbols:person text-secondary text-14px"></div>
              </div>
              <span class="text-13px text-secondary">{{ record.manager || '未指派' }}</span>
            </div>
          </template>

          <!-- Progress -->
          <template v-if="column.key === 'progress'">
            <div class="w-180px">
              <div class="flex justify-between mb-4px">
                <span class="text-11px font-bold text-primary">{{ Math.round((record.pointDone / (record.pointTotal || 1)) * 100) }}%</span>
              </div>
              <AProgress :percent="Math.round((record.pointDone / (record.pointTotal || 1)) * 100)" size="small" :show-info="false" />
            </div>
          </template>

          <!-- Status -->
          <template v-if="column.key === 'status'">
            <span
              :class="[
                'px-12px py-4px rd-full text-11px font-bold inline-flex items-center gap-6px',
                record.status == 1 ? 'bg-primary/10 text-primary' : 
                record.status == 2 ? 'bg-warning/10 text-warning' : 
                record.status == 3 ? 'bg-success/10 text-success' : 
                record.status == 4 ? 'bg-secondary/10 text-secondary' : 'bg-info/10 text-info'
              ]"
            >
              <span v-if="record.status == 1" class="size-6px rd-full bg-primary animate-pulse"></span>
              <span v-else class="size-6px rd-full bg-current"></span>
              {{ 
                record.status == 0 ? '草稿' : 
                record.status == 1 ? '进行中' : 
                record.status == 2 ? '已暂停' : 
                record.status == 3 ? '已完成' : 
                record.status == 4 ? '已归档' : '未知状态'
              }}
            </span>
          </template>

          <!-- Action -->
          <template v-if="column.key === 'action'">
            <div class="flex-y-center justify-end gap-8px">
              <AButton type="link" size="small" class="p-0! font-bold!" @click="handleViewProject(record.id)">查看看板</AButton>
              <ADivider type="vertical" />
              <ADropdown>
                <AButton type="link" size="small" class="p-0! font-bold! text-secondary!">
                  更多 <div class="i-material-symbols:keyboard-arrow-down-rounded"></div>
                </AButton>
                <template #overlay>
                  <AMenu>
                    <AMenuItem @click="handleEditProject(record)">
                      <div class="flex-y-center gap-8px">
                        <div class="i-material-symbols:edit-rounded text-14px"></div>
                        <span>编辑项目</span>
                      </div>
                    </AMenuItem>
                    <AMenuItem @click="handleUpdateStatus(record, 1)" v-if="record.status == 0">
                      <div class="flex-y-center gap-8px">
                        <div class="i-material-symbols:play-arrow-rounded text-14px"></div>
                        <span>开始项目</span>
                      </div>
                    </AMenuItem>
                    <AMenuItem @click="handleUpdateStatus(record, 2)" v-if="record.status == 1">
                      <div class="flex-y-center gap-8px">
                        <div class="i-material-symbols:pause-rounded text-14px"></div>
                        <span>暂停项目</span>
                      </div>
                    </AMenuItem>
                    <AMenuItem @click="handleUpdateStatus(record, 1)" v-if="record.status == 2">
                      <div class="flex-y-center gap-8px">
                        <div class="i-material-symbols:play-arrow-rounded text-14px"></div>
                        <span>恢复项目</span>
                      </div>
                    </AMenuItem>
                    <AMenuItem @click="handleUpdateStatus(record, 3)" v-if="record.status == 1">
                      <div class="flex-y-center gap-8px">
                        <div class="i-material-symbols:check-circle-rounded text-14px"></div>
                        <span>完成项目</span>
                      </div>
                    </AMenuItem>
                    <AMenuItem @click="handleUpdateStatus(record, 4)" v-if="record.status == 3">
                      <div class="flex-y-center gap-8px">
                        <div class="i-material-symbols:archive-rounded text-14px"></div>
                        <span>归档项目</span>
                      </div>
                    </AMenuItem>
                    <AMenuDivider />
                    <AMenuItem danger @click="handleDeleteProject(record)">
                      <div class="flex-y-center gap-8px">
                        <div class="i-material-symbols:delete-rounded text-14px"></div>
                        <span>删除项目</span>
                      </div>
                    </AMenuItem>
                  </AMenu>
                </template>
              </ADropdown>
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
:deep(.ant-table-thead > tr > th) {
  background-color: var(--bg-card-alt) !important;
  color: var(--color-text-secondary);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 16px 24px !important;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 16px 24px !important;
  border-bottom: 1px solid var(--color-divider) !important;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background-color: var(--bg-hover) !important;
}
</style>
