<template>
  <div class="h-full flex flex-col bg-[var(--bg-page)] p-4">
    <!-- Header -->
    <div class="flex items-center justify-between mb-3">
      <div>
        <h1 class="text-20px font-bold text-[var(--color-text-primary)] mb-0.5">操作日志</h1>
        <p class="text-12px text-[var(--color-text-secondary)]">
          共 <span class="font-600 text-[var(--color-text-primary)]">{{ total }}</span> 条 ·
          今日 <span class="font-600 text-[var(--color-success)]">{{ todayCount }}</span> 条
        </p>
      </div>
      <a-button size="small" @click="refreshData">
        <template #icon><ReloadOutlined /></template>
        刷新
      </a-button>
    </div>

    <!-- Filter Bar -->
    <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-3 mb-3 shadow-[var(--shadow-card)]">
      <!-- Action Type Tags -->
      <div class="flex items-center gap-1.5 mb-2">
        <span class="text-12px text-[var(--color-text-secondary)] font-medium whitespace-nowrap">动作</span>
        <div class="flex gap-1.5 flex-wrap">
          <a-tag
            :color="activeAction === '' ? 'blue' : 'default'"
            :style="activeAction === '' ? { background: 'var(--color-primary)', color: '#fff', borderColor: 'var(--color-primary)' } : {}"
            class="cursor-pointer px-2 py-0.5 text-12px"
            @click="handleActionFilter('')"
          >
            全部操作
          </a-tag>
          <a-tag
            v-for="action in actionTypes"
            :key="action.key"
            :color="activeAction === action.key ? action.color : 'default'"
            :style="activeAction === action.key ? { background: action.bgColor, color: action.color, borderColor: action.borderColor } : {}"
            class="cursor-pointer px-2 py-0.5 text-12px"
            @click="handleActionFilter(action.key)"
          >
            {{ action.label }}
          </a-tag>
        </div>
        <div class="ml-auto flex gap-1.5">
          <a-tag color="success" class="px-2 py-0.5 text-12px">
            <span class="inline-block w-5px h-5px rounded-full bg-[var(--color-success)] mr-1"></span>
            今日 {{ todayCount }}
          </a-tag>
          <a-tag color="error" class="px-2 py-0.5 text-12px">
            <span class="inline-block w-5px h-5px rounded-full bg-[var(--color-danger)] mr-1"></span>
            删除 {{ deleteCount }}
          </a-tag>
        </div>
      </div>

      <!-- Search and Date Filter -->
      <div class="flex items-center gap-2">
        <a-input
          v-model:value="filters.keyword"
          placeholder="搜索操作人或对象..."
          class="w-200px"
          size="small"
          allow-clear
        >
          <template #prefix><SearchOutlined class="text-[var(--color-text-secondary)]" /></template>
        </a-input>

        <a-range-picker
          v-model:value="filters.dateRange"
          :placeholder="['年/月/日', '年/月/日']"
          size="small"
          class="!w-260px"
        />

        <div class="flex-1"></div>

        <a-button size="small" @click="handleSearch">
          <template #icon><SearchOutlined /></template>
          搜索
        </a-button>
        <a-button size="small" @click="handleReset">重置</a-button>
      </div>
    </div>

    <!-- Table -->
    <div class="flex-1 bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px overflow-hidden shadow-[var(--shadow-card)] flex flex-col">
      <a-table
        :columns="columns"
        :data-source="logData"
        :loading="loading"
        :pagination="pagination"
        class="log-table"
        row-key="id"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            <span class="text-12px text-[var(--color-text-secondary)]">{{ (pagination.current - 1) * pagination.pageSize + index + 1 }}</span>
          </template>

          <template v-if="column.key === 'operator'">
            <div class="flex items-center gap-2">
              <div
                class="w-24px h-24px rounded-full flex items-center justify-center text-11px text-white font-600"
                :style="{ backgroundColor: record.operatorColor }"
              >
                {{ record.operatorInitial }}
              </div>
              <div class="font-500 text-13px text-[var(--color-text-primary)]">{{ record.operator }}</div>
            </div>
          </template>

          <template v-if="column.key === 'module'">
            <div class="flex items-center gap-2">
              <component :is="iconComponents[record.moduleIcon]" class="text-14px" :style="{ color: record.moduleColor }" />
              <span class="text-13px text-[var(--color-text-primary)]">{{ record.module }}</span>
            </div>
          </template>

          <template v-if="column.key === 'actionType'">
            <a-tag
              :color="record.actionColor"
              :style="{ background: record.actionBg, color: record.actionColor, borderColor: record.actionBorderColor }"
              class="px-1.5 py-0"
            >
              {{ record.actionType }}
            </a-tag>
          </template>

          <template v-if="column.key === 'target'">
            <span class="text-13px text-[var(--color-text-primary)]">{{ record.target }}</span>
          </template>

          <template v-if="column.key === 'time'">
            <span class="text-12px text-[var(--color-text-secondary)]">{{ record.time }}</span>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, ref } from 'vue';
