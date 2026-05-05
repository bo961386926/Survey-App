<template>
  <div class="h-full flex flex-col bg-[var(--bg-page)]">
    <!-- Header -->
    <div class="bg-[var(--bg-card)] border-b border-[var(--color-border)] px-6 py-4">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h1 class="text-20px font-bold text-[var(--color-text-primary)] mb-1">勘查审核</h1>
          <p class="text-13px text-[var(--color-text-secondary)]">对勘查结果进行审核，通过或驳回</p>
        </div>
        <div class="flex gap-3">
          <a-button>
            <template #icon><DownloadOutlined /></template>
            导出Excel
          </a-button>
          <a-button>
            <template #icon><HistoryOutlined /></template>
            操作日志
          </a-button>
          <span class="text-13px text-[var(--color-text-secondary)] flex items-center gap-1">
            <ClockCircleOutlined />
            上次更新：13:49
          </span>
        </div>
      </div>

      <!-- Statistics Cards -->
      <div class="grid grid-cols-4 gap-4">
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-14px text-[var(--color-text-secondary)]">总记录数</span>
            <div class="w-32px h-32px rounded-full bg-[rgba(22,119,255,0.1)] flex items-center justify-center">
              <FileTextOutlined class="text-16px text-[var(--color-primary)]" />
            </div>
          </div>
          <div class="text-28px font-bold text-[var(--color-text-primary)]">{{ statistics.total }}</div>
        </div>
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-14px text-[var(--color-text-secondary)]">待审核</span>
            <div class="w-32px h-32px rounded-full bg-[rgba(250,173,20,0.1)] flex items-center justify-center">
              <ClockCircleOutlined class="text-16px text-[var(--color-warning)]" />
            </div>
          </div>
          <div class="text-28px font-bold text-[var(--color-text-primary)]">{{ statistics.pending }}</div>
        </div>
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-14px text-[var(--color-text-secondary)]">已通过</span>
            <div class="w-32px h-32px rounded-full bg-[rgba(82,196,26,0.1)] flex items-center justify-center">
              <CheckCircleOutlined class="text-16px text-[var(--color-success)]" />
            </div>
          </div>
          <div class="text-28px font-bold text-[var(--color-text-primary)]">{{ statistics.approved }}</div>
        </div>
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-14px text-[var(--color-text-secondary)]">已驳回</span>
            <div class="w-32px h-32px rounded-full bg-[rgba(255,77,79,0.1)] flex items-center justify-center">
              <CloseCircleOutlined class="text-16px text-[var(--color-danger)]" />
            </div>
          </div>
          <div class="text-28px font-bold text-[var(--color-text-primary)]">{{ statistics.rejected }}</div>
        </div>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="bg-[var(--bg-card)] border-b border-[var(--color-border)] px-6 py-4">
      <div class="flex items-center gap-3">
        <a-button :type="filters.status === '' ? 'primary' : 'default'" shape="round" @click="handleFilterChange('')">
          全部 <a-badge :count="statistics.total" :number-style="{ backgroundColor: 'var(--color-primary)' }" />
        </a-button>
        <a-button :type="filters.status === 'pending' ? 'primary' : 'default'" shape="round" @click="handleFilterChange('pending')">
          待审核 <a-badge :count="statistics.pending" :number-style="{ backgroundColor: 'var(--color-warning)' }" />
        </a-button>
        <a-button :type="filters.status === 'approved' ? 'primary' : 'default'" shape="round" @click="handleFilterChange('approved')">
          已通过 <a-badge :count="statistics.approved" :number-style="{ backgroundColor: 'var(--color-success)' }" />
        </a-button>
        <a-button :type="filters.status === 'rejected' ? 'primary' : 'default'" shape="round" @click="handleFilterChange('rejected')">
          已驳回 <a-badge :count="statistics.rejected" :number-style="{ backgroundColor: 'var(--color-danger)' }" />
        </a-button>
        <a-divider type="vertical" class="!h-24px !mx-2" />
        <a-select style="width: 160px" placeholder="全部项目">
          <a-select-option value="all">全部项目</a-select-option>
        </a-select>
        <a-range-picker style="width: 280px" />
        <a-input-search
          v-model:value="filters.keyword"
          placeholder="搜索点位、项目、勘查人..."
          style="width: 280px"
          allow-clear
          @search="handleSearch"
        />
      </div>
    </div>

    <!-- Data Table -->
    <div class="flex-1 overflow-auto p-6">
      <a-table
        :dataSource="auditList"
        :columns="columns"
        rowKey="resultId"
        :loading="loading"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: true,
          showTotal: (total: number) => `共 ${total} 条`,
          onChange: handlePageChange
        }"
        :scroll="{ y: 'calc(100vh - 400px)' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'point'">
            <div class="flex items-center gap-3">
              <div class="w-36px h-36px rounded-8px bg-[rgba(22,119,255,0.1)] flex items-center justify-center">
                <FileTextOutlined class="text-16px text-[var(--color-primary)]" />
              </div>
              <div>
                <div class="text-14px font-600 text-[var(--color-text-primary)] mb-1">{{ record.pointName }}</div>
                <div class="text-12px text-[var(--color-text-secondary)]">项目ID: {{ record.projectId }}</div>
              </div>
            </div>
          </template>

          <template v-if="column.key === 'status'">
            <div>
              <a-tag v-if="record.status === 'approved'" color="success" class="mb-1">已通过</a-tag>
              <a-tag v-else-if="record.status === 'rejected'" color="error" class="mb-1">已驳回</a-tag>
              <a-tag v-else color="processing">待审核</a-tag>
              <div v-if="record.status === 'rejected' && record.rejectReason" class="text-12px text-[var(--color-danger)] truncate max-w-200px">
                {{ record.rejectReason }}
              </div>
            </div>
          </template>

          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleViewDetail(record)">
              查看详情
            </a-button>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
  DownloadOutlined,
  HistoryOutlined,
  ClockCircleOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue';
