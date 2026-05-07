<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { fetchGetDictDetail, fetchGetDictItems, fetchBatchSaveDictItems } from '@/api/dict';

const route = useRoute();
const dictId = route.params.id as string;

// 字典信息
const dictInfo = ref<any>(null);

// 字典项列表
const loading = ref(false);
const dictItems = ref<any[]>([]);

// 编辑状态
const editingIndex = ref<number | null>(null);

// 加载字典信息
const loadDictInfo = async () => {
  try {
    const { data, error } = await fetchGetDictDetail(dictId);
    if (!error && data) {
      dictInfo.value = data;
    }
  } catch (err) {
    console.error('Failed to load dict info', err);
    message.error('加载字典信息失败');
  }
};

// 加载字典项
const loadDictItems = async () => {
  loading.value = true;
  try {
    const { data, error } = await fetchGetDictItems(dictId);
    if (!error && data) {
      dictItems.value = data.map((item: any, index: number) => ({
        ...item,
        _key: item.id || `new_${index}`
      }));
    }
  } catch (err) {
    console.error('Failed to load dict items', err);
    message.error('加载字典项失败');
  } finally {
    loading.value = false;
  }
};

// 添加新项
const handleAdd = () => {
  dictItems.value.push({
    _key: `new_${Date.now()}`,
    itemLabel: '',
    itemValue: '',
    sortOrder: dictItems.value.length,
    status: 1,
    isReadonly: 0,
    remark: ''
  });
  editingIndex.value = dictItems.value.length - 1;
};

// 删除项
const handleDelete = (index: number) => {
  Modal.confirm({
    title: '确认删除？',
    content: '确定要删除这个字典项吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      dictItems.value.splice(index, 1);
      message.success('删除成功');
    }
  });
};

// 上移
const handleMoveUp = (index: number) => {
  if (index === 0) return;
  const temp = dictItems.value[index];
  dictItems.value[index] = dictItems.value[index - 1];
  dictItems.value[index - 1] = temp;
  
  // 更新排序
  dictItems.value.forEach((item, idx) => {
    item.sortOrder = idx;
  });
};

// 下移
const handleMoveDown = (index: number) => {
  if (index === dictItems.value.length - 1) return;
  const temp = dictItems.value[index];
  dictItems.value[index] = dictItems.value[index + 1];
  dictItems.value[index + 1] = temp;
  
  // 更新排序
  dictItems.value.forEach((item, idx) => {
    item.sortOrder = idx;
  });
};

// 保存
const handleSave = async () => {
  // 验证
  for (let i = 0; i < dictItems.value.length; i++) {
    const item = dictItems.value[i];
    if (!item.itemLabel || !item.itemLabel.trim()) {
      message.error(`第 ${i + 1} 行的标签不能为空`);
      editingIndex.value = i;
      return;
    }
    if (!item.itemValue && item.itemValue !== 0) {
      message.error(`第 ${i + 1} 行的值不能为空`);
      editingIndex.value = i;
      return;
    }
  }
  
  loading.value = true;
  try {
    // 准备保存的数据（移除内部字段）
    const itemsToSave = dictItems.value.map(item => ({
      id: typeof item._key === 'number' ? item._key : undefined,
      itemLabel: item.itemLabel,
      itemValue: String(item.itemValue),
      sortOrder: item.sortOrder,
      status: item.status,
      isReadonly: item.isReadonly || 0,
      remark: item.remark || ''
    }));
    
    const { error } = await fetchBatchSaveDictItems(dictId, itemsToSave);
    if (!error) {
      message.success('保存成功');
      await loadDictItems();
    }
  } catch (err) {
    console.error('Save failed:', err);
    message.error('保存失败');
  } finally {
    loading.value = false;
  }
};

// 取消编辑
const handleCancel = () => {
  editingIndex.value = null;
};

onMounted(async () => {
  await loadDictInfo();
  await loadDictItems();
});
</script>

