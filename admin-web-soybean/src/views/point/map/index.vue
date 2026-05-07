<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { fetchGetPointList, fetchGetProjectList } from '@/service/api';
import AMapComponent from '@/components/custom/amap-component.vue';
import { getFriendlyErrorMessage } from '@/utils/error-code';

defineOptions({ name: 'PointMap' });

const loading = ref(false);
const searchKeyword = ref('');
const statusFilter = ref('');
const projectFilter = ref<string | undefined>(undefined);
const amapRef = ref<InstanceType<typeof AMapComponent> | null>(null);
const mapCenter = ref<[number, number]>([116.397428, 39.90923]);
const mapZoom = ref(12);

// 视图模式：'list' 列表模式 | 'map' 地图模式
const viewMode = ref<'list' | 'map'>('map');
const tabOptions = [
  { key: 'list' as const, label: '列表模式', icon: 'i-material-symbols:format-list-bulleted-rounded' },
  { key: 'map' as const, label: '地图模式', icon: 'i-material-symbols:map-outline-rounded' }
];

// 分页
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
});

// 表格列
const tableColumns = [
  { title: '点位名称', dataIndex: 'name', key: 'name', width: 180, ellipsis: true },
  { title: '编号', dataIndex: 'pointId', key: 'pointId', width: 120 },
  { title: '所属项目', dataIndex: 'projectName', key: 'projectName', width: 180, ellipsis: true },
  { title: '坐标', dataIndex: 'coordinate', key: 'coordinate', width: 220 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' as const }
];

// 项目筛选选项
const projectOptions = ref<{ label: string; value: string }[]>([]);

// Point list data
const pointList = ref<any[]>([]);

// Filtered points for map
const mapPoints = computed(() => {
  let data = [...pointList.value];
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    data = data.filter(point => 
      point.pointName?.toLowerCase().includes(keyword) ||
      point.pointCode?.toLowerCase().includes(keyword)
    );
  }
  
  if (statusFilter.value) {
    data = data.filter(point => point.status === statusFilter.value);
  }
  
  return data
    .filter(point => point.longitude && point.latitude)
    .map(point => ({
      ...point,
      pointId: point.pointId,
      name: point.pointName,
      longitude: point.longitude,
      latitude: point.latitude,
      status: point.status
    }));
});

// Stats (using numeric status: 0=待采集, 1=草稿中, 2=待审核, 3=审核通过, 4=驳回)
const pendingCount = computed(() => pointList.value.filter(p => p.status === 0).length);
const draftCount = computed(() => pointList.value.filter(p => p.status === 1).length);
const submittedCount = computed(() => pointList.value.filter(p => p.status === 2).length);
const approvedCount = computed(() => pointList.value.filter(p => p.status === 3).length);
const rejectedCount = computed(() => pointList.value.filter(p => p.status === 4).length);

// 列表模式下的表格数据（带分页+筛选）
const tablePoints = computed(() => {
  let data = [...pointList.value];
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase();
    data = data.filter(p => p.pointName?.toLowerCase().includes(kw) || p.pointCode?.toLowerCase().includes(kw));
  }
  if (statusFilter.value) {
    data = data.filter(p => p.status === statusFilter.value);
  }
  if (projectFilter.value) {
    data = data.filter(p => p.projectName === projectFilter.value);
  }
  // Map to table format
  return data.map(p => ({
    ...p,
    name: p.pointName || p.name,
    coordinate: p.longitude && p.latitude ? `${p.longitude.toFixed(6)}, ${p.latitude.toFixed(6)}` : '-'
  }));
});

const getStatusTagColor = (status: number | string) => {
  const map: Record<string, string> = {
    0: 'default',    // 待采集
    1: 'gold',       // 草稿中
    2: 'blue',       // 待审核
    3: 'green',      // 审核通过
    4: 'red',        // 驳回待修改
    5: 'purple',     // 已归档
    6: 'gray'       // 作废
  };
  return map[status] ?? 'default';
};

const switchToMap = (point: any) => {
  viewMode.value = 'map';
  focusPoint(point);
};

const viewDetail = (point: any) => {
  message.info(`点位详情: ${point.name}`);
};

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
};

