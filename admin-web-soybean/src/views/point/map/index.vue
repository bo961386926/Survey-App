<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import { getFriendlyErrorMessage } from '@/utils/error-code';
import {
  AimOutlined,
  AppstoreOutlined,
  EnvironmentOutlined,
  EyeOutlined,
  FilterOutlined,
  ReloadOutlined,
  SearchOutlined,
  UnorderedListOutlined
} from '@ant-design/icons-vue';
import { fetchGetPointList } from '@/service/api';
import AMapComponent from '@/components/custom/amap-component.vue';

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
  { title: '编号', dataIndex: 'pointCode', key: 'pointCode', width: 120 },
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
const pendingCount = computed(() => pointList.value.filter(p => Number(p.status) === 0).length);
const draftCount = computed(() => pointList.value.filter(p => Number(p.status) === 1).length);
const submittedCount = computed(() => pointList.value.filter(p => Number(p.status) === 2).length);
const approvedCount = computed(() => pointList.value.filter(p => Number(p.status) === 3).length);

// 列表模式下的表格数据（带分页+筛选）
const tablePoints = computed(() => {
  let data = [...pointList.value];
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase();
    data = data.filter(p => p.pointName?.toLowerCase().includes(kw) || p.pointCode?.toLowerCase().includes(kw));
  }
  if (statusFilter.value) {
    data = data.filter(p => Number(p.status) === Number(statusFilter.value));
  }
  if (projectFilter.value) {
    data = data.filter(p => p.projectName === projectFilter.value);
  }
  // Map to table format - 确保所有字段正确映射
  return data.map(p => ({
    ...p,
    name: p.pointName || p.name || '-',
    pointCode: p.pointCode || p.pointId || p.code || '-',
    projectName: p.projectName || p.project?.name || p.project?.projectName || '-',
    coordinate: p.longitude && p.latitude ? `${p.longitude.toFixed(6)}, ${p.latitude.toFixed(6)}` : '-',
    status: p.status,
    createTime: p.createTime || p.createAt || '-'
  }));
});

const getStatusClass = (status: number | string) => {
  const statusMap: Record<number, string> = {
    0: 'status-tag--pending',
    1: 'status-tag--draft',
    2: 'status-tag--submitted',
    3: 'status-tag--approved',
    4: 'status-tag--rejected',
    5: 'status-tag--archived',
    6: 'status-tag--void'
  };
  return statusMap[Number(status)] || 'status-tag--pending';
};

const getStatusText = (status: number | string) => {
  const textMap: Record<number, string> = {
    0: '待采集',
    1: '草稿中',
    2: '待审核',
    3: '已通过',
    4: '已驳回',
    5: '已归档',
    6: '已作废'
  };
  return textMap[Number(status)] || `未知(${status})`;
};

// 状态映射辅助函数
const iconMap: Record<string, any> = {
  pending: FilterOutlined,
  draft: FilterOutlined,
  submitted: FilterOutlined,
  approved: FilterOutlined
};

const metrics = computed(() => [
  { title: '总点位数', value: pointList.value.length, icon: EnvironmentOutlined, color: 'var(--color-primary)' },
  { title: '待采集', value: pendingCount.value, icon: iconMap.pending, color: 'var(--color-warning)' },
  { title: '草稿中', value: draftCount.value, icon: iconMap.draft, color: 'var(--color-info)' },
  { title: '待审核', value: submittedCount.value, icon: iconMap.submitted, color: 'var(--color-primary)' },
  { title: '已通过', value: approvedCount.value, icon: iconMap.approved, color: 'var(--color-success)' }
]);

// Update project filter options from loaded point data
const updateProjectOptions = () => {
  const projectSet = new Set<string>();
  pointList.value.forEach(point => {
    // 支持多种字段名
    const projectName = point.projectName || point.project?.name || point.project?.projectName;
    if (projectName) {
      projectSet.add(projectName);
    }
  });
  projectOptions.value = Array.from(projectSet).map(name => ({
    value: name,
    label: name
  }));
  console.log('Project options:', projectOptions.value);
};

