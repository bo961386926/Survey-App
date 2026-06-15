<script setup lang="ts">
import { ref, reactive } from 'vue';
import { message, Modal } from 'ant-design-vue';
import type { TableColumnsType } from 'ant-design-vue';
import { fetchGetDictItems, fetchBatchSaveDictItems } from '@/api/dict';

interface DictItemData {
  id?: number;
  dictId?: number;
  dictCode: string;
  itemLabel: string;
  itemValue: string;
  sortOrder?: number;
  status?: number;
  description?: string;
}

interface DictInfo {
  id: number;
  dictCode: string;
  dictName: string;
}

const emit = defineEmits<{
  success: [];
}>();

const modalVisible = ref(false);
const loading = ref(false);
const currentDict = ref<DictInfo | null>(null);
const dictItems = ref<DictItemData[]>([]);

// 编辑弹窗
const editModalVisible = ref(false);
const editTitle = ref('');
const editingItem = ref<DictItemData | null>(null);
const editFormRef = ref();

const editFormData = reactive<DictItemData>({
  dictCode: '',
  itemLabel: '',
  itemValue: '',
  sortOrder: 0,
  status: 1,
  description: ''
});

// 表格列定义
const columns: TableColumnsType = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    customRender: ({ index }: any) => index + 1
  },
  { title: '字典项标签', dataIndex: 'itemLabel', key: 'itemLabel', width: 150 },
  { title: '字典项值', dataIndex: 'itemValue', key: 'itemValue', width: 150 },
  { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const }
];

// 打开弹窗
const open = (dict: DictInfo) => {
  currentDict.value = dict;
  modalVisible.value = true;
  loadDictItems();
};

// 加载字典项
const loadDictItems = async () => {
  if (!currentDict.value) return;

  loading.value = true;
  try {
    const { data, error } = await fetchGetDictItems(currentDict.value.id);
    if (!error && data) {
      dictItems.value = data || [];
    }
  } catch (err) {
    console.error('Failed to load dict items', err);
    message.error('加载字典项失败');
  } finally {
    loading.value = false;
  }
};

// 打开新增弹窗
const handleAdd = () => {
  editTitle.value = '新增字典项';
  editingItem.value = null;
  Object.assign(editFormData, {
    dictCode: currentDict.value?.dictCode || '',
    itemLabel: '',
    itemValue: '',
    sortOrder: dictItems.value.length,
    status: 1,
    description: ''
  });
  editModalVisible.value = true;
};

// 打开编辑弹窗
const handleEdit = (record: any) => {
  editTitle.value = '编辑字典项';
  editingItem.value = record;
  Object.assign(editFormData, {
    dictCode: record.dictCode,
    itemLabel: record.itemLabel,
    itemValue: record.itemValue,
    sortOrder: record.sortOrder || 0,
    status: record.status ?? 1,
    description: record.description || ''
  });
  editModalVisible.value = true;
};

// 提交编辑表单
const handleEditSubmit = async () => {
  try {
    await editFormRef.value?.validate();

    if (editingItem.value) {
      // 编辑模式
      const index = dictItems.value.findIndex(item => item.id === editingItem.value?.id);
      if (index !== -1) {
        dictItems.value[index] = { ...editFormData, id: editingItem.value.id };
      }
    } else {
      // 新增模式
      dictItems.value.push({ ...editFormData, id: Date.now() });
    }

    editModalVisible.value = false;
  } catch (err) {
    console.error('Submit failed:', err);
  }
};

// 删除字典项
const handleDelete = (record: any) => {
  Modal.confirm({
    title: '确认删除？',
    content: `确定要删除字典项「${record.itemLabel}」吗？`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: () => {
      const index = dictItems.value.findIndex(item => item.id === record.id);
      if (index !== -1) {
        dictItems.value.splice(index, 1);
        message.success('删除成功');
      }
    }
  });
};

// 保存所有字典项
const handleSaveAll = async () => {
  if (!currentDict.value) return;

  if (dictItems.value.length === 0) {
    message.warning('暂无字典项可保存');
    return;
  }

  try {
    const { error } = await fetchBatchSaveDictItems(
      currentDict.value.id,
      dictItems.value.map(item => ({
        ...item,
        dictId: currentDict.value!.id,
        status: item.status ?? 1
      }))
    );

    if (!error) {
      message.success('保存成功');
      modalVisible.value = false;
      emit('success');
    }
  } catch (err) {
    console.error('Save failed:', err);
    message.error('保存失败');
  }
};

// 关闭弹窗
const handleClose = () => {
  modalVisible.value = false;
  dictItems.value = [];
  currentDict.value = null;
};

defineExpose({ open });
</script>

<template>
  <AModal
    v-model:open="modalVisible"
    :title="`管理字典项 - ${currentDict?.dictName || ''}`"
    width="900px"
    :footer="null"
    @cancel="handleClose"
  >
    <!-- 操作栏 -->
    <div class="flex justify-between items-center mb-16px">
      <AButton type="primary" @click="handleAdd">
        <div class="i-material-symbols:add-rounded text-16px mr-4px"></div>
        新增字典项
      </AButton>
      <div class="flex gap-8px">
        <AButton @click="handleClose">取消</AButton>
        <AButton type="primary" @click="handleSaveAll">保存</AButton>
      </div>
    </div>

    <!-- 表格 -->
    <ATable
      :dataSource="dictItems"
      :columns="columns"
      :loading="loading"
      :pagination="false"
      rowKey="id"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <!-- 状态 -->
        <template v-if="column.key === 'status'">
          <ATag :color="record.status === 1 ? 'success' : 'default'">
            {{ record.status === 1 ? '启用' : '禁用' }}
          </ATag>
        </template>

        <!-- 操作 -->
        <template v-if="column.key === 'action'">
          <div class="flex gap-8px">
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

    <!-- 新增/编辑弹窗 -->
    <AModal
      v-model:open="editModalVisible"
      :title="editTitle"
      width="500px"
      @ok="handleEditSubmit"
      @cancel="editModalVisible = false"
    >
      <AForm
        ref="editFormRef"
        :model="editFormData"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <AFormItem
          label="字典项标签"
          name="itemLabel"
          :rules="[{ required: true, message: '请输入字典项标签' }]"
        >
          <AInput v-model:value="editFormData.itemLabel" placeholder="显示名称" />
        </AFormItem>

        <AFormItem
          label="字典项值"
          name="itemValue"
          :rules="[{ required: true, message: '请输入字典项值' }]"
        >
          <AInput v-model:value="editFormData.itemValue" placeholder="实际值" />
        </AFormItem>

        <AFormItem label="描述" name="description">
          <ATextarea v-model:value="editFormData.description" placeholder="请输入描述" :rows="2" />
        </AFormItem>

        <AFormItem label="排序" name="sortOrder">
          <AInputNumber v-model:value="editFormData.sortOrder" :min="0" class="w-full!" />
        </AFormItem>

        <AFormItem label="状态" name="status">
          <ARadioGroup v-model:value="editFormData.status">
            <ARadio :value="1">启用</ARadio>
            <ARadio :value="0">禁用</ARadio>
          </ARadioGroup>
        </AFormItem>
      </AForm>
    </AModal>
  </AModal>
</template>
