/**
 * 点位列表页面
 * 支持按状态筛选、搜索、批量分配
 */
<template>
  <view class="point-list-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrapper">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          v-model="searchKeyword"
          placeholder="搜索点位名称/编号"
          @confirm="handleSearch"
        />
      </view>
    </view>
    
    <!-- 状态筛选 -->
    <view class="filter-bar">
      <scroll-view scroll-x class="filter-scroll">
        <view class="filter-list">
          <view 
            v-for="status in statusList" 
            :key="status.value"
            class="filter-item"
            :class="{ active: currentStatus === status.value }"
            @click="changeStatus(status.value)"
          >
            {{ status.label }}
            <text v-if="status.count" class="count">({{ status.count }})</text>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 点位列表 -->
    <view class="list-content">
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
      
      <view v-else-if="pointList.length === 0" class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无点位</text>
      </view>
      
      <view v-else class="point-list">
        <view 
          v-for="point in pointList" 
          :key="point.id"
          class="point-item"
          @click="goToPointDetail(point.id)"
        >
          <view class="point-header">
            <text class="point-name">{{ point.name }}</text>
            <status-tag :status="point.status" type="point" />
          </view>
          <view class="point-info">
            <text class="point-code">编号: {{ point.code }}</text>
            <text class="point-project">项目: {{ point.projectName }}</text>
          </view>
          <view class="point-footer">
            <text class="point-location">📍 {{ point.address || '未设置地址' }}</text>
          </view>
          <!-- 操作按钮 -->
          <view v-if="point.status === '待采集'" class="action-btns">
            <button class="btn-start" @click.stop="startCollect(point)">开始采集</button>
          </view>
          <view v-else-if="point.status === '草稿中'" class="action-btns">
            <button class="btn-continue" @click.stop="continueSurvey(point)">继续填报</button>
          </view>
          <view v-else-if="point.status === '驳回待修改'" class="action-btns">
            <button class="btn-modify" @click.stop="modifyRejected(point)">修改重提</button>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 加载更多 -->
    <view v-if="hasMore && pointList.length > 0" class="load-more" @click="loadMore">
      <text>{{ loadingMore ? '加载中...' : '加载更多' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pointApi } from '@/utils/api'
import StatusTag from '@/components/status-tag/status-tag.vue'

const searchKeyword = ref('')
const currentStatus = ref('all')
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10

const pointList = ref([])

// 状态列表
const statusList = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '待采集', value: '待采集', count: 0 },
  { label: '草稿中', value: '草稿中', count: 0 },
  { label: '待审核', value: '待审核', count: 0 },
  { label: '已驳回', value: '驳回待修改', count: 0 },
  { label: '已通过', value: '审核通过', count: 0 }
])

onMounted(() => {
  loadPointList()
})

// 加载点位列表
async function loadPointList(isRefresh = false) {
  if (isRefresh) {
    page.value = 1
    hasMore.value = true
  }
  
  loading.value = !isRefresh
  loadingMore.value = isRefresh
  
  try {
    const params = {
      page: page.value,
      size: pageSize,
      keyword: searchKeyword.value || undefined,
      status: currentStatus.value === 'all' ? undefined : currentStatus.value
    }
    
    const result = await pointApi.getMyPoints(params)
    const list = result.list || result || []
    
    if (isRefresh) {
      pointList.value = list
    } else {
      pointList.value = [...pointList.value, ...list]
    }
    
    hasMore.value = list.length >= pageSize
    page.value++
    
    // 更新状态计数
    updateStatusCounts(result.total || 0)
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 更新状态计数
function updateStatusCounts(total) {
  // 简化处理，实际应该从后端获取各状态数量
  statusList.value[0].count = total
}

// 切换状态筛选
function changeStatus(status) {
  currentStatus.value = status
  loadPointList(true)
}

// 搜索
function handleSearch() {
  loadPointList(true)
}

// 加载更多
function loadMore() {
  if (!loadingMore.value && hasMore.value) {
    loadPointList(true)
  }
}

// 开始采集
async function startCollect(point) {
  try {
    await pointApi.startCollect(point.id)
    uni.showToast({ title: '已开始采集', icon: 'success' })
    loadPointList(true)
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

// 继续填报
function continueSurvey(point) {
  uni.navigateTo({ url: `/pages/survey/survey?id=${point.id}&mode=draft` })
}

// 修改重提
function modifyRejected(point) {
  uni.navigateTo({ url: `/pages/survey/survey?id=${point.id}&mode=modify` })
}

// 跳转到点位详情
function goToPointDetail(id) {
  uni.navigateTo({ url: `/pages/point-detail/point-detail?id=${id}` })
}

// 下拉刷新
onPullDownRefresh(() => {
  loadPointList(true)
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 1000)
})
</script>

<style scoped>
.point-list-container {
  min-height: 100vh;
  background-color: #F5F7FA;
}

.search-bar {
  padding: 12px 16px;
  background-color: #fff;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 20px;
  padding: 0 16px;
  height: 40px;
}

.search-icon {
  font-size: 18px;
  margin-right: 8px;
}

.search-input {
  flex: 1;
  font-size: 14px;
}

.filter-bar {
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

.filter-scroll {
  white-space: nowrap;
}

.filter-list {
  display: inline-flex;
  padding: 12px 16px;
}

.filter-item {
  padding: 6px 12px;
  margin-right: 8px;
  font-size: 13px;
  color: #666;
  border-radius: 16px;
  background-color: #f5f5f5;
}

.filter-item.active {
  background-color: #409EFF;
  color: #fff;
}

.count {
  font-size: 11px;
}

.list-content {
  padding: 12px 16px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.point-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.point-item {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.point-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.point-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.point-info {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.point-code,
.point-project {
  font-size: 12px;
  color: #999;
}

.point-footer {
  margin-top: 8px;
}

.point-location {
  font-size: 12px;
  color: #666;
}

.action-btns {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.action-btns button {
  width: 100%;
  height: 36px;
  line-height: 36px;
  font-size: 14px;
  border: none;
  border-radius: 6px;
}

.btn-start {
  background-color: #409EFF;
  color: #fff;
}

.btn-continue {
  background-color: #E6A23C;
  color: #fff;
}

.btn-modify {
  background-color: #F56C6C;
  color: #fff;
}

.load-more {
  text-align: center;
  padding: 16px;
  color: #999;
  font-size: 14px;
}
</style>
