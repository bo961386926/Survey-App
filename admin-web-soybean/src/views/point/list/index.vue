<template>
  <div class="h-full flex flex-col bg-[var(--bg-page)] p-24px overflow-hidden">
    <!-- Header Section -->
    <div class="flex-shrink-0 flex items-center justify-between mb-16px">
      <div>
        <h1 class="text-18px font-600 text-[var(--color-text-primary)] mb-4px">点位管理</h1>
        <p class="text-13px text-[var(--color-text-secondary)]">管理所有勘察点位，查看分布与状态</p>
      </div>
      <div class="flex gap-12px">
        <!-- Mode Toggle Switcher -->
        <a-radio-group v-model:value="viewMode" button-style="solid" class="view-mode-group">
          <a-radio-button value="list">
            <template #icon><UnorderedListOutlined class="mr-4px" /></template>
            列表
          </a-radio-button>
          <a-radio-button value="map">
            <template #icon><EnvironmentOutlined class="mr-4px" /></template>
            GIS 一张图
          </a-radio-button>
        </a-radio-group>

        <a-button class="rd-6px flex items-center gap-8px" @click="handleDownloadTemplate">
          <template #icon><DownloadOutlined /></template>
          导出Excel
        </a-button>
      </div>
    </div>

    <!-- Stats Summary Cards -->
    <div class="flex-shrink-0 grid grid-cols-4 gap-16px mb-16px">
      <div class="bg-[var(--bg-card)] rd-8px p-20px shadow-[var(--shadow-card)] flex items-center justify-between">
        <div>
          <div class="text-13px text-[var(--color-text-secondary)] mb-8px">总点位数</div>
          <div class="text-24px font-bold text-[var(--color-text-primary)]">18</div>
        </div>
        <div class="w-40px h-40px rd-8px bg-[rgba(22,119,255,0.1)] flex items-center justify-center">
          <EnvironmentOutlined class="text-20px text-[var(--color-primary)]" />
        </div>
      </div>
      <div class="bg-[var(--bg-card)] rd-8px p-20px shadow-[var(--shadow-card)] flex items-center justify-between">
        <div>
          <div class="text-13px text-[var(--color-text-secondary)] mb-8px">未勘查</div>
          <div class="text-24px font-bold text-[var(--color-text-primary)]">5</div>
        </div>
        <div class="w-40px h-40px rd-8px bg-[rgba(255,125,0,0.1)] flex items-center justify-center">
          <ClockCircleOutlined class="text-20px text-[var(--color-warning)]" />
        </div>
      </div>
      <div class="bg-[var(--bg-card)] rd-8px p-20px shadow-[var(--shadow-card)] flex items-center justify-between">
        <div>
          <div class="text-13px text-[var(--color-text-secondary)] mb-8px">已提交</div>
          <div class="text-24px font-bold text-[var(--color-text-primary)]">7</div>
        </div>
        <div class="w-40px h-40px rd-8px bg-[rgba(22,119,255,0.1)] flex items-center justify-center">
          <SendOutlined class="text-20px text-[var(--color-primary)]" />
        </div>
      </div>
      <div class="bg-[var(--bg-card)] rd-8px p-20px shadow-[var(--shadow-card)] flex items-center justify-between">
        <div>
          <div class="text-13px text-[var(--color-text-secondary)] mb-8px">已通过</div>
          <div class="text-24px font-bold text-[var(--color-text-primary)]">4</div>
        </div>
        <div class="w-40px h-40px rd-8px bg-[rgba(0,180,42,0.1)] flex items-center justify-center">
          <CheckCircleOutlined class="text-20px text-[var(--color-success)]" />
        </div>
      </div>
    </div>

    <!-- Filter Bar Area -->
    <div class="flex-shrink-0 bg-[var(--bg-card)] rd-8px p-16px shadow-[var(--shadow-card)] mb-16px flex items-center gap-16px flex-wrap">
      <div class="w-180px">
        <a-select v-model:value="filters.projectId" placeholder="全部项目" class="w-full h-32px" @change="handleFilterChange">
          <a-select-option :value="undefined">全部项目</a-select-option>
          <a-select-option v-for="project in projectOptions" :key="project.id" :value="project.id">{{ project.projectName }}</a-select-option>
        </a-select>
      </div>
      <div class="w-240px">
        <a-input v-model:value="filters.keyword" placeholder="搜索点位名称或项目..." class="h-32px rd-4px" allow-clear @press-enter="handleFilterChange">
          <template #prefix><SearchOutlined class="text-[var(--color-text-secondary)]" /></template>
        </a-input>
      </div>

      <!-- Status Filter Toggle Buttons -->
      <div class="flex gap-8px">
        <div 
          v-for="status in statusOptions" 
          :key="status.value"
          class="px-12px h-32px rd-4px flex items-center cursor-pointer transition-all border"
          :class="filters.status === status.value ? 'bg-[var(--color-primary)] border-[var(--color-primary)] text-white' : 'bg-[var(--bg-card-alt)] border-[var(--color-border)] text-[var(--color-text-secondary)] hover:border-[var(--color-primary)]'"
          @click="filters.status = status.value; handleFilterChange()"
        >
          <div v-if="status.value" class="w-6px h-6px rd-full mr-8px" :class="filters.status === status.value ? 'bg-white' : status.dotClass"></div>
          <span class="text-13px">{{ status.label }}</span>
        </div>
      </div>

      <!-- Batch Action Button (Conditional) -->
      <div v-if="selectedRowKeys.length > 0" class="ml-auto flex items-center gap-12px">
        <span class="text-13px text-[var(--color-primary)] font-500">已选 {{ selectedRowKeys.length }} 项</span>
        <a-button size="small" type="primary" @click="handleBatchAssign">批量分配</a-button>
        <a-button size="small" danger @click="handleBatchDelete">批量删除</a-button>
        <a-button size="small" type="text" @click="selectedRowKeys = []">取消</a-button>
      </div>
    </div>

    <!-- Main Content Container -->
    <div class="flex-1 overflow-hidden">
      <!-- 1. List View Mode -->
      <div v-if="viewMode === 'list'" class="flex-1 min-h-0 bg-[var(--bg-card)] rd-8px shadow-[var(--shadow-card)] overflow-hidden flex flex-col border border-[var(--color-border)]">
        <a-table 
          :columns="columns" 
          :data-source="tableData" 
          :row-key="record => record.pointId"
          :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: (keys) => selectedRowKeys = keys }"
          :pagination="{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showTotal: total => `共 ${total} 条数据`,
            onChange: handlePageChange
          }"
          :loading="loading"
          class="custom-point-table flex-1"
          :scroll="{ y: 'calc(100vh - 430px)' }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'name'">
              <div class="flex items-center gap-12px">
                <div class="w-32px h-32px rd-8px bg-[rgba(22,119,255,0.1)] flex items-center justify-center">
                  <EnvironmentOutlined class="text-16px text-[var(--color-primary)]" />
                </div>
                <span class="font-500 text-[var(--color-text-primary)] cursor-pointer hover:text-[var(--color-primary)]" @click="viewDetail(record)">
                  {{ record.pointName }}
                </span>
              </div>
            </template>
            <template v-if="column.key === 'project'">
              <div class="flex flex-col gap-2px">
                <span class="text-13px text-[var(--color-text-primary)]">{{ record.projectName || '--' }}</span>
                <span v-if="record.projectCode" class="text-11px font-mono text-[var(--color-text-secondary)] px-2px py-0.5 bg-[var(--bg-hover)] rd-2px border border-[var(--color-border)] w-fit">
                  {{ record.projectCode }}
                </span>
              </div>
            </template>
            <template v-if="column.key === 'projectCode'">
              <span class="font-mono text-12px text-[var(--color-text-secondary)]">{{ record.projectCode || '--' }}</span>
            </template>
            <template v-if="column.key === 'coordinates'">
              <span class="font-mono text-12px text-[var(--color-text-secondary)]">{{ record.longitude }}, {{ record.latitude }}</span>
            </template>
            <template v-if="column.key === 'status'">
              <div 
                class="inline-flex items-center gap-6px px-8px py-2px rd-12px text-12px border"
                :class="getStatusStyle(record.status)"
              >
                <div class="w-6px h-6px rd-full bg-current"></div>
                {{ getStatusText(record.status) }}
              </div>
            </template>
            <template v-if="column.key === 'createdAt'">
              <span class="text-12px text-[var(--color-text-secondary)]">{{ record.createTime?.split(' ')[0] || '2024-04-28' }}</span>
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="text" size="small" class="p-0!" @click="viewDetail(record)">
                <template #icon><EyeOutlined class="text-16px text-[var(--color-text-secondary)]" /></template>
              </a-button>
            </template>
          </template>
        </a-table>
      </div>

      <!-- 2. Map View Mode -->
      <div v-else class="flex-1 h-full min-h-0 flex gap-16px overflow-hidden">
        <!-- Map Sidebar Point List -->
        <div class="w-320px bg-[var(--bg-card)] rd-8px shadow-[var(--shadow-card)] border border-[var(--color-border)] flex flex-col overflow-hidden">
          <div class="p-16px border-b border-[var(--color-divider)]">
            <div class="flex items-center justify-between mb-12px">
              <div class="flex items-center gap-8px">
                <EnvironmentOutlined class="text-[var(--color-primary)]" />
                <span class="font-600 text-14px">点位列表</span>
                <span class="text-12px text-[var(--color-text-secondary)]">({{ pagination.total }})</span>
              </div>
            </div>
            <a-input v-model:value="filters.keyword" placeholder="搜索点位名称..." size="small" class="rd-4px" allow-clear @press-enter="handleFilterChange">
              <template #prefix><SearchOutlined class="text-12px text-[var(--color-text-secondary)]" /></template>
            </a-input>
          </div>
          
          <div class="flex-1 overflow-y-auto p-8px custom-scrollbar">
            <div 
              v-for="point in tableData" 
              :key="point.pointId"
              class="p-12px mb-8px rd-6px border border-transparent hover:border-[var(--color-primary)] hover:bg-[var(--bg-hover)] cursor-pointer transition-all group"
              :class="{ 'border-[var(--color-primary)] bg-[var(--bg-hover)]': activePointId === point.pointId }"
              @click="focusPoint(point)"
            >
              <div class="flex items-start justify-between mb-4px">
                <div class="flex items-center gap-6px">
                  <div class="w-6px h-6px rd-full" :class="getStatusDotClass(point.status)"></div>
                  <span class="text-14px font-500 text-[var(--color-text-primary)] group-hover:text-[var(--color-primary)]">{{ point.pointName }}</span>
                </div>
                <div class="text-11px text-[var(--color-success)] bg-[rgba(0,180,42,0.1)] px-4px rd-2px" v-if="point.status === 'approved'">已通过</div>
                <div class="text-11px text-[var(--color-primary)] bg-[rgba(22,119,255,0.1)] px-4px rd-2px" v-else-if="point.status === 'submitted'">已提交</div>
                <div class="text-11px text-[var(--color-text-secondary)] bg-[var(--bg-hover)] px-4px rd-2px border" v-else>未勘查</div>
              </div>
              <div class="text-12px text-[var(--color-text-secondary)] mb-4px line-clamp-1">{{ point.projectName }}</div>
              <div class="text-11px font-mono text-[var(--color-text-placeholder)]">{{ point.longitude }}, {{ point.latitude }}</div>
            </div>
          </div>
        </div>

        <!-- Map Main Canvas -->
        <div class="flex-1 bg-[var(--bg-card)] rd-8px shadow-[var(--shadow-card)] border border-[var(--color-border)] relative overflow-hidden">
          <AMapComponent
            ref="amapRef"
            :points="mapPoints"
            :center="mapCenter"
            :zoom="mapZoom"
            @marker-click="handleMarkerClick"
            class="w-full h-full"
          />
          
          <!-- Map Overlay UI Controls -->
          <div class="absolute top-16px left-16px z-10 flex flex-col gap-8px">
            <div class="bg-white rd-4px shadow-md border p-4px flex flex-col gap-4px">
              <a-button size="small" class="!w-24px !h-24px !p-0 flex-center" @click="mapZoom++"><PlusOutlined class="text-12px" /></a-button>
              <a-divider class="!m-0" />
              <a-button size="small" class="!w-24px !h-24px !p-0 flex-center" @click="mapZoom--"><MinusOutlined class="text-12px" /></a-button>
            </div>
          </div>

          <!-- Map Legend -->
          <div class="absolute bottom-16px right-16px z-10 bg-white/90 backdrop-blur-sm rd-4px shadow-sm border p-12px min-w-100px">
            <div class="text-12px font-600 text-[var(--color-text-primary)] mb-8px">点位状态</div>
            <div class="space-y-4px">
              <div class="flex items-center gap-8px text-11px text-[var(--color-text-secondary)]">
                <div class="w-6px h-6px rd-full bg-[var(--color-text-disabled)]"></div>
                <span>未勘查</span>
              </div>
              <div class="flex items-center gap-8px text-11px text-[var(--color-text-secondary)]">
                <div class="w-6px h-6px rd-full bg-[var(--color-primary)]"></div>
                <span>已提交</span>
              </div>
              <div class="flex items-center gap-8px text-11px text-[var(--color-text-secondary)]">
                <div class="w-6px h-6px rd-full bg-[var(--color-success)]"></div>
                <span>已通过</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <!-- Import Modal -->
    <a-modal v-model:open="importModalVisible" title="导入点位" @ok="handleImportExcel" :confirm-loading="importLoading">
      <div class="py-16px">
        <a-form layout="vertical">
          <a-form-item label="所属项目">
            <a-select v-model:value="importProjectId" placeholder="请选择项目" allow-clear>
              <a-select-option v-for="p in projectOptions" :key="p.id" :value="p.id">{{ p.projectName }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="选择文件">
            <a-upload
              :before-upload="() => false"
              :max-count="1"
              accept=".xlsx,.xls,.csv"
              @change="handleImportFileChange"
            >
              <a-button>
                <template #icon><InboxOutlined /></template>
                选择Excel/CSV文件
              </a-button>
            </a-upload>
          </a-form-item>
          <div class="text-12px text-[var(--color-text-secondary)]">
            <p>支持 .xlsx / .xls / .csv 格式</p>
            <a @click="handleDownloadTemplate" class="text-[var(--color-primary)] cursor-pointer">下载导入模板</a>
          </div>
        </a-form>
      </div>
    </a-modal>

    <!-- Batch Assign Modal -->
    <a-modal v-model:open="batchAssignModalVisible" title="批量分配点位" @ok="handleBatchAssignSubmit" :confirm-loading="batchAssignLoading">
      <div class="py-16px">
        <p class="mb-12px text-[var(--color-text-secondary)]">已选择 {{ selectedRowKeys.length }} 个点位，请输入分配人：</p>
        <a-input v-model:value="batchAssignee" placeholder="请输入分配人ID" />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { 
  UnorderedListOutlined, 
  EnvironmentOutlined, 
  DownloadOutlined,
  ClockCircleOutlined,
  SendOutlined,
  CheckCircleOutlined,
  SearchOutlined,
  FolderOutlined,
  EyeOutlined,
  PlusOutlined,
  MinusOutlined,
  InboxOutlined
} from '@ant-design/icons-vue';
import { fetchGetPointList, fetchImportPoints, fetchBatchAssignPoints, fetchDeletePoint } from '@/service/api';
import { fetchGetProjectList } from '@/service/api/project';
import AMapComponent from '@/components/custom/amap-component.vue';

defineOptions({ name: 'PointManagement' });

const router = useRouter();
const route = useRoute();

// 从 URL query 读取 projectId 过滤参数
const urlProjectId = computed(() => route.query.projectId as string | undefined);
const viewMode = ref<'list' | 'map'>('list');
const activePointId = ref<string | number | null>(null);

// Loading & Data
const loading = ref(false);
const tableData = ref<Api.Point.SurveyPoint[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});

