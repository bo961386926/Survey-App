/**
 * 图片上传组件
 * 支持拍照、相册选择、预览、删除
 */
<template>
  <view class="image-uploader">
    <!-- 图片列表 -->
    <view class="image-list">
      <view 
        v-for="(image, index) in imageList" 
        :key="index" 
        class="image-item"
      >
        <image 
          :src="image.url || image.path" 
          mode="aspectFill" 
          class="image"
          @click="previewImage(index)"
        />
        <view class="delete-btn" @click="deleteImage(index)">
          <text class="delete-icon">×</text>
        </view>
        <!-- 上传进度/状态 -->
        <view v-if="image.uploading" class="uploading-mask">
          <text class="uploading-text">上传中...</text>
        </view>
        <view v-if="image.error" class="error-mask" @click="retryUpload(index)">
          <text class="error-text">重试</text>
        </view>
      </view>
      
      <!-- 添加按钮 -->
      <view 
        v-if="imageList.length < maxCount" 
        class="add-btn"
        @click="showActionSheet"
      >
        <text class="add-icon">+</text>
        <text class="add-text">添加图片</text>
      </view>
    </view>
    
    <!-- 底部提示 -->
    <view class="tip-text" v-if="tipText">
      <text>{{ tipText }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { upload } from '@/utils/api'

const props = defineProps({
  // 已上传的图片列表
  modelValue: {
    type: Array,
    default: () => []
  },
  // 最大数量
  maxCount: {
    type: Number,
    default: 9
  },
  // 上传地址
  uploadUrl: {
    type: String,
    default: '/file/upload'
  },
  // 是否仅允许相机拍摄
  cameraOnly: {
    type: Boolean,
    default: false
  },
  // 提示文本
  tipText: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const imageList = ref([...props.modelValue])

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  imageList.value = [...newVal]
})

// 提示文本
const tipText = computed(() => {
  if (props.tipText) return props.tipText
  return `最多可上传${props.maxCount}张图片`
})

// 显示操作面板
function showActionSheet() {
  const itemList = []
  if (!props.cameraOnly) {
    itemList.push('从相册选择')
  }
  itemList.push('拍照')
  
  uni.showActionSheet({
    itemList,
    success: (res) => {
      if (!props.cameraOnly && res.tapIndex === 0) {
        chooseFromAlbum()
      } else {
        takePhoto()
      }
    }
  })
}

// 从相册选择
function chooseFromAlbum() {
  const remainCount = props.maxCount - imageList.value.length
  if (remainCount <= 0) return
  
  uni.chooseImage({
    count: remainCount,
    sizeType: ['compressed'],
    sourceType: ['album'],
    success: (res) => {
      handleImages(res.tempFilePaths)
    }
  })
}

// 拍照
function takePhoto() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['camera'],
    success: (res) => {
      handleImages(res.tempFilePaths)
    }
  })
}

// 处理选择的图片
async function handleImages(tempFilePaths) {
  for (const path of tempFilePaths) {
    const image = {
      path,
      url: path,
      uploading: true,
      error: false
    }
    imageList.value.push(image)
    
    // 上传图片
    await uploadImage(imageList.value.length - 1, path)
  }
  
  emitChange()
}

// 上传图片
async function uploadImage(index, filePath) {
  try {
    const result = await upload(props.uploadUrl, filePath)
    imageList.value[index] = {
      ...imageList.value[index],
      url: result.url,
      uploading: false,
      error: false
    }
  } catch (error) {
    imageList.value[index].uploading = false
    imageList.value[index].error = true
    uni.showToast({
      title: '上传失败',
      icon: 'none'
    })
  }
}

// 重试上传
async function retryUpload(index) {
  const image = imageList.value[index]
  image.uploading = true
  image.error = false
  
  await uploadImage(index, image.path)
  emitChange()
}

// 删除图片
function deleteImage(index) {
  uni.showModal({
    title: '提示',
    content: '确定删除这张图片吗？',
    success: (res) => {
      if (res.confirm) {
        imageList.value.splice(index, 1)
        emitChange()
      }
    }
  })
}

// 预览图片
function previewImage(index) {
  const urls = imageList.value.map(img => img.url || img.path)
  uni.previewImage({
    current: urls[index],
    urls
  })
}

// 触发变化事件
function emitChange() {
  const uploadedList = imageList.value
    .filter(img => img.url && !img.uploading && !img.error)
    .map(img => ({ url: img.url }))
  
  emit('update:modelValue', uploadedList)
  emit('change', uploadedList)
}
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 24px;
  height: 24px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 0 8px 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-icon {
  color: #fff;
  font-size: 18px;
  line-height: 1;
}

.uploading-mask,
.error-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.uploading-text,
.error-text {
  color: #fff;
  font-size: 12px;
}

.add-btn {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  border: 1px dashed #ddd;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.add-icon {
  font-size: 32px;
  color: #999;
  line-height: 1;
}

.add-text {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.tip-text {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}
</style>