import {
  AuditOutlined,
  EnvironmentOutlined,
  FolderOutlined,
  FormOutlined,
  LoginOutlined,
  ReloadOutlined,
  SearchOutlined
} from '@ant-design/icons-vue';
import { fetchCountByRiskLevel, fetchGetOperationLogPage } from '@/api/log';

// Define components for dynamic rendering
const iconComponents: Record<string, any> = {
  FolderOutlined,
  AuditOutlined,
  FormOutlined,
  EnvironmentOutlined,
  LoginOutlined
};

defineOptions({ name: 'SystemLog' });

// ============ Types ============
// 后端返回的日志数据类型
interface BackendLogItem {
  id: number;
  userId: number;
  username: string;
  role?: string;
  module: string;
  action: string;
  detail: string;
  createTime: string;
}

// 日志项类型
interface LogItem {
  id: number;
  operator: string;
  operatorInitial: string;
  operatorColor: string;
  role: string;
  module: string;
  moduleIcon: string;
  moduleColor: string;
  actionType: string;
  actionColor: string;
  actionBg: string;
  actionBorderColor: string;
  actionKey: string;
  target: string;
  time: string;
}

// ============ State ============
const total = ref(0);
const todayCount = ref(0);
const deleteCount = ref(0);
const activeAction = ref('');
const loading = ref(false);

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
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (t: number) => `共 ${t} 条`,
  hideOnSinglePage: false,
  pageSizeOptions: ['10', '20', '50', '100'],
  onChange: (page: number, pageSize: number) => {
    pagination.value.current = page;
    pagination.value.pageSize = pageSize;
    loadLogs();
  },
  onShowSizeChange: (current: number, size: number) => {
    pagination.value.current = 1;
    pagination.value.pageSize = size;
    loadLogs();
  }
});

const columns = [
  { title: '#', key: 'index', width: 60 },
  { title: '操作人 / 角色', key: 'operator', width: 200 },
  { title: '模块', key: 'module', width: 180 },
  { title: '操作类型', key: 'actionType', width: 120 },
  { title: '操作对象', key: 'target' },
  { title: '时间', key: 'time', width: 150 }
];

const logData = ref<LogItem[]>([]);

// ============ Constants ============
// 动作英文key到中文标签的映射
const actionLabelMap: Record<string, string> = {
  create: '创建', update: '更新', delete: '删除',
  submit: '提交', approve: '通过', reject: '驳回',
  export: '导出', login: '登录'
};

// 动作中文标签到颜色配置的映射
const actionMap: Record<string, { key: string; color: string; bg: string; borderColor: string }> = {
  '创建': { key: 'create', color: 'var(--color-success)', bg: 'rgba(0,180,42,0.1)', borderColor: 'rgba(0,180,42,0.3)' },
  '更新': { key: 'update', color: 'var(--color-warning)', bg: 'rgba(255,193,7,0.1)', borderColor: 'rgba(255,193,7,0.3)' },
  '删除': { key: 'delete', color: 'var(--color-danger)', bg: 'rgba(245,63,63,0.1)', borderColor: 'rgba(245,63,63,0.3)' },
  '提交': { key: 'submit', color: 'var(--color-primary)', bg: 'rgba(22,119,255,0.1)', borderColor: 'rgba(22,119,255,0.3)' },
  '通过': { key: 'approve', color: 'var(--color-success)', bg: 'rgba(0,180,42,0.1)', borderColor: 'rgba(0,180,42,0.3)' },
  '驳回': { key: 'reject', color: 'var(--color-danger)', bg: 'rgba(245,63,63,0.1)', borderColor: 'rgba(245,63,63,0.3)' },
  '导出': { key: 'export', color: 'var(--color-text-secondary)', bg: 'var(--bg-hover)', borderColor: 'var(--color-border)' },
  '登录': { key: 'login', color: 'var(--color-text-secondary)', bg: 'var(--bg-hover)', borderColor: 'var(--color-border)' }
};