// Load point data
const loadData = async () => {
  loading.value = true;
  try {
    const response = await fetchGetPointList({ current: 1, size: 1000 });
    console.log('Point list response:', response);

    if (response.data) {
      const records = response.data.records || [];
      // 标准化数据
      pointList.value = records.map((item: any) => ({
        ...item,
        pointName: item.pointName || item.name,
        pointCode: item.pointCode || item.pointId || item.code,
        projectName: item.projectName || item.project?.name || item.project?.projectName,
        longitude: item.longitude || item.lng,
        latitude: item.latitude || item.lat
      }));

      // Update project options from loaded data
      updateProjectOptions();

      // Auto-fit map when data loads
      setTimeout(() => {
        if (amapRef.value && mapPoints.value.length > 0) {
          amapRef.value.fitView();
        }
      }, 1000);
    }
  } catch (error: any) {
    console.error('Load data error:', error);
    const friendlyMsg = getFriendlyErrorMessage(
      error?.response?.data?.code,
      '加载点位数据失败'
    );
    message.error(friendlyMsg);
  } finally {
    loading.value = false;
  }
};

// Status color mapping
const getStatusColor = (status: number | string) => {
  const colorMap: Record<string, string> = {
    0: 'var(--color-warning)',
    1: 'var(--color-info)',
    2: 'var(--color-primary)',
    3: 'var(--color-success)',
    4: 'var(--color-danger)',
    5: 'var(--color-secondary)',
    6: 'var(--color-text-placeholder)'
  };
  return colorMap[status] || 'var(--color-warning)';
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

const switchToMap = (point: any) => {
  viewMode.value = 'map';
  focusPoint(point);
};

const viewDetail = (point: any) => {
  message.info(`点位详情: ${point.name}`);
};

const handleSearch = () => {
  // 前端过滤，无需重新加载数据
};

const handleFilterChange = () => {
  // 前端过滤，无需重新加载数据
};

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
};

// Map event handlers
const handleMarkerClick = (point: any) => {
  message.info(`点位: ${point.name}`);
};

const handleMapClick = (lnglat: { lng: number; lat: number }) => {
  console.log('Map clicked at:', lnglat);
};

