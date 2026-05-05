<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  fetchGetExportList,
  fetchCreateExport,
  fetchDownloadExport,
  fetchDeleteExport
} from '@/service/api';

defineOptions({ name: 'ExportList' });

const loading = ref(false);
const searchForm = ref({
  taskName: '',
  status: undefined
});

const exportTasks = ref<Api.Export.ExportTask[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
});

const columns = [
  { title: '任务名称', dataIndex: 'taskName', key: 'taskName', width: 280, ellipsis: true },
  { title: '导出类型', dataIndex: 'taskType', key: 'taskType', width: 120 },
  { title: '任务状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '进度', dataIndex: 'progress', key: 'progress', width: 180 },
  { title: '文件大小', key: 'fileSize', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const }
];

const loadData = async () => {
  loading.value = true;
  try {
    const { data, error } = await fetchGetExportList({
      current: pagination.value.current,
      size: pagination.value.pageSize,
      status: searchForm.value.status
    });
    
    if (!error && data) {
      exportTasks.value = data.records || [];
      pagination.value.total = data.total || 0;
    }
  } catch (err) {
    console.error('Failed to load export tasks', err);
    message.error('加载导出任务失败');
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
  searchForm.value = { taskName: '', status: undefined };
  handleSearch();
};

const handleCreateExport = () => {
  message.info('打开创建导出任务对话框');
  // TODO: Implement create export modal
};

const handleDownload = async (record: Api.Export.ExportTask) => {
  try {
    const response = await fetchDownloadExport(record.id);
    // Create download link
    if (response.data) {
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', record.taskName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      message.success('下载成功');
    }
  } catch (err) {
    console.error('Failed to download', err);
    message.error('下载失败');
  }
};

const handleDelete = async (record: Api.Export.ExportTask) => {
  try {
    await fetchDeleteExport(record.id);
    message.success('删除成功');
    loadData();
  } catch (err) {
    console.error('Failed to delete', err);
    message.error('删除失败');
  }
};

const handleRetry = (record: Api.Export.ExportTask) => {
  message.info('重试导出任务');
  // TODO: Implement retry logic
};

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '等待中',
    processing: '处理中',
    completed: '已完成',
    failed: '失败'
  };
  return statusMap[status] || status;
};

const getTaskTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    point_list: '点位清单',
    audit_result: '审核结果',
    pdf_report: 'PDF报告',
    batch_pdf: '批量PDF'
  };
  return typeMap[type] || type;
};

const getTaskTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    point_list: 'blue',
    audit_result: 'green',
    pdf_report: 'purple',
    batch_pdf: 'orange'
  };
  return colorMap[type] || 'default';
};

const getFileSize = (record: Api.Export.ExportTask) => {
  if (record.fileSize) {
    const bytes = record.fileSize;
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  }
  return '--';
};

const getProgress = (record: Api.Export.ExportTask) => {
  if (record.status === 'completed') return 100;
  if (record.status === 'failed') return 0;
  if (record.status === 'processing') return 45; // Mock progress
  return 0;
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="p-6 h-full flex flex-col gap-4">
    <!-- Filter Bar -->
    <div class="search-card">
      <a-form layout="inline" :model="searchForm" class="flex-1">
        <a-form-item label="任务名称">
          <a-input v-model:value="searchForm.taskName" placeholder="请输入报表/任务名称" class="w-64" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="全部" class="w-32" allow-clear>
            <a-select-option value="PROCESSING">处理中</a-select-option>
            <a-select-option value="COMPLETED">已完成</a-select-option>
            <a-select-option value="FAILED">失败</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <div class="flex gap-2">
            <a-button type="primary">查询</a-button>
            <a-button>重置</a-button>
          </div>
        </a-form-item>
      </a-form>
      <a-button type="primary" @click="handleCreateExport">新建导出任务</a-button>
    </div>

    <!-- Data Table -->
    <div class="table-card flex-1">
      <div class="flex justify-between items-center mb-4">
        <span class="text-16px font-500 title-text">导出任务列表</span>
        <a-button type="link">
          <template #icon><icon-lucide-refresh-cw /></template>
          刷新状态
        </a-button>
      </div>

      <a-table
        :dataSource="exportTasks"
        :columns="columns"
        :loading="loading"
        rowKey="id"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'taskType'">
            <a-tag :color="getTaskTypeColor(record.taskType)">
              {{ getTaskTypeText(record.taskType) }}
            </a-tag>
          </template>

          <template v-if="column.key === 'status'">
            <div class="flex items-center gap-2">
              <span class="status-dot" :class="`status-${record.status}`"></span>
              <span>{{ getStatusText(record.status) }}</span>
            </div>
          </template>

          <template v-if="column.key === 'progress'">
            <a-progress 
              :percent="getProgress(record)" 
              :status="record.status === 'failed' ? 'exception' : record.status === 'completed' ? 'success' : 'active'"
              size="small"
              :strokeColor="record.status === 'processing' ? 'var(--color-primary)' : undefined"
            />
          </template>

          <template v-if="column.key === 'fileSize'">
            {{ getFileSize(record) }}
          </template>

          <template v-if="column.key === 'action'">
            <div class="flex gap-2">
              <a-button 
                type="link" 
                size="small" 
                :disabled="record.status !== 'completed'"
                @click="handleDownload(record)"
              >
                下载文件
              </a-button>
              <a-button 
                type="link" 
                size="small" 
                danger 
                v-if="record.status === 'failed'"
                @click="handleRetry(record)"
              >
                重试
              </a-button>
              <a-popconfirm
                title="确定删除此导出任务吗？"
                @confirm="handleDelete(record)"
              >
                <a-button type="link" size="small" danger>
                  删除
                </a-button>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<style scoped>
.search-card {
  background-color: var(--bg-card);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-card);
}

.table-card {
  background-color: var(--bg-card);
  border-radius: 8px;
  padding: 20px;
  box-shadow: var(--shadow-card);
}

.title-text {
  color: var(--color-text-primary);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
}

.status-processing { background-color: var(--color-primary); }
.status-completed { background-color: var(--color-success); }
.status-failed { background-color: var(--color-danger); }

:deep(.ant-table-thead > tr > th) {
  background-color: var(--bg-card) !important;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-divider) !important;
}

:deep(.ant-table-tbody > tr > td) {
  background-color: var(--bg-card) !important;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-divider) !important;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background-color: var(--bg-hover) !important;
}
</style>
