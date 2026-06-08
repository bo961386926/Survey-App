<script setup lang="ts">
import { ref, computed } from 'vue';
import { message } from 'ant-design-vue';
import { request } from '@/service/request';

defineOptions({ name: 'RolePermissionModal' });

const visible = ref(false);
const loading = ref(false);
const currentRoleId = ref<number | null>(null);
const currentRoleName = ref('');
const selectedPermissions = ref<Set<string>>(new Set());

// 权限分组配置（与后端 com.qhiot.survey.common.constant.Permissions 保持一致）
const permissionGroups = [
  {
    key: 'project',
    name: '项目管理',
    permissions: [
      { key: 'project:view', name: '查看项目' },
      { key: 'project:edit', name: '编辑项目' },
    ]
  },
  {
    key: 'template',
    name: '模板管理',
    permissions: [
      { key: 'template:view', name: '查看模板' },
      { key: 'template:edit', name: '编辑模板' },
      { key: 'template:bind', name: '模板绑定' },
    ]
  },
  {
    key: 'point',
    name: '点位管理',
    permissions: [
      { key: 'point:view', name: '查看点位' },
      { key: 'point:edit', name: '编辑点位' },
    ]
  },
  {
    key: 'survey',
    name: '勘查管理',
    permissions: [
      { key: 'survey:create', name: '创建勘查' },
      { key: 'survey:edit', name: '编辑勘查' },
      { key: 'survey:submit', name: '提交勘查' },
      { key: 'survey:assist', name: '协作勘查' },
    ]
  },
  {
    key: 'audit',
    name: '审核管理',
    permissions: [
      { key: 'audit:view', name: '查看审核' },
      { key: 'audit:pass', name: '审核通过' },
      { key: 'audit:reject', name: '审核驳回' },
    ]
  },
  {
    key: 'task',
    name: '任务管理',
    permissions: [
      { key: 'task:view', name: '查看任务' },
      { key: 'task:edit', name: '编辑任务' },
      { key: 'task:assign', name: '指派任务' },
    ]
  },
  {
    key: 'system',
    name: '系统管理',
    permissions: [
      { key: 'system:user', name: '用户管理' },
      { key: 'system:role', name: '角色管理' },
      { key: 'system:dict', name: '字典管理' },
      { key: 'system:log', name: '系统操作日志' },
    ]
  },
  {
    key: 'message',
    name: '消息管理',
    permissions: [
      { key: 'message:push', name: '推送消息' },
    ]
  },
  {
    key: 'export',
    name: '数据导出',
    permissions: [
      { key: 'export:project', name: '导出项目数据' },
      { key: 'export:audit', name: '导出审核数据' },
    ]
  }
];

// 全部权限列表
const allPermissions = computed(() => {
  const perms: string[] = [];
  for (const group of permissionGroups) {
    for (const perm of group.permissions) {
      perms.push(perm.key);
    }
  }
  return perms;
});

// 全选/取消全选
const isAllSelected = computed(() => {
  return selectedPermissions.value.size === allPermissions.value.length;
});

const toggleAll = () => {
  if (isAllSelected.value) {
    selectedPermissions.value.clear();
  } else {
    selectedPermissions.value = new Set(allPermissions.value);
  }
};

// 切换单个权限
const togglePermission = (permKey: string) => {
  if (selectedPermissions.value.has(permKey)) {
    selectedPermissions.value.delete(permKey);
  } else {
    selectedPermissions.value.add(permKey);
  }
};

// 分组全选状态
const getGroupSelectState = (group: any) => {
  const groupPerms = group.permissions.map((p: any) => p.key);
  const selectedCount = groupPerms.filter((p: string) => selectedPermissions.value.has(p)).length;

  if (selectedCount === 0) return 'none';
  if (selectedCount === groupPerms.length) return 'all';
  return 'partial';
};

