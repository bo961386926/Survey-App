<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import type { TableColumnsType } from 'ant-design-vue';
import { fetchGetDictList, fetchCreateDict, fetchUpdateDict, fetchDeleteDict } from '@/api/dict';

interface DictItem {
  id: number;
  dictCode: string;
  dictName: string;
  description?: string;
  isSystem?: number;
  status: number;
  sortOrder?: number;
  createTime: string;
  updateTime?: string;
}

// 搜索表单
const searchForm = reactive({
  dictCode: '',
  dictName: ''
});

// 表格数据
const loading = ref(false);
const dictList = ref<DictItem[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
});

// 模态框
const modalVisible = ref(false);
const modalTitle = ref('');
const editingDict = ref<DictItem | null>(null);
const formRef = ref();

const formData = reactive({
  dictCode: '',
  dictName: '',
  description: '',
  isSystem: 0,
  status: 1,
  sortOrder: 0
});

// 表格列定义
const columns: TableColumnsType = [
  { title: '字典编码', dataIndex: 'dictCode', key: 'dictCode', width: 150 },
  { title: '字典名称', dataIndex: 'dictName', key: 'dictName', width: 150 },
  { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true, width: 200 },
  { 
    title: '类型', 
    dataIndex: 'isSystem', 
    key: 'isSystem', 
    width: 100,
    customRender: ({ record }: any) => {
      return record.isSystem === 1 
        ? '<a-tag color="blue">系统内置</a-tag>' 
        : '<a-tag>自定义</a-tag>';
    }
  },
  { 
    title: '状态', 
    dataIndex: 'status', 
    key: 'status', 
    width: 100,
    customRender: ({ record }: any) => {
      return record.status === 1 
        ? '<a-tag color="success">启用</a-tag>' 
        : '<a-tag color="error">禁用</a-tag>';
    }
  },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 250, fixed: 'right' as const }
];

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const { data, error } = await fetchGetDictList({
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      dictCode: searchForm.dictCode || undefined,
      dictName: searchForm.dictName || undefined
    });
    
    if (!error && data) {
      dictList.value = data.records || [];
      pagination.value.total = data.total || 0;
    }
  } catch (err) {
    console.error('Failed to load dicts', err);
    message.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

// 表格分页变化
const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadData();
};

// 搜索
const handleSearch = () => {
  pagination.value.current = 1;
  loadData();
};

// 重置
const handleReset = () => {
  searchForm.dictCode = '';
  searchForm.dictName = '';
  handleSearch();
};

// 打开创建模态框
const handleCreate = () => {
  modalTitle.value = '创建字典';
  editingDict.value = null;
  Object.assign(formData, {
    dictCode: '',
    dictName: '',
    status: 1
  });
  modalVisible.value = true;
};

// 打开编辑模态框
const handleEdit = (record: DictItem) => {
  modalTitle.value = '编辑字典';
  editingDict.value = record;
  Object.assign(formData, {
    dictCode: record.dictCode,
    dictName: record.dictName,
    description: record.description || '',
    isSystem: record.isSystem || 0,
    status: record.status,
    sortOrder: record.sortOrder || 0
  });
  modalVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    
    if (editingDict.value) {
      // 更新
      const { error } = await fetchUpdateDict(editingDict.value.id, formData);
      if (!error) {
        message.success('更新成功');
        modalVisible.value = false;
        loadData();
      }
    } else {
      // 创建
      const { error } = await fetchCreateDict(formData);
      if (!error) {
        message.success('创建成功');
        modalVisible.value = false;
        loadData();
      }
    }
  } catch (err) {
    console.error('Submit failed:', err);
  }
};

// 取消
const handleCancel = () => {
  modalVisible.value = false;
};