<template>
  <div class="h-full flex-col gap-24px p-24px overflow-y-auto">
    <!-- Page Header -->
    <div class="flex justify-between items-end">
      <div>
        <div class="flex items-center gap-12px mb-8px">
          <ARouterLink to="/system/dict" class="text-secondary hover:text-primary">
            <div class="i-material-symbols:arrow-back text-20px"></div>
          </ARouterLink>
          <h1 class="text-24px font-700 text-primary-text">
            {{ dictInfo?.dictName || '字典项管理' }}
          </h1>
          <ATag color="blue">{{ dictInfo?.dictCode }}</ATag>
        </div>
        <p class="text-14px text-secondary">管理字典的选项值，用于下拉框、单选框等组件的数据源</p>
      </div>
      <div class="flex gap-12px">
        <AButton @click="$router.back()">
          返回
        </AButton>
        <AButton type="primary" class="rd-8px! flex-center gap-8px" @click="handleAdd">
          <div class="i-material-symbols:add-rounded text-18px"></div>
          <span>添加字典项</span>
        </AButton>
        <AButton 
          type="primary" 
          class="rd-8px!" 
          :loading="loading"
          @click="handleSave"
        >
          保存更改
        </AButton>
      </div>
    </div>

    <!-- Tips -->
    <AAlert
      message="操作提示"
      description="点击表格行即可编辑，编辑完成后点击「保存更改」按钮提交。可以通过上下箭头调整顺序。"
      type="info"
      show-icon
      class="mb-16px"
    />

    <!-- Table -->
    <div class="bg-card rd-12px border border-border overflow-hidden">
      <ATable
        :dataSource="dictItems"
        :loading="loading"
        :pagination="false"
        rowKey="_key"
        class="editable-table"
      >
        <ATableColumn title="序号" width="80">
          <template #default="{ index }">
            {{ index + 1 }}
          </template>
        </ATableColumn>
        
        <ATableColumn title="标签" width="300">
          <template #default="{ record, index }">
            <AInput
              v-if="editingIndex === index"
              v-model:value="record.itemLabel"
              placeholder="显示文本"
              size="small"
            />
            <span v-else>{{ record.itemLabel }}</span>
          </template>
        </ATableColumn>
        
        <ATableColumn title="值" width="300">
          <template #default="{ record, index }">
            <AInput
              v-if="editingIndex === index"
              v-model:value="record.itemValue"
              placeholder="存储值"
              size="small"
            />
            <span v-else>{{ record.itemValue }}</span>
          </template>
        </ATableColumn>
        
        <ATableColumn title="排序" width="100">
          <template #default="{ record }">
            {{ record.sortOrder }}
          </template>
        </ATableColumn>
        
        <ATableColumn title="只读" width="100">
          <template #default="{ record, index }">
            <ASwitch
              v-if="editingIndex === index"
              v-model:checked="record.isReadonly"
              :checked-value="1"
              :un-checked-value="0"
              checked-children="是"
              un-checked-children="否"
              size="small"
            />
            <ATag v-else :color="record.isReadonly === 1 ? 'warning' : 'default'">
              {{ record.isReadonly === 1 ? '是' : '否' }}
            </ATag>
          </template>
        </ATableColumn>
        
        <ATableColumn title="状态" width="120">
          <template #default="{ record, index }">
            <ASwitch
              v-if="editingIndex === index"
              v-model:checked="record.status"
              :checked-value="1"
              :un-checked-value="0"
              checked-children="启用"
              un-checked-children="禁用"
              size="small"
            />
            <ATag v-else :color="record.status === 1 ? 'success' : 'default'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </ATag>
          </template>
        </ATableColumn>
        
        <ATableColumn title="备注" width="200">
          <template #default="{ record, index }">
            <AInput
              v-if="editingIndex === index"
              v-model:value="record.remark"
              placeholder="备注信息"
              size="small"
            />
            <span v-else class="text-secondary">{{ record.remark || '-' }}</span>
          </template>
        </ATableColumn>
        
        <ATableColumn title="操作" width="200" fixed="right">
          <template #default="{ record, index }">
            <div class="flex gap-8px">
              <template v-if="editingIndex === index">
                <AButton type="link" size="small" @click="handleCancel">
                  取消
                </AButton>
              </template>
              <template v-else>
                <AButton 
                  type="link" 
                  size="small" 
                  :disabled="record.isReadonly === 1"
                  @click="editingIndex = index"
                >
                  编辑
                </AButton>
              </template>
              <AButton 
                type="link" 
                size="small" 
                :disabled="index === 0"
                @click="handleMoveUp(index)"
              >
                <div class="i-material-symbols:arrow-upward text-16px"></div>
              </AButton>
              <AButton 
                type="link" 
                size="small" 
                :disabled="index === dictItems.length - 1"
                @click="handleMoveDown(index)"
              >
                <div class="i-material-symbols:arrow-downward text-16px"></div>
              </AButton>
              <AButton 
                type="link" 
                danger 
                size="small" 
                :disabled="record.isReadonly === 1"
                @click="handleDelete(index)"
              >
                删除
              </AButton>
            </div>
          </template>
        </ATableColumn>
      </ATable>
    </div>
  </div>
</template>

<style scoped>
.editable-table :deep(.ant-table-cell) {
  cursor: pointer;
}
.editable-table :deep(.ant-table-row:hover) {
  background-color: #f5f5f5;
}
</style>
