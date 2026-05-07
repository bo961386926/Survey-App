<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message, Tag, Timeline } from 'ant-design-vue';
import dayjs from 'dayjs';
import { fetchGetProjectDetail, fetchGetPointList } from '@/service/api';
import AMapComponent from '@/components/custom/amap-component.vue';
import ProjectCreateModal from '../modules/create-modal.vue';

defineOptions({ name: 'ProjectDetail' });

const router = useRouter();
const route = useRoute();

const loading = ref(true);
const editModalVisible = ref(false);
const projectInfo = ref<Api.Project.ProjectInfo | null>(null);

// 获取路由参数中的项目ID
const projectId = computed(() => route.params.id as string);

// ========== Point List State ==========
const pointLoading = ref(false);
const pointList = ref<any[]>([]);
const pointPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
});
const pointStatusFilter = ref<string | undefined>(undefined);

// ========== Tab State ==========
const activeTab = ref<'points' | 'records' | 'map' | 'logs'>('points');

// ========== Mock: Survey Records ==========
const surveyRecords = ref([
  { id: 1, pointName: '雨水井-YJ-001', pointCode: 'YJ-001', submitter: '张三', version: 'v2.1', time: '2026-05-06 14:30', status: 'approved', comment: '数据完整，影像清晰' },
  { id: 2, pointName: '污水井-WS-003', pointCode: 'WS-003', submitter: '李四', version: 'v1.0', time: '2026-05-06 10:15', status: 'submitted', comment: '待审核' },
  { id: 3, pointName: '检查井-JC-005', pointCode: 'JC-005', submitter: '王五', version: 'v3.2', time: '2026-05-05 16:00', status: 'rejected', comment: '影像不清晰，需重新采集' },
  { id: 4, pointName: '雨水井-YJ-002', pointCode: 'YJ-002', submitter: '张三', version: 'v1.0', time: '2026-05-05 11:20', status: 'submitted', comment: '待审核' },
  { id: 5, pointName: '污水井-WS-001', pointCode: 'WS-001', submitter: '李四', version: 'v2.0', time: '2026-05-04 15:45', status: 'approved', comment: '数据完整' },
]);

// ========== Mock: Activity Logs ==========
const activityLogs = ref([
  { id: 1, user: '系统', action: '创建项目', target: projectInfo.value?.projectName || '本项目', time: '2026-04-28 09:00', type: 'create' },
  { id: 2, user: '管理员', action: '导入点位数据', target: '45个点位', time: '2026-04-28 10:30', type: 'import' },
  { id: 3, user: '张三', action: '提交勘查数据', target: '雨水井-YJ-001', time: '2026-05-06 14:30', type: 'submit' },
  { id: 4, user: '审核员', action: '审核通过', target: '雨水井-YJ-001', time: '2026-05-06 15:00', type: 'approve' },
  { id: 5, user: '张三', action: '提交勘查数据', target: '雨水井-YJ-002', time: '2026-05-05 11:20', type: 'submit' },
  { id: 6, user: '李四', action: '提交勘查数据', target: '污水井-WS-003', time: '2026-05-06 10:15', type: 'submit' },
  { id: 7, user: '审核员', action: '审核驳回', target: '检查井-JC-005', time: '2026-05-05 16:00', type: 'reject' },
]);

// ========== Stats ==========
const overview = computed(() => {
  const list = pointList.value;
  return {
    total: list.length,
    approved: list.filter(p => p.status === 'approved' || p.status === 3).length,
    pending: list.filter(p => p.status === 'pending' || p.status === 'submitted' || p.status === 0 || p.status === 1).length,
    unreviewed: list.filter(p => p.status === 'pending' || p.status === 0).length,
    submitted: list.filter(p => p.status === 'submitted' || p.status === 1).length
  };
});

// 进度百分比
const progressPercent = computed(() => {
  const total = overview.value.total;
  if (total === 0) return 0;
  return Math.round((overview.value.approved / total) * 100);
});