const handleMapReady = (_map: any) => {
  console.log('AMap is ready');
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="h-full flex-col overflow-y-auto custom-scrollbar point-map-page">
    <!-- 毛玻璃背景层 -->
    <div class="glass-bg-layer"></div>

    <div class="p-24px relative z-1">
      <!-- Page Header -->
      <div class="flex justify-between items-center page-header mb-24px">
        <div class="flex items-center gap-12px">
          <div class="page-header-icon">
            <EnvironmentOutlined class="text-20px" />
          </div>
          <div>
            <h1 class="text-20px font-700 text-[var(--color-text-primary)]">GIS 一张图</h1>
            <p class="text-12px text-[var(--color-text-secondary)] mt-2px">项目点位分布与空间分析</p>
          </div>
        </div>

        <!-- View Mode Switcher -->
        <div class="view-switcher">
          <button
            :class="['switch-btn', { 'switch-btn--active': viewMode === 'list' }]"
            @click="viewMode = 'list'"
          >
            <UnorderedListOutlined class="text-16px" />
            <span>列表模式</span>
          </button>
          <button
            :class="['switch-btn', { 'switch-btn--active': viewMode === 'map' }]"
            @click="viewMode = 'map'"
          >
            <AppstoreOutlined class="text-16px" />
            <span>地图模式</span>
          </button>
        </div>
      </div>

      <!-- Stats Bar -->
      <div class="metrics-grid mb-24px">
        <div
          v-for="item in metrics"
          :key="item.title"
          class="metric-card group"
        >
          <div class="flex items-center justify-between mb-8px">
            <span class="text-12px text-[var(--color-text-secondary)]">{{ item.title }}</span>
            <div class="metric-icon-wrapper" :style="{ color: item.color }">
              <component :is="item.icon" class="metric-icon" />
            </div>
          </div>
          <div class="metric-value">{{ item.value.toLocaleString() }}</div>
          <!-- 底部装饰线 -->
          <div class="metric-line" :style="{ background: `linear-gradient(90deg, transparent, ${item.color}40, transparent)` }"></div>
        </div>
      </div>

      <!-- Content Area -->
      <div class="content-area">
        <!-- ==================== 列表模式 ==================== -->
        <div v-if="viewMode === 'list'" class="list-panel-wrapper glass-card" v-mouse-glow="{ color: '22,119,255', size: 300, intensity: 0.04 }">
          <!-- Filter Bar -->
          <div class="filter-bar">
            <ASelect v-model:value="projectFilter" placeholder="选择项目" class="project-select" allow-clear @change="handleFilterChange">
              <ASelectOption v-for="p in projectOptions" :key="p.value" :value="p.value">{{ p.label }}</ASelectOption>
            </ASelect>
            <AInput v-model:value="searchKeyword" placeholder="搜索点位编号/名称..." class="search-input" @pressEnter="handleSearch" allow-clear>
              <template #prefix>
                <SearchOutlined class="text-14px text-[var(--color-text-placeholder)]" />
              </template>
            </AInput>
            <ASelect v-model:value="statusFilter" placeholder="全部状态" class="status-select" allow-clear @change="handleFilterChange">
              <ASelectOption :value="0">待采集</ASelectOption>
              <ASelectOption :value="1">草稿中</ASelectOption>
              <ASelectOption :value="2">待审核</ASelectOption>
              <ASelectOption :value="3">已通过</ASelectOption>
              <ASelectOption :value="4">已驳回</ASelectOption>
              <ASelectOption :value="5">已归档</ASelectOption>
              <ASelectOption :value="6">已作废</ASelectOption>
            </ASelect>
            <AButton @click="loadData" class="refresh-btn">
              <ReloadOutlined class="text-16px" />
              <span>刷新</span>
            </AButton>
          </div>

          <!-- Table -->
          <ATable
            :data-source="tablePoints"
            :columns="tableColumns"
            :loading="loading"
            :pagination="pagination"
            :row-key="(record: any) => record.pointId"
            class="point-table"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <span
                  class="status-tag"
                  :class="getStatusClass(record.status)"
                >
                  <span v-if="record.status == 3" class="status-dot status-dot--pulse"></span>
                  <span v-else class="status-dot"></span>
                  {{ getStatusText(record.status) }}
                </span>
              </template>
              <template v-else-if="column.key === 'action'">
                <div class="action-group">
                  <ATooltip title="地图查看">
                    <AButton type="text" size="small" class="action-btn" @click="switchToMap(record)">
                      <AppstoreOutlined class="text-15px" />
                    </AButton>
                  </ATooltip>
                  <ATooltip title="详情">
                    <AButton type="text" size="small" class="action-btn" @click="viewDetail(record)">
                      <EyeOutlined class="text-15px" />
                    </AButton>
                  </ATooltip>
                </div>
              </template>
            </template>
          </ATable>
        </div>

        <!-- ==================== 地图模式 ==================== -->
        <div v-if="viewMode === 'map'" class="map-mode-wrapper">
          <!-- Left Panel: Point List -->
          <div class="list-panel glass-card" v-mouse-glow="{ color: '22,119,255', size: 300, intensity: 0.04 }">
            <div class="panel-header">
              <h2 class="panel-title">
                点位列表
                <span class="panel-count">({{ mapPoints.length }})</span>
              </h2>
              <AInput v-model:value="searchKeyword" placeholder="搜索点位编号/名称..." class="panel-search" allow-clear>
                <template #prefix>
                  <SearchOutlined class="text-14px text-[var(--color-text-placeholder)]" />
                </template>
              </AInput>
              <ASelect
                v-model:value="statusFilter"
                placeholder="全部状态"
                class="panel-filter"
                allow-clear
              >
                <ASelectOption :value="0">待采集</ASelectOption>
                <ASelectOption :value="1">草稿中</ASelectOption>
                <ASelectOption :value="2">待审核</ASelectOption>
                <ASelectOption :value="3">已通过</ASelectOption>
                <ASelectOption :value="4">已驳回</ASelectOption>
                <ASelectOption :value="5">已归档</ASelectOption>
                <ASelectOption :value="6">已作废</ASelectOption>
              </ASelect>
            </div>

            <!-- List Content -->
            <div class="list-content">
              <div
                v-for="point in mapPoints"
                :key="point.pointId"
                class="point-item"
                @click="focusPoint(point)"
              >
                <div class="point-item-header">
                  <div class="point-item-name">{{ point.name }}</div>
                  <div class="status-dot-wrapper">
                    <span class="status-dot" :style="{ backgroundColor: getStatusColor(point.status) }"></span>
                  </div>
                </div>
                <div class="point-item-code">编号: {{ point.pointId }}</div>
                <div class="point-item-coords">{{ point.longitude }}, {{ point.latitude }}</div>
                <div class="point-item-action">
                  <AButton type="text" size="small" class="locate-btn" @click.stop="focusPoint(point)">
                    <AimOutlined class="text-14px" />
                    <span>定位</span>
                  </AButton>
                </div>
              </div>

              <div v-if="mapPoints.length === 0" class="empty-state">
                <EnvironmentOutlined class="empty-icon text-40px text-[var(--color-text-placeholder)] mb-12px" />
                <div class="empty-text">{{ pointList.length === 0 ? '暂无点位数据' : '暂无匹配的点位' }}</div>
              </div>
            </div>
          </div>

          <!-- Right Panel: Map Container -->
          <div class="map-panel glass-card" v-mouse-glow="{ color: '22,119,255', size: 300, intensity: 0.04 }">
            <div class="map-header">
              <div class="map-title">青泓项目勘察地图</div>
              <div class="map-coords">中心: {{ mapCenter[0].toFixed(4) }}, {{ mapCenter[1].toFixed(4) }}</div>
            </div>
            <div class="map-area">
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
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ===== 页面整体 ===== */
.point-map-page {
  --bg-card: rgba(255, 255, 255, 0.65);
  --bg-card-alt: rgba(247, 248, 250, 0.6);
  --color-divider: #E5E6EB;
  --color-text-primary: #1D2129;
  --color-text-secondary: #4E5969;
  --color-text-placeholder: #86909C;
  --color-primary: #1677FF;
  --color-success: #00B42A;
  --color-warning: #FF7D00;
  --color-danger: #F53F3F;
  --color-info: #0FC6C2;
  --color-secondary: #86909C;
}

/* 毛玻璃背景 */
.glass-bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  background: linear-gradient(135deg,
    rgba(22, 119, 255, 0.08) 0%,
    rgba(22, 119, 255, 0) 50%,
    rgba(16, 185, 129, 0.05) 100%);
  pointer-events: none;
}

