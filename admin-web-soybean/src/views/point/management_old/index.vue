<template>
  <div class="p-6 max-w-1400px mx-auto">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-24px font-bold text-[var(--color-text-primary)] mb-1">点位管理</h1>
        <p class="text-14px text-[var(--color-text-secondary)]">管理和查看所有排污口勘察点位信息</p>
      </div>
      <div class="flex gap-3">
        <a-button type="default" style="border-radius: 6px; border-color: var(--color-border); color: var(--color-text-primary)">
          <template #icon><DownloadOutlined /></template>
          导出清单
        </a-button>
        <a-button type="primary" style="border-radius: 6px; background: var(--color-primary);">
          <template #icon><PlusOutlined /></template>
          新建点位
        </a-button>
      </div>
    </div>

    <!-- Stats Bar -->
    <div class="flex gap-4 mb-6">
      <div class="flex items-center gap-2.5 p-3 px-5 rounded-8px cursor-pointer transition-all bg-[var(--bg-card)] border border-[var(--color-primary)] shadow-[var(--shadow-card)]" style="background: rgba(22,119,255,0.1)">
        <div class="w-2 h-2 rounded-full bg-[var(--color-primary)]"></div>
        <span class="text-13px text-[var(--color-text-secondary)]">全部点位</span>
        <span class="text-16px font-bold text-[var(--color-text-primary)]">1,284</span>
      </div>
      <div class="flex items-center gap-2.5 p-3 px-5 rounded-8px cursor-pointer transition-all bg-[var(--bg-card)] border border-[var(--color-border)] shadow-[var(--shadow-card)] hover:border-[var(--color-primary)]">
        <div class="w-2 h-2 rounded-full bg-[var(--color-warning)]"></div>
        <span class="text-13px text-[var(--color-text-secondary)]">待审核</span>
        <span class="text-16px font-bold text-[var(--color-text-primary)]">42</span>
      </div>
      <div class="flex items-center gap-2.5 p-3 px-5 rounded-8px cursor-pointer transition-all bg-[var(--bg-card)] border border-[var(--color-border)] shadow-[var(--shadow-card)] hover:border-[var(--color-primary)]">
        <div class="w-2 h-2 rounded-full bg-[var(--color-success)]"></div>
        <span class="text-13px text-[var(--color-text-secondary)]">已通过</span>
        <span class="text-16px font-bold text-[var(--color-text-primary)]">956</span>
      </div>
      <div class="flex items-center gap-2.5 p-3 px-5 rounded-8px cursor-pointer transition-all bg-[var(--bg-card)] border border-[var(--color-border)] shadow-[var(--shadow-card)] hover:border-[var(--color-primary)]">
        <div class="w-2 h-2 rounded-full bg-[var(--color-danger)]"></div>
        <span class="text-13px text-[var(--color-text-secondary)]">已驳回</span>
        <span class="text-16px font-bold text-[var(--color-text-primary)]">18</span>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4 px-5 mb-6 flex items-center gap-4 flex-wrap shadow-[var(--shadow-card)]">
      <div class="flex items-center gap-2">
        <span class="text-13px text-[var(--color-text-secondary)] font-medium whitespace-nowrap">项目标段</span>
        <a-select v-model:value="filters.section" placeholder="全部标段" class="w-140px">
          <a-select-option value="">全部标段</a-select-option>
          <a-select-option value="1">第一标段</a-select-option>
          <a-select-option value="2">第二标段</a-select-option>
        </a-select>
      </div>
      
      <div class="w-1px h-7 bg-[var(--color-divider)]"></div>
      
      <div class="flex items-center gap-2">
        <span class="text-13px text-[var(--color-text-secondary)] font-medium whitespace-nowrap">排口类型</span>
        <a-select v-model:value="filters.type" placeholder="全部类型" class="w-140px">
          <a-select-option value="">全部类型</a-select-option>
          <a-select-option value="rainwater">雨水排口</a-select-option>
          <a-select-option value="sewage">污水排口</a-select-option>
          <a-select-option value="mixed">雨污混排</a-select-option>
        </a-select>
      </div>
      
      <div class="w-1px h-7 bg-[var(--color-divider)]"></div>
      
      <div class="flex items-center gap-2">
        <span class="text-13px text-[var(--color-text-secondary)] font-medium whitespace-nowrap">状态</span>
        <a-select v-model:value="filters.status" placeholder="全部状态" class="w-140px">
          <a-select-option value="">全部状态</a-select-option>
          <a-select-option value="pending">待审核</a-select-option>
          <a-select-option value="approved">已通过</a-select-option>
          <a-select-option value="rejected">已驳回</a-select-option>
        </a-select>
      </div>

      <div class="flex-1 min-w-200px ml-auto">
        <a-input v-model:value="filters.keyword" placeholder="搜索点位名称、编号..." allow-clear>
          <template #prefix><SearchOutlined class="text-[var(--color-text-secondary)]" /></template>
        </a-input>
      </div>
    </div>

    <!-- Table Card -->
    <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px overflow-hidden shadow-[var(--shadow-card)]">
      <div class="flex items-center justify-between p-4 px-5 border-b border-[var(--color-border)]">
        <div class="text-16px font-medium text-[var(--color-text-primary)]">点位列表</div>
        <div class="flex gap-2">
          <a-button class="rounded-4px">
            <template #icon><FilterOutlined /></template>
            高级筛选
          </a-button>
          <a-button class="rounded-4px">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </div>
      </div>

      <a-table 
        :columns="columns" 
        :data-source="tableData" 
        :pagination="false"
        class="custom-antd-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'id'">
            <span class="font-mono text-[var(--color-primary)]">{{ record.id }}</span>
          </template>
          <template v-if="column.key === 'name'">
            <span class="font-medium text-[var(--color-text-primary)]">{{ record.name }}</span>
          </template>
          <template v-if="column.key === 'type'">
            <span 
              class="inline-flex items-center px-2 py-0.5 rounded-4px text-12px"
              :class="{
                'bg-[rgba(22,119,255,0.1)] text-[var(--color-primary)]': record.typeCode === 'rainwater',
                'bg-[rgba(139,92,246,0.1)] text-[#8B5CF6]': record.typeCode === 'sewage',
                'bg-[rgba(255,125,0,0.1)] text-[var(--color-warning)]': record.typeCode === 'mixed'
              }"
            >
              {{ record.type }}
            </span>
          </template>
          <template v-if="column.key === 'location'">
            <span class="text-13px text-[var(--color-text-secondary)]">{{ record.location }}</span>
          </template>
          <template v-if="column.key === 'assignee'">
            <div class="flex items-center gap-2">
              <div 
                class="w-6 h-6 rounded-4px flex items-center justify-center text-12px text-white"
                :class="{
                  'bg-[var(--color-primary)]': record.assigneeColor === 'blue',
                  'bg-[var(--color-success)]': record.assigneeColor === 'green',
                  'bg-[var(--color-warning)]': record.assigneeColor === 'orange'
                }"
              >
                {{ record.assigneeInitials }}
              </div>
              <span class="text-13px text-[var(--color-text-secondary)]">{{ record.assignee }}</span>
            </div>
          </template>
          <template v-if="column.key === 'status'">
            <span 
              class="inline-flex items-center gap-1 px-2 py-0.5 rounded-4px text-12px"
              :class="{
                'bg-[rgba(255,125,0,0.1)] text-[var(--color-warning)]': record.statusCode === 'review',
                'bg-[rgba(0,180,42,0.1)] text-[var(--color-success)]': record.statusCode === 'approved',
                'bg-[rgba(245,63,63,0.1)] text-[var(--color-danger)]': record.statusCode === 'rejected'
              }"
            >
              <div class="w-1.5 h-1.5 rounded-full bg-current"></div>
              {{ record.status }}
            </span>
          </template>
          <template v-if="column.key === 'version'">
            <span class="font-mono text-12px text-[var(--color-text-secondary)] bg-[var(--bg-hover)] px-1.5 py-0.5 rounded-4px">
              {{ record.version }}
            </span>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="viewDetail(record)">详情</a-button>
          </template>
        </template>
      </a-table>

      <div class="flex items-center justify-between p-4 px-5">
        <div class="text-13px text-[var(--color-text-secondary)]">显示 1 到 10 条，共 1,284 条记录</div>
        <a-pagination :total="1284" :show-size-changer="false" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { 
  PlusOutlined, 
  DownloadOutlined, 
  SearchOutlined, 
  FilterOutlined, 
  ReloadOutlined 
} from '@ant-design/icons-vue';

