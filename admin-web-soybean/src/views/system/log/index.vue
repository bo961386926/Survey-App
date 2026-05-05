<template>
  <div class="h-full flex flex-col bg-[var(--bg-page)] p-6">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-24px font-bold text-[var(--color-text-primary)] mb-1">操作日志</h1>
        <p class="text-14px text-[var(--color-text-secondary)]">
          共 <span class="font-600 text-[var(--color-text-primary)]">{{ total }}</span> 条 ·
          今日 <span class="font-600 text-[var(--color-success)]">{{ todayCount }}</span> 条
        </p>
      </div>
      <a-button @click="refreshData">
        <template #icon><ReloadOutlined /></template>
        刷新
      </a-button>
    </div>

    <!-- Filter Bar -->
    <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4 mb-6 shadow-[var(--shadow-card)]">
      <!-- Action Type Tags -->
      <div class="flex items-center gap-2 mb-4">
        <span class="text-13px text-[var(--color-text-secondary)] font-medium whitespace-nowrap">动作</span>
        <div class="flex gap-2 flex-wrap">
          <a-tag
            :color="activeAction === '' ? 'blue' : 'default'"
            :style="activeAction === '' ? { background: 'var(--color-primary)', color: '#fff', borderColor: 'var(--color-primary)' } : {}"
            class="cursor-pointer px-3 py-1"
            @click="activeAction = ''"
          >
            全部操作
          </a-tag>
          <a-tag
            v-for="action in actionTypes"
            :key="action.key"
            :color="activeAction === action.key ? action.color : 'default'"
            :style="activeAction === action.key ? { background: action.bgColor, color: action.color, borderColor: action.borderColor } : {}"
            class="cursor-pointer px-3 py-1"
            @click="activeAction = action.key"
          >
            {{ action.label }}
          </a-tag>
        </div>
        <div class="ml-auto flex gap-2">
          <a-tag color="success" class="px-3 py-1">
            <span class="inline-block w-6px h-6px rounded-full bg-[var(--color-success)] mr-1"></span>
            今日 {{ todayCount }}
          </a-tag>
          <a-tag color="error" class="px-3 py-1">
            <span class="inline-block w-6px h-6px rounded-full bg-[var(--color-danger)] mr-1"></span>
            删除 {{ deleteCount }}
          </a-tag>
        </div>
      </div>

      <!-- Search and Date Filter -->
      <div class="flex items-center gap-3">
        <a-input
          v-model:value="filters.keyword"
          placeholder="搜索操作人或对象..."
          class="w-240px"
          allow-clear
        >
          <template #prefix><SearchOutlined class="text-[var(--color-text-secondary)]" /></template>
        </a-input>
        
        <a-range-picker
          v-model:value="filters.dateRange"
          :placeholder="['年/月/日', '年/月/日']"
          class="!w-300px"
        />
        
        <div class="flex-1"></div>
        
        <a-button @click="handleSearch">
          <template #icon><SearchOutlined /></template>
          搜索
        </a-button>
        <a-button @click="handleReset">重置</a-button>
      </div>
    </div>

    <!-- Table -->
    <div class="flex-1 bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px overflow-hidden shadow-[var(--shadow-card)] flex flex-col">
      <a-table
        :columns="columns"
        :data-source="filteredLogs"
        :pagination="pagination"
        class="log-table"
        :scroll="{ y: 'calc(100vh - 320px)' }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            <span class="text-13px text-[var(--color-text-secondary)]">{{ (pagination.current - 1) * pagination.pageSize + index + 1 }}</span>
          </template>
          
          <template v-if="column.key === 'operator'">
            <div class="flex items-center gap-2">
              <div
                class="w-32px h-32px rounded-full flex items-center justify-center text-14px text-white font-600"
                :style="{ backgroundColor: record.operatorColor }"
              >
                {{ record.operatorInitial }}
              </div>
              <div>
                <div class="font-500 text-14px text-[var(--color-text-primary)]">{{ record.operator }}</div>
                <div class="text-12px text-[var(--color-text-secondary)]">{{ record.role }}</div>
              </div>
            </div>
          </template>
          
          <template v-if="column.key === 'module'">
            <div class="flex items-center gap-2">
              <component :is="iconComponents[record.moduleIcon]" class="text-16px" :style="{ color: record.moduleColor }" />
              <span class="text-13px text-[var(--color-text-primary)]">{{ record.module }}</span>
            </div>
          </template>
          
          <template v-if="column.key === 'actionType'">
            <a-tag
              :color="record.actionColor"
              :style="{ background: record.actionBg, color: record.actionColor, borderColor: record.actionBorderColor }"
              class="px-2 py-0.5"
            >
              {{ record.actionType }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'target'">
            <span class="text-13px text-[var(--color-text-primary)]">{{ record.target }}</span>
          </template>
          
          <template v-if="column.key === 'time'">
            <span class="text-13px text-[var(--color-text-secondary)]">{{ record.time }}</span>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { 
  ReloadOutlined, 
  SearchOutlined, 
  FolderOutlined, 
  AuditOutlined, 
  FormOutlined, 
  EnvironmentOutlined, 
  LoginOutlined 
} from '@ant-design/icons-vue';

// Define components for dynamic rendering
const iconComponents: Record<string, any> = {
  FolderOutlined,
  AuditOutlined,
  FormOutlined,
  EnvironmentOutlined,
  LoginOutlined
};

defineOptions({ name: 'SystemLog' });

const total = ref(20);
const todayCount = ref(0);
const deleteCount = ref(1);
const activeAction = ref('');

const actionTypes = [
  { key: 'create', label: '创建', color: 'var(--color-success)', bgColor: 'rgba(0,180,42,0.1)', borderColor: 'rgba(0,180,42,0.3)' },
  { key: 'update', label: '更新', color: 'var(--color-warning)', bgColor: 'rgba(255,193,7,0.1)', borderColor: 'rgba(255,193,7,0.3)' },
  { key: 'delete', label: '删除', color: 'var(--color-danger)', bgColor: 'rgba(245,63,63,0.1)', borderColor: 'rgba(245,63,63,0.3)' },
  { key: 'submit', label: '提交', color: 'var(--color-primary)', bgColor: 'rgba(22,119,255,0.1)', borderColor: 'rgba(22,119,255,0.3)' },
  { key: 'approve', label: '通过', color: 'var(--color-success)', bgColor: 'rgba(0,180,42,0.1)', borderColor: 'rgba(0,180,42,0.3)' },
  { key: 'reject', label: '驳回', color: 'var(--color-danger)', bgColor: 'rgba(245,63,63,0.1)', borderColor: 'rgba(245,63,63,0.3)' },
  { key: 'export', label: '导出', color: 'var(--color-text-secondary)', bgColor: 'var(--bg-hover)', borderColor: 'var(--color-border)' },
  { key: 'login', label: '登录', color: 'var(--color-text-secondary)', bgColor: 'var(--bg-hover)', borderColor: 'var(--color-border)' }
];

const filters = ref({
  keyword: '',
  dateRange: undefined as [string, string] | undefined
});

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 20,
  showSizeChanger: false
});

