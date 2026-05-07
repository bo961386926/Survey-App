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
        :data-source="logData"
        :loading="loading"
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
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { 
  ReloadOutlined, 
  SearchOutlined, 
  FolderOutlined, 
  AuditOutlined, 
  FormOutlined, 
  EnvironmentOutlined, 
  LoginOutlined 
} from '@ant-design/icons-vue';
import { getOperationLogPage, countByModule, countByRiskLevel } from '@/api/log';

// Define components for dynamic rendering
const iconComponents: Record<string, any> = {
  FolderOutlined,
  AuditOutlined,
  FormOutlined,
  EnvironmentOutlined,
  LoginOutlined
};

defineOptions({ name: 'SystemLog' });

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
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
  onChange: (page, pageSize) => {
    pagination.value.current = page;
    pagination.value.pageSize = pageSize;
    loadLogs();
  },
  onShowSizeChange: (current, size) => {
    pagination.value.current = current;
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

const logData = ref([]);

// 加载日志数据
const loadLogs = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      keyword: filters.value.keyword || undefined
    };
    
    // 如果有动作类型筛选，添加到参数中
    if (activeAction.value) {
      params.module = activeAction.value; // 临时使用module字段过滤
    }
    
    const res = await getOperationLogPage(params);
    if (res.code === 200 && res.data) {
      logData.value = res.data.records || [];
      pagination.value.total = res.data.total || 0;
      total.value = res.data.total || 0;
      
      // 格式化数据以适配前端显示
      logData.value = logData.value.map(item => formatLogItem(item));
    }
  } catch (error) {
    console.error('加载日志失败:', error);
    message.error('加载日志失败');
  } finally {
    loading.value = false;
  }
};

// 格式化日志项
const formatLogItem = (item) => {
  const actionMap = {
    '创建': { key: 'create', color: 'var(--color-success)', bg: 'rgba(0,180,42,0.1)', borderColor: 'rgba(0,180,42,0.3)' },
    '更新': { key: 'update', color: 'var(--color-warning)', bg: 'rgba(255,193,7,0.1)', borderColor: 'rgba(255,193,7,0.3)' },
    '删除': { key: 'delete', color: 'var(--color-danger)', bg: 'rgba(245,63,63,0.1)', borderColor: 'rgba(245,63,63,0.3)' },
    '提交': { key: 'submit', color: 'var(--color-primary)', bg: 'rgba(22,119,255,0.1)', borderColor: 'rgba(22,119,255,0.3)' },
    '通过': { key: 'approve', color: 'var(--color-success)', bg: 'rgba(0,180,42,0.1)', borderColor: 'rgba(0,180,42,0.3)' },
    '驳回': { key: 'reject', color: 'var(--color-danger)', bg: 'rgba(245,63,63,0.1)', borderColor: 'rgba(245,63,63,0.3)' },
    '导出': { key: 'export', color: 'var(--color-text-secondary)', bg: 'var(--bg-hover)', borderColor: 'var(--color-border)' },
    '登录': { key: 'login', color: 'var(--color-text-secondary)', bg: 'var(--bg-hover)', borderColor: 'var(--color-border)' }
  };
  
  const moduleIconMap = {
    '项目管理': 'FolderOutlined',
    '勘查审核': 'AuditOutlined',
    '表单模板': 'FormOutlined',
    '模板管理': 'FormOutlined',
    '点位管理': 'EnvironmentOutlined',
    '登录认证': 'LoginOutlined',
    '用户管理': 'LoginOutlined'
  };
  
  const actionInfo = actionMap[item.action] || actionMap['创建'];
  const moduleIcon = moduleIconMap[item.module] || 'FolderOutlined';
  
  // 获取操作人首字
  const operatorInitial = item.username ? item.username.charAt(0) : '?';
  
  // 生成随机颜色
  const colors = ['var(--color-primary)', 'var(--color-success)', 'var(--color-warning)', 'var(--color-info)'];
  const operatorColor = colors[item.userId % colors.length] || 'var(--color-primary)';
  
  return {
    id: item.id,
    operator: item.username || '未知',
    operatorInitial,
    operatorColor,
    role: '-', // 后端未返回角色信息
    module: item.module || '未知',
    moduleIcon,
    moduleColor: 'var(--color-primary)',
    actionType: item.action || '未知',
    actionColor: actionInfo.color,
    actionBg: actionInfo.bg,
    actionBorderColor: actionInfo.borderColor,
    actionKey: actionInfo.key,
    target: item.description || '-',
    time: formatTime(item.createTime)
  };
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-';
  
  const now = new Date();
  const time = new Date(timeStr);
  const diff = now - time;
  
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 30) return `${days}天前`;
  
  return time.toLocaleDateString('zh-CN');
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 加载风险等级统计
    const riskRes = await countByRiskLevel();
    if (riskRes.code === 200 && riskRes.data) {
      deleteCount.value = riskRes.data['2'] || 0; // 高风险操作数
    }
    
    // TODO: 可以从其他接口获取更多统计数据
    todayCount.value = 0; // 暂时设置为0，后续可以添加今日统计接口
  } catch (error) {
    console.error('加载统计失败:', error);
  }
};

const handleSearch = () => {
  pagination.value.current = 1;
  loadLogs();
};

const handleReset = () => {
  filters.value.keyword = '';
  filters.value.dateRange = undefined;
  activeAction.value = '';
  pagination.value.current = 1;
  loadLogs();
};

const refreshData = () => {
  loadLogs();
  loadStatistics();
  message.success('刷新成功');
};

// 组件挂载时加载数据
onMounted(() => {
  loadLogs();
  loadStatistics();
});
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
