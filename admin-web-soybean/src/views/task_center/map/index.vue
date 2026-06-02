<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getFriendlyErrorMessage } from '@/utils/error-code';
import { AimOutlined, AppstoreOutlined, EnvironmentOutlined, EyeOutlined, FilterOutlined, ReloadOutlined, SearchOutlined, UnorderedListOutlined } from '@ant-design/icons-vue';
import { fetchTaskPage } from '@/service/api/task';
import AMapComponent from '@/components/custom/amap-component.vue';

defineOptions({ name: 'TaskMap' });

const loading = ref(false);
const searchKeyword = ref('');
const statusFilter = ref('');
const projectFilter = ref<string | undefined>(undefined);

const amapRef = ref<InstanceType<typeof AMapComponent> | null>(null);
const mapCenter = ref<[number, number]>([116.397428, 39.90923]);
const mapZoom = ref(12);

// view mode not needed here – this component is map only

// pagination (optional, we fetch all for map)
const pagination = ref({
  current: 1,
  pageSize: 1000,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
});

// Table columns for potential list view (reuse from point map)
const tableColumns = [
  { title: '任务名称', dataIndex: 'taskName', key: 'taskName', width: 180, ellipsis: true },
  { title: '所属项目', dataIndex: 'projectName', key: 'projectName', width: 180 },
  { title: '创建人', dataIndex: 'ownerUserName', key: 'ownerUserName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '进度', dataIndex: 'progress', key: 'progress', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' as const }
];

// Data containers
const taskList = ref<any[]>([]);

// Computed points for map (only tasks with coordinates)
const mapPoints = computed(() => {
  return taskList.value
    .filter(t => t.longitude && t.latitude)
    .map(t => ({
      ...t,
      pointId: t.id,
      name: t.taskName,
      longitude: t.longitude,
      latitude: t.latitude,
      status: t.status
    }));
});

const loadData = async () => {
  loading.value = true;
  try {
    const { data } = await fetchTaskPage({
      current: pagination.value.current,
      size: pagination.value.pageSize,
      taskName: searchKeyword.value,
      // optional filters could be added here
    });
    taskList.value = (data.records || []).map((item: any) => ({
      ...item,
      projectName: item.projectName || (item.project && item.project.name) || '-',
      ownerUserName: item.ownerUserName || (item.owner && item.owner.username) || '-',
      // Ensure coordinate fields exist – backend should provide longitude/latitude
      longitude: item.longitude,
      latitude: item.latitude
    }));
    pagination.value.total = data.total || 0;
    // Fit map view after data loads
    setTimeout(() => {
      if (amapRef.value && mapPoints.value.length > 0) {
        amapRef.value.fitView();
      }
    }, 500);
  } catch (error: any) {
    console.error('加载任务数据错误', error);
    const friendly = getFriendlyErrorMessage(error?.response?.data?.code, '加载任务数据失败');
    message.error(friendly);
  } finally {
    loading.value = false;
  }
};

const getStatusClass = (status: number | string) => {
  const map: Record<number, string> = {
    0: 'status-tag--pending',
    1: 'status-tag--draft',
    2: 'status-tag--submitted',
    3: 'status-tag--approved',
    4: 'status-tag--rejected'
  };
  return map[Number(status)] || 'status-tag--pending';
};

const getStatusText = (status: number | string) => {
  const txt: Record<number, string> = {
    0: '待分配',
    1: '进行中',
    2: '已完成',
    3: '已逾期',
    4: '已终止'
  };
  return txt[Number(status)] || `未知(${status})`;
};

const getStatusColor = (status: number | string) => {
  const colors: Record<number, string> = {
    0: 'var(--color-warning)',
    1: 'var(--color-primary)',
    2: 'var(--color-success)',
    3: 'var(--color-danger)',
    4: 'var(--color-info)'
  };
  return colors[Number(status)] || 'var(--color-warning)';
};

const focusPoint = (point: any) => {
  if (amapRef.value && point.longitude && point.latitude) {
    const map = amapRef.value.getMap();
    if (map) {
      map.setCenter([point.longitude, point.latitude]);
      map.setZoom(16);
    }
  }
};