// Filters
const filters = ref({
  projectId: undefined as number | undefined,
  keyword: '',
  status: ''
});

// 项目选项
const projectOptions = ref<any[]>([]);

const statusOptions = [
  { label: '全部', value: '' },
  { label: '未勘查', value: 'pending', dotClass: 'bg-[var(--color-text-disabled)]' },
  { label: '已提交', value: 'submitted', dotClass: 'bg-[var(--color-primary)]' },
  { label: '已通过', value: 'approved', dotClass: 'bg-[var(--color-success)]' },
  { label: '已驳回', value: 'rejected', dotClass: 'bg-[var(--color-danger)]' }
];

// Map state
const amapRef = ref<any>(null);
const mapCenter = ref<[number, number]>([115.78, 33.86]); // Roughly around area in screenshots
const mapZoom = ref(12);

const mapPoints = computed(() => {
  return tableData.value.map(p => ({
    pointId: p.pointId,
    name: p.pointName || '',
    longitude: p.longitude || 0,
    latitude: p.latitude || 0,
    status: p.status
  }));
});

// Table columns
const columns = [
  { title: '点位名称', key: 'name', width: 220 },
  { title: '所属项目', key: 'project', width: 250 },
  { title: '项目编号', key: 'projectCode', width: 180 },
  { title: '坐标', key: 'coordinates', width: 180 },
  { title: '状态', key: 'status', width: 120 },
  { title: '创建时间', key: 'createdAt', width: 120 },
  { title: '操作', key: 'action', width: 80, align: 'center' as const }
];

