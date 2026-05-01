/**
 * 草稿本地存储工具
 * 处理离线草稿保存、恢复、提交重试
 */

const DRAFT_PREFIX = 'survey_draft_'
const DRAFT_LIST_KEY = 'survey_draft_list'

/**
 * 保存草稿到本地存储
 * @param {string} pointId - 点位ID
 * @param {object} draftData - 草稿数据
 */
export function saveDraft(pointId, draftData) {
  try {
    const key = DRAFT_PREFIX + pointId
    const data = {
      ...draftData,
      pointId,
      savedAt: Date.now(),
      version: draftData.version || 1
    }
    uni.setStorageSync(key, JSON.stringify(data))
    
    // 更新草稿列表
    addToDraftList(pointId, data)
    
    console.log('草稿已保存:', pointId)
    return true
  } catch (e) {
    console.error('保存草稿失败:', e)
    return false
  }
}

/**
 * 获取本地草稿
 * @param {string} pointId - 点位ID
 */
export function getDraft(pointId) {
  try {
    const key = DRAFT_PREFIX + pointId
    const data = uni.getStorageSync(key)
    return data ? JSON.parse(data) : null
  } catch (e) {
    console.error('获取草稿失败:', e)
    return null
  }
}

/**
 * 清除单个草稿（别名，与deleteDraft功能相同）
 * @param {string} pointId - 点位ID
 */
export function clearDraft(pointId) {
  return deleteDraft(pointId)
}

/**
 * 删除本地草稿
 * @param {string} pointId - 点位ID
 */
export function deleteDraft(pointId) {
  try {
    const key = DRAFT_PREFIX + pointId
    uni.removeStorageSync(key)
    removeFromDraftList(pointId)
    return true
  } catch (e) {
    console.error('删除草稿失败:', e)
    return false
  }
}

/**
 * 检查是否存在草稿
 * @param {string} pointId - 点位ID
 */
export function hasDraft(pointId) {
  return !!getDraft(pointId)
}

/**
 * 获取所有草稿列表
 */
export function getDraftList() {
  try {
    const list = uni.getStorageSync(DRAFT_LIST_KEY)
    return list ? JSON.parse(list) : []
  } catch (e) {
    return []
  }
}

/**
 * 添加到草稿列表
 */
function addToDraftList(pointId, draftData) {
  const list = getDraftList()
  const existingIndex = list.findIndex(item => item.pointId === pointId)
  
  const item = {
    pointId,
    pointName: draftData.pointName || '',
    savedAt: draftData.savedAt,
    status: draftData.status || 'draft'
  }
  
  if (existingIndex >= 0) {
    list[existingIndex] = item
  } else {
    list.unshift(item)
  }
  
  uni.setStorageSync(DRAFT_LIST_KEY, JSON.stringify(list))
}

/**
 * 从草稿列表移除
 */
function removeFromDraftList(pointId) {
  const list = getDraftList()
  const newList = list.filter(item => item.pointId !== pointId)
  uni.setStorageSync(DRAFT_LIST_KEY, JSON.stringify(newList))
}

/**
 * 清空所有草稿
 */
export function clearAllDrafts() {
  try {
    const list = getDraftList()
    list.forEach(item => {
      uni.removeStorageSync(DRAFT_PREFIX + item.pointId)
    })
    uni.removeStorageSync(DRAFT_LIST_KEY)
    return true
  } catch (e) {
    console.error('清空草稿失败:', e)
    return false
  }
}

/**
 * 获取草稿数量
 */
export function getDraftCount() {
  return getDraftList().length
}

/**
 * 格式化草稿保存时间
 */
export function formatDraftTime(timestamp) {
  if (!timestamp) return ''
  
  const now = Date.now()
  const diff = now - timestamp
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else {
    const date = new Date(timestamp)
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString().slice(0, 5)
  }
}

/**
 * 检查草稿是否过期（7天）
 */
export function isDraftExpired(pointId, maxDays = 7) {
  const draft = getDraft(pointId)
  if (!draft) return true
  
  const now = Date.now()
  const maxTime = maxDays * 24 * 60 * 60 * 1000
  
  return (now - draft.savedAt) > maxTime
}

/**
 * 清理过期草稿
 */
export function cleanExpiredDrafts(maxDays = 7) {
  const list = getDraftList()
  const expiredList = []
  
  list.forEach(item => {
    if (isDraftExpired(item.pointId, maxDays)) {
      expiredList.push(item.pointId)
    }
  })
  
  expiredList.forEach(pointId => deleteDraft(pointId))
  
  return expiredList.length
}
