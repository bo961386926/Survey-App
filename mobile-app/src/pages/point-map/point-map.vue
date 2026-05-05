/**
 * 点位地图页面
 * 展示点位在地图上的分布,支持筛选和查看
 */
<template>
  <view class="point-map-container">
    <!-- 筛选标签 -->
    <view class="filter-tags">
      <scroll-view scroll-x class="tags-scroll">
        <view class="tags-list">
          <view 
            v-for="tag in statusTags" 
            :key="tag.status"
            class="tag-item"
            :class="{ active: currentStatus === tag.status }"
            @click="changeStatus(tag.status)"
          >
            <view class="tag-dot" :style="{ backgroundColor: tag.color }"></view>
            <text class="tag-label">{{ tag.label }}</text>
            <text class="tag-count">{{ tag.count }}</text>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 地图区域 -->
    <view class="map-wrapper">
      <view id="mapContainer" class="map-container"></view>
      
      <!-- 在地图中打开按钮 -->
      <view class="open-map-btn" @click="openInExternalMap">
        <text class="btn-icon"></text>
        <text class="btn-text">在地图中打开</text>
      </view>
      
      <!-- 点位数量标签 -->
      <view class="points-count-tag">
        <text class="icon">📍</text>
        <text class="count">{{ pointList.length }} 个点位</text>
      </view>
    </view>
    
    <!-- 底部点位列表 -->
    <view class="bottom-list">
      <view class="list-header">
        <text class="list-title">点位列表</text>
        <text class="list-count">{{ pointList.length }} 个</text>
      </view>
      
      <scroll-view scroll-y class="points-scroll">
        <view 
          v-for="point in pointList" 
          :key="point.id"
          class="point-item"
          @click="goToPointDetail(point)"
        >
          <view class="point-left">
            <view class="point-dot" :style="{ backgroundColor: getStatusColor(point.status) }"></view>
            <view class="point-info">
              <text class="point-name">{{ point.name }}</text>
              <text class="point-project">{{ point.projectName }}</text>
            </view>
          </view>
          <view class="point-actions">
            <view class="nav-btn" @click.stop="navigateToPoint(point)">
              <text class="nav-icon">🧭</text>
            </view>
            <button 
              v-if="point.status === '待勘查'" 
              class="survey-btn"
              @click.stop="startSurvey(point)"
            >
              勘查
            </button>
            <text v-else class="status-text" :style="{ color: getStatusColor(point.status) }">
              {{ point.status }}
            </text>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { pointApi } from '@/utils/api'
import { initMap, addMarkers, clearMarkers, openExternalMap, STATUS_COLORS } from '@/utils/map'

let mapInstance = null
let markers = []

const currentStatus = ref('all')
const pointList = ref([])

// 状态标签
const statusTags = reactive([
  { label: '未勘查', status: '待勘查', color: '#F59E0B', count: 0 },
  { label: '已提交', status: '已提交', color: '#3B82F6', count: 0 },
  { label: '已通过', status: '已通过', color: '#10B981', count: 0 },
  { label: '已驳回', status: '已驳回', color: '#EF4444', count: 0 }
])

onMounted(() => {
  loadPoints()
})

onUnmounted(() => {
  if (mapInstance) {
    mapInstance.destroy()
  }
})

// 加载点位数据
async function loadPoints() {
  try {
    const params = currentStatus.value === 'all' 
      ? {} 
      : { status: currentStatus.value }
    
    const result = await pointApi.getMapData(params)
    const points = result.list || result || []
    
    pointList.value = points
    updateStatusCounts(points)
    
    // 初始化地图
    initMapInstance()
    
    // 添加标记点
    if (mapInstance) {
      clearMarkers(mapInstance, markers)
      markers = addMarkers(mapInstance, points)
    }
  } catch (error) {
    console.error('加载点位失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 初始化地图
async function initMapInstance() {
  try {
    // #ifdef H5
    mapInstance = await initMap('mapContainer', {
      zoom: 12,
      center: [116.397428, 39.90923]
    })
    // #endif
    
    // #ifndef H5
    // 非H5环境使用uni-app地图组件
    // #endif
  } catch (error) {
    console.error('地图初始化失败:', error)
  }
}

// 更新状态计数
function updateStatusCounts(points) {
  statusTags[0].count = points.filter(p => p.status === '待勘查').length
  statusTags[1].count = points.filter(p => p.status === '已提交' || p.status === '审核中').length
  statusTags[2].count = points.filter(p => p.status === '已通过').length
  statusTags[3].count = points.filter(p => p.status === '已驳回' || p.status === '驳回待修改').length
}

// 切换状态筛选
function changeStatus(status) {
  currentStatus.value = status
  loadPoints()
}

// 获取状态颜色
function getStatusColor(status) {
  return STATUS_COLORS[status] || '#999999'
}

// 在外部地图打开
function openInExternalMap() {
  if (pointList.value.length === 0) {
    uni.showToast({ title: '暂无点位', icon: 'none' })
    return
  }
  
  // 使用第一个点位的坐标
  const point = pointList.value[0]
  openExternalMap(point.longitude, point.latitude, point.name)
}

// 导航到点位
function navigateToPoint(point) {
  if (!point.longitude || !point.latitude) {
    uni.showToast({ title: '点位坐标无效', icon: 'none' })
    return
  }
  openExternalMap(point.longitude, point.latitude, point.name)
}

// 开始勘查
function startSurvey(point) {
  uni.navigateTo({ url: `/pages/survey/survey?id=${point.id}&mode=new` })
}

// 查看点位详情
function goToPointDetail(point) {
  uni.navigateTo({ url: `/pages/point-detail/point-detail?id=${point.id}` })
}
</script>

<style scoped>
.point-map-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F8FAFC;
}

.filter-tags {
  background: #FFFFFF;
  padding: 12px 16px;
  border-bottom: 1px solid #E2E8F0;
}

.tags-scroll {
  white-space: nowrap;
}

.tags-list {
  display: inline-flex;
  gap: 8px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: #F1F5F9;
  border-radius: 16px;
  font-size: 13px;
  color: #64748B;
}

.tag-item.active {
  background: #EFF6FF;
  color: #2563EB;
  font-weight: 500;
}

.tag-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.tag-count {
  font-size: 12px;
}

.map-wrapper {
  flex: 1;
  position: relative;
  min-height: 300px;
}

.map-container {
  width: 100%;
  height: 100%;
}

.open-map-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.btn-icon {
  font-size: 16px;
}

.btn-text {
  font-size: 13px;
  color: #2563EB;
  font-weight: 500;
}

.points-count-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.icon {
  font-size: 16px;
}

.count {
  font-size: 13px;
  color: #1E293B;
  font-weight: 500;
}

.bottom-list {
  background: #FFFFFF;
  border-top-left-radius: 16px;
  border-top-right-radius: 16px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.05);
  max-height: 40vh;
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #E2E8F0;
}

.list-title {
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
}

.list-count {
  font-size: 13px;
  color: #94A3B8;
}

.points-scroll {
  flex: 1;
}

.point-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #F1F5F9;
}

.point-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.point-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.point-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.point-name {
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
}

.point-project {
  font-size: 12px;
  color: #94A3B8;
}

.point-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-btn {
  padding: 8px;
}

.nav-icon {
  font-size: 20px;
}

.survey-btn {
  padding: 6px 16px;
  background: #2563EB;
  color: #FFFFFF;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  height: auto;
  line-height: normal;
}

.status-text {
  font-size: 13px;
  font-weight: 500;
}
</style>