const columns = [
  { title: '#', key: 'index', width: 60 },
  { title: '操作人 / 角色', key: 'operator', width: 200 },
  { title: '模块', key: 'module', width: 180 },
  { title: '操作类型', key: 'actionType', width: 120 },
  { title: '操作对象', key: 'target' },
  { title: '时间', key: 'time', width: 150 }
];

const logData = ref([
  {
    id: '1',
    operator: '张管理',
    operatorInitial: '张',
    operatorColor: 'var(--color-primary)',
    role: '管理员',
    module: '项目管理',
    moduleIcon: 'FolderOutlined',
    moduleColor: 'var(--color-primary)',
    actionType: '创建',
    actionColor: 'var(--color-success)',
    actionBg: 'rgba(0,180,42,0.1)',
    actionBorderColor: 'rgba(0,180,42,0.3)',
    actionKey: 'create',
    target: 'A1标段排污口勘查项目',
    time: '3天前'
  },
  {
    id: '2',
    operator: '刘晨',
    operatorInitial: '刘',
    operatorColor: 'var(--color-primary)',
    role: '勘查员',
    module: '勘查审核',
    moduleIcon: 'AuditOutlined',
    moduleColor: 'var(--color-primary)',
    actionType: '提交',
    actionColor: 'var(--color-primary)',
    actionBg: 'rgba(22,119,255,0.1)',
    actionBorderColor: 'rgba(22,119,255,0.3)',
    actionKey: 'submit',
    target: '西闸口 001',
    time: '3天前'
  },
  {
    id: '3',
    operator: '王审核',
    operatorInitial: '王',
    operatorColor: 'var(--color-primary)',
    role: '审核员',
    module: '勘查审核',
    moduleIcon: 'AuditOutlined',
    moduleColor: 'var(--color-primary)',
    actionType: '通过',
    actionColor: 'var(--color-success)',
    actionBg: 'rgba(0,180,42,0.1)',
    actionBorderColor: 'rgba(0,180,42,0.3)',
    actionKey: 'approve',
    target: '东排口 003',
    time: '3天前'
  },
  {
    id: '4',
    operator: '张管理',
    operatorInitial: '张',
    operatorColor: 'var(--color-primary)',
    role: '管理员',
    module: '表单模板',
    moduleIcon: 'FormOutlined',
    moduleColor: 'var(--color-warning)',
    actionType: '更新',
    actionColor: 'var(--color-warning)',
    actionBg: 'rgba(255,193,7,0.1)',
    actionBorderColor: 'rgba(255,193,7,0.3)',
    actionKey: 'update',
    target: '排污口标准模板 v2',
    time: '3天前'
  },
  {
    id: '5',
    operator: '李勘查',
    operatorInitial: '李',
    operatorColor: 'var(--color-success)',
    role: '勘查员',
    module: '勘查审核',
    moduleIcon: 'AuditOutlined',
    moduleColor: 'var(--color-primary)',
    actionType: '提交',
    actionColor: 'var(--color-primary)',
    actionBg: 'rgba(22,119,255,0.1)',
    actionBorderColor: 'rgba(22,119,255,0.3)',
    actionKey: 'submit',
    target: '北闸口 007',
    time: '3天前'
  },
  {
    id: '6',
    operator: '王审核',
    operatorInitial: '王',
    operatorColor: 'var(--color-primary)',
    role: '审核员',
    module: '勘查审核',
    moduleIcon: 'AuditOutlined',
    moduleColor: 'var(--color-primary)',
    actionType: '驳回',
    actionColor: 'var(--color-danger)',
    actionBg: 'rgba(245,63,63,0.1)',
    actionBorderColor: 'rgba(245,63,63,0.3)',
    actionKey: 'reject',
    target: '西闸口 001',
    time: '4天前'
  },
  {
    id: '7',
    operator: '张管理',
    operatorInitial: '张',
    operatorColor: 'var(--color-primary)',
    role: '管理员',
    module: '点位管理',
    moduleIcon: 'EnvironmentOutlined',
    moduleColor: 'var(--color-success)',
    actionType: '创建',
    actionColor: 'var(--color-success)',
    actionBg: 'rgba(0,180,42,0.1)',
    actionBorderColor: 'rgba(0,180,42,0.3)',
    actionKey: 'create',
    target: '南排口 012',
    time: '4天前'
  },
  {
    id: '8',
    operator: '张管理',
    operatorInitial: '张',
    operatorColor: 'var(--color-primary)',
    role: '管理员',
    module: '点位管理',
    moduleIcon: 'EnvironmentOutlined',
    moduleColor: 'var(--color-success)',
    actionType: '删除',
    actionColor: 'var(--color-danger)',
    actionBg: 'rgba(245,63,63,0.1)',
    actionBorderColor: 'rgba(245,63,63,0.3)',
    actionKey: 'delete',
    target: '废弃点位 X01',
    time: '4天前'
  },
  {
    id: '9',
    operator: '刘晨',
    operatorInitial: '刘',
    operatorColor: 'var(--color-primary)',
    role: '勘查员',
    module: '登录认证',
    moduleIcon: 'LoginOutlined',
    moduleColor: 'var(--color-text-secondary)',
    actionType: '登录',
    actionColor: 'var(--color-text-secondary)',
    actionBg: 'var(--bg-hover)',
    actionBorderColor: 'var(--color-border)',
    actionKey: 'login',
    target: '—',
    time: '5天前'
  }
]);

const filteredLogs = computed(() => {
  let result = logData.value;
  
  // Filter by action type
  if (activeAction.value) {
    result = result.filter(log => log.actionKey === activeAction.value);
  }
  
  // Filter by keyword
  if (filters.value.keyword) {
    const keyword = filters.value.keyword.toLowerCase();
    result = result.filter(log =>
      log.operator.toLowerCase().includes(keyword) ||
      log.target.toLowerCase().includes(keyword)
    );
  }
  
  return result;
});

const handleSearch = () => {
  // Implement search logic
  console.log('Searching with:', filters.value);
};

const handleReset = () => {
  filters.value.keyword = '';
  filters.value.dateRange = undefined;
  activeAction.value = '';
};

const refreshData = () => {
  // Implement refresh logic
  console.log('Refreshing data...');
};
</script>

<style scoped>
.log-table :deep(.ant-table-thead > tr > th) {
  background-color: var(--bg-card-alt);
  color: var(--color-text-secondary);
  font-weight: 500;
  border-bottom: 1px solid var(--color-divider);
}

.log-table :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid var(--color-divider);
}

.log-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: var(--bg-hover);
}

.log-table :deep(.ant-pagination) {
  margin: 16px;
}
</style>
