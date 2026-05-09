<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { request } from '@/service/request';
import { useAuth } from '@/hooks/common/auth';
import RolePermissionModal from './components/RolePermissionModal.vue';

defineOptions({ name: 'SystemRole' });

const { hasPermission } = useAuth();

const loading = ref(false);
const searchKeyword = ref('');
const showAddRoleModal = ref(false);
const showEditRoleModal = ref(false);
const permissionModalRef = ref<InstanceType<typeof RolePermissionModal> | null>(null);

// 角色列表
const roleList = ref<any[]>([]);

// 角色表单
const newRoleForm = ref({
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
});

const editRoleForm = ref({
  id: null as number | null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
});

// 表格列定义
const columns = [
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 200 },
  { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 200 },
  { title: '角色描述', dataIndex: 'description', key: 'description' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
];

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true;
  try {
    const res: any = await request({
      url: '/role/page',
      method: 'get',
      params: {
        pageNum: 1,
        pageSize: 100,
        keyword: searchKeyword.value || undefined
      }
    });
    if (res?.records) {
      roleList.value = res.records.map((item: any) => ({
        ...item,
        key: item.id,
        roleName: item.roleName || item.name,
        roleCode: item.roleCode || item.code,
        description: item.description || item.desc,
        status: item.status === 1 ? 'ACTIVE' : 'INACTIVE'
      }));
    }
  } catch (error) {
    console.error('获取角色列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 创建角色
const handleCreateRole = async () => {
  if (!newRoleForm.value.roleName || !newRoleForm.value.roleCode) {
    message.warning('请填写角色名称和编码');
    return;
  }

  try {
    await request({
      url: '/role',
      method: 'post',
      data: {
        roleName: newRoleForm.value.roleName,
        roleCode: newRoleForm.value.roleCode,
        description: newRoleForm.value.description,
        status: newRoleForm.value.status
      }
    });
    message.success('角色创建成功');
    showAddRoleModal.value = false;
    resetNewRoleForm();
    fetchRoleList();
  } catch (error) {
    message.error('创建失败');
  }
};

// 编辑角色
const handleEditRole = (record: any) => {
  showEditRoleModal.value = true;
  editRoleForm.value = {
    id: record.id,
    roleName: record.roleName || record.name,
    roleCode: record.roleCode || record.code,
    description: record.description || record.desc,
    status: record.status === 'ACTIVE' ? 1 : 0
  };
};

// 更新角色
const handleUpdateRole = async () => {
  if (!editRoleForm.value.roleName || !editRoleForm.value.id) {
    message.warning('请填写角色名称');
    return;
  }

  try {
    await request({
      url: `/role/${editRoleForm.value.id}`,
      method: 'put',
      data: {
        roleName: editRoleForm.value.roleName,
        description: editRoleForm.value.description,
        status: editRoleForm.value.status
      }
    });
    message.success('角色更新成功');
    showEditRoleModal.value = false;
    fetchRoleList();
  } catch (error) {
    message.error('更新失败');
  }
};

// 删除角色
const handleDeleteRole = (record: any) => {
  Modal.confirm({
    title: '删除角色',
    content: `确定要删除角色 "${record.roleName}" 吗？`,
    async onOk() {
      try {
        await request({
          url: `/role/${record.id}`,
          method: 'delete'
        });
        message.success('角色已删除');
        fetchRoleList();
      } catch (error) {
        message.error('删除失败');
      }
    }
  });
};

// 切换角色状态（乐观更新：先改 UI，再调 API，失败回滚）
const handleToggleStatus = async (record: any) => {
  const newStatus = record.status === 'ACTIVE' ? 0 : 1;
  const oldStatus = record.status;

  // 即时乐观更新 — UI 立即响应
  record.status = newStatus === 1 ? 'ACTIVE' : 'INACTIVE';

  try {
    await request({
      url: `/role/${record.id}/status`,
      method: 'put',
      params: { status: newStatus }   // 使用 query 参数，匹配后端 @RequestParam
    });
    message.success(`角色已${newStatus === 1 ? '启用' : '禁用'}`);
  } catch (error) {
    // 失败回滚到原状态
    record.status = oldStatus;
    message.error('状态更新失败');
  }
};

// 重置表单
const resetNewRoleForm = () => {
  newRoleForm.value = {
    roleName: '',
    roleCode: '',
    description: '',
    status: 1
  };
};

// 打开新增弹窗
const openAddModal = () => {
  resetNewRoleForm();
  showAddRoleModal.value = true;
};

// 打开权限配置弹窗
const openPermissionModal = (record: any) => {
  permissionModalRef.value?.open(record.id, record.roleName);
};

onMounted(() => {
  fetchRoleList();
});
</script>

<template>
  <div class="p-6 h-full flex flex-col gap-4">
    <div class="header-card flex justify-between items-center">
      <div class="flex items-center gap-4">
        <h2 class="m-0 text-16px font-600 title-text">角色管理</h2>
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索角色名称/编码"
          style="width: 250px"
          @search="fetchRoleList"
        />
      </div>
      <a-button v-if="hasPermission('role:edit')" type="primary" class="h-36px! px-16px! rd-8px! font-medium! flex items-center gap-6px" @click="openAddModal">
        <div class="i-material-symbols:add-rounded text-16px flex-shrink-0"></div>
        新增角色
      </a-button>
    </div>

    <div class="table-card flex-1">
      <a-table
        :dataSource="roleList"
        :columns="columns"
        rowKey="id"
        :loading="loading"
        :pagination="{ total: roleList.length, current: 1, pageSize: 10 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'success' : 'default'">
              {{ record.status === 'ACTIVE' ? '启用' : '禁用' }}
            </a-tag>
          </template>

          <template v-if="column.key === 'action'">
            <div class="flex-y-center gap-8px">
              <a-tooltip title="配置权限">
                <a-button
                  type="text"
                  size="small"
                  class="!p-0 !h-auto !w-22px text-[var(--color-primary)]!"
                  @click="openPermissionModal(record)"
                >
                  <div class="i-material-symbols:key-rounded text-15px"></div>
                </a-button>
              </a-tooltip>
              <a-tooltip title="编辑角色">
                <a-button
                  type="text"
                  size="small"
                  class="!p-0 !h-auto !w-22px text-secondary!"
                  :disabled="record.roleCode === 'admin'"
                  @click="handleEditRole(record)"
                >
                  <div class="i-material-symbols:edit-outline-rounded text-15px"></div>
                </a-button>
              </a-tooltip>
              <a-tooltip :title="record.status === 'ACTIVE' ? '禁用角色' : '启用角色'">
                <a-switch
                  :checked="record.status === 'ACTIVE'"
                  size="small"
                  @change="handleToggleStatus(record)"
                />
              </a-tooltip>
              <a-tooltip title="删除角色">
                <a-button
                  type="text"
                  size="small"
                  class="!p-0 !h-auto !w-22px text-error!"
                  :disabled="record.roleCode === 'admin'"
                  @click="handleDeleteRole(record)"
                >
                  <div class="i-material-symbols:delete-rounded text-15px"></div>
                </a-button>
              </a-tooltip>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- Add Role Modal -->
    <a-modal
      v-model:open="showAddRoleModal"
      title="新增角色"
      :footer="null"
      width="500px"
    >
      <a-form layout="vertical" class="space-y-4">
        <a-form-item label="角色名称" required>
          <a-input v-model:value="newRoleForm.roleName" placeholder="请输入角色名称" />
        </a-form-item>

        <a-form-item label="角色编码" required>
          <a-input v-model:value="newRoleForm.roleCode" placeholder="如: PROJECT_MANAGER" />
        </a-form-item>

        <a-form-item label="角色描述">
          <a-textarea v-model:value="newRoleForm.description" placeholder="请输入角色描述" :rows="3" />
        </a-form-item>

        <a-form-item label="状态">
          <a-switch :checked="newRoleForm.status === 1" @change="(checked: boolean) => newRoleForm.status = checked ? 1 : 0" />
          <span class="ml-2">{{ newRoleForm.status === 1 ? '启用' : '禁用' }}</span>
        </a-form-item>
      </a-form>

      <div class="flex gap-3 mt-6">
        <a-button block @click="showAddRoleModal = false">取消</a-button>
        <a-button type="primary" block @click="handleCreateRole">创建</a-button>
      </div>
    </a-modal>

    <!-- Edit Role Modal -->
    <a-modal
      v-model:open="showEditRoleModal"
      title="编辑角色"
      :footer="null"
      width="500px"
    >
      <a-form layout="vertical" class="space-y-4">
        <a-form-item label="角色名称" required>
          <a-input v-model:value="editRoleForm.roleName" placeholder="请输入角色名称" />
        </a-form-item>

        <a-form-item label="角色编码">
          <a-input v-model:value="editRoleForm.roleCode" disabled />
        </a-form-item>

        <a-form-item label="角色描述">
          <a-textarea v-model:value="editRoleForm.description" placeholder="请输入角色描述" :rows="3" />
        </a-form-item>

        <a-form-item label="状态">
          <a-switch
            :checked="editRoleForm.status === 1"
            @change="(checked: boolean) => editRoleForm.status = checked ? 1 : 0"
          />
          <span class="ml-2">{{ editRoleForm.status === 1 ? '启用' : '禁用' }}</span>
        </a-form-item>
      </a-form>

      <div class="flex gap-3 mt-6">
        <a-button block @click="showEditRoleModal = false">取消</a-button>
        <a-button type="primary" block @click="handleUpdateRole">保存</a-button>
      </div>
    </a-modal>

    <!-- 权限配置弹窗 -->
    <RolePermissionModal ref="permissionModalRef" />
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

::deep(.ant-table-thead > tr > th) {
  background-color: var(--bg-card) !important;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-divider) !important;
}

::deep(.ant-table-tbody > tr > td) {
  background-color: var(--bg-card) !important;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-divider) !important;
}

::deep(.ant-table-tbody > tr:hover > td) {
  background-color: var(--bg-hover) !important;
}
</style>