const selectedRowKeys = ref<any[]>([]);

// Methods
const loadData = async () => {
  loading.value = true;
  try {
    const params: any = {
      current: pagination.value.current,
      size: pagination.value.pageSize,
      projectId: filters.value.projectId,
      keyword: filters.value.keyword || undefined,
      status: filters.value.status || undefined
    };
    const res = await fetchGetPointList(params);
    if (res.data) {
      tableData.value = res.data.records;
      pagination.value.total = res.data.total;
    }
  } catch (err) {
    message.error('加载点位数据失败');
  } finally {
    loading.value = false;
  }
};

// 加载项目列表
const loadProjects = async () => {
  try {
    // fetchGetProjectList 期望 { current, size } 参数
    const res = await fetchGetProjectList({ current: 1, size: 1000 });
    if (res.data) {
      projectOptions.value = res.data.records || [];
      console.log('[PointManagement] 项目列表加载完成:', projectOptions.value.length, '个项目');
    }
  } catch (err) {
    console.error('加载项目列表失败', err);
  }
};

const handlePageChange = (page: number, pageSize: number) => {
  pagination.value.current = page;
  pagination.value.pageSize = pageSize;
  loadData();
};

const handleFilterChange = () => {
  pagination.value.current = 1;
  loadData();
};

const getStatusText = (status: string) => {
  const map: any = {
    pending: '未勘查',
    submitted: '已提交',
    approved: '已通过',
    rejected: '已驳回'
  };
  return map[status] || status;
};

