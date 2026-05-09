/**
 * 定位工具类
 * 处理GPS定位、人工纠偏、坐标转换、高德地图选点
 */

// 地图选点结果事件名
export const MAP_PICKER_RESULT_EVENT = 'mapPickerResult'
// 地图选点取消事件名
export const MAP_PICKER_CANCEL_EVENT = 'mapPickerCancel'

/**
 * 打开高德地图选点页面
 * 使用专用的地图选点页面，支持搜索、拖拽选点、反向地理编码
 * 通过 uni.$emit/uni.$on 实现跨页面通信
 * @param {Object} options - 配置选项
 * @param {number} options.latitude - 初始纬度
 * @param {number} options.longitude - 初始经度
 * @param {number} options.zoom - 地图缩放级别
 * @param {string} options.title - 页面标题
 * @returns {Promise} 返回选择的位置 {lng, lat, name, address, province, city, district}
 */
export function openAmapPicker(options = {}) {
  return new Promise((resolve, reject) => {
    try {
      const {
        latitude = 39.9042,
        longitude = 116.4074,
        zoom = 15,
        title = '选择位置'
      } = options

      // 生成唯一请求ID，防止事件冲突
      const requestId = `picker_${Date.now()}_${Math.random().toString(36).substr(2, 6)}`
      const resultEvent = `${MAP_PICKER_RESULT_EVENT}_${requestId}`
      const cancelEvent = `${MAP_PICKER_CANCEL_EVENT}_${requestId}`

      // 监听结果事件（一次性）
      uni.$once(resultEvent, (result) => {
        uni.$off(cancelEvent)
        resolve(result)
      })

      // 监听取消事件（一次性）
      uni.$once(cancelEvent, () => {
        uni.$off(resultEvent)
        reject(new Error('用户取消选择'))
      })

      // 超时兜底（120秒后自动取消）
      const timeoutId = setTimeout(() => {
        uni.$off(resultEvent)
        uni.$off(cancelEvent)
        reject(new Error('地图选点超时'))
      }, 120000)

      // 包装一下监听，超时时清除定时器
      const origResultOff = uni.$off.bind(uni)
      const wrappedResolve = (result) => {
        clearTimeout(timeoutId)
        resolve(result)
      }
      // 重新注册以清除超时
      uni.$off(resultEvent)
      uni.$on(resultEvent, (result) => {
        clearTimeout(timeoutId)
        resolve(result)
      })

      uni.navigateTo({
        url: `/pages/map-picker/index?latitude=${latitude}&longitude=${longitude}&zoom=${zoom}&requestId=${requestId}`,
        fail: (err) => {
          clearTimeout(timeoutId)
          uni.$off(resultEvent)
          uni.$off(cancelEvent)
          console.warn('地图选点页面加载失败，回退到系统选点:', err)
          openMapPicker(options).then(resolve).catch(reject)
        }
      })
    } catch (error) {
      console.warn('地图选点异常，回退到系统选点:', error)
      openMapPicker(options).then(resolve).catch(reject)
    }
  })
}

/**
 * 反向地理编码 - 获取地址描述
 * @param {number} lng - 经度
 * @param {number} lat - 纬度
 * @returns {Promise<Object>} 地址信息
 */
export function reverseGeocode(lng, lat) {
  return new Promise((resolve) => {
    // #ifdef H5
    // H5环境使用高德JS API
    if (typeof AMap !== 'undefined' && AMap.Geocoder) {
      const geocoder = new AMap.Geocoder({ city: '', radius: 1000 })
      geocoder.getAddress([lng, lat], (status, result) => {
        if (status === 'complete' && result.info === 'OK') {
          const regeo = result.regeocode
          resolve({
            address: regeo.formattedAddress || '',
            province: regeo.addressComponent?.province || '',
            city: regeo.addressComponent?.city || '',
            district: regeo.addressComponent?.district || ''
          })
        } else {
          resolve({ address: '', province: '', city: '', district: '' })
        }
      })
    } else {
      resolve({ address: '', province: '', city: '', district: '' })
    }
    // #endif

    // #ifndef H5
    // 非H5环境使用 REST API
    uni.request({
      url: `https://restapi.amap.com/v3/geocode/regeo?output=json&location=${lng},${lat}&key=6b6697c339d48f245660d0e79ecc0945&radius=1000`,
      method: 'GET',
      success: (res) => {
        if (res.data?.status === '1' && res.data?.regeocode) {
          const regeo = res.data.regeocode
          resolve({
            address: regeo.formattedAddress || '',
            province: regeo.addressComponent?.province || '',
            city: regeo.addressComponent?.city || '',
            district: regeo.addressComponent?.district || ''
          })
        } else {
          resolve({ address: '', province: '', city: '', district: '' })
        }
      },
      fail: () => resolve({ address: '', province: '', city: '', district: '' })
    })
    // #endif
  })
}

/**
 * 获取当前位置
 * @returns {Promise} 返回位置信息 {lng, lat, accuracy, address}
 */
