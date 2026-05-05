<template>
  <div class="amap-container" ref="mapContainerRef">
    <!-- Loading State -->
    <div v-if="loading" class="amap-loading">
      <div class="amap-loading-content">
        <div class="amap-loading-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M24 4L4 14v20l20 10 20-10V14L24 4z" stroke="currentColor" stroke-width="2" fill="none"/>
            <path d="M4 14l20 10 20-10M24 24v20" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="amap-loading-title">地图视图组件加载区</div>
        <div class="amap-loading-desc">这里将接入高德地图SDK，渲染 `PointLayer` 和交互逻辑</div>
        <a-spin size="large" class="mt-4" />
      </div>
    </div>
    
    <!-- Error State -->
    <div v-if="error" class="amap-error">
      <a-result status="error" title="地图加载失败" :sub-title="error">
        <template #extra>
          <a-button type="primary" @click="handleRetry">重试</a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup lang="ts">
/// <reference types="@amap/amap-jsapi-types" />
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import { message } from 'ant-design-vue';
import { loadAMap } from '@/utils/amap-loader';

defineOptions({ name: 'AMapComponent' });

interface PointMarker {
  pointId: number | string;
  name: string;
  longitude: number;
  latitude: number;
  status?: string;
  [key: string]: any;
}

interface Props {
  points?: PointMarker[];
  center?: [number, number];
  zoom?: number;
  enableCluster?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  points: () => [],
  center: () => [116.397428, 39.90923], // Default: Beijing
  zoom: 12,
  enableCluster: true
});

const emit = defineEmits<{
  'marker-click': [point: PointMarker];
  'map-click': [lnglat: { lng: number; lat: number }];
  'map-ready': [map: any];
}>();

const mapContainerRef = ref<HTMLDivElement | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

let map: any = null;
let markers: any[] = [];
let markerCluster: any = null;

// Status color mapping
const statusColors: Record<string, string> = {
  pending: '#ffc107',      // Yellow - 未勘查
  submitted: '#1677ff',    // Blue - 已提交
  approved: '#00b42a',     // Green - 已通过
  rejected: '#f53f3f',     // Red - 已驳回
  default: '#1677ff'       // Default blue
};

/**
 * Initialize AMap
 */
const initMap = async () => {
  try {
    loading.value = true;
    error.value = null;

    console.log('[AMap Component] Starting map initialization...');
    console.log('[AMap Component] Container ref:', mapContainerRef.value);

    await loadAMap();

    console.log('[AMap Component] AMap loaded, window.AMap:', typeof (window as any).AMap);

    if (!mapContainerRef.value) {
      throw new Error('地图容器未找到');
    }

    // Check if AMap is available
    if (typeof (window as any).AMap === 'undefined') {
      throw new Error('AMap对象未定义，请检查API Key是否正确');
    }

    // Create map instance
    console.log('[AMap Component] Creating map instance...');
    map = new (window as any).AMap.Map(mapContainerRef.value, {
      zoom: props.zoom,
      center: props.center,
      resizeEnable: true,
      viewMode: '2D',
      mapStyle: 'amap://styles/normal'
    });

    console.log('[AMap Component] Map instance created:', map);

    // Load and add controls
    (window as any).AMap.plugin(['AMap.Scale', 'AMap.ToolBar'], () => {
      map.addControl(new (window as any).AMap.Scale());
      map.addControl(new (window as any).AMap.ToolBar({
        position: 'RB'
      }));
      console.log('[AMap Component] Map controls added');
    });

    // Map click event
    map.on('click', (e: any) => {
      emit('map-click', {
        lng: e.lnglat.getLng(),
        lat: e.lnglat.getLat()
      });
    });

    emit('map-ready', map);

    // Render markers
    renderMarkers();

    // Force map to resize after initialization
    setTimeout(() => {
      if (map) {
        map.resize();
        console.log('[AMap Component] Map resized');
      }
    }, 100);

    loading.value = false;
    console.log('[AMap Component] Map initialization complete');
  } catch (err: any) {
    console.error('[AMap Component] Failed to initialize map:', err);
    error.value = err.message || '地图加载失败，请检查网络连接';
    loading.value = false;
    message.error('地图加载失败: ' + err.message);
  }
};

/**
 * Render point markers on map
 */
