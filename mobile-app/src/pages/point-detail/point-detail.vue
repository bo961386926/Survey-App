/**
 * 点位详情页面
 * 展示点位基本信息、采集结果、审核记录
 */
<template>
  <view class="detail-container">
    <view v-if="loading" class="loading-state">
      <text>加载中...</text>
    </view>
    
    <template v-else-if="pointDetail">
      <!-- 基本信息 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">基本信息</text>
          <status-tag :status="pointDetail.status" type="point" size="medium" />
        </view>
        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">点位名称</text>
            <text class="info-value">{{ pointDetail.name }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">点位编号</text>
            <text class="info-value">{{ pointDetail.code }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">所属项目</text>
            <text class="info-value">{{ pointDetail.projectName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">排口类型</text>
            <text class="info-value">{{ pointDetail.outfallType }}</text>
          </view>
          <view class="info-item full-width">
            <text class="info-label">点位地址</text>
            <text class="info-value">{{ pointDetail.address || '未设置' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">经度</text>
            <text class="info-value">{{ pointDetail.lng || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">纬度</text>
            <text class="info-value">{{ pointDetail.lat || '-' }}</text>
          </view>
        </view>
      </view>
      
      <!-- 采集结果 -->
      <view v-if="surveyResult" class="section">
        <view class="section-header">
          <text class="section-title">采集结果</text>
          <status-tag :status="surveyResult.status" type="result" size="medium" />
        </view>
        <view class="result-info">
          <view class="result-item">
            <text class="result-label">采集人</text>
            <text class="result-value">{{ surveyResult.collectorName }}</text>
          </view>
          <view class="result-item">
            <text class="result-label">采集时间</text>
            <text class="result-value">{{ surveyResult.collectTime }}</text>
          </view>
          <view class="result-item">
            <text class="result-label">提交时间</text>
            <text class="result-value">{{ surveyResult.submitTime || '-' }}</text>
          </view>
          <view v-if="surveyResult.rejectReason" class="result-item reject">
            <text class="result-label">驳回原因</text>
            <text class="result-value">{{ surveyResult.rejectReason }}</text>
          </view>
        </view>
        
        <!-- 现场照片 -->
        <view v-if="surveyResult.photos && surveyResult.photos.length > 0" class="photo-section">
          <text class="photo-title">现场照片</text>
          <view class="photo-grid">
            <image 
              v-for="(photo, index) in surveyResult.photos" 
              :key="index"
              :src="photo.url"
              mode="aspectFill"
              class="photo-item"
              @click="previewPhoto(index)"
            />
          </view>
        </view>
      </view>
      
      <!-- 审核记录 -->
      <view v-if="auditRecords.length > 0" class="section">
        <view class="section-header">
          <text class="section-title">审核记录</text>
        </view>
        <view class="audit-list">
          <view 
            v-for="record in auditRecords" 
            :key="record.id"
            class="audit-item"
          >
            <view class="audit-header">
              <text class="audit-status" :class="record.status">{{ record.statusText }}</text>
              <text class="audit-time">{{ record.auditTime }}</text>
            </view>
            <view class="audit-content">
              <text class="audit-person">审核人: {{ record.auditorName }}</text>
              <text v-if="record.comment" class="audit-comment">{{ record.comment }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 底部操作按钮 -->
      <view class="bottom-actions">
        <button 
          v-if="pointDetail.status === '待采集'" 
          class="btn btn-primary"
          @click="startSurvey"
        >
          开始采集
        </button>
        <button 
          v-else-if="pointDetail.status === '草稿中'" 
          class="btn btn-warning"
          @click="continueSurvey"
        >
          继续填报
        </button>
        <button 
          v-else-if="pointDetail.status === '驳回待修改'" 
          class="btn btn-danger"
          @click="modifySurvey"
        >
          修改重提
        </button>
        <button 
          v-else-if="pointDetail.status === '审核通过'" 
          class="btn btn-default"
          @click="viewResult"
        >
          查看结果
        </button>
      </view>
    </template>
    
    <view v-else class="empty-state">
      <text class="empty-icon">📭</text>
      <text class="empty-text">点位不存在</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pointApi, resultApi, auditApi } from '@/utils/api'
import StatusTag from '@/components/status-tag/status-tag.vue'

const loading = ref(true)
const pointId = ref(null)
const pointDetail = ref(null)
const surveyResult = ref(null)
const auditRecords = ref([])

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  pointId.value = currentPage.options.id
  
  if (pointId.value) {
    loadPointDetail()
  } else {
    loading.value = false
  }
})

// 加载点位详情
async function loadPointDetail() {
  try {
    pointDetail.value = await pointApi.getDetail(pointId.value)
    
    // 加载采集结果
    try {
      surveyResult.value = await resultApi.getDetail(pointId.value)
    } catch (e) {
      // 没有采集结果
    }
    
    // 加载审核记录
    try {
      const records = await auditApi.getList({ pointId: pointId.value })
      auditRecords.value = records.list || records || []
    } catch (e) {
      // 没有审核记录
    }
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 开始采集
function startSurvey() {
  uni.navigateTo({ url: `/pages/survey/survey?id=${pointId.value}&mode=new` })
}

// 继续填报
function continueSurvey() {
  uni.navigateTo({ url: `/pages/survey/survey?id=${pointId.value}&mode=draft` })
}

// 修改重提
function modifySurvey() {
  uni.navigateTo({ url: `/pages/survey/survey?id=${pointId.value}&mode=modify` })
}

// 查看结果
function viewResult() {
  uni.navigateTo({ url: `/pages/survey/survey?id=${pointId.value}&mode=view` })
}

// 预览照片
function previewPhoto(index) {
  const urls = surveyResult.value.photos.map(p => p.url)
  uni.previewImage({
    current: urls[index],
    urls
  })
}
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background-color: #F5F7FA;
  padding-bottom: 80px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.section {
  background-color: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item.full-width {
  grid-column: span 2;
}

.info-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.result-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-item.reject {
  background-color: #FEF0F0;
  padding: 8px 12px;
  border-radius: 6px;
}

.result-label {
  font-size: 13px;
  color: #666;
}

.result-value {
  font-size: 13px;
  color: #333;
}

.photo-section {
  margin-top: 16px;
}

.photo-title {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 12px;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.photo-item {
  width: 100%;
  height: 100px;
  border-radius: 8px;
}

.audit-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.audit-item {
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.audit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.audit-status {
  font-size: 13px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 4px;
}

.audit-status.approved {
  color: #67C23A;
  background-color: #F0F9EB;
}

.audit-status.rejected {
  color: #F56C6C;
  background-color: #FEF0F0;
}

.audit-time {
  font-size: 12px;
  color: #999;
}

.audit-person {
  font-size: 13px;
  color: #666;
}

.audit-comment {
  font-size: 13px;
  color: #333;
  margin-top: 4px;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background-color: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.btn {
  width: 100%;
  height: 44px;
  line-height: 44px;
  font-size: 16px;
  border: none;
  border-radius: 8px;
}

.btn-primary {
  background-color: #409EFF;
  color: #fff;
}

.btn-warning {
  background-color: #E6A23C;
  color: #fff;
}

.btn-danger {
  background-color: #F56C6C;
  color: #fff;
}

.btn-default {
  background-color: #f5f5f5;
  color: #666;
}
</style>
