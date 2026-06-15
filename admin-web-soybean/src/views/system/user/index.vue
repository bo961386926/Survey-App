<template>
  <div class="min-h-full flex flex-col bg-[#f7f8fa]">
    <!-- 页面标题 -->
    <div class="px-24px pt-24px pb-16px">
      <h1 class="text-18px font-600 text-[#1D2129] mb-4px">用户管理</h1>
      <p class="text-13px text-[#4E5969]">管理系统用户账号、角色分配与权限控制</p>
    </div>

    <!-- 统计卡片 -->
    <div class="px-24px pb-16px">
      <div class="grid grid-cols-4 gap-16px">
        <div class="bg-white rounded-lg p-20px" style="box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
          <div class="text-13px text-[#4E5969] mb-8px">总用户数</div>
          <div class="text-24px font-600 text-[#1D2129]">{{ stats.total }}</div>
        </div>
        <div class="bg-white rounded-lg p-20px" style="box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
          <div class="text-13px text-[#4E5969] mb-8px">管理员</div>
          <div class="text-24px font-600 text-[#1D2129]">{{ stats.admin || 0 }}</div>
        </div>
        <div class="bg-white rounded-lg p-20px" style="box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
          <div class="text-13px text-[#4E5969] mb-8px">审核员</div>
          <div class="text-24px font-600 text-[#1D2129]">{{ stats.auditor || 0 }}</div>
        </div>
        <div class="bg-white rounded-lg p-20px" style="box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
          <div class="text-13px text-[#4E5969] mb-8px">勘查员</div>
          <div class="text-24px font-600 text-[#1D2129]">{{ stats.collector || 0 }}</div>
        </div>
      </div>
    </div>

    <!-- 表格卡片 -->
    <div class="flex-1 px-24px pb-24px">
      <div class="bg-white rounded-lg p-20px" style="box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
        <!-- 操作栏 -->
        <div class="flex items-center justify-between mb-16px">
          <div class="flex items-center gap-12px">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索用户姓名或账号"
              style="width: 240px"
              allow-clear
              @search="initData"
            />
            <a-select
              v-model:value="selectedRole"
              style="width: 140px"
              placeholder="角色筛选"
              allow-clear
              @change="initData"
            >
              <a-select-option value="">全部角色</a-select-option>
              <a-select-option v-for="role in roleOptions" :key="role.value" :value="role.value">
                {{ role.name }}
              </a-select-option>
            </a-select>
          </div>
          <a-button v-if="hasPermission('user:create')" type="primary" @click="handleAddUser">
            <template ><PlusOutlined /></template>
            新增用户
          </a-button>
        </div>

        <!-- 表格 -->
        <a-table
          :columns="columns"
          :data-source="userList"
          :loading="loading"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <!-- 用户信息列 -->
            <template v-if="column.key === 'user'">
              <div class="flex items-center gap-12px">
                <div class="w-32px h-32px rounded-full flex items-center justify-center text-white text-12px font-500 bg-[#1677FF] flex-shrink-0">
                  {{ (record.realName || record.username || '?').substring(0, 1) }}
                </div>
                <div class="min-w-0">
                  <div class="text-14px font-500 text-[#1D2129] truncate">{{ record.realName || record.username }}</div>
                  <div class="text-12px text-[#86909C] truncate">{{ record.username }}</div>
                </div>
              </div>
            </template>

            <!-- 角色列 -->
            <template v-if="column.key === 'roles'">
              <div class="flex flex-wrap gap-4px">
                <template v-if="record.roleIds && record.roleIds.length > 0">
                  <a-tag
                    v-for="rid in record.roleIds.slice(0, 2)"
                    :key="rid"
                    class="!text-12px"
                  >
                    {{ getRoleName(Number(rid)) }}
                  </a-tag>
                  <span v-if="record.roleIds.length > 2" class="text-12px text-[#86909C]">
                    +{{ record.roleIds.length - 2 }}
                  </span>
                </template>
                <span v-else class="text-12px text-[#86909C]">未分配</span>
              </div>
            </template>

            <!-- 状态列 -->
            <template v-if="column.key === 'status'">
              <a-tag :color="Number(record.status) === 1 ? 'success' : 'default'" class="!text-12px">
                {{ Number(record.status) === 1 ? '启用' : '停用' }}
              </a-tag>
            </template>

            <!-- 操作列 -->
            <template v-if="column.key === 'action'">
              <div class="flex items-center gap-8px">
                <a-button v-if="hasPermission('user:edit')" type="link" size="small" @click="handleEdit(record)">
                  编辑
                </a-button>
                <a-button v-if="hasPermission('user:edit')" type="link" size="small" @click="handleResetPassword(record)">
                  重置密码
                </a-button>
                <a-button v-if="hasPermission('user:edit')" type="link" size="small" :class="Number(record.status) === 1 ? '!text-[#F53F3F]' : '!text-[#00B42A]'" @click="handleDisable(record)">
                  {{ Number(record.status) === 1 ? '停用' : '启用' }}
                </a-button>
              </div>
            </template>
          </template>
        </a-table>
      </div>
    </div>

    <!-- 新增用户弹窗 -->
    <a-modal
      v-model:open="showAddUserModal"
      title="新增用户"
      :footer="null"
      width="600px"
      centered
    >
      <div class="p-4px">
        <a-form :model="newUserForm" layout="vertical">
          <a-form-item label="登录账号" required>
            <a-input v-model:value="newUserForm.username" placeholder="请输入登录账号" />
          </a-form-item>
          <a-form-item label="姓名" required>
            <a-input v-model:value="newUserForm.realName" placeholder="请输入真实姓名" />
          </a-form-item>
          <a-form-item label="初始密码" required>
            <a-input-password v-model:value="newUserForm.password" placeholder="请输入密码（至少8位）" />
          </a-form-item>
          <a-form-item label="邮箱">
            <a-input v-model:value="newUserForm.email" placeholder="请输入邮箱" />
          </a-form-item>
          <a-form-item label="手机号">
            <a-input v-model:value="newUserForm.phone" placeholder="请输入手机号" />
          </a-form-item>
          <a-form-item label="角色">
            <a-checkbox-group v-model:value="newUserForm.roleIds" class="flex flex-col gap-8px">
              <a-checkbox v-for="role in roleOptions" :key="role.value" :value="role.value">
                {{ role.name }}
              </a-checkbox>
            </a-checkbox-group>
          </a-form-item>
          <div class="flex justify-end gap-12px mt-24px">
            <a-button @click="showAddUserModal = false">取消</a-button>
            <a-button type="primary" :loading="submitLoading" @click="handleCreateUser">确定</a-button>
          </div>
        </a-form>
      </div>
    </a-modal>

    <!-- 编辑用户弹窗 -->
    <a-modal
      v-model:open="showEditUserModal"
      title="编辑用户"
      :footer="null"
      width="600px"
      centered
    >
      <div class="p-4px">
        <a-form :model="editUserForm" layout="vertical">
          <a-form-item label="姓名" required>
            <a-input v-model:value="editUserForm.realName" placeholder="请输入真实姓名" />
          </a-form-item>
          <a-form-item label="邮箱">
            <a-input v-model:value="editUserForm.email" placeholder="请输入邮箱" />
          </a-form-item>
          <a-form-item label="手机号">
            <a-input v-model:value="editUserForm.phone" placeholder="请输入手机号" />
          </a-form-item>
          <a-form-item label="角色">
            <a-checkbox-group v-model:value="editUserForm.roleIds" class="flex flex-col gap-8px">
              <a-checkbox v-for="role in roleOptions" :key="role.value" :value="role.value">
                {{ role.name }}
              </a-checkbox>
            </a-checkbox-group>
          </a-form-item>
          <div class="flex justify-end gap-12px mt-24px">
            <a-button @click="showEditUserModal = false">取消</a-button>
            <a-button type="primary" :loading="submitLoading" @click="handleUpdateUser">确定</a-button>
          </div>
        </a-form>
      </div>
    </a-modal>

    <!-- 重置密码弹窗 -->
    <a-modal
      v-model:open="showResetPwdModal"
      title="重置密码"
      :footer="null"
      width="400px"
      centered
    >
      <div class="p-4px">
        <div class="mb-16px text-13px text-[#4E5969]">
          为用户 <span class="font-500 text-[#1D2129]">{{ resetPwdTarget?.realName || resetPwdTarget?.username }}</span> 设置新密码
        </div>
        <a-form :model="resetPwdForm" layout="vertical">
          <a-form-item label="新密码" required>
            <a-input-password v-model:value="resetPwdForm.newPassword" placeholder="请输入新密码（至少8位）" />
          </a-form-item>
          <a-form-item label="确认密码" required>
            <a-input-password v-model:value="resetPwdForm.confirmPassword" placeholder="请再次输入新密码" />
          </a-form-item>
          <div class="flex justify-end gap-12px mt-24px">
            <a-button @click="showResetPwdModal = false">取消</a-button>
            <a-button type="primary" :loading="resetPwdLoading" @click="handleConfirmResetPwd">确定</a-button>
          </div>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { fetchGetUserList, fetchCreateUser, fetchUpdateUser, fetchResetPassword, fetchUpdateUserStatus, fetchGetUserDetailById } from '@/api/user';
