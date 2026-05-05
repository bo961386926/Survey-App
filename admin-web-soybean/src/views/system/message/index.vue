<template>
  <div class="h-full flex flex-col bg-[var(--bg-page)] p-6">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-24px font-bold text-[var(--color-text-primary)] mb-1">消息中心</h1>
        <p class="text-14px text-[var(--color-text-secondary)]">
          共 <span class="font-600 text-[var(--color-text-primary)]">{{ totalCount }}</span> 条消息
        </p>
      </div>
      <a-input
        v-model:value="searchKeyword"
        placeholder="搜索消息内容..."
        class="w-280px"
        allow-clear
      >
        <template #prefix><SearchOutlined class="text-[var(--color-text-secondary)]" /></template>
      </a-input>
    </div>

    <!-- Filter Bar -->
    <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4 mb-6 shadow-[var(--shadow-card)]">
      <!-- Message Type -->
      <div class="flex items-center gap-3 mb-3">
        <span class="text-13px text-[var(--color-text-secondary)] font-medium whitespace-nowrap">类型</span>
        <div class="flex gap-2 flex-wrap">
          <a-tag
            :color="activeType === 'all' ? 'blue' : 'default'"
            :style="activeType === 'all' ? { background: 'var(--color-primary)', color: '#fff', borderColor: 'var(--color-primary)' } : {}"
            class="cursor-pointer px-3 py-1"
            @click="activeType = 'all'"
          >
            全部消息 <span class="ml-1 text-12px">{{ typeCounts.all }}</span>
          </a-tag>
          <a-tag
            :color="activeType === 'approved' ? 'success' : 'default'"
            :style="activeType === 'approved' ? { background: 'rgba(0,180,42,0.1)', color: 'var(--color-success)', borderColor: 'rgba(0,180,42,0.3)' } : {}"
            class="cursor-pointer px-3 py-1"
            @click="activeType = 'approved'"
          >
            <span class="inline-block w-6px h-6px rounded-full bg-[var(--color-success)] mr-1"></span>
            审核通过 <span class="ml-1 text-12px">{{ typeCounts.approved }}</span>
          </a-tag>
          <a-tag
            :color="activeType === 'rejected' ? 'error' : 'default'"
            :style="activeType === 'rejected' ? { background: 'rgba(245,63,63,0.1)', color: 'var(--color-danger)', borderColor: 'rgba(245,63,63,0.3)' } : {}"
            class="cursor-pointer px-3 py-1"
            @click="activeType = 'rejected'"
          >
            <span class="inline-block w-6px h-6px rounded-full bg-[var(--color-danger)] mr-1"></span>
            审核驳回 <span class="ml-1 text-12px">{{ typeCounts.rejected }}</span>
          </a-tag>
          <a-tag
            :color="activeType === 'submitted' ? 'processing' : 'default'"
            :style="activeType === 'submitted' ? { background: 'rgba(22,119,255,0.1)', color: 'var(--color-primary)', borderColor: 'rgba(22,119,255,0.3)' } : {}"
            class="cursor-pointer px-3 py-1"
            @click="activeType = 'submitted'"
          >
            <span class="inline-block w-6px h-6px rounded-full bg-[var(--color-primary)] mr-1"></span>
            勘查提交 <span class="ml-1 text-12px">{{ typeCounts.submitted }}</span>
          </a-tag>
          <a-tag
            :color="activeType === 'reminder' ? 'warning' : 'default'"
            :style="activeType === 'reminder' ? { background: 'rgba(255,193,7,0.1)', color: 'var(--color-warning)', borderColor: 'rgba(255,193,7,0.3)' } : {}"
            class="cursor-pointer px-3 py-1"
            @click="activeType = 'reminder'"
          >
            <span class="inline-block w-6px h-6px rounded-full bg-[var(--color-warning)] mr-1"></span>
            待审提醒 <span class="ml-1 text-12px">{{ typeCounts.reminder }}</span>
          </a-tag>
        </div>
      </div>

      <!-- Status -->
      <div class="flex items-center gap-3">
        <span class="text-13px text-[var(--color-text-secondary)] font-medium whitespace-nowrap">状态</span>
        <div class="flex gap-2">
          <a-tag
            :color="activeStatus === 'all' ? 'blue' : 'default'"
            :style="activeStatus === 'all' ? { background: 'var(--color-primary)', color: '#fff', borderColor: 'var(--color-primary)' } : {}"
            class="cursor-pointer px-3 py-1"
            @click="activeStatus = 'all'"
          >
            全部 <span class="ml-1 text-12px">{{ statusCounts.all }}</span>
          </a-tag>
          <a-tag
            :color="activeStatus === 'unread' ? 'default' : 'default'"
            :style="activeStatus === 'unread' ? { background: '#f5f5f5', color: '#666', borderColor: '#d9d9d9' } : {}"
            class="cursor-pointer px-3 py-1"
            @click="activeStatus = 'unread'"
          >
            未读 <span class="ml-1 text-12px">{{ statusCounts.unread }}</span>
          </a-tag>
          <a-tag
            :color="activeStatus === 'read' ? 'default' : 'default'"
            :style="activeStatus === 'read' ? { background: '#f5f5f5', color: '#666', borderColor: '#d9d9d9' } : {}"
            class="cursor-pointer px-3 py-1"
            @click="activeStatus = 'read'"
          >
            已读 <span class="ml-1 text-12px">{{ statusCounts.read }}</span>
          </a-tag>
        </div>
      </div>
    </div>

    <!-- Message List or Empty State -->
    <div class="flex-1 bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px overflow-hidden shadow-[var(--shadow-card)]">
      <!-- Empty State -->
      <div v-if="filteredMessages.length === 0" class="h-full flex flex-col items-center justify-center p-8">
        <div class="w-64px h-64px rounded-full bg-[rgba(22,119,255,0.1)] flex items-center justify-center mb-4">
          <BellOutlined class="text-32px text-[var(--color-text-disabled)]" />
        </div>
        <div class="text-16px font-500 text-[var(--color-text-primary)] mb-2">暂无消息</div>
        <div class="text-13px text-[var(--color-text-secondary)] mb-6">消息会在有审核、提交等操作时自动产生</div>
        <a-button type="link" @click="goToAudit">
          <template #icon><RightOutlined /></template>
          去勘查审核页面看看
        </a-button>
      </div>

      <!-- Message List -->
      <div v-else class="divide-y divide-[var(--color-border)]">
        <div
          v-for="msg in filteredMessages"
          :key="msg.id"
          class="p-4 hover:bg-[var(--bg-hover)] cursor-pointer transition-all"
          :class="{ 'bg-[rgba(22,119,255,0.02)]': !msg.read }"
          @click="handleMessageClick(msg)"
        >
          <div class="flex items-start gap-3">
            <!-- Status Dot -->
            <div class="mt-2">
              <div v-if="!msg.read" class="w-8px h-8px rounded-full bg-[var(--color-primary)]"></div>
              <div v-else class="w-8px h-8px rounded-full bg-transparent"></div>
            </div>

            <!-- Content -->
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-2">
                <a-tag
                  :color="msg.typeColor"
                  :style="{ background: msg.typeBg, color: msg.typeColor, borderColor: msg.typeBorderColor }"
                  class="px-2 py-0.5 text-12px"
                >
                  {{ msg.typeLabel }}
                </a-tag>
                <span class="text-14px font-500 text-[var(--color-text-primary)]">{{ msg.title }}</span>
              </div>
              <div class="text-13px text-[var(--color-text-secondary)] mb-2">{{ msg.content }}</div>
              <div class="text-12px text-[var(--color-text-secondary)]">{{ msg.time }}</div>
            </div>

            <!-- Actions -->
            <div class="flex gap-2">
              <a-button v-if="!msg.read" type="link" size="small" @click.stop="markAsRead(msg)">
                标记已读
              </a-button>
              <a-button type="link" size="small" danger @click.stop="deleteMessage(msg)">
                删除
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { SearchOutlined, BellOutlined, RightOutlined } from '@ant-design/icons-vue';