export function getCurrentLocation() {
  return new Promise((resolve, reject) => {
    uni.getLocation({
      type: 'gcj02', // 高德地图坐标系
      isHighAccuracy: true,
      success: (res) => {
        resolve({
          lng: res.longitude,
          lat: res.latitude,
          accuracy: res.accuracy,
          altitude: res.altitude,
          speed: res.speed
        })
      },
      fail: (err) => {
        console.error('获取位置失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 打开地图选择器
 * @param {Object} options - 配置选项
 * @returns {Promise} 返回选择的位置
 */
export function openMapPicker(options = {}) {
  const {
    latitude = 39.9042,
    longitude = 116.4074,
    title = '选择位置'
  } = options
  
  return new Promise((resolve, reject) => {
    uni.chooseLocation({
      latitude,
      longitude,
      success: (res) => {
        resolve({
          lng: res.longitude,
          lat: res.latitude,
          name: res.name,
          address: res.address
        })
      },
      fail: (err) => {
        console.error('地图选择失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 计算两点间距离（米）
 * @param {number} lng1 - 点1经度
 * @param {number} lat1 - 点1纬度
 * @param {number} lng2 - 点2经度
 * @param {number} lat2 - 点2纬度
 * @returns {number} 距离（米）
 */
export function calculateDistance(lng1, lat1, lng2, lat2) {
  const EARTH_RADIUS = 6378137 // 地球半径（米）
  
  const radLat1 = lat1 * Math.PI / 180
  const radLat2 = lat2 * Math.PI / 180
  const a = radLat1 - radLat2
  const b = lng1 * Math.PI / 180 - lng2 * Math.PI / 180
  
  const distance = 2 * EARTH_RADIUS * Math.asin(
    Math.sqrt(Math.pow(Math.sin(a / 2), 2) + 
              Math.cos(radLat1) * Math.cos(radLat2) * 
              Math.pow(Math.sin(b / 2), 2))
  )
  
  return Math.round(distance)
}

/**
 * 检查位置是否在允许范围内
 * @param {number} currentLng - 当前位置经度
 * @param {number} currentLat - 当前位置纬度
 * @param {number} targetLng - 目标位置经度
 * @param {number} targetLat - 目标位置纬度
 * @param {number} maxDistance - 最大允许距离（米）
 * @returns {boolean} 是否在范围内
 */
export function isInRange(currentLng, currentLat, targetLng, targetLat, maxDistance = 100) {
  const distance = calculateDistance(currentLng, currentLat, targetLng, targetLat)
  return distance <= maxDistance
}

/**
 * 记录纠偏日志
 * @param {Object} correctionData - 纠偏数据
 * @returns {Object} 格式化后的纠偏记录
 */
export function formatCorrectionLog(correctionData) {
  const {
    pointId,
    resultId,
    originalLng,
    originalLat,
    correctedLng,
    correctedLat,
    userId
  } = correctionData
  
  return {
    pointId,
    resultId,
    originalLng: parseFloat(originalLng).toFixed(7),
    originalLat: parseFloat(originalLat).toFixed(7),
    correctedLng: parseFloat(correctedLng).toFixed(7),
    correctedLat: parseFloat(correctedLat).toFixed(7),
    userId,
    distance: calculateDistance(originalLng, originalLat, correctedLng, correctedLat),
    createTime: new Date().toISOString()
  }
}

/**
 * 坐标转换：WGS84转GCJ02
 * @param {number} lng - WGS84经度
 * @param {number} lat - WGS84纬度
 * @returns {Object} GCJ02坐标
 */
export function wgs84ToGcj02(lng, lat) {
  const PI = 3.1415926535897932384626
  const A = 6378245.0 // 长半轴
  const EE = 0.00669342162296594323 // 偏心率平方
  
  if (isOutOfChina(lng, lat)) {
    return { lng, lat }
  }
  
  let dLat = transformLat(lng - 105.0, lat - 35.0)
  let dLng = transformLng(lng - 105.0, lat - 35.0)
  const radLat = lat / 180.0 * PI
  let magic = Math.sin(radLat)
  magic = 1 - EE * magic * magic
  const sqrtMagic = Math.sqrt(magic)
  dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
  dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI)
  
  return {
    lng: lng + dLng,
    lat: lat + dLat
  }
}

/**
 * 判断是否在中国境外
 */
function isOutOfChina(lng, lat) {
  return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
}

/**
 * 转换纬度
 */
function transformLat(x, y) {
  const PI = 3.1415926535897932384626
  let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
  ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0
  ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0
  ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0
  return ret
}

/**
 * 转换经度
 */
function transformLng(x, y) {
  const PI = 3.1415926535897932384626
  let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
  ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0
  ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0
  ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0
  return ret
}

/**
 * 格式化位置显示
 * @param {Object} location - 位置信息
 * @returns {string} 格式化后的位置字符串
 */
export function formatLocation(location) {
  if (!location || !location.lng || !location.lat) {
    return '位置未知'
  }
  return `经度: ${location.lng.toFixed(6)}, 纬度: ${location.lat.toFixed(6)}`
}

/**
 * 获取位置状态描述
 * @param {number} distance - 距离目标点位的距离（米）
 * @returns {Object} 状态描述 {status, message, color}
 */
export function getLocationStatus(distance) {
  if (distance === null || distance === undefined) {
    return { status: 'unknown', message: '未获取位置', color: '#999' }
  }
  
  if (distance <= 10) {
    return { status: 'accurate', message: '位置精确', color: '#67C23A' }
  } else if (distance <= 50) {
    return { status: 'close', message: '位置较近', color: '#E6A23C' }
  } else {
    return { status: 'far', message: '距离较远', color: '#F56C6C' }
  }
}
