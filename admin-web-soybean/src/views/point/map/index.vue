<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { fetchGetPointList } from '@/service/api';
import AMapComponent from '@/components/custom/amap-component.vue';
import { getFriendlyErrorMessage } from '@/utils/error-code';

defineOptions({ name: 'PointMap' });

const loading = ref(false);
const searchKeyword = ref('');
const statusFilter = ref('');
const amapRef = ref<InstanceType<typeof AMapComponent> | null>(null);
const mapCenter = ref<[number, number]>([116.397428, 39.90923]);
const mapZoom = ref(12);

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

// Load point data
const loadData = async () => {
  loading.value = true;
  try {
    const response = await fetchGetPointList({ current: 1, size: 1000 });
    
    if (response.data) {
      pointList.value = response.data.records || [];
      console.log('[Point Map] Loaded points:', pointList.value.length);
      console.log('[Point Map] Points data:', pointList.value);
      
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

// Status color mapping
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    approved: 'var(--color-success)',
    rejected: 'var(--color-danger)',
    submitted: 'var(--color-primary)',
    pending: 'var(--color-warning)'
  };
  return colorMap[status] || 'var(--color-warning)';
};

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    approved: '已通过',
    rejected: '已驳回',
    submitted: '已提交',
    pending: '未勘查'
  };
  return textMap[status] || status;
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
  <div class="h-full flex gap-4 p-4">
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
            <a-select-option value="pending">未勘查</a-select-option>
            <a-select-option value="submitted">已提交</a-select-option>
            <a-select-option value="approved">已通过</a-select-option>
            <a-select-option value="rejected">已驳回</a-select-option>
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
        <div class="flex gap-2">
          <a-button size="small">框选点位</a-button>
          <a-button size="small" type="primary">批量派发</a-button>
        </div>
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
