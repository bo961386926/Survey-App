/**
 * 高德地图工具类
 * 封装地图初始化、标记点添加、信息窗口等功能
 */

// 状态颜色映射
const STATUS_COLORS = {
  '待勘查': '#F59E0B',
  '草稿中': '#F59E0B',
  '已提交': '#3B82F6',
  '审核中': '#3B82F6',
  '已通过': '#10B981',
  '已驳回': '#EF4444',
  '驳回待修改': '#EF4444'
}

/**
 * 初始化地图
 * @param {String} containerId - 地图容器ID
 * @param {Object} options - 地图配置选项
 * @returns {Promise} 地图实例
 */
export function initMap(containerId, options = {}) {
  return new Promise((resolve, reject) => {
    try {
      const map = new AMap.Map(containerId, {
        zoom: 13,
        center: [116.397428, 39.90923], // 默认北京
        viewMode: '2D',
        pitch: 0,
        ...options
      })
      resolve(map)
    } catch (error) {
      reject(error)
    }
  })
}

/**
 * 添加标记点
 * @param {Object} map - 地图实例
 * @param {Array} points - 点位数组
 * @returns {Array} 标记点数组
 */
export function addMarkers(map, points = []) {
  const markers = []
  
  points.forEach(point => {
    if (!point.longitude || !point.latitude) return
    
    const color = STATUS_COLORS[point.status] || '#999999'
    
    const marker = new AMap.Marker({
      position: [point.longitude, point.latitude],
      title: point.name,
      extData: point,
      content: createMarkerContent(color)
    })
    
    marker.on('click', () => {
      showInfoWindow(map, marker, point)
    })
    
    map.add(marker)
    markers.push(marker)
  })
  
  return markers
}

/**
 * 创建标记点内容
 * @param {String} color - 标记点颜色
 * @returns {String} HTML内容
 */
function createMarkerContent(color) {
  return `
    <div style="
      width: 24px;
      height: 24px;
      background: ${color};
      border: 3px solid #fff;
      border-radius: 50%;
      box-shadow: 0 2px 8px rgba(0,0,0,0.3);
    "></div>
  `
}

/**
 * 显示信息窗口
 * @param {Object} map - 地图实例
 * @param {Object} marker - 标记点
 * @param {Object} point - 点位数据
 */
function showInfoWindow(map, marker, point) {
  const content = `
    <div style="padding: 12px; min-width: 200px;">
      <h3 style="margin: 0 0 8px 0; font-size: 16px; color: #333;">${point.name}</h3>
      <p style="margin: 0 0 4px 0; font-size: 13px; color: #666;">项目: ${point.projectName || '-'}</p>
      <p style="margin: 0 0 8px 0; font-size: 13px; color: #999;">
        坐标: ${point.longitude}, ${point.latitude}
      </p>
      <div style="display: inline-block; padding: 4px 12px; background: ${STATUS_COLORS[point.status]}; color: #fff; border-radius: 12px; font-size: 12px;">
        ${point.status}
      </div>
    </div>
  `
  
  const infoWindow = new AMap.InfoWindow({
    content,
    offset: new AMap.Pixel(0, -30)
  })
  
  infoWindow.open(map, marker.getPosition())
}

/**
 * 定位到用户位置
 * @param {Object} map - 地图实例
 * @returns {Promise} 位置信息
 */
export function locateUser(map) {
  return new Promise((resolve, reject) => {
    AMap.plugin('AMap.Geolocation', function() {
      const geolocation = new AMap.Geolocation({
        enableHighAccuracy: true,
        timeout: 10000,
        buttonPosition: 'RB',
        buttonOffset: new AMap.Pixel(10, 20),
        zoomToAccuracy: true
      })
      
      map.addControl(geolocation)
      
      geolocation.getCurrentPosition(function(status, result) {
        if (status === 'complete') {
          resolve(result)
        } else {
          reject(new Error('定位失败'))
        }
      })
    })
  })
}

/**
 * 打开外部地图导航
 * @param {Number} longitude - 经度
 * @param {Number} latitude - 纬度
 * @param {String} name - 点位名称
 */
export function openExternalMap(longitude, latitude, name) {
  const url = `https://uri.amap.com/marker?position=${longitude},${latitude}&name=${encodeURIComponent(name)}&callnative=1`
  
  // #ifdef H5
  window.open(url, '_blank')
  // #endif
  
  // #ifdef APP-PLUS
  plus.runtime.openURL(url)
  // #endif
  
  // #ifdef MP-WEIXIN
  uni.openLocation({
    longitude,
    latitude,
    name,
    scale: 18
  })
  // #endif
}

/**
 * 清除所有标记点
 * @param {Object} map - 地图实例
 * @param {Array} markers - 标记点数组
 */
export function clearMarkers(map, markers = []) {
  markers.forEach(marker => {
    map.remove(marker)
  })
  markers.length = 0
}

/**
 * 自适应显示所有标记点
 * @param {Object} map - 地图实例
 * @param {Array} points - 点位数组
 */
export function fitView(map, points = []) {
  if (points.length === 0) return
  
  const validPoints = points.filter(p => p.longitude && p.latitude)
  if (validPoints.length === 0) return
  
  const bounds = new AMap.Bounds(
    [Math.min(...validPoints.map(p => p.longitude)), Math.min(...validPoints.map(p => p.latitude))],
    [Math.max(...validPoints.map(p => p.longitude)), Math.max(...validPoints.map(p => p.latitude))]
  )
  
  map.setBounds(bounds)
}

export default {
  initMap,
  addMarkers,
  locateUser,
  openExternalMap,
  clearMarkers,
  fitView,
  STATUS_COLORS
}