const getStatusStyle = (status: string) => {
  if (status === 'approved') return 'bg-[rgba(0,180,42,0.1)] text-[var(--color-success)] border-[rgba(0,180,42,0.2)]';
  if (status === 'submitted') return 'bg-[rgba(22,119,255,0.1)] text-[var(--color-primary)] border-[rgba(22,119,255,0.2)]';
  if (status === 'rejected') return 'bg-[rgba(245,63,63,0.1)] text-[var(--color-danger)] border-[rgba(245,63,63,0.2)]';
  return 'bg-[var(--bg-hover)] text-[var(--color-text-secondary)] border-[var(--color-border)]';
};

const getStatusDotClass = (status: string) => {
  if (status === 'approved') return 'bg-[var(--color-success)]';
  if (status === 'submitted') return 'bg-[var(--color-primary)]';
  if (status === 'rejected') return 'bg-[var(--color-danger)]';
  return 'bg-[var(--color-text-disabled)]';
};

const viewDetail = (record: any) => {
  router.push(`/point/detail/${record.pointId}`);
};

const focusPoint = (point: Api.Point.SurveyPoint) => {
  activePointId.value = point.pointId;
  if (point.longitude && point.latitude) {
    mapCenter.value = [point.longitude, point.latitude];
    mapZoom.value = 16;
  }
};

