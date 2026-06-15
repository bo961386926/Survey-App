<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import {
  fetchGetAnnouncementList,
  fetchCreateAnnouncement,
  fetchUpdateAnnouncement,
  fetchPublishAnnouncement,
  fetchRecallAnnouncement,
  fetchDeleteAnnouncement
} from '@/service/api/announcement';

defineOptions({ name: 'SystemAnnouncement' });

// ============ State ============
const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);
const searchForm = reactive({ keyword: '', type: '', status: undefined as number | undefined });
const pagination = reactive({ current: 1, pageSize: 10 });

// Modal state
const showModal = ref(false);
const isEdit = ref(false);
const editingId = ref<number | null>(null);
const modalForm = reactive({ title: '', type: 'system_notification', content: '', targetScope: 'all' });
const submitLoading = ref(false);

// ============ Columns ============
const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 240 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '发布人', dataIndex: 'publisherId', key: 'publisherId', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' as const }
];

const typeMap: Record<string, string> = {
  work_spec: '工作规范',
  maintenance_reminder: '维护提醒',
  system_notification: '系统通知'
};

const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '草稿', color: 'default' },
  1: { label: '定时发布', color: 'processing' },
  2: { label: '已发布', color: 'success' },
  3: { label: '已撤回', color: 'warning' }
};

// ============ Methods ============
const loadData = async () => {
  loading.value = true;
  try {
    const { data } = await fetchGetAnnouncementList({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status
    });
    if (data) {
      tableData.value = data.records || [];
      total.value = data.total || 0;
    }
  } catch (e) {
    console.error('Failed to load announcements', e);
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
  searchForm.type = '';
  searchForm.status = undefined;
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
  modalForm.title = '';
  modalForm.type = 'system_notification';
  modalForm.content = '';
  modalForm.targetScope = 'all';
  showModal.value = true;
};

const openEditModal = (record: any) => {
  isEdit.value = true;
  editingId.value = record.id;
  modalForm.title = record.title;
  modalForm.type = record.type;
  modalForm.content = record.content;
  modalForm.targetScope = record.targetScope || 'all';
  showModal.value = true;
};

const handleSubmit = async () => {
  if (!modalForm.title || !modalForm.content) {
    message.warning('请填写标题和内容');
    return;
  }
  submitLoading.value = true;
  try {
    if (isEdit.value && editingId.value) {
      await fetchUpdateAnnouncement(editingId.value, modalForm);
      message.success('更新成功');
    } else {
      await fetchCreateAnnouncement(modalForm);
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

const handlePublish = (record: any) => {
  Modal.confirm({
    title: '确认发布',
    content: `确定要发布公告「${record.title}」吗？`,
    onOk: async () => {
      await fetchPublishAnnouncement(record.id);
      message.success('发布成功');
      loadData();
    }
  });
};

const handleRecall = (record: any) => {
  Modal.confirm({
    title: '确认撤回',
    content: `确定要撤回公告「${record.title}」吗？`,
    onOk: async () => {
      await fetchRecallAnnouncement(record.id);
      message.success('撤回成功');
      loadData();
    }
  });
};

const handleDelete = (record: any) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除公告「${record.title}」吗？`,
    okType: 'danger',
    onOk: async () => {
      await fetchDeleteAnnouncement(record.id);
      message.success('删除成功');
      loadData();
    }
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
      <div class="flex items-center gap-12px flex-wrap">
        <a-input v-model:value="searchForm.keyword" placeholder="搜索公告标题" allow-clear class="!w-200px" @pressEnter="handleSearch" />
        <a-select v-model:value="searchForm.type" placeholder="公告类型" allow-clear class="!w-160px">
          <a-select-option value="system_notification">系统通知</a-select-option>
          <a-select-option value="work_spec">工作规范</a-select-option>
          <a-select-option value="maintenance_reminder">维护提醒</a-select-option>
        </a-select>
        <a-select v-model:value="searchForm.status" placeholder="状态" allow-clear class="!w-120px">
          <a-select-option :value="0">草稿</a-select-option>
          <a-select-option :value="2">已发布</a-select-option>
          <a-select-option :value="3">已撤回</a-select-option>
        </a-select>
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
        <div class="flex-1" />
        <a-button type="primary" @click="openCreateModal">
          <template #icon><PlusOutlined /></template>
          新建公告
        </a-button>
      </div>
    </div>

    <!-- Table -->
    <div class="flex-1 rounded-8px bg-[var(--bg-card)] p-16px border border-[var(--color-divider)]">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <span class="font-medium text-[var(--color-text-primary)]">{{ record.title }}</span>
          </template>
          <template v-if="column.key === 'type'">
            <a-tag>{{ typeMap[record.type] || record.type }}</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusMap[record.status]?.color || 'default'">
              {{ statusMap[record.status]?.label || '未知' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="flex gap-8px">
              <a-button v-if="record.status === 0" type="link" size="small" @click="openEditModal(record)">编辑</a-button>
              <a-button v-if="record.status === 0" type="link" size="small" @click="handlePublish(record)">发布</a-button>
              <a-button v-if="record.status === 2" type="link" size="small" danger @click="handleRecall(record)">撤回</a-button>
              <a-button type="link" danger size="small" @click="handleDelete(record)">删除</a-button>
            </div>
          </template>
        </template>
      </a-table>

      <!-- Pagination -->
      <div class="flex justify-end mt-16px">
        <a-pagination
          :current="pagination.current"
          :page-size="pagination.pageSize"
          :total="total"
          show-size-changer
          show-quick-jumper
          @change="handlePageChange"
        />
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <a-modal v-model:open="showModal" :title="isEdit ? '编辑公告' : '新建公告'" width="600px" @ok="handleSubmit" :confirmLoading="submitLoading">
      <a-form layout="vertical" class="mt-16px">
        <a-form-item label="公告标题" required>
          <a-input v-model:value="modalForm.title" placeholder="请输入公告标题" />
        </a-form-item>
        <a-form-item label="公告类型">
          <a-select v-model:value="modalForm.type">
            <a-select-option value="system_notification">系统通知</a-select-option>
            <a-select-option value="work_spec">工作规范</a-select-option>
            <a-select-option value="maintenance_reminder">维护提醒</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="公告内容" required>
          <a-textarea v-model:value="modalForm.content" placeholder="请输入公告内容" :rows="6" />
        </a-form-item>
        <a-form-item label="受众范围">
          <a-select v-model:value="modalForm.targetScope">
            <a-select-option value="all">全部用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="operator">操作人员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
