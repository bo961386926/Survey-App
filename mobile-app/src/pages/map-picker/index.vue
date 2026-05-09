/**
 * 高德地图选点页面
 * 全屏地图，支持搜索、拖拽选点、反向地理编码
 * 返回选择的经纬度和地址信息
 */
<template>
  <view class="map-picker-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrapper">
        <text class="search-icon">🔍</text>
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索地点"
          @confirm="onSearch"
          confirm-type="search"
        />
      </view>
      <text class="cancel-btn" @click="onCancel">取消</text>
    </view>

    <!-- 地图容器 -->
    <view class="map-wrapper">
      <!-- #ifdef H5 -->
      <view id="mapPickerContainer" class="map-container"></view>
      <!-- #endif -->
      <!-- #ifndef H5 -->
      <map
        id="mapPicker"
        class="map-container"
        :latitude="centerLat"
        :longitude="centerLng"
        :scale="zoom"
        :show-location="true"
        @regionchange="onRegionChange"
        @tap="onMapTap"
      >
        <cover-view class="center-marker">
          <cover-view class="marker-pin">
            <cover-view class="marker-inner"></cover-view>
          </cover-view>
        </cover-view>
      </map>
      <!-- #endif -->

      <!-- 中心标记（H5用DOM） -->
      <!-- #ifdef H5 -->
      <view class="center-marker">
        <view class="marker-pin">
          <view class="marker-inner"></view>
        </view>
      </view>
      <!-- #endif -->

      <!-- 底部信息面板 -->
      <view class="bottom-panel" :class="{ expanded: showAddressDetail }">
        <view class="panel-handle" @click="showAddressDetail = !showAddressDetail">
          <view class="handle-bar"></view>
        </view>

        <view v-if="selectedAddress" class="address-info">
          <text class="address-name">{{ selectedAddress.name || '未知位置' }}</text>
          <text class="address-detail">{{ selectedAddress.address || '' }}</text>
          <view class="coords-row">
            <text class="coord-text">经度: {{ selectedAddress.lng.toFixed(6) }}</text>
            <text class="coord-text">纬度: {{ selectedAddress.lat.toFixed(6) }}</text>
          </view>
        </view>
        <view v-else class="address-info">
          <text class="address-name">拖动地图选择位置</text>
          <text class="address-detail">将中心标记移动到目标位置</text>
        </view>

        <!-- 操作按钮 -->
        <view class="action-row">
          <view class="btn btn-gps" @click="locateCurrent">
            <text class="btn-icon">📍</text>
            <text class="btn-label">我的位置</text>
          </view>
          <view class="btn btn-confirm" :class="{ disabled: !selectedAddress }" @click="confirmLocation">
            <text>确认选择</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { getCurrentLocation } from '@/utils/location'
import { initMap } from '@/utils/map'
import { MAP_PICKER_RESULT_EVENT, MAP_PICKER_CANCEL_EVENT } from '@/utils/location'

// 从上一页传入的参数
const requestId = ref('')
const centerLng = ref(116.397428)
const centerLat = ref(39.90923)
const zoom = ref(15)
const searchKeyword = ref('')
const showAddressDetail = ref(false)

const selectedAddress = ref(null)

// H5地图实例
let mapInstance = null
let markerInstance = null
let geocoder = null

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  requestId.value = options.requestId || ''
  if (options.latitude && options.longitude) {
    centerLat.value = parseFloat(options.latitude)
    centerLng.value = parseFloat(options.longitude)
  }
  if (options.zoom) {
    zoom.value = parseInt(options.zoom)
  }

  // 尝试获取当前位置作为默认
  getCurrentLocation().then(pos => {
    if (!options.latitude) {
      centerLat.value = pos.lat
      centerLng.value = pos.lng
    }
    initMapPicker()
  }).catch(() => {
    initMapPicker()
  })
})

onUnmounted(() => {
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
})

