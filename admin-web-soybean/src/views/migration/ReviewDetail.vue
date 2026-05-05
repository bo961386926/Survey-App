<template>
  <div class="review-detail">
    <a-page-header title="审核详情" style="padding:16px 24px" />
    <a-card style="margin:16px">
      <div v-if="loading">加载中...</div>
      <div v-else>
        <a-descriptions bordered column="1">
          <a-descriptions-item label="结果ID">{{ detail.id }}</a-descriptions-item>
          <a-descriptions-item label="点位">{{ detail.pointName }} （ID: {{ detail.pointId }}）</a-descriptions-item>
          <a-descriptions-item label="提交人">{{ detail.creatorName }}</a-descriptions-item>
          <a-descriptions-item label="提交时间">{{ detail.createTime }}</a-descriptions-item>
          <a-descriptions-item label="状态">{{ detail.statusText || detail.status }}</a-descriptions-item>
        </a-descriptions>

        <a-divider />

        <div v-if="detail.files && detail.files.length">
          <h4>附件</h4>
          <a-list :data-source="detail.files">
            <template #renderItem="{ item }">
              <a-list-item>
                <a href="" :href="item.url" target="_blank">{{ item.name || item.fileName }}</a>
              </a-list-item>
            </template>
          </a-list>
        </div>

        <a-divider />

        <a-form layout="vertical">
          <a-form-item label="审核意见">
            <a-textarea v-model:value="comment" rows="4" />
          </a-form-item>
          <div style="display:flex;gap:12px">
            <a-button type="primary" @click="handlePass">通过</a-button>
            <a-button danger @click="handleReject">驳回</a-button>
          </div>
        </a-form>
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getResultById, passAudit, rejectAudit } from '@/api/result'

const route = useRoute()
const loading = ref(false)
const detail = ref({})
const comment = ref('')

async function load(id) {
  try {
    loading.value = true
    const res = await getResultById(id)
    detail.value = res?.data || res || {}
  } catch (err) {
    console.error(err)
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const id = route.query.id || route.params.id
  if (id) load(id)
})

const handlePass = async () => {
  if (!detail.value.id) return message.error('无效的结果')
  try {
    await passAudit(detail.value.id, comment.value)
    message.success('审核通过')
  } catch (err) {
    console.error(err)
    message.error('操作失败')
  }
}

const handleReject = async () => {
  if (!detail.value.id) return message.error('无效的结果')
  try {
    await rejectAudit(detail.value.id, comment.value)
    message.success('已驳回')
  } catch (err) {
    console.error(err)
    message.error('操作失败')
  }
}
</script>

<style scoped>
.review-detail { padding-bottom:40px }
</style>