// Load point data
const loadData = async () => {
  loading.value = true;
  try {
    const response = await fetchGetPointList({ current: 1, size: 1000 });
    
    if (response.data) {
      pointList.value = response.data.records || [];
      console.log('[Point Map] Loaded points:', pointList.value.length);
      console.log('[Point Map] Points data:', pointList.value);
      
      // Update project options from loaded data
      updateProjectOptions();
      
      // Auto-fit map when data loads
      setTimeout(() => {
        if (amapRef.value && mapPoints.value.length > 0) {
          amapRef.value.fitView();
        }
      }, 1000);
    } else {
      console.log('[Point Map] No data returned from API');
    }
  } catch (error: any) {
    console.error('[Point Map] Failed to load points:', error);
    const friendlyMsg = getFriendlyErrorMessage(
      error?.response?.data?.code,
      '加载点位数据失败'
    );
    message.error(friendlyMsg);
  } finally {
    loading.value = false;
  }
};

// Update project filter options from loaded point data
const updateProjectOptions = () => {
  const projectMap = new Map<string, string>();
  pointList.value.forEach(point => {
    if (point.projectId && point.projectName) {
      projectMap.set(String(point.projectId), point.projectName);
    }
  });
  projectOptions.value = Array.from(projectMap.entries()).map(([value, label]) => ({
    value,
    label
  }));
};

// Status color mapping
const getStatusColor = (status: number | string) => {
  const colorMap: Record<string, string> = {
    0: 'var(--color-warning)',  // 待采集
    1: 'var(--color-info)',     // 草稿中
    2: 'var(--color-primary)',  // 待审核
    3: 'var(--color-success)', // 审核通过
    4: 'var(--color-danger)',  // 驳回待修改
    5: 'var(--color-secondary)', // 已归档
    6: 'var(--color-disabled)'  // 作废
  };
  return colorMap[status] || 'var(--color-warning)';
};

const getStatusText = (status: number | string) => {
  const textMap: Record<string, string> = {
    0: '待采集',
    1: '草稿中',
    2: '待审核',
    3: '已通过',
    4: '已驳回',
    5: '已归档',
    6: '已作废'
  };
  return textMap[status] || `未知(${status})`;
};

// Focus point on map
const focusPoint = (point: any) => {
  if (amapRef.value && point.longitude && point.latitude) {
    const map = amapRef.value.getMap();
    if (map) {
      map.setCenter([point.longitude, point.latitude]);
      map.setZoom(16);
    }
  }
};

// Map event handlers
const handleMarkerClick = (point: any) => {
  message.info(`点位: ${point.name}`);
};

const handleMapClick = (lnglat: { lng: number; lat: number }) => {
  console.log('Map clicked at:', lnglat);
};