/** 初始化H5地图 */
async function initMapPicker() {
  // #ifdef H5
  try {
    await nextTick()
    mapInstance = await initMap('mapPickerContainer', {
      zoom: zoom.value,
      center: [centerLng.value, centerLat.value],
      resizeEnable: true
    })

    // 添加中心标记
    markerInstance = new AMap.Marker({
      position: [centerLng.value, centerLat.value],
      draggable: false,
      icon: new AMap.Icon({
        size: new AMap.Size(30, 42),
        image: '/static/images/marker-drop.png',
        imageSize: new AMap.Size(30, 42)
      })
    })
    mapInstance.add(markerInstance)

    // 初始化地理编码
    AMap.plugin('AMap.Geocoder', () => {
      geocoder = new AMap.Geocoder({ city: '', radius: 1000 })
    })

    // 监听地图拖动
    mapInstance.on('moveend', onMapMoved)

    // 初始反编码
    setTimeout(() => reverseGeocode(centerLng.value, centerLat.value), 500)
  } catch (e) {
    console.error('地图初始化失败:', e)
  }
  // #endif
}

/** H5 地图拖动后更新 */
async function onMapMoved() {
  if (!mapInstance) return
  const center = mapInstance.getCenter()
  centerLng.value = center.lng
  centerLat.value = center.lat
  reverseGeocode(center.lng, center.lat)
}

/** 非H5地图区域变化 */
function onRegionChange(e) {
  if (e.type === 'end') {
    centerLng.value = e.detail.centerLocation.longitude
    centerLat.value = e.detail.centerLocation.latitude
    reverseGeocode(e.detail.centerLocation.longitude, e.detail.centerLocation.latitude)
  }
}

/** 非H5地图点击 */
function onMapTap(e) {
  centerLng.value = e.detail.longitude
  centerLat.value = e.detail.latitude
  reverseGeocode(e.detail.longitude, e.detail.latitude)
}

/** 反向地理编码 */
async function reverseGeocode(lng, lat) {
  // #ifdef H5
  if (geocoder) {
    geocoder.getAddress([lng, lat], (status, result) => {
      if (status === 'complete' && result.info === 'OK') {
        const regeo = result.regeocode
        selectedAddress.value = {
          lng,
          lat,
          name: regeo.formattedAddress || '未知位置',
          address: regeo.formattedAddress || '',
          province: regeo.addressComponent?.province || '',
          city: regeo.addressComponent?.city || '',
          district: regeo.addressComponent?.district || ''
        }
      }
    })
  } else {
    selectedAddress.value = { lng, lat, name: `${lng.toFixed(6)}, ${lat.toFixed(6)}`, address: '' }
  }
  // #endif

  // #ifndef H5
  // 非H5使用 UniApp 的反地理编码
  try {
    const res = await uni.request({
      url: `https://restapi.amap.com/v3/geocode/regeo?output=json&location=${lng},${lat}&key=${getAmapKey()}&radius=1000&extensions=all`,
      method: 'GET'
    })
    if (res.data?.status === '1' && res.data?.regeocode) {
      const regeo = res.data.regeocode
      selectedAddress.value = {
        lng,
        lat,
        name: regeo.formattedAddress || '未知位置',
        address: regeo.formattedAddress || '',
        province: regeo.addressComponent?.province || '',
        city: regeo.addressComponent?.city || '',
        district: regeo.addressComponent?.district || ''
      }
    } else {
      selectedAddress.value = { lng, lat, name: `${lng.toFixed(6)}, ${lat.toFixed(6)}`, address: '' }
    }
  } catch {
    selectedAddress.value = { lng, lat, name: `${lng.toFixed(6)}, ${lat.toFixed(6)}`, address: '' }
  }
  // #endif
}

/** 获取高德地图Key */
function getAmapKey() {
  // #ifdef APP-PLUS
  return '6b6697c339d48f245660d0e79ecc0945'
  // #endif
  // #ifdef MP-WEIXIN
  return '6b6697c339d48f245660d0e79ecc0945'
  // #endif
  // #ifdef H5
  return '6b6697c339d48f245660d0e79ecc0945'
  // #endif
}