const renderMarkers = () => {
  if (!map || !props.points || props.points.length === 0) {
    clearMarkers();
    return;
  }

  clearMarkers();

  props.points.forEach((point) => {
    if (!point.longitude || !point.latitude) return;

    const color = statusColors[point.status || 'default'] || statusColors.default;

    // Create marker
    const marker = new (AMap as any).Marker({
      position: [point.longitude, point.latitude],
      title: point.name,
      cursor: 'pointer',
      icon: new (AMap as any).Icon({
        size: new (AMap as any).Size(25, 34),
        image: createMarkerSVG(color),
        imageSize: new (AMap as any).Size(25, 34),
        imageOffset: new (AMap as any).Pixel(0, 0)
      }),
      offset: new (AMap as any).Pixel(-12, -34)
    });

    // Add info window
    const infoWindow = new (AMap as any).InfoWindow({
      content: createInfoWindowContent(point),
      offset: new (AMap as any).Pixel(0, -30)
    });

    marker.on('click', () => {
      const position = marker.getPosition();
      if (position) {
        infoWindow.open(map, position);
      }
      emit('marker-click', point);
    });

    marker.setMap(map);
    markers.push(marker);
  });

  // Enable marker clustering if needed
  if (props.enableCluster && props.points.length > 10) {
    enableMarkerCluster();
  }
};

/**
 * Create marker SVG icon
 */
const createMarkerSVG = (color: string): string => {
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="25" height="34" viewBox="0 0 25 34">
      <path d="M12.5 0C5.598 0 0 5.598 0 12.5c0 9.375 12.5 21.5 12.5 21.5s12.5-12.125 12.5-21.5C25 5.598 19.402 0 12.5 0z" fill="${color}"/>
      <circle cx="12.5" cy="12.5" r="5" fill="white"/>
    </svg>
  `)}`;
};

/**
 * Create info window HTML content
 */
const createInfoWindowContent = (point: PointMarker): string => {
  const statusText: Record<string, string> = {
    pending: '未勘查',
    submitted: '已提交',
    approved: '已通过',
    rejected: '已驳回'
  };

  return `
    <div style="padding: 12px; min-width: 200px;">
      <h4 style="margin: 0 0 8px 0; font-size: 14px; font-weight: 600;">${point.name}</h4>
      <div style="font-size: 12px; color: #666; line-height: 1.8;">
        <div>状态: ${statusText[point.status || ''] || point.status || '-'}</div>
        <div>经度: ${point.longitude.toFixed(6)}</div>
        <div>纬度: ${point.latitude.toFixed(6)}</div>
      </div>
    </div>
  `;
};

/**
 * Enable marker clustering
 */
const enableMarkerCluster = () => {
  if (markers.length === 0) return;

  // Load MarkerCluster plugin
  (AMap as any).plugin(['AMap.MarkerClusterer'], () => {
    markerCluster = new (AMap as any).MarkerClusterer(map, markers, {
      gridSize: 50,
      maxZoom: 15
    });
  });
};

/**
 * Clear all markers
 */
const clearMarkers = () => {
  markers.forEach(marker => {
    marker.setMap(null);
  });
  markers = [];

  if (markerCluster) {
    markerCluster.setMap(null);
    markerCluster = null;
  }
};

/**
 * Fit map view to show all markers
 */
const fitView = () => {
  if (!map || markers.length === 0) return;

  map.setFitView(markers, false, [50, 50, 50, 50]);
};

/**
 * Handle retry
 */
const handleRetry = () => {
  initMap();
};

// Watch for point changes
watch(() => props.points, () => {
  if (map) {
    renderMarkers();
  }
}, { deep: true });

// Lifecycle
onMounted(() => {
  initMap();
});

onBeforeUnmount(() => {
  if (map) {
    map.destroy();
    map = null;
  }
  clearMarkers();
});

// Expose methods
defineExpose({
  fitView,
  getMap: () => map
});
</script>

<style scoped>
.amap-container {
  width: 100%;
  height: 100%;
  position: relative;
  background: var(--bg-page, #f5f5f5);
}

.amap-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page, #f5f5f5);
  z-index: 1000;
}

.amap-loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 32px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--color-border, #e5e7eb);
}

.amap-loading-icon {
  color: var(--color-primary, #1677ff);
  opacity: 0.6;
}

.amap-loading-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1f2937);
  text-align: center;
}

.amap-loading-desc {
  font-size: 13px;
  color: var(--color-text-secondary, #6b7280);
  text-align: center;
  line-height: 1.6;
  max-width: 300px;
}

.amap-error {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  z-index: 1000;
}
</style>