const handleMarkerClick = (point: any) => {
  activePointId.value = point.pointId;
};

// Batch
const batchAssignModalVisible = ref(false);
const batchAssignLoading = ref(false);
const batchAssignee = ref<string>('');

const handleBatchAssign = () => {
  batchAssignee.value = '';
  batchAssignModalVisible.value = true;
};

const handleBatchAssignSubmit = async () => {
  if (!batchAssignee.value) {
    message.warning('请输入分配人ID');
    return;
  }
  batchAssignLoading.value = true;
  try {
    await fetchBatchAssignPoints({ pointIds: selectedRowKeys.value.map(Number), assigneeId: Number(batchAssignee.value) });
    message.success(`成功分配 ${selectedRowKeys.value.length} 个点位`);
    batchAssignModalVisible.value = false;
    selectedRowKeys.value = [];
    loadData();
  } catch (e) {
    message.error('批量分配失败');
  } finally {
    batchAssignLoading.value = false;
  }
};

const handleBatchDelete = () => {
  Modal.confirm({
    title: `确认删除 ${selectedRowKeys.value.length} 个点位？`,
    content: '删除后不可恢复，请谨慎操作。',
    okType: 'danger',
    onOk: async () => {
      try {
        const promises = selectedRowKeys.value.map(id => fetchDeletePoint(id));
        await Promise.allSettled(promises);
        message.success(`成功删除 ${selectedRowKeys.value.length} 个点位`);
        selectedRowKeys.value = [];
        loadData();
      } catch (e) {
        message.error('批量删除失败');
      }
    }
  });
};