import { fetchGetRoleList } from '@/api/role';
import { useAuthStore } from '@/store/modules/auth';

// 权限检查函数 - 基于角色编码判断
function hasPermission(permission: string): boolean {
  const authStore = useAuthStore();
  const roles = authStore.userInfo?.roles || [];
  const permissions = authStore.userInfo?.permissions || [];
  // admin 角色拥有所有权限
  if (roles.includes('admin')) return true;
  return permissions.includes(permission);
}

interface UserItem {
  id: string;
  username: string;
  realName?: string;
  email?: string;
  phone?: string;
  status: number;
  createTime?: string;
  roleIds?: number[];
}

interface RoleOption {
  name: string;
  value: number;
  key: string;
  description?: string;
}


// 表格列定义
const columns = [
  {
    title: '用户信息',
    key: 'user',
    width: 200
  },
  {
    title: '角色',
    key: 'roles',
    width: 200
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
    width: 200
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
    width: 150
  },
  {
    title: '状态',
    key: 'status',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right' as const
  }
];

// 数据状态
const loading = ref(false);
const userList = ref<UserItem[]>([]);
const searchKeyword = ref('');
const selectedRole = ref('');

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
});

// 统计数据
const stats = reactive({
  total: 0,
  admin: 0,
  auditor: 0,
  collector: 0
});