// 模块名称到图标名称的映射
const moduleIconMap: Record<string, string> = {
  '项目管理': 'FolderOutlined',
  '勘查审核': 'AuditOutlined',
  '表单模板': 'FormOutlined',
  '模板管理': 'FormOutlined',
  '点位管理': 'EnvironmentOutlined',
  '登录认证': 'LoginOutlined',
  '用户管理': 'LoginOutlined'
};

// 颜色数组
const colorArray = ['#1677FF', '#00B42A', '#FF7D00', '#4E5969'];

// ============ Utility Functions ============
/**
 * 格式化时间
 * @param timeStr 时间字符串
 * @returns 格式化后的时间字符串
 */
const formatTime = (timeStr: string): string => {
  if (!timeStr) return '-';

  const now = new Date();
  const time = new Date(timeStr);
  const diff = now.getTime() - time.getTime();

  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 30) return `${days}天前`;

  return time.toLocaleDateString('zh-CN');
};

/**
 * 格式化日志项
 * @param item 后端返回的日志数据
 * @returns 格式化后的日志项
 */
const formatLogItem = (item: BackendLogItem): LogItem => {
  const actionInfo = actionMap[item.action] || actionMap['创建'];
  const moduleIcon = moduleIconMap[item.module] || 'FolderOutlined';
  const operatorInitial = item.username ? item.username.charAt(0) : '?';
  const operatorColor = colorArray[item.userId % colorArray.length] || '#1677FF';

  return {
    id: item.id,
    operator: item.username || '未知',
    operatorInitial,
    operatorColor,
    role: item.role || '普通用户',
    module: item.module || '未知',
    moduleIcon,
    moduleColor: 'var(--color-primary)',
    actionType: item.action || '未知',
    actionColor: actionInfo.color,
    actionBg: actionInfo.bg,
    actionBorderColor: actionInfo.borderColor,
    actionKey: actionInfo.key,
    target: item.detail || '-',
    time: formatTime(item.createTime)
  };
};

// ============ Data Loading ============
/**
 * 加载日志数据
 */
const loadLogs = async (): Promise<void> => {
  loading.value = true;
  try {
    const params: Record<string, any> = {
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize
    };

    // 动作类型筛选
    if (activeAction.value) {
      params.keyword = actionLabelMap[activeAction.value] || activeAction.value;
    } else {
      params.keyword = filters.value.keyword || undefined;
    }

    const result = await fetchGetOperationLogPage(params);
    const data = result?.data;

    if (data) {
      const records: BackendLogItem[] = data.records || [];
      pagination.value.total = data.total || 0;
      total.value = data.total || 0;
      logData.value = records.map(item => formatLogItem(item));
    }
  } catch (error: any) {
    console.error('加载日志失败:', error);
    message.error(error?.message || '加载日志失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 加载统计数据
 */
const loadStatistics = async (): Promise<void> => {
  try {
    const result = await fetchCountByRiskLevel();
    const riskData = result?.data;
    if (riskData) {
      deleteCount.value = riskData['2'] || 0;
    }
  } catch (error: any) {
    console.error('加载统计失败:', error);
  }
};

// ============ Event Handlers ============
const handleActionFilter = (actionKey: string): void => {
  activeAction.value = actionKey;
  filters.value.keyword = '';
  pagination.value.current = 1;
  loadLogs();
};

const handleSearch = (): void => {
  activeAction.value = '';
  pagination.value.current = 1;
  loadLogs();
};

const handleReset = (): void => {
  filters.value.keyword = '';
  activeAction.value = '';
  pagination.value.current = 1;
  loadLogs();
};

const refreshData = (): void => {
  loadLogs();
  loadStatistics();
  message.success('刷新成功');
};

// ============ Lifecycle ============
onMounted(() => {
  loadLogs();
  loadStatistics();
});
</script>

<style scoped>
/* 减小表格行的内边距 */
.log-table :deep(.ant-table-cell) {
  padding: 6px 12px !important;
}

/* 表头样式 */
.log-table :deep(.ant-table-thead > tr > th) {
  background-color: var(--bg-card-alt);
  color: var(--color-text-secondary);
  font-weight: 500;
  border-bottom: 1px solid var(--color-divider);
  padding: 8px 12px !important;
  font-size: 13px;
}

/* 表格行间距 */
.log-table :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid var(--color-divider);
}

/* 鼠标悬停效果 */
.log-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: var(--bg-hover);
}

/* 分页区域优化 */
.log-table :deep(.ant-pagination) {
  margin: 12px 16px;
}

/* 优化操作对象列的文字显示 */
.log-table :deep(.ant-table-tbody .ant-table-cell[aria-label="操作对象"]) {
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