const viewDetail = (record: any) => {
  message.info(`任务详情: ${record.taskName}`);
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="h-full flex-col overflow-y-auto custom-scrollbar task-map-page">
    <div class="glass-bg-layer"></div>
    <div class="p-24px relative z-1">
      <!-- Header -->
      <div class="flex justify-between items-center page-header mb-24px">
        <div class="flex items-center gap-12px">
          <div class="page-header-icon">
            <EnvironmentOutlined class="text-20px" />
          </div>
          <div>
            <h1 class="text-20px font-700 text-[var(--color-text-primary)]">任务中心地图</h1>
            <p class="text-12px text-[var(--color-text-secondary)] mt-2px">任务位置分布与状态分析</p>
          </div>
        </div>
      </div>

      <div class="content-area">
        <!-- Left Panel: Task List -->
        <div class="list-panel glass-card" v-mouse-glow="{ color: '22,119,255', size: 300, intensity: 0.04 }">
          <div class="panel-header">
            <h2 class="panel-title">任务列表 <span class="panel-count">({{ taskList.length }})</span></h2>
            <AInput v-model:value="searchKeyword" placeholder="搜索任务名称/编号..." class="panel-search" allow-clear />
            <ASelect v-model:value="projectFilter" placeholder="选择项目" class="project-select" allow-clear>
              <!-- 项目选项需要自行填充，暂留 -->
            </ASelect>
            <ASelect v-model:value="statusFilter" placeholder="全部状态" class="status-select" allow-clear>
              <ASelectOption :value="0">待分配</ASelectOption>
              <ASelectOption :value="1">进行中</ASelectOption>
              <ASelectOption :value="2">已完成</ASelectOption>
              <ASelectOption :value="3">已逾期</ASelectOption>
              <ASelectOption :value="4">已终止</ASelectOption>
            </ASelect>
            <AButton @click="loadData" class="refresh-btn"><ReloadOutlined class="text-16px" /><span>刷新</span></AButton>
          </div>
          <ATable :data-source="taskList" :columns="tableColumns" :loading="loading" :row-key="(record:any)=>record.id" class="task-table" @change="() => {}">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <span class="status-tag" :class="getStatusClass(record.status)">
                  <span v-if="record.status == 3" class="status-dot status-dot--pulse"></span>
                  <span v-else class="status-dot"></span>
                  {{ getStatusText(record.status) }}
                </span>
              </template>
              <template v-else-if="column.key === 'action'">
                <div class="action-group">
                  <ATooltip title="地图定位">
                    <AButton type="text" size="small" class="action-btn" @click="() => focusPoint(record)"><AimOutlined class="text-15px" /></AButton>
                  </ATooltip>
                  <ATooltip title="详情">
                    <AButton type="text" size="small" class="action-btn" @click="() => viewDetail(record)"><EyeOutlined class="text-15px" /></AButton>
                  </ATooltip>
                </div>
              </template>
            </template>
          </ATable>
        </div>

        <!-- Right Panel: Map -->
        <div class="map-panel glass-card" v-mouse-glow="{ color: '22,119,255', size: 300, intensity: 0.04 }">
          <div class="map-header">
            <div class="map-title">任务分布地图</div>
            <div class="map-coords">中心: {{ mapCenter[0].toFixed(4) }}, {{ mapCenter[1].toFixed(4) }}</div>
          </div>
          <div class="map-area">
            <AMapComponent
              ref="amapRef"
              :points="mapPoints"
              :center="mapCenter"
              :zoom="mapZoom"
              :enable-cluster="true"
              @marker-click="(pt:any)=>focusPoint(pt)"
              @map-click="() => {}"
              class="w-full h-full"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.task-map-page {
  --bg-card: rgba(255,255,255,0.65);
  --color-primary: #1677FF;
  --color-success: #00B42A;
  --color-warning: #FF7D00;
  --color-danger: #F53F3F;
  --color-info: #0FC6C2;
}
.glass-bg-layer {
  position: fixed; inset: 0; z-index: 0; background: linear-gradient(135deg, rgba(22,119,255,0.08) 0%, rgba(22,119,255,0) 50%, rgba(16,185,129,0.05) 100%); pointer-events: none;
}
.page-header { padding: 8px 12px; margin: -8px -12px; border-radius: 8px; transition: all .3s; }
.page-header:hover { background: rgba(22,119,255,0.03); }
.page-header-icon { width: 40px; height: 40px; border-radius: 8px; display:flex; align-items:center; justify-content:center; background:rgba(22,119,255,0.1); color:var(--color-primary); transition:.25s; }
.page-header:hover .page-header-icon { background:rgba(22,119,255,0.15); transform:scale(1.05); }
.content-area { display:flex; gap:16px; flex:1; min-height:0; }
.list-panel { flex:1; display:flex; flex-direction:column; }
.panel-header { display:flex; align-items:center; gap:8px; padding:12px; border-bottom:1px solid var(--color-divider); }
.panel-title { font-size:16px; font-weight:600; }
.panel-count { margin-left:4px; color:var(--color-text-secondary); }
.map-panel { flex:2; display:flex; flex-direction:column; }
.map-header { display:flex; justify-content:space-between; padding:12px; border-bottom:1px solid var(--color-divider); }
.map-area { flex:1; }
</style>
