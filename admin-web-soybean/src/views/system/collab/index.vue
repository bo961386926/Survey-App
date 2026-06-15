<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import {
  fetchGetCollabList,
  fetchCreateCollab,
  fetchUpdateCollab,
  fetchRevokeCollab,
  fetchResetCollabToken,
  fetchIssueCollabToken
} from '@/service/api/collab';

defineOptions({ name: 'SystemCollab' });

// ============ State ============
const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);
const searchForm = reactive({ keyword: '' });
const pagination = reactive({ current: 1, pageSize: 10 });

// Modal state
const showModal = ref(false);
const isEdit = ref(false);
const editingId = ref<number | null>(null);
const modalForm = reactive({ entryName: '', projectIds: '', pointIds: '', permissions: 'read', expireTime: '' });
const submitLoading = ref(false);

// Token display
const showTokenModal = ref(false);
const tokenValue = ref('');

// ============ Columns ============
const columns = [
  { title: '入口名称', dataIndex: 'entryName', key: 'entryName', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '访问次数', dataIndex: 'accessCount', key: 'accessCount', width: 100 },
  { title: '最后访问', dataIndex: 'lastAccessTime', key: 'lastAccessTime', width: 180 },
  { title: '过期时间', dataIndex: 'expireTime', key: 'expireTime', width: 180 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 300, fixed: 'right' as const }
];

const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '未启用', color: 'default' },
  1: { label: '启用中', color: 'success' },
  2: { label: '已过期', color: 'warning' },
  3: { label: '已撤销', color: 'error' }
};

// ============ Methods ============
const loadData = async () => {
  loading.value = true;
  try {
    const { data } = await fetchGetCollabList({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: searchForm.keyword || undefined
    });
    if (data) {
      tableData.value = data.records || [];
      total.value = data.total || 0;
    }
  } catch (e) {
    console.error('Failed to load collab entries', e);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  loadData();
};

const handleReset = () => {
  searchForm.keyword = '';
  handleSearch();
};

const handlePageChange = (page: number, pageSize: number) => {
  pagination.current = page;
  pagination.pageSize = pageSize;
  loadData();
};

const openCreateModal = () => {
  isEdit.value = false;
  editingId.value = null;
  modalForm.entryName = '';
  modalForm.projectIds = '';
  modalForm.pointIds = '';
  modalForm.permissions = 'read';
  modalForm.expireTime = '';
  showModal.value = true;
};

const openEditModal = (record: any) => {
  isEdit.value = true;
  editingId.value = record.id;
  modalForm.entryName = record.entryName;
  modalForm.projectIds = record.projectIds || '';
  modalForm.pointIds = record.pointIds || '';
  modalForm.permissions = record.permissions || 'read';
  modalForm.expireTime = record.expireTime || '';
  showModal.value = true;
};

const handleSubmit = async () => {
  if (!modalForm.entryName) {
    message.warning('请填写入口名称');
    return;
  }
  submitLoading.value = true;
  try {
    if (isEdit.value && editingId.value) {
      await fetchUpdateCollab(editingId.value, modalForm);
      message.success('更新成功');
    } else {
      await fetchCreateCollab(modalForm);
      message.success('创建成功');
    }
    showModal.value = false;
    loadData();
  } catch (e) {
    message.error('操作失败');
  } finally {
    submitLoading.value = false;
  }
};

const handleRevoke = (record: any) => {
  Modal.confirm({
    title: '确认撤销',
    content: `确定要撤销协作入口「${record.entryName}」吗？已签发的Token仍可用至过期。`,
    okType: 'danger',
    onOk: async () => {
      await fetchRevokeCollab(record.id);
      message.success('撤销成功');
      loadData();
    }
  });
};

const handleResetToken = (record: any) => {
  Modal.confirm({
    title: '确认重置Token',
    content: `确定要重置「${record.entryName}」的访问Token吗？旧Token将立即失效。`,
    okType: 'danger',
    onOk: async () => {
      const { data } = await fetchResetCollabToken(record.id);
      if (data) {
        tokenValue.value = data;
        showTokenModal.value = true;
      }
      message.success('Token已重置');
      loadData();
    }
  });
};

const handleIssueToken = async (record: any) => {
  try {
    const { data } = await fetchIssueCollabToken(record.id);
    if (data) {
      tokenValue.value = data;
      showTokenModal.value = true;
    }
    message.success('Token签发成功');
  } catch (e) {
    message.error('签发失败');
  }
};

const copyToken = () => {
  navigator.clipboard.writeText(tokenValue.value).then(() => {
    message.success('已复制到剪贴板');
  });
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="h-full flex-col-stretch gap-16px overflow-y-auto custom-scrollbar">
    <!-- Search Bar -->
    <div class="rounded-8px bg-[var(--bg-card)] p-16px border border-[var(--color-divider)]">
      <div class="flex items-center gap-12px">
        <a-input v-model:value="searchForm.keyword" placeholder="搜索入口名称" allow-clear class="!w-200px" @pressEnter="handleSearch" />
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
        <div class="flex-1" />
        <a-button type="primary" @click="openCreateModal">
          <template #icon><PlusOutlined /></template>
          新建协作入口
        </a-button>
      </div>
    </div>

    <!-- Table -->
    <div class="flex-1 rounded-8px bg-[var(--bg-card)] p-16px border border-[var(--color-divider)]">
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'entryName'">
            <span class="font-medium text-[var(--color-text-primary)]">{{ record.entryName }}</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusMap[record.status]?.color || 'default'">
              {{ statusMap[record.status]?.label || '未知' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="flex gap-4px flex-wrap">
              <a-button v-if="record.status !== 3" type="link" size="small" @click="openEditModal(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleIssueToken(record)">签发Token</a-button>
              <a-button type="link" size="small" @click="handleResetToken(record)">重置Token</a-button>
              <a-button v-if="record.status !== 3" type="link" danger size="small" @click="handleRevoke(record)">撤销</a-button>
            </div>
          </template>
        </template>
      </a-table>

      <div class="flex justify-end mt-16px">
        <a-pagination :current="pagination.current" :page-size="pagination.pageSize" :total="total" show-size-changer show-quick-jumper @change="handlePageChange" />
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <a-modal v-model:open="showModal" :title="isEdit ? '编辑协作入口' : '新建协作入口'" width="560px" @ok="handleSubmit" :confirmLoading="submitLoading">
      <a-form layout="vertical" class="mt-16px">
        <a-form-item label="入口名称" required>
          <a-input v-model:value="modalForm.entryName" placeholder="请输入入口名称" />
        </a-form-item>
        <a-form-item label="授权项目ID列表">
          <a-input v-model:value="modalForm.projectIds" placeholder="JSON数组，如 [1,2,3]" />
        </a-form-item>
        <a-form-item label="授权点位ID列表">
          <a-input v-model:value="modalForm.pointIds" placeholder="JSON数组，如 [10,20,30]" />
        </a-form-item>
        <a-form-item label="权限范围">
          <a-select v-model:value="modalForm.permissions">
            <a-select-option value="read">只读</a-select-option>
            <a-select-option value="read_write">读写</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="过期时间">
          <a-date-picker v-model:value="modalForm.expireTime" show-time format="YYYY-MM-DD HH:mm:ss" class="!w-full" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Token Display Modal -->
    <a-modal v-model:open="showTokenModal" title="协作访问Token" :footer="null">
      <div class="p-12px bg-[var(--bg-page)] rounded-8px font-mono text-13px break-all">{{ tokenValue }}</div>
      <a-button type="primary" block class="mt-12px" @click="copyToken">复制Token</a-button>
    </a-modal>
  </div>
</template>