// 删除
const handleDelete = (record: DictItem) => {
  Modal.confirm({
    title: '确认删除？',
    content: `删除字典「${record.dictName}」后，关联的所有字典项也将被删除，此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      const { error } = await fetchDeleteDict(record.id);
      if (!error) {
        message.success('删除成功');
        loadData();
      }
    }
  });
};

// 管理字典项
const handleManageItems = (record: DictItem) => {
  window.location.href = `/system/dict/${record.id}/items`;
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="h-full flex-col gap-24px p-24px overflow-y-auto">
    <!-- Page Header -->
    <div class="flex justify-between items-end">
      <div>
        <h1 class="text-24px font-700 text-primary-text mb-8px">数据字典管理</h1>
        <p class="text-14px text-secondary">管理系统中的枚举值和配置项，方便统一维护和调用</p>
      </div>
      <AButton type="primary" class="h-40px! rd-8px! font-bold! flex-center gap-8px shadow-lg!" @click="handleCreate">
        <div class="i-material-symbols:add-rounded text-18px"></div>
        <span>创建字典</span>
      </AButton>
    </div>

    <!-- Filter Bar -->
    <div class="bg-card rd-12px border border-border px-24px py-16px flex-y-center justify-between">
      <div class="flex-y-center gap-16px">
        <AInput v-model:value="searchForm.dictCode" placeholder="字典编码" class="w-200px! rd-8px!" @pressEnter="handleSearch">
          <template #prefix>
            <div class="i-material-symbols:code text-placeholder"></div>
          </template>
        </AInput>
        <AInput v-model:value="searchForm.dictName" placeholder="字典名称" class="w-200px! rd-8px!" @pressEnter="handleSearch">
          <template #prefix>
            <div class="i-material-symbols:label text-placeholder"></div>
          </template>
        </AInput>
        <AButton class="rd-8px!" @click="handleReset">重置</AButton>
      </div>
      <AButton type="primary" class="rd-8px!" @click="handleSearch">
        <div class="i-material-symbols:search text-18px mr-4px"></div>
        搜索
      </AButton>
    </div>

    <!-- Table -->
    <div class="bg-card rd-12px border border-border overflow-hidden flex-1">
      <ATable
        :dataSource="dictList"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="id"
        class="flex-1"
      >
        <template #bodyCell="{ column, record }">
          <!-- Status -->
          <template v-if="column.key === 'status'">
            <ATag :color="record.status === 1 ? 'success' : 'default'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </ATag>
          </template>

          <!-- Action -->
          <template v-if="column.key === 'action'">
            <div class="flex gap-8px">
              <AButton type="link" size="small" @click="handleManageItems(record)">
                管理字典项
              </AButton>
              <AButton type="link" size="small" @click="handleEdit(record)">
                编辑
              </AButton>
              <AButton type="link" danger size="small" @click="handleDelete(record)">
                删除
              </AButton>
            </div>
          </template>
        </template>
      </ATable>
    </div>

    <!-- Create/Edit Modal -->
    <AModal
      v-model:open="modalVisible"
      :title="modalTitle"
      width="600px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <AForm
        ref="formRef"
        :model="formData"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <AFormItem
          label="字典编码"
          name="dictCode"
          :rules="[
            { required: true, message: '请输入字典编码' },
            { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线' }
          ]"
        >
          <AInput v-model:value="formData.dictCode" placeholder="例如：project_status" :disabled="!!editingDict" />
        </AFormItem>
        
        <AFormItem
          label="字典名称"
          name="dictName"
          :rules="[{ required: true, message: '请输入字典名称' }]"
        >
          <AInput v-model:value="formData.dictName" placeholder="例如：项目状态" />
        </AFormItem>
        
        <AFormItem label="描述" name="description">
          <ATextarea v-model:value="formData.description" placeholder="请输入字典描述" :rows="2" />
        </AFormItem>
        
        <AFormItem label="类型" name="isSystem">
          <ARadioGroup v-model:value="formData.isSystem">
            <ARadio :value="0">自定义</ARadio>
            <ARadio :value="1">系统内置</ARadio>
          </ARadioGroup>
        </AFormItem>
        
        <AFormItem label="排序" name="sortOrder">
          <AInputNumber v-model:value="formData.sortOrder" :min="0" placeholder="数字越小越靠前" class="w-full!" />
        </AFormItem>
        
        <AFormItem label="状态" name="status">
          <ARadioGroup v-model:value="formData.status">
            <ARadio :value="1">启用</ARadio>
            <ARadio :value="0">禁用</ARadio>
          </ARadioGroup>
        </AFormItem>
      </AForm>
    </AModal>
  </div>
</template>

<style scoped>
.text-success {
  color: #52c41a;
}
.text-error {
  color: #ff4d4f;
}
</style>