// ========== Table Columns ==========
const tableColumns = [
  { title: '点位名称', dataIndex: 'pointName', key: 'pointName', width: 160, ellipsis: true },
  { title: '编号', dataIndex: 'pointCode', key: 'pointCode', width: 110 },
  { title: '排口类型', dataIndex: 'outfallType', key: 'outfallType', width: 100 },
  { title: '坐标', dataIndex: 'coordinate', key: 'coordinate', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '采集人', dataIndex: 'collector', key: 'collector', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' as const }
];

// ========== Status Tags ==========
const statusTagOptions = [
  { label: '全部', value: undefined },
  { label: '未勘查', value: 'pending' },
  { label: '已提交', value: 'submitted' },
  { label: '已通过', value: 'approved' },
  { label: '已驳回', value: 'rejected' }
];

const statusColorMap: Record<string, string> = {
  pending: 'orange', submitted: 'blue', approved: 'green', rejected: 'red'
};

const statusTextMap: Record<string, string> = {
  pending: '未勘查', submitted: '已提交', approved: '已通过', rejected: '已驳回'
};

const getStatusTagColor = (status: string | number) => {
  if (typeof status === 'number') {
    const map: Record<number, string> = { 0: 'orange', 1: 'blue', 2: 'green', 3: 'red' };
    return map[status] || 'default';
  }
  return statusColorMap[status] || 'default';
};

const getStatusText = (status: string | number) => {
  if (typeof status === 'number') {
    const map: Record<number, string> = { 0: '未勘查', 1: '已提交', 2: '已通过', 3: '已驳回' };
    return map[status] || '未知';
  }
  return statusTextMap[status] || status;
};

const logDotClass = (type: string) => {
  const map: Record<string, string> = {
    create: 'bg-blue', import: 'bg-purple', submit: 'bg-blue',
    approve: 'bg-green', reject: 'bg-red'
  };
  return map[type] || 'bg-gray';
};

// ========== Load Data ==========
const loadDetail = async () => {
  loading.value = true;
  const id = projectId.value;
  console.log('[ProjectDetail] loading detail for projectId:', id);
  
  if (!id || id === 'undefined' || id === 'null') {
    console.error('[ProjectDetail] invalid projectId:', id, 'route params:', route.params);
    message.error('项目ID无效，请检查链接参数');
    setTimeout(() => router.push('/project/list'), 2000);
    loading.value = false;
    return;
  }
  
  try {
    const { data, error } = await fetchGetProjectDetail(id);
    console.log('[ProjectDetail] API response:', { data, error, errorMsg: (error as any)?.message });
    if (!error && data) {
      projectInfo.value = data;
    } else {
      let errMsg = '项目不存在或已被删除';
      if (error) {
        const axiosErr = error as any;
        errMsg = axiosErr?.message || (axiosErr?.response?.data?.message) || '请求失败';
        if (axiosErr?.response?.status === 401) errMsg = '登录已过期，请重新登录';
        else if (axiosErr?.response?.status >= 500) errMsg = '服务器内部错误';
      }
      console.error('[ProjectDetail] load failed:', { error, errMsg });
      message.error(errMsg);
      // 延迟跳转回项目列表
      setTimeout(() => {
        router.push('/project/list');
      }, 2000);
    }
  } catch (err) {
    const axiosErr = err as any;
    let errMsg = '加载项目详情失败';
    if (axiosErr?.response?.status === 401) errMsg = '登录已过期，请重新登录';
    else if (axiosErr?.response?.status >= 500) errMsg = '服务器内部错误，请联系管理员';
    else if (axiosErr?.message?.includes('Network Error')) errMsg = '无法连接到服务器，请检查后端是否启动';
    console.error('[ProjectDetail] exception:', axiosErr?.response?.status, axiosErr?.message, axiosErr);
    message.error(errMsg);
  } finally {
    loading.value = false;
  }
};

const loadPoints = async () => {
  pointLoading.value = true;
  const id = projectId.value;
  if (!id || id === 'undefined' || id === 'null') {
    pointLoading.value = false;
    return;
  }
  try {
    const { data, error } = await fetchGetPointList({
      current: pointPagination.value.current,
      size: pointPagination.value.pageSize,
      projectId: Number(id),
      status: pointStatusFilter.value
    });
    if (!error && data) {
      pointList.value = data.records || [];
      pointPagination.value.total = data.total || 0;
    } else {
      console.warn('[ProjectDetail] loadPoints failed:', error);
    }
  } catch (err) {
    console.error('Failed to load points:', err);
  } finally {
    pointLoading.value = false;
  }
};

// ========== Map State ==========
const amapRef = ref<InstanceType<typeof AMapComponent> | null>(null);
const mapCenter = ref<[number, number]>([116.397428, 39.90923]);
const mapZoom = ref(12);

const mapPoints = computed(() =>
  pointList.value
    .filter(p => p.longitude && p.latitude)
    .map(p => ({
      pointId: p.pointId || p.id,
      name: p.pointName || p.name,
      longitude: p.longitude,
      latitude: p.latitude,
      status: p.status
    }))
);

// ========== Actions ==========
const handleEdit = () => {
  editModalVisible.value = true;
};

const handleManagePoints = () => {
  router.push(`/point/map?projectId=${projectId.value}`);
};

const handleExport = () => {
  message.loading({ content: '正在导出点位数据...', key: 'export', duration: 0 });
  setTimeout(() => {
    message.success({ content: '导出成功', key: 'export' });
  }, 1500);
};

const handleViewDetail = (record: any) => {
  message.info(`点位详情: ${record.pointName || record.pointCode}`);
};

const handleLocateMap = (record: any) => {
  router.push(`/point/map?projectId=${projectId.value}&pointId=${record.pointId}`);
};

const handleEditSuccess = () => {
  loadDetail();
};

const handleTableChange = (pag: any) => {
  pointPagination.value.current = pag.current;
  pointPagination.value.pageSize = pag.pageSize;
  loadPoints();
};

const handleStatusFilterChange = (status: string | undefined) => {
  pointStatusFilter.value = status;
  pointPagination.value.current = 1;
  loadPoints();
};

const handleTabChange = (key: string) => {
  activeTab.value = key as any;
  if (key === 'points' && pointList.value.length === 0) {
    loadPoints();
  }
};

onMounted(() => {
  loadDetail();
  loadPoints();
});
</script>

<template>
  <div class="p-6 h-full flex flex-col gap-16px overflow-y-auto">

    <!-- ====== Header（去面包屑+统计卡，整合为紧凑行） ====== -->
    <div class="header-card flex-shrink-0">
      <div class="flex justify-between items-start">
        <!-- 左侧：名称+状态+元信息 -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-10px mb-8px">
            <a-skeleton v-if="loading" active :paragraph="false" style="width: 200px; height: 28px" />
            <h1 v-else class="text-18px font-700 m-0 text-[var(--color-text-primary)] truncate">
              {{ projectInfo?.projectName || '未知项目' }}
            </h1>
            <span
              v-if="!loading && projectInfo"
              :class="[
                'px-10px py-3px rd-full text-11px font-bold inline-flex items-center gap-5px flex-shrink-0 whitespace-nowrap',
                projectInfo.status == 1 ? 'bg-primary/10 text-primary' :
                projectInfo.status == 2 ? 'bg-warning/10 text-warning' :
                projectInfo.status == 3 ? 'bg-success/10 text-success' :
                projectInfo.status == 4 ? 'bg-secondary/10 text-secondary' : 'bg-info/10 text-info'
              ]"
            >
              <span v-if="projectInfo.status == 1" class="size-6px rd-full bg-primary animate-pulse flex-shrink-0"></span>
              <span v-else class="size-6px rd-full bg-current flex-shrink-0"></span>
              {{
                projectInfo.status == 0 ? '草稿' :
                projectInfo.status == 1 ? '进行中' :
                projectInfo.status == 2 ? '已暂停' :
                projectInfo.status == 3 ? '已完成' :
                projectInfo.status == 4 ? '已归档' : '未知'
              }}
            </span>
          </div>

          <!-- 元信息行 -->
          <div v-if="!loading && projectInfo" class="flex items-center gap-x-20px gap-y-4px text-12px text-[var(--color-text-secondary)] flex-wrap mb-10px">
            <span>{{ projectInfo.projectCode || '--' }}</span>
            <span class="text-[var(--color-divider)]">|</span>
            <span>{{ projectInfo.manager || '未指派' }}</span>
            <span class="text-[var(--color-divider)]" v-if="projectInfo.clientName">|</span>
            <span v-if="projectInfo.clientName">{{ projectInfo.clientName }}</span>
            <span class="text-[var(--color-divider)]">|</span>
            <span>{{ projectInfo.startDate || '--' }} ~ {{ projectInfo.endDate || '--' }}</span>
          </div>

          <!-- 进度条（整合进header） -->
          <div class="flex items-center gap-10px">
            <a-progress
              :percent="progressPercent"
              :stroke-color="{ from: '#3B82F6', to: '#10B981' }"
              :format="(p: number) => `${p}%`"
              class="flex-1"
              :size="['', 8]"
            />
            <span class="text-12px text-[var(--color-text-secondary)] whitespace-nowrap">
              {{ overview.approved }}/{{ overview.total }} 点
            </span>
          </div>
        </div>

        <!-- 右侧操作按钮 -->
        <div class="flex gap-8px flex-shrink-0 ml-16px">
          <a-button @click="handleManagePoints" type="primary" size="small" class="h-32px!">
            <template #icon><div class="i-material-symbols:pin-drop-outline-rounded text-14px"></div></template>
            管理点位
          </a-button>
          <a-button @click="handleEdit" size="small" class="h-32px!">
            <template #icon><div class="i-material-symbols:edit-outline-rounded text-14px"></div></template>
            编辑
          </a-button>
        </div>
      </div>
    </div>

    <!-- ====== Content ====== -->
    <a-skeleton active :loading="loading">

      <!-- ====== Tabs（去图标） ====== -->
      <div class="content-card flex-1 flex flex-col min-h-0">
        <div class="flex items-center justify-between flex-shrink-0 border-b border-[var(--color-divider)]">
          <div class="flex -mb-px">
            <button
              v-for="tab in [
                { key: 'points', label: '点位列表' },
                { key: 'records', label: '勘查记录' },
                { key: 'map', label: '点位分布' },
                { key: 'logs', label: '活动日志' }
              ]"
              :key="tab.key"
              class="px-16px py-12px text-13px border-b-2 border-transparent cursor-pointer transition-all bg-transparent whitespace-nowrap"
              :class="activeTab === tab.key
                ? 'text-primary border-b-primary font-500'
                : 'text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]'"
              @click="handleTabChange(tab.key)"
            >
              {{ tab.label }}
              <span v-if="tab.key === 'points' && overview.total > 0" class="ml-4px text-11px text-[var(--color-text-placeholder)]">
                {{ overview.total }}
              </span>
            </button>
          </div>

          <!-- Tab actions -->
          <div v-if="activeTab === 'points'" class="flex items-center gap-8px pr-4px">
            <a-button size="small" @click="handleExport" class="h-28px!">
              <template #icon><div class="i-material-symbols:file-download-outline-rounded text-13px"></div></template>
              导出
            </a-button>
          </div>
        </div>

        <!-- ========== Tab: 点位列表（行模式） ========== -->
        <div v-if="activeTab === 'points'" class="flex-1 flex flex-col min-h-0">
          <!-- Status filter tags -->
          <div class="flex items-center gap-6px mb-3 flex-shrink-0">
            <a-button
              v-for="tag in statusTagOptions"
              :key="tag.label"
              :type="pointStatusFilter === tag.value ? 'primary' : 'default'"
              size="small"
              class="rd-16px! text-12px h-26px!"
              @click="handleStatusFilterChange(tag.value)"
            >
              {{ tag.label }}
            </a-button>
          </div>

          <!-- Point rows -->
          <div class="flex-1 overflow-y-auto">
            <table class="w-full">
              <thead>
                <tr class="border-b border-[var(--color-divider)]">
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px">点位名称</th>
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px w-100px">编号</th>
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px w-90px">类型</th>
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px w-160px">坐标</th>
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px w-70px">状态</th>
                  <th class="text-right text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px w-80px">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="point in pointList"
                  :key="point.pointId || point.id"
                  class="border-b border-[var(--color-divider)] cursor-pointer hover:bg-[var(--bg-hover)] transition-colors group"
                >
                  <td class="py-10px px-12px">
                    <span class="text-13px font-medium text-[var(--color-text-primary)] truncate block max-w-200px">{{ point.pointName || '未命名' }}</span>
                  </td>
                  <td class="py-10px px-12px">
                    <span class="text-12px text-[var(--color-text-secondary)]">{{ point.pointCode || '-' }}</span>
                  </td>
                  <td class="py-10px px-12px">
                    <span class="text-12px text-[var(--color-text-secondary)]">{{ point.outfallType || '-' }}</span>
                  </td>
                  <td class="py-10px px-12px">
                    <span class="text-12px text-[var(--color-text-secondary)]">
                      {{ point.longitude && point.latitude ? `${Number(point.longitude).toFixed(4)}, ${Number(point.latitude).toFixed(4)}` : '-' }}
                    </span>
                  </td>
                  <td class="py-10px px-12px">
                    <a-tag
                      :color="getStatusTagColor(point.status)"
                      class="!text-11px !px-8px !py-0 !h-20px !leading-20px !m-0"
                    >
                      {{ getStatusText(point.status) }}
                    </a-tag>
                  </td>
                  <td class="py-10px px-12px">
                    <div class="flex-y-center justify-end gap-4px opacity-0 group-hover:opacity-100 transition-opacity">
                      <a-tooltip title="地图定位">
                        <a-button type="text" size="small" class="!p-0 !h-auto !w-22px text-secondary!" @click.stop="handleLocateMap(point)">
                          <div class="i-material-symbols:location-on-outline text-14px"></div>
                        </a-button>
                      </a-tooltip>
                      <a-tooltip title="查看详情">
                        <a-button type="text" size="small" class="!p-0 !h-auto !w-22px text-secondary!" @click.stop="handleViewDetail(point)">
                          <div class="i-material-symbols:info-outline-rounded text-14px"></div>
                        </a-button>
                      </a-tooltip>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- Empty state -->
            <div v-if="pointList.length === 0" class="py-20 text-center">
              <div class="i-material-symbols:explore-outline text-36px mx-auto mb-8px text-[var(--color-text-placeholder)]"></div>
              <p class="text-13px text-[var(--color-text-secondary)] mb-8px">暂无点位数据</p>
              <a-button type="primary" size="small" @click="handleManagePoints">导入点位</a-button>
            </div>
          </div>
        </div>

        <!-- ========== Tab: 勘查记录（行模式） ========== -->
        <div v-if="activeTab === 'records'" class="flex-1 flex flex-col min-h-0">
          <div class="flex-1 overflow-y-auto">
            <table class="w-full">
              <thead>
                <tr class="border-b border-[var(--color-divider)]">
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px">点位</th>
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px w-80px">状态</th>
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px w-90px">提交人</th>
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px w-60px">版本</th>
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px w-140px">时间</th>
                  <th class="text-left text-11px font-600 text-[var(--color-text-secondary)] py-8px px-12px">备注</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="record in surveyRecords"
                  :key="record.id"
                  class="border-b border-[var(--color-divider)] hover:bg-[var(--bg-hover)] transition-colors"
                >
                  <td class="py-10px px-12px">
                    <span class="text-13px font-medium text-[var(--color-text-primary)]">{{ record.pointName }}</span>
                  </td>
                  <td class="py-10px px-12px">
                    <a-tag
                      :color="record.status === 'approved' ? 'green' : record.status === 'rejected' ? 'red' : 'blue'"
                      class="!text-11px !px-8px !py-0 !h-20px !leading-20px !m-0"
                    >
                      {{ record.status === 'approved' ? '已通过' : record.status === 'rejected' ? '已驳回' : '待审核' }}
                    </a-tag>
                  </td>
                  <td class="py-10px px-12px">
                    <span class="text-12px text-[var(--color-text-secondary)]">{{ record.submitter }}</span>
                  </td>
                  <td class="py-10px px-12px">
                    <span class="text-11px font-mono text-primary bg-primary/10 px-6px py-1px rd-4px">{{ record.version }}</span>
                  </td>
                  <td class="py-10px px-12px">
                    <span class="text-12px text-[var(--color-text-secondary)]">{{ record.time }}</span>
                  </td>
                  <td class="py-10px px-12px">
                    <span v-if="record.comment" class="text-12px text-[var(--color-text-secondary)]">{{ record.comment }}</span>
                    <span v-else class="text-12px text-[var(--color-text-placeholder)]">-</span>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="surveyRecords.length === 0" class="py-20 text-center">
              <div class="i-material-symbols:receipt-long-outline text-36px mx-auto mb-8px text-[var(--color-text-placeholder)]"></div>
              <p class="text-13px text-[var(--color-text-secondary)]">暂无勘查记录</p>
            </div>
          </div>
        </div>

        <!-- ========== Tab: 点位分布（地图图例行内化） ========== -->
        <div v-if="activeTab === 'map'" class="flex-1 flex min-h-0 relative">
          <!-- Inline Legend overlay -->
          <div class="absolute top-10px left-10px z-10 bg-white/90 rd-6px px-10px py-8px border border-[var(--color-divider)] flex items-center gap-12px shadow-sm">
            <div v-for="item in [
              { color: 'orange', label: '未勘查', count: mapPoints.filter(p => p.status === 'pending' || p.status === 0).length },
              { color: '#3B82F6', label: '已提交', count: mapPoints.filter(p => p.status === 'submitted' || p.status === 1).length },
              { color: '#10B981', label: '已通过', count: mapPoints.filter(p => p.status === 'approved' || p.status === 3).length },
              { color: '#EF4444', label: '已驳回', count: mapPoints.filter(p => p.status === 'rejected' || p.status === 4).length }
            ]" :key="item.label" class="flex items-center gap-4px text-11px text-[var(--color-text-secondary)]">
              <div class="w-10px h-10px rd-2px flex-shrink-0" :style="{ background: item.color }"></div>
              <span>{{ item.label }}</span>
              <span class="text-primary font-600">{{ item.count }}</span>
            </div>
          </div>
          <!-- Map -->
          <div class="flex-1 rd-6px overflow-hidden">
            <AMapComponent
              ref="amapRef"
              :points="mapPoints"
              :center="mapCenter"
              :zoom="mapZoom"
              :enable-cluster="true"
              class="w-full h-full"
            />
          </div>
        </div>

        <!-- ========== Tab: 活动日志 ========== -->
        <div v-if="activeTab === 'logs'" class="flex-1 overflow-y-auto pr-1">
          <div class="py-2">
            <div class="relative">
              <!-- Timeline line -->
              <div class="absolute left-4 top-0 bottom-0 w-1px bg-[var(--color-divider)]"></div>
              <div v-for="(log, idx) in activityLogs" :key="log.id" class="relative pl-12 pb-5">
                <!-- Timeline dot -->
                <div
                  class="absolute left-[10px] top-4px w-10px h-10px rd-full border-2 border-white shadow-sm"
                  :class="logDotClass(log.type)"
                ></div>
                <!-- Content -->
                <div class="bg-[var(--bg-page)] rd-6px p-3 border border-[var(--color-divider)]">
                  <div class="flex items-start justify-between">
                    <div class="flex items-center gap-2 text-13px">
                      <span class="font-500 text-[var(--color-text-primary)]">{{ log.user }}</span>
                      <span class="text-[var(--color-text-secondary)]">{{ log.action }}</span>
                      <a-tag v-if="log.type === 'approve'" color="green" class="!text-11px">通过</a-tag>
                      <a-tag v-else-if="log.type === 'reject'" color="red" class="!text-11px">驳回</a-tag>
                      <a-tag v-else-if="log.type === 'create'" color="blue" class="!text-11px">创建</a-tag>
                      <a-tag v-else-if="log.type === 'import'" color="purple" class="!text-11px">导入</a-tag>
                    </div>
                    <span class="text-11px text-[var(--color-text-placeholder)]">{{ log.time }}</span>
                  </div>
                  <div class="mt-1 text-12px text-[var(--color-text-secondary)]">
                    目标：{{ log.target }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </a-skeleton>

    <!-- Edit Modal -->
    <ProjectCreateModal
      v-model:visible="editModalVisible"
      :editData="projectInfo"
      @success="handleEditSuccess"
    />
  </div>
</template>

<style scoped>
.header-card, .content-card {
  background-color: var(--bg-card);
  border-radius: 8px;
  box-shadow: var(--shadow-card);
}
.header-card {
  padding: 16px 20px;
}
.content-card {
  padding: 14px 20px;
}

/* Timeline dot colors */
.bg-blue { background-color: #3B82F6; }
.bg-green { background-color: #10B981; }
.bg-red { background-color: #EF4444; }
.bg-purple { background-color: #8B5CF6; }
.bg-gray { background-color: #9CA3AF; }
</style>