// Import
const importModalVisible = ref(false);
const importLoading = ref(false);
const importFile = ref<File | null>(null);
const importProjectId = ref<number | undefined>(undefined);
const handleShowImportModal = () => {
  importFile.value = null;
  importProjectId.value = filters.value.projectId;
  importModalVisible.value = true;
};

const handleImportFileChange = (info: any) => {
  const file = info.fileList?.[0]?.originFileObj || info.file;
  if (file) importFile.value = file;
};

const handleImportExcel = async () => {
  if (!importFile.value) {
    message.warning('请选择Excel文件');
    return;
  }
  importLoading.value = true;
  try {
    const res = await fetchImportPoints(importFile.value);
    if ((res as any)?.error) {
      message.error('导入失败：' + ((res as any).message || '未知错误'));
    } else {
      message.success('导入成功');
      importModalVisible.value = false;
      loadData();
    }
  } catch (e) {
    message.error('导入失败');
  } finally {
    importLoading.value = false;
  }
};

const handleDownloadTemplate = () => {
  // Create a simple CSV template for download
  const header = '点位名称,经度,纬度,所属项目,备注\n';
  const sample = '示例点位001,116.397128,39.916527,项目名称,备注信息\n';
  const blob = new Blob(['\uFEFF' + header + sample], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = '点位导入模板.csv';
  link.click();
  URL.revokeObjectURL(url);
  message.success('模板已下载');
};

watch(() => route.query.projectId, () => {
  filters.value.projectId = undefined;
  filters.value.keyword = '';
  pagination.value.current = 1;
  loadData();
});

onMounted(() => {
  loadData();
  loadProjects();
});

// Watch for view mode changes to fit map view
watch(viewMode, (newVal) => {
  if (newVal === 'map') {
    setTimeout(() => {
      amapRef.value?.fitView();
    }, 100);
  }
});
</script>

<style scoped>
.custom-point-table :deep(.ant-table-thead > tr > th) {
  background-color: var(--bg-card-alt) !important;
  font-size: 13px;
  padding: 12px 16px;
}

.custom-point-table :deep(.ant-table-tbody > tr > td) {
  padding: 12px 16px;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
</style>
