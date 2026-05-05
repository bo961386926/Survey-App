<script setup lang="ts">
import { ref } from 'vue';

defineOptions({ name: 'SystemRole' });

const roleList = ref([
  {
    id: 'R-001',
    name: '系统管理员',
    code: 'SUPER_ADMIN',
    description: '拥有系统最高权限',
    status: 'ACTIVE',
    createdAt: '2025-01-01 08:00:00'
  },
  {
    id: 'R-002',
    name: '项目经理',
    code: 'PROJECT_MANAGER',
    description: '管理项目、分配标段和任务',
    status: 'ACTIVE',
    createdAt: '2025-01-02 10:00:00'
  },
  {
    id: 'R-003',
    name: '外业勘察员',
    code: 'SURVEYOR',
    description: '负责移动端外业数据采集',
    status: 'ACTIVE',
    createdAt: '2025-01-02 10:30:00'
  },
  {
    id: 'R-004',
    name: '内业审核员',
    code: 'AUDITOR',
    description: '负责PC端审核外业提交的数据',
    status: 'ACTIVE',
    createdAt: '2025-01-02 11:00:00'
  }
]);

const columns = [
  { title: '角色名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '角色编码', dataIndex: 'code', key: 'code', width: 200 },
  { title: '角色描述', dataIndex: 'description', key: 'description' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
];

const handleCreateRole = () => {
  console.log('Create Role');
};
</script>

<template>
  <div class="p-6 h-full flex flex-col gap-4">
    <div class="header-card flex justify-between items-center">
      <div class="flex items-center gap-4">
        <h2 class="m-0 text-16px font-600 title-text">角色管理</h2>
        <a-input-search placeholder="搜索角色名称/编码" style="width: 250px" />
      </div>
      <a-button type="primary" @click="handleCreateRole">
        <template #icon><icon-lucide-plus /></template>
        新增角色
      </a-button>
    </div>

    <div class="table-card flex-1">
      <a-table
        :dataSource="roleList"
        :columns="columns"
        rowKey="id"
        :pagination="{ total: 4, current: 1, pageSize: 10 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-switch :checked="record.status === 'ACTIVE'" size="small" />
          </template>

          <template v-if="column.key === 'action'">
            <div class="flex gap-2">
              <a-button type="link" size="small">编辑</a-button>
              <a-button type="link" size="small">分配权限</a-button>
              <a-button type="link" size="small" danger :disabled="record.code === 'SUPER_ADMIN'">删除</a-button>
            </div>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<style scoped>
.header-card {
  background-color: var(--bg-card);
  border-radius: 8px;
  padding: 16px 20px;
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