// 切换分组全选
const toggleGroupAll = (group: any) => {
  const groupPerms = group.permissions.map((p: any) => p.key);
  const state = getGroupSelectState(group);

  if (state === 'all') {
    // 取消全选
    groupPerms.forEach((p: string) => selectedPermissions.value.delete(p));
  } else {
    // 全选
    groupPerms.forEach((p: string) => selectedPermissions.value.add(p));
  }
};

// 打开弹窗
const open = async (roleId: number, roleName: string) => {
  visible.value = true;
  currentRoleId.value = roleId;
  currentRoleName.value = roleName;
  await loadRolePermissions(roleId);
};

// 加载角色已有权限
const loadRolePermissions = async (roleId: number) => {
  try {
    const res: any = await request({
      url: `/role/${roleId}/permissions`,
      method: 'get'
    });

    selectedPermissions.value.clear();
    if (Array.isArray(res)) {
      res.forEach((p: string) => selectedPermissions.value.add(p));
    }
  } catch (error) {
    console.error('加载角色权限失败:', error);
    selectedPermissions.value.clear();
  }
};

// 保存权限配置
const handleSave = async () => {
  if (!currentRoleId.value) return;

  loading.value = true;
  try {
    await request({
      url: `/role/${currentRoleId.value}/permissions`,
      method: 'put',
      data: Array.from(selectedPermissions.value)
    });
    message.success('权限配置保存成功');
    visible.value = false;
  } catch (error) {
    message.error('保存失败，请重试');
  } finally {
    loading.value = false;
  }
};

defineExpose({ open });
</script>

<template>
  <a-modal
    v-model:open="visible"
    :title="`配置权限 - ${currentRoleName}`"
    width="720px"
    :confirm-loading="loading"
    @ok="handleSave"
  >
    <div class="permission-config">
      <!-- 顶部操作栏 -->
      <div class="flex items-center justify-between mb-16px">
        <span class="text-14px text-[var(--color-text-secondary)]">
          已选择 {{ selectedPermissions.size }} / {{ allPermissions.length }} 项权限
        </span>
        <a-button type="link" size="small" @click="toggleAll">
          {{ isAllSelected ? '取消全选' : '全选' }}
        </a-button>
      </div>

      <!-- 权限分组列表 -->
      <div class="max-h-400px overflow-y-auto pr-8px">
        <div
          v-for="group in permissionGroups"
          :key="group.key"
          class="mb-16px border border-[var(--color-border)] rounded-8px p-16px"
        >
          <!-- 分组标题 -->
          <div class="flex items-center justify-between mb-12px pb-12px border-b border-[var(--color-border)]">
            <div class="flex items-center gap-8px">
              <a-checkbox
                :checked="getGroupSelectState(group) !== 'none'"
                :indeterminate="getGroupSelectState(group) === 'partial'"
                @change="toggleGroupAll(group)"
              />
              <span class="text-14px font-500 text-[var(--color-text-primary)]">{{ group.name }}</span>
            </div>
            <span class="text-12px text-[var(--color-text-secondary)]">
              {{ group.permissions.filter((p: any) => selectedPermissions.has(p.key)).length }} / {{ group.permissions.length }}
            </span>
          </div>

          <!-- 权限项网格 -->
          <div class="grid grid-cols-2 gap-8px">
            <div
              v-for="perm in group.permissions"
              :key="perm.key"
              class="flex items-center gap-8px px-12px py-8px rounded-6px cursor-pointer transition-all hover:bg-[var(--color-bg-hover)]"
              @click="togglePermission(perm.key)"
            >
              <a-checkbox :checked="selectedPermissions.has(perm.key)" @click.prevent />
              <span class="text-13px text-[var(--color-text-primary)]">{{ perm.name }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <a-button @click="visible = false">取消</a-button>
      <a-button type="primary" :loading="loading" @click="handleSave">确定</a-button>
    </template>
  </a-modal>
</template>

<style scoped>
.permission-config {
  min-height: 300px;
}

.permission-config::-webkit-scrollbar {
  width: 6px;
}

.permission-config::-webkit-scrollbar-thumb {
  background-color: var(--color-border);
  border-radius: 3px;
}
</style>