// 角色选项
const roleOptions = ref<RoleOption[]>([]);
const adminRoleId = ref<number | null>(null);

// 弹窗状态
const showAddUserModal = ref(false);
const showEditUserModal = ref(false);
const showResetPwdModal = ref(false);
const submitLoading = ref(false);
const resetPwdLoading = ref(false);
const resetPwdTarget = ref<UserItem | null>(null);

// 表单数据
const newUserForm = reactive({
  username: '',
  realName: '',
  password: '',
  email: '',
  phone: '',
  roleIds: [] as number[]
});

const editUserForm = reactive({
  id: '',
  username: '',
  realName: '',
  password: '',
  email: '',
  phone: '',
  roleIds: [] as number[]
});

const resetPwdForm = reactive({
  newPassword: '',
  confirmPassword: ''
});

// 初始化数据
const initData = async () => {
  loading.value = true;
  try {
    const params: Record<string, any> = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    };

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value;
    }

    if (selectedRole.value) {
      params.roleId = selectedRole.value;
    }

    const { data, error } = await fetchGetUserList(params);
    if (!error && data) {
      userList.value = (data.records || []) as UserItem[];
      pagination.total = data.total || 0;

      // 计算统计数据
      stats.total = data.total || 0;
      stats.admin = userList.value.filter(u => u.roleIds?.includes(adminRoleId.value!)).length;
      stats.auditor = userList.value.filter(u => u.roleIds?.some(rid => getRoleName(rid).includes('审核'))).length;
      stats.collector = userList.value.filter(u => u.roleIds?.some(rid => getRoleName(rid).includes('勘查'))).length;
    }
  } finally {
    loading.value = false;
  }
};

// 获取角色名称
const getRoleName = (roleId: number): string => {
  const role = roleOptions.value.find(r => r.value === roleId);
  return role?.name || `角色${roleId}`;
};