defineOptions({ name: 'SystemMessage' });

const router = useRouter();

const totalCount = ref(0);
const searchKeyword = ref('');
const activeType = ref('all');
const activeStatus = ref('all');

const typeCounts = ref({
  all: 0,
  approved: 0,
  rejected: 0,
  submitted: 0,
  reminder: 0
});

const statusCounts = ref({
  all: 0,
  unread: 0,
  read: 0
});

const messages = ref<any[]>([]);

const filteredMessages = computed(() => {
  let result = messages.value;

  // Filter by type
  if (activeType.value !== 'all') {
    result = result.filter(msg => msg.type === activeType.value);
  }

  // Filter by status
  if (activeStatus.value !== 'all') {
    result = result.filter(msg => msg.read === (activeStatus.value === 'read'));
  }

  // Filter by keyword
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(msg =>
      msg.title.toLowerCase().includes(keyword) ||
      msg.content.toLowerCase().includes(keyword)
    );
  }

  return result;
});

const handleMessageClick = (msg: any) => {
  if (!msg.read) {
    markAsRead(msg);
  }
  // Navigate to related page
  if (msg.relatedUrl) {
    router.push(msg.relatedUrl);
  }
};

const markAsRead = (msg: any) => {
  msg.read = true;
  updateCounts();
};

const deleteMessage = (msg: any) => {
  const index = messages.value.findIndex(m => m.id === msg.id);
  if (index > -1) {
    messages.value.splice(index, 1);
    updateCounts();
  }
};

const updateCounts = () => {
  totalCount.value = messages.value.length;
  statusCounts.value.all = messages.value.length;
  statusCounts.value.unread = messages.value.filter(m => !m.read).length;
  statusCounts.value.read = messages.value.filter(m => m.read).length;

  typeCounts.value.all = messages.value.length;
  typeCounts.value.approved = messages.value.filter(m => m.type === 'approved').length;
  typeCounts.value.rejected = messages.value.filter(m => m.type === 'rejected').length;
  typeCounts.value.submitted = messages.value.filter(m => m.type === 'submitted').length;
  typeCounts.value.reminder = messages.value.filter(m => m.type === 'reminder').length;
};

const goToAudit = () => {
  router.push('/audit/list');
};
</script>

<style scoped>
.divide-y > :not([hidden]) ~ :not([hidden]) {
  border-top-width: 1px;
  border-top-color: var(--color-border);
}
</style>