import { fetchGetAuditList } from '@/service/api';

defineOptions({ name: 'AuditList' });

const router = useRouter();

// Loading and pagination
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});

// Filter state
const filters = ref<{
  status: Api.Audit.AuditStatus | '';
  projectId: number | undefined;
  keyword: string;
  dateRange: any[];
}>({
  status: '',
  projectId: undefined,
  keyword: '',
  dateRange: []
});

// Statistics
const statistics = ref<{
  total: number;
  pending: number;
  approved: number;
  rejected: number;
}>({
  total: 0,
  pending: 0,
  approved: 0,
  rejected: 0
});

// Audit data
const auditList = ref<Api.Audit.AuditRecord[]>([]);

// Load audit list
const loadData = async () => {
  loading.value = true;
  try {
    const response = await fetchGetAuditList({
      current: pagination.value.current,
      size: pagination.value.pageSize,
      status: filters.value.status || undefined,
      projectId: filters.value.projectId,
      keyword: filters.value.keyword || undefined
    });
    
    if (response.data) {
      auditList.value = response.data.records || [];
      pagination.value.total = response.data.total || 0;
      
      // Update statistics
      statistics.value.total = response.data.total || 0;
      statistics.value.pending = auditList.value.filter(item => item.status === 'pending').length;
      statistics.value.approved = auditList.value.filter(item => item.status === 'approved').length;
      statistics.value.rejected = auditList.value.filter(item => item.status === 'rejected').length;
    }
  } catch (error) {
    console.error('Failed to load audit list:', error);
    message.error('加载审核列表失败');
  } finally {
    loading.value = false;
  }
};

// Handle filter change
const handleFilterChange = (status: Api.Audit.AuditStatus | '') => {
  filters.value.status = status;
  pagination.value.current = 1;
  loadData();
};

// Handle page change
const handlePageChange = (page: number, pageSize: number) => {
  pagination.value.current = page;
  pagination.value.pageSize = pageSize;
  loadData();
};

// Handle search
const handleSearch = () => {
  pagination.value.current = 1;
  loadData();
};

// Handle reset
const handleReset = () => {
  filters.value = {
    status: '',
    projectId: undefined,
    keyword: '',
    dateRange: []
  };
  handleSearch();
};

const columns = [
  {
    title: '点位',
    key: 'point',
    width: 300
  },
  {
    title: '项目ID',
    dataIndex: 'projectId',
    key: 'projectId',
    width: 120
  },
  {
    title: '版本',
    dataIndex: 'versionNo',
    key: 'versionNo',
    width: 80
  },
  {
    title: '状态',
    key: 'status',
    width: 150
  },
  {
    title: '提交时间',
    dataIndex: 'submitTime',
    key: 'submitTime',
    width: 180,
    sorter: true
  },
  {
    title: '审核人',
    dataIndex: 'auditorName',
    key: 'auditorName',
    width: 120
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right' as const
  }
];

const handleViewDetail = (record: any) => {
  router.push(`/audit/detail/${record.resultId}`);
};

// Lifecycle
onMounted(() => {
  loadData();
});
</script>