/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--color-divider);
  border-radius: 10px;
  transition: background-color 0.2s ease;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-placeholder);
}

/* ===== 页面头部 ===== */
.page-header {
  padding: 8px 12px;
  margin: -8px -12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.page-header:hover {
  background: rgba(22, 119, 255, 0.03);
}

.page-header-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(22, 119, 255, 0.1);
  color: var(--color-primary);
  transition: all 0.25s ease;
}

.page-header:hover .page-header-icon {
  background: rgba(22, 119, 255, 0.15);
  transform: scale(1.05);
}

/* 视图切换器 */
.view-switcher {
  display: flex;
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  padding: 4px;
  gap: 4px;
}

.switch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.switch-btn:hover {
  background: rgba(22, 119, 255, 0.05);
  color: var(--color-text-primary);
}

.switch-btn--active {
  background: var(--color-primary);
  color: white;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.25);
}

/* ===== 指标卡片 ===== */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.metric-card {
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  padding: 8px 12px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.metric-card:hover {
  border-color: rgba(22, 119, 255, 0.25);
  box-shadow: 0 8px 32px rgba(22, 119, 255, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.metric-card:active {
  transform: translateY(0);
  transition-duration: 0.1s;
}

.metric-card .flex {
  margin-bottom: 2px;
}

.metric-icon-wrapper {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(22, 119, 255, 0.1);
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.metric-card:hover .metric-icon-wrapper {
  transform: scale(1.1);
}

.metric-icon {
  font-size: 13px;
}

.metric-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: -0.5px;
  transition: transform 0.3s ease;
  line-height: 1.2;
}

.metric-card:hover .metric-value {
  transform: translateX(4px);
}

.metric-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.metric-card:hover .metric-line {
  opacity: 1;
}

/* ===== 内容区域 ===== */
.content-area {
  display: flex;
  gap: 16px;
  min-height: 0;
  flex: 1;
}

/* ===== 毛玻璃卡片 ===== */
.glass-card {
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card:hover {
  border-color: rgba(22, 119, 255, 0.25);
  box-shadow: 0 8px 32px rgba(22, 119, 255, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* ===== 列表模式面板 ===== */
.list-panel-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* ===== 筛选栏 ===== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--color-divider);
}

.project-select {
  width: 180px;
}

.project-select :deep(.ant-select-selector) {
  border-radius: 4px;
  height: 32px;
}

.search-input {
  width: 280px;
}

.search-input :deep(.ant-input-affix-wrapper) {
  border-radius: 4px;
  height: 32px !important;
  min-height: 32px !important;
}

.search-input :deep(.ant-input-affix-wrapper .ant-input) {
  height: 30px !important;
  line-height: 30px !important;
}

.search-input :deep(.ant-input-affix-wrapper .ant-input-prefix) {
  margin-right: 6px;
  display: flex;
  align-items: center;
}

.status-select {
  width: 140px;
}

.status-select :deep(.ant-select-selector) {
  border-radius: 4px;
  height: 32px;
}

.refresh-btn {
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.refresh-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
}

/* ===== 表格样式 ===== */
.point-table {
  flex: 1;
  min-height: 0;
}

.point-table :deep(.ant-table-thead > tr > th) {
  background-color: rgba(247, 248, 250, 0.8) !important;
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 600;
  padding: 12px 20px !important;
  border-bottom: 1px solid var(--color-divider) !important;
}

.point-table :deep(.ant-table-tbody > tr > td) {
  padding: 14px 20px !important;
  border-bottom: 1px solid var(--color-divider) !important;
  transition: all 0.2s ease;
}

.point-table :deep(.ant-table-tbody > tr) {
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.point-table :deep(.ant-table-tbody > tr:hover) {
  background: rgba(22, 119, 255, 0.03);
}

.point-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: transparent !important;
}

.point-table :deep(.ant-table-pagination) {
  padding: 12px 20px !important;
  margin: 0 !important;
  border-top: 1px solid var(--color-divider);
}

/* ===== 状态标签 ===== */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 100px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.25s ease;
}

.status-tag:hover {
  transform: scale(1.05);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-dot--pulse {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-tag--pending {
  background: rgba(255, 125, 0, 0.1);
  color: var(--color-warning);
}

.status-tag--draft {
  background: rgba(15, 198, 194, 0.1);
  color: var(--color-info);
}

.status-tag--submitted {
  background: rgba(22, 119, 255, 0.1);
  color: var(--color-primary);
}

.status-tag--approved {
  background: rgba(0, 180, 42, 0.1);
  color: var(--color-success);
}

.status-tag--rejected {
  background: rgba(245, 63, 63, 0.1);
  color: var(--color-danger);
}

.status-tag--archived {
  background: rgba(134, 144, 156, 0.1);
  color: var(--color-secondary);
}

.status-tag--void {
  background: rgba(134, 144, 156, 0.1);
  color: var(--color-text-placeholder);
}

/* ===== 操作按钮组 ===== */
.action-group {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}

.action-btn {
  padding: 0 !important;
  height: auto !important;
  width: 28px !important;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-btn:hover {
  transform: scale(1.1);
  background: rgba(22, 119, 255, 0.08) !important;
}

.action-btn:active {
  transform: scale(0.95);
}

/* ===== 地图模式布局 ===== */
.map-mode-wrapper {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

/* ===== 左侧列表面板 ===== */
.list-panel {
  width: 340px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-divider);
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.panel-count {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

.panel-search {
  margin-top: 12px;
}

.panel-search :deep(.ant-input) {
  border-radius: 4px;
  height: 32px;
}

.panel-filter {
  margin-top: 12px;
  width: 100%;
}

.panel-filter :deep(.ant-select-selector) {
  border-radius: 4px;
  height: 32px;
}

/* 列表内容 */
.list-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.list-content::-webkit-scrollbar {
  width: 6px;
}

.list-content::-webkit-scrollbar-thumb {
  background: var(--color-divider);
  border-radius: 3px;
}

.list-content::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-placeholder);
}

/* 点位卡片 */
.point-item {
  padding: 12px 16px;
  border-radius: 6px;
  border: 1px solid var(--color-divider);
  background: rgba(247, 248, 250, 0.5);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 8px;
}

.point-item:hover {
  border-color: rgba(22, 119, 255, 0.25);
  background: rgba(22, 119, 255, 0.03);
  transform: translateX(4px);
}

.point-item:active {
  transform: translateX(2px);
  transition-duration: 0.1s;
}

.point-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.point-item-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.status-dot-wrapper {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.point-item-code {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.point-item-coords {
  font-size: 11px;
  font-family: 'SF Mono', 'Menlo', monospace;
  color: var(--color-text-placeholder);
  margin-top: 4px;
}

.point-item-action {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.locate-btn {
  height: 24px;
  padding: 0 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-primary);
  font-size: 12px;
  transition: all 0.2s ease;
}

.locate-btn:hover {
  background: rgba(22, 119, 255, 0.08);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--color-text-secondary);
}

.empty-text {
  font-size: 13px;
}

/* ===== 右侧地图面板 ===== */
.map-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.map-header {
  padding: 12px 20px;
  border-bottom: 1px solid var(--color-divider);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.map-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.map-coords {
  font-size: 12px;
  font-family: 'SF Mono', 'Menlo', monospace;
  color: var(--color-text-placeholder);
}

.map-area {
  flex: 1;
  min-height: 400px;
  position: relative;
  background: #f0f0f0;
}
</style>