// 获取角色列表
const loadRoles = async () => {
  const { data, error } = await fetchGetRoleList({ pageNum: 1, pageSize: 100 });
  if (!error && data) {
    roleOptions.value = (data.records || []).map((role: any) => ({
      name: role.roleName,
      value: role.id,
      key: role.roleCode,
      description: role.remark || ''
    }));

    // 查找管理员角色ID（后端 role_code 为 admin 或 project_manager）
    const adminRole = roleOptions.value.find(r => r.key === 'admin' || r.key === 'project_manager' || r.name.includes('管理员'));
    if (adminRole) {
      adminRoleId.value = adminRole.value;
    }
  }
};


// 表格分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  initData();
};

// 新增用户
const handleAddUser = () => {
  Object.assign(newUserForm, {
    username: '',
    realName: '',
    password: '',
    email: '',
    phone: '',
    roleIds: []
  });
  showAddUserModal.value = true;
};

// 创建用户
const handleCreateUser = async () => {
  if (!newUserForm.username || !newUserForm.realName || !newUserForm.password) {
    message.warning('请填写必填项');
    return;
  }
  if (newUserForm.password.length < 8) {
    message.warning('密码长度至少8位');
    return;
  }

  submitLoading.value = true;
  try {
    const { error } = await fetchCreateUser(newUserForm);
    if (!error) {
      message.success('用户创建成功');
      showAddUserModal.value = false;
      initData();
    }
  } finally {
    submitLoading.value = false;
  }
};

// 编辑用户
const handleEdit = async (record: any) => {
  showEditUserModal.value = true;

  editUserForm.id = String(record.id);
  editUserForm.username = record.username;
  editUserForm.realName = record.realName || '';
  editUserForm.password = '';
  editUserForm.email = record.email || '';
  editUserForm.phone = record.phone || '';
  editUserForm.roleIds = record.roleIds || [];

  // 如果没有roleIds，尝试获取详情
  if (!record.roleIds || record.roleIds.length === 0) {
    try {
      const { data, error } = await fetchGetUserDetailById(record.id);
      if (!error && data) {
        editUserForm.roleIds = (data as any).roleIds || [];
      }
    } catch {
      // 忽略错误
    }
  }
};

// 更新用户
const handleUpdateUser = async () => {
  if (!editUserForm.realName) {
    message.warning('请填写姓名');
    return;
  }

  submitLoading.value = true;
  try {
    const { error } = await fetchUpdateUser(editUserForm);
    if (!error) {
      message.success('用户信息已更新');
      showEditUserModal.value = false;
      initData();
    }
  } finally {
    submitLoading.value = false;
  }
};

// 重置密码
const handleResetPassword = (record: any) => {
  resetPwdTarget.value = record;
  resetPwdForm.newPassword = '';
  resetPwdForm.confirmPassword = '';
  showResetPwdModal.value = true;
};

// 确认重置密码
const handleConfirmResetPwd = async () => {
  const { newPassword, confirmPassword } = resetPwdForm;
  if (!newPassword || newPassword.length < 8) {
    message.warning('密码长度至少8位');
    return;
  }
  if (newPassword !== confirmPassword) {
    message.warning('两次输入的密码不一致');
    return;
  }

  if (!resetPwdTarget.value?.id) {
    message.error('用户ID无效');
    return;
  }

  resetPwdLoading.value = true;
  try {
    const { error } = await fetchResetPassword(resetPwdTarget.value.id, newPassword);
    if (!error) {
      message.success('密码重置成功');
      showResetPwdModal.value = false;
    }
  } finally {
    resetPwdLoading.value = false;
  }
};

// 启用/停用用户
const handleDisable = (record: any) => {
  const newStatus = Number(record.status) === 1 ? 0 : 1;
  const actionText = newStatus === 1 ? '启用' : '停用';

  Modal.confirm({
    title: `${actionText}用户`,
    content: `确定要${actionText}用户 ${record.realName || record.username} 吗？`,
    async onOk() {
      const { error } = await fetchUpdateUserStatus(record.id, newStatus);
      if (!error) {
        message.success(`用户已${actionText}`);
        initData();
      }
    }
  });
};

onMounted(() => {
  loadRoles();
  initData();
});
</script>

<style scoped>
</style>