/** 搜索地点 */
async function onSearch() {
  if (!searchKeyword.value.trim()) return
  // #ifdef H5
  if (mapInstance) {
    AMap.plugin('AMap.PlaceSearch', () => {
      const placeSearch = new AMap.PlaceSearch({ city: '', pageSize: 1 })
      placeSearch.search(searchKeyword.value, (status, result) => {
        if (status === 'complete' && result.poiList?.pois?.length > 0) {
          const poi = result.poiList.pois[0]
          centerLng.value = poi.location.lng
          centerLat.value = poi.location.lat
          mapInstance.setCenter([poi.location.lng, poi.location.lat])
          markerInstance.setPosition([poi.location.lng, poi.location.lat])
          reverseGeocode(poi.location.lng, poi.location.lat)
        } else {
          uni.showToast({ title: '未找到该地点', icon: 'none' })
        }
      })
    })
  }
  // #endif

  // #ifndef H5
  try {
    const res = await uni.request({
      url: `https://restapi.amap.com/v3/place/text?key=${getAmapKey()}&keywords=${encodeURIComponent(searchKeyword.value)}&offset=1&page=1&extensions=base`,
      method: 'GET'
    })
    if (res.data?.status === '1' && res.data?.pois?.length > 0) {
      const poi = res.data.pois[0]
      const [lng, lat] = poi.location.split(',')
      centerLng.value = parseFloat(lng)
      centerLat.value = parseFloat(lat)
      reverseGeocode(centerLng.value, centerLat.value)
    } else {
      uni.showToast({ title: '未找到该地点', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '搜索失败', icon: 'none' })
  }
  // #endif
}

/** 定位到当前位置 */
async function locateCurrent() {
  try {
    uni.showLoading({ title: '定位中...' })
    const pos = await getCurrentLocation()
    centerLng.value = pos.lng
    centerLat.value = pos.lat

    // #ifdef H5
    if (mapInstance) {
      mapInstance.setCenter([pos.lng, pos.lat])
      markerInstance.setPosition([pos.lng, pos.lat])
    }
    // #endif

    reverseGeocode(pos.lng, pos.lat)
    uni.hideLoading()
  } catch {
    uni.hideLoading()
    uni.showToast({ title: '定位失败', icon: 'none' })
  }
}

/** 确认选择 */
function confirmLocation() {
  if (!selectedAddress.value) {
    uni.showToast({ title: '请先选择位置', icon: 'none' })
    return
  }
  // 通过 uni.$emit 触发结果事件（使用 requestId 区分不同调用方）
  const resultEvent = `${MAP_PICKER_RESULT_EVENT}_${requestId.value}`
  uni.$emit(resultEvent, { ...selectedAddress.value })
  // 延迟返回确保事件传递完成
  setTimeout(() => uni.navigateBack(), 50)
}

/** 取消 */
function onCancel() {
  // 触发取消事件
  if (requestId.value) {
    const cancelEvent = `${MAP_PICKER_CANCEL_EVENT}_${requestId.value}`
    uni.$emit(cancelEvent)
  }
  uni.navigateBack()
}
</script>

<style scoped>
.map-picker-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
  position: relative;
}

.search-bar {
  position: absolute;
  top: 12px;
  left: 12px;
  right: 12px;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 0 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.search-icon {
  font-size: 16px;
  margin-right: 8px;
}

.search-input {
  flex: 1;
  height: 40px;
  font-size: 14px;
  color: #333;
  border: none;
  background: transparent;
}

.cancel-btn {
  font-size: 14px;
  color: #409EFF;
  padding: 8px 4px;
  white-space: nowrap;
}

.map-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
}

/* 中心标记 */
.center-marker {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -100%);
  z-index: 50;
  pointer-events: none;
}

.marker-pin {
  width: 28px;
  height: 40px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.marker-inner {
  width: 14px;
  height: 14px;
  background: #409EFF;
  border: 3px solid #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  margin-top: 4px;
}

.marker-icon {
  width: 30px;
  height: 42px;
}

/* 底部面板 */
.bottom-panel {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.1);
  padding: 8px 16px 24px;
  z-index: 80;
  transition: transform 0.3s;
  max-height: 45vh;
}

.panel-handle {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
  cursor: pointer;
}

.handle-bar {
  width: 36px;
  height: 4px;
  background: #D1D5DB;
  border-radius: 2px;
}

.address-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.address-name {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
}

.address-detail {
  font-size: 13px;
  color: #94A3B8;
}

.coords-row {
  display: flex;
  gap: 16px;
}

.coord-text {
  font-size: 12px;
  color: #64748B;
  font-family: monospace;
}

.action-row {
  display: flex;
  gap: 12px;
}

.btn {
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 500;
}

.btn-gps {
  width: 96px;
  background: #F1F5F9;
  color: #475569;
  gap: 4px;
}

.btn-icon {
  font-size: 16px;
}

.btn-label {
  font-size: 13px;
}

.btn-confirm {
  flex: 1;
  background: #409EFF;
  color: #fff;
}

.btn-confirm.disabled {
  background: #DCDFE6;
  color: #C0C4CC;
}
</style>