const handleMapReady = (map: any) => {
  console.log('AMap is ready');
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="h-full flex flex-col p-4 pt-3">
    <!-- Mode Switcher Tabs -->
    <div class="flex items-center gap-2 mb-3 px-1">
      <div class="flex bg-[var(--bg-page)] rd-8px p-2px border border-[var(--color-border)]">
        <button
          v-for="tab in tabOptions"
          :key="tab.key"
          class="flex items-center gap-1.5 px-4 py-1.5 text-13px rd-6px border-none cursor-pointer transition-all"
          :class="viewMode === tab.key
            ? 'bg-white text-primary font-500 shadow-sm'
            : 'bg-transparent text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]'"
          @click="viewMode = tab.key"
        >
          <div :class="tab.icon" class="text-16px"></div>
          {{ tab.label }}
        </button>
      </div>
      <span class="text-12px text-[var(--color-text-placeholder)]">
        {{ mapPoints.length }} 个点位
      </span>
    </div>

    <!-- Content Area -->
    <div class="flex-1 flex gap-4 overflow-hidden">
      <!-- ==================== 列表模式 ==================== -->
      <template v-if="viewMode === 'list'">
        <div class="w-full flex overflow-hidden">
          <div class="list-panel-table flex-1 flex flex-col overflow-hidden">
            <!-- Stats Cards -->
            <div class="grid grid-cols-5 gap-4 mb-4 flex-shrink-0">
              <div class="stat-card bg-[var(--bg-card)] rd-8px p-4 border-l-4 border-l-primary shadow-sm">
                <div class="text-12px text-[var(--color-text-secondary)]">总点位数</div>
                <div class="text-28px font-700 mt-1" style="color: var(--color-primary)">{{ pointList.length }}</div>
              </div>
              <div class="stat-card bg-[var(--bg-card)] rd-8px p-4 border-l-4 border-l-warning shadow-sm">
                <div class="text-12px text-[var(--color-text-secondary)]">待采集</div>
                <div class="text-28px font-700 mt-1" style="color: var(--color-warning)">{{ pendingCount }}</div>
              </div>
              <div class="stat-card bg-[var(--bg-card)] rd-8px p-4 border-l-4 border-l-info shadow-sm">
                <div class="text-12px text-[var(--color-text-secondary)]">待审核</div>
                <div class="text-28px font-700 mt-1" style="color: var(--color-info)">{{ submittedCount }}</div>
              </div>
              <div class="stat-card bg-[var(--bg-card)] rd-8px p-4 border-l-4 border-l-success shadow-sm">
                <div class="text-12px text-[var(--color-text-secondary)]">已通过</div>
                <div class="text-28px font-700 mt-1" style="color: var(--color-success)">{{ approvedCount }}</div>
              </div>
              <div class="stat-card bg-[var(--bg-card)] rd-8px p-4 border-l-4 border-l-danger shadow-sm">
                <div class="text-12px text-[var(--color-text-secondary)]">已驳回</div>
                <div class="text-28px font-700 mt-1" style="color: var(--color-danger)">{{ rejectedCount }}</div>
              </div>
            </div>

            <!-- Filter Bar -->
            <div class="flex items-center gap-3 mb-4 flex-shrink-0">
              <a-select
                v-model:value="projectFilter"
                placeholder="选择项目"
                class="w-200px"
                size="small"
                allow-clear
              >
                <a-select-option v-for="p in projectOptions" :key="p.value" :value="p.value">{{ p.label }}</a-select-option>
              </a-select>
              <a-input-search
                v-model:value="searchKeyword"
                placeholder="搜索点位编号/名称"
                class="w-280px"
                size="small"
                allow-clear
              />
              <a-select
                v-model:value="statusFilter"
                placeholder="全部状态"
                class="w-140px"
                size="small"
                allow-clear
              >
                <a-select-option value="">全部状态</a-select-option>
                <a-select-option :value="0">待采集</a-select-option>
                <a-select-option :value="1">草稿中</a-select-option>
                <a-select-option :value="2">待审核</a-select-option>
                <a-select-option :value="3">已通过</a-select-option>
                <a-select-option :value="4">已驳回</a-select-option>
                <a-select-option :value="5">已归档</a-select-option>
                <a-select-option :value="6">已作废</a-select-option>
              </a-select>
              <a-button size="small" class="ml-auto" @click="loadData">刷新</a-button>
            </div>

            <!-- Table -->
            <a-table
              :data-source="tablePoints"
              :columns="tableColumns"
              :loading="loading"
              :pagination="pagination"
              :row-key="(record: any) => record.pointId"
              size="small"
              class="flex-1"
              @change="handleTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="getStatusTagColor(record.status)">{{ getStatusText(record.status) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <div class="flex gap-2">
                    <a-button type="link" size="small" class="p-0 text-12px" @click="switchToMap(record)">地图查看</a-button>
                    <a-divider type="vertical" />
                    <a-button type="link" size="small" class="p-0 text-12px" @click="viewDetail(record)">详情</a-button>
                  </div>
                </template>
              </template>
              <template #emptyText>
                <div class="py-8 text-center text-[var(--color-text-secondary)]">
                  {{ pointList.length === 0 ? '暂无点位数据' : '暂无匹配的点位' }}
                </div>
              </template>
            </a-table>
          </div>
        </div>
      </template>

      <!-- ==================== 地图模式 ==================== -->
      <template v-if="viewMode === 'map'">
        <!-- Left Panel: Point List -->
        <div class="list-panel flex flex-col">
          <div class="panel-header mb-4">
            <h2 class="text-16px font-600 m-0 title-text">
              点位列表
              <span class="text-12px text-[var(--color-text-secondary)] font-normal">({{ mapPoints.length }})</span>
            </h2>

            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索点位编号/名称"
              class="mt-3"
              allow-clear
            />

            <div class="flex gap-2 mt-3">
              <a-select
                v-model:value="statusFilter"
                placeholder="全部状态"
                class="flex-1"
                size="small"
                allow-clear
              >
                <a-select-option value="">全部状态</a-select-option>
                <a-select-option :value="0">待采集</a-select-option>
                <a-select-option :value="1">草稿中</a-select-option>
                <a-select-option :value="2">待审核</a-select-option>
                <a-select-option :value="3">已通过</a-select-option>
                <a-select-option :value="4">已驳回</a-select-option>
                <a-select-option :value="5">已归档</a-select-option>
                <a-select-option :value="6">已作废</a-select-option>
              </a-select>
            </div>
          </div>

          <!-- List Content -->
          <div class="list-content flex-1 overflow-y-auto pr-2 flex flex-col gap-3">
            <div
              v-for="point in mapPoints"
              :key="point.pointId"
              class="point-card"
              @click="focusPoint(point)"
            >
              <div class="flex justify-between items-start">
                <div class="font-500 title-text">{{ point.name }}</div>
                <div class="status-indicator" :style="{ backgroundColor: getStatusColor(point.status) }"></div>
              </div>
              <div class="text-12px desc-text mt-1">编号: {{ point.pointId }}</div>
              <div class="text-12px desc-text mt-1">类型: {{ (point as any).type || '-' }}</div>
              <div class="text-11px font-mono desc-text mt-2">{{ point.longitude }}, {{ point.latitude }}</div>
              <div class="mt-2 flex justify-end">
                <a-button
                  type="link"
                  size="small"
                  class="p-0 text-12px"
                  @click.stop="focusPoint(point)"
                >
                  定位地图
                </a-button>
              </div>
            </div>

            <div v-if="mapPoints.length === 0" class="text-center py-8 desc-text">
              {{ pointList.length === 0 ? '暂无点位数据' : '暂无匹配的点位' }}
            </div>
          </div>
        </div>

        <!-- Right Panel: Map Container -->
        <div class="map-panel flex-1 relative flex flex-col">
          <div class="map-header">
            <div class="font-500 title-text">青泓项目勘察地图</div>
          </div>

          <!-- Map Area with AMap -->
          <div class="map-area flex-1 relative">
            <AMapComponent
              ref="amapRef"
              :points="mapPoints"
              :center="mapCenter"
              :zoom="mapZoom"
              :enable-cluster="true"
              @marker-click="handleMarkerClick"
              @map-click="handleMapClick"
              @map-ready="handleMapReady"
              class="w-full h-full"
            />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.list-panel {
  width: 320px;
  background-color: var(--bg-card);
  border-radius: 8px;
  padding: 16px;
  box-shadow: var(--shadow-card);
}

.map-panel {
  background-color: var(--bg-card);
  border-radius: 8px;
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.map-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-divider);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--bg-card);
  z-index: 20;
}

.map-area {
  background-color: var(--bg-page);
  position: relative;
  min-height: 500px;
  height: 100%;
}

.map-grid {
  background-image: 
    linear-gradient(to right, var(--color-divider) 1px, transparent 1px),
    linear-gradient(to bottom, var(--color-divider) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.5;
}

.point-card {
  padding: 12px;
  border-radius: 6px;
  border: 1px solid var(--color-divider);
  background-color: var(--bg-card-alt);
  cursor: pointer;
  transition: all 0.2s ease;
}

.point-card:hover {
  border-color: var(--color-primary);
  background-color: var(--bg-hover);
}

.title-text {
  color: var(--color-text-primary);
}

.desc-text {
  color: var(--color-text-secondary);
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.bg-success { background-color: var(--color-success); }
.bg-danger { background-color: var(--color-danger); }
.bg-warning { background-color: var(--color-warning); }

/* Scrollbar styling for list */
.list-content::-webkit-scrollbar {
  width: 6px;
}
.list-content::-webkit-scrollbar-thumb {
  background-color: var(--color-text-disabled);
  border-radius: 3px;
}
</style>