defineOptions({ name: 'PointList' });

const router = useRouter();

const filters = ref({
  section: '',
  type: '',
  status: '',
  keyword: ''
});

const columns = [
  { title: '点位编号', dataIndex: 'id', key: 'id', width: 120 },
  { title: '点位名称', dataIndex: 'name', key: 'name' },
  { title: '排口类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '所在位置', dataIndex: 'location', key: 'location' },
  { title: '采集人', dataIndex: 'assignee', key: 'assignee', width: 150 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '版本', dataIndex: 'version', key: 'version', width: 100 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' as const }
];

const tableData = ref([
  {
    key: '1',
    id: 'P-2023-001',
    name: '滨江路1号排口',
    type: '雨水排口',
    typeCode: 'rainwater',
    location: '滨江区滨江路与江虹路交叉口',
    assignee: '张三',
    assigneeInitials: '张',
    assigneeColor: 'blue',
    status: '待审核',
    statusCode: 'review',
    version: 'v1.0'
  },
  {
    key: '2',
    id: 'P-2023-002',
    name: '工业园南区排口',
    type: '污水排口',
    typeCode: 'sewage',
    location: '工业园区南门向东200米',
    assignee: '李四',
    assigneeInitials: '李',
    assigneeColor: 'green',
    status: '已通过',
    statusCode: 'approved',
    version: 'v2.1'
  },
  {
    key: '3',
    id: 'P-2023-003',
    name: '老城区混排口A',
    type: '雨污混排',
    typeCode: 'mixed',
    location: '解放路与中山路交汇处地下暗渠',
    assignee: '王五',
    assigneeInitials: '王',
    assigneeColor: 'orange',
    status: '已驳回',
    statusCode: 'rejected',
    version: 'v1.2'
  }
]);

const viewDetail = (record: any) => {
  // Use elegant router routing
  if (record.statusCode === 'review') {
    router.push(`/audit/detail/${record.id}`);
  } else {
    router.push(`/audit/detail/${record.id}`);
  }
};
</script>

<style scoped>
.custom-antd-table :deep(.ant-table-thead > tr > th) {
  background-color: var(--bg-card-alt);
  color: var(--color-text-secondary);
  font-weight: 500;
  border-bottom: 1px solid var(--color-divider);
}

.custom-antd-table :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid var(--color-divider);
}

.custom-antd-table :deep(.ant-table-tbody > tr:hover > td) {
  background-color: var(--bg-hover);
}
</style>
