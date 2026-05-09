<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { fetchGetProjectDetail, fetchGetPointList } from '@/service/api';
import {
  fetchGetBindings,
  fetchBindOutfallType,
  fetchDeleteBinding,
  fetchGetTemplateSimpleList,
  fetchGetTemplateVersions
} from '@/service/api/template';
import ProjectCreateModal from '../modules/create-modal.vue';

defineOptions({ name: 'ProjectDetail' });

const router = useRouter();
const route = useRoute();
const loading = ref(true);
const editModalVisible = ref(false);
const projectInfo = ref<Record<string, any> | null>(null);
const pointList = ref<any[]>([]);
const pointLoading = ref(false);
const pointStatusFilter = ref<number | undefined>(undefined);
const activeTab = ref('points');

const tabs = [
  { key: 'points', label: '点位列表', icon: 'i-material-symbols:location-on-outline-rounded' },
  { key: 'records', label: '勘查记录', icon: 'i-material-symbols:edit-note-outline-rounded' },
  { key: 'map', label: '点位分布', icon: 'i-material-symbols:map-outline-rounded' },
  { key: 'templates', label: '模板配置', icon: 'i-material-symbols:settings-outline-rounded' },
  { key: 'logs', label: '活动日志', icon: 'i-material-symbols:history-outline-rounded' }
];

// Outfall types (should match dictionary or be fetched from API)
const outfallTypes = [
  { value: 'rainwater', label: '雨水排口' },
  { value: 'sewage', label: '污水排口' },
  { value: 'mixed', label: '雨污混流口' },
  { value: 'stormwater', label: '雨水口' },
  { value: 'industrial', label: '工业排口' }
];

// Template binding state
const bindings = ref<any[]>([]);
const templates = ref<any[]>([]);
const templateVersionsMap = ref<Record<number, any[]>>({});
const bindingLoading = ref(false);
const savingBinding = ref(false);

const statusFilterOptions = [
  { label: '全部', value: undefined },
  { label: '未勘查', value: 0 },
  { label: '草稿中', value: 1 },
  { label: '待审核', value: 2 },
  { label: '已通过', value: 3 },
  { label: '已驳回', value: 4 }
];

const statusTagInfo = computed(() => {
  const s = projectInfo.value?.status;
  const map: Record<number, { label: string; color: string; bg: string }> = {
    0: { label: '草稿', color: 'var(--color-text-secondary)', bg: 'rgba(134,144,156,0.1)' },
    1: { label: '进行中', color: 'var(--color-primary)', bg: 'rgba(22,119,255,0.1)' },
    2: { label: '已暂停', color: 'var(--color-warning)', bg: 'rgba(255,125,0,0.1)' },
    3: { label: '已完成', color: 'var(--color-success)', bg: 'rgba(0,180,42,0.1)' },
    4: { label: '已归档', color: 'var(--color-text-secondary)', bg: 'rgba(134,144,156,0.1)' }
  };
  return map[s] || { label: '未知', color: 'var(--color-text-secondary)', bg: 'rgba(134,144,156,0.1)' };
});

const totalCount = computed(() => pointList.value.length);
const approvedCount = computed(() => pointList.value.filter(p => p.status === 3).length);
const progressPercent = computed(() => totalCount.value === 0 ? 0 : Math.round((approvedCount.value / totalCount.value) * 100));

// 点位状态映射
const pointStatusMap: Record<number | string, { label: string; color: string; bg: string }> = {
  0: { label: '待采集', color: '#FF7D00', bg: 'rgba(255,125,0,0.1)' },
  1: { label: '草稿中', color: '#FFB800', bg: 'rgba(255,184,0,0.1)' },
  2: { label: '待审核', color: '#1677FF', bg: 'rgba(22,119,255,0.1)' },
  3: { label: '已通过', color: '#00B42A', bg: 'rgba(0,180,42,0.1)' },
  4: { label: '已驳回', color: '#F53F3F', bg: 'rgba(245,63,63,0.1)' },
  5: { label: '已归档', color: '#86909C', bg: 'rgba(134,144,156,0.1)' },
  6: { label: '作废', color: '#86909C', bg: 'rgba(134,144,156,0.1)' }
};

const getPointStatusStyle = (s: any) => pointStatusMap[s] || pointStatusMap[String(s)] || { label: String(s), color: '#86909C', bg: 'rgba(134,144,156,0.1)' };

let fetchDetailId = 0;
let fetchPointsId = 0;

async function loadDetail() {
  const id = route.params.id as string;
  if (!id) {
    message.error('项目ID无效');
    router.push('/project/list');
    return;
  }
  loading.value = true;
  const fid = ++fetchDetailId;
  try {
    const resp = await fetchGetProjectDetail(id);
    if (fid !== fetchDetailId) return;
    const data = (resp as any)?.data;
    const error = (resp as any)?.error;
    if (!error && data) {
      projectInfo.value = data;
    } else if (error) {
      const status = (error as any)?.response?.status;
      if (status === 401) message.error('登录已过期，请重新登录');
      else if (status >= 500) message.error('服务器错误');
      else message.error('项目不存在或已被删除');
      setTimeout(() => router.push('/project/list'), 2000);
    } else {
      message.error('项目不存在或已被删除');
      setTimeout(() => router.push('/project/list'), 2000);
    }
  } catch (err: any) {
    if (fid !== fetchDetailId) return;
    if (err?.response?.status === 401) message.error('登录已过期');
    else if (err?.message?.includes('Network Error')) message.error('无法连接服务器');
    else message.error('加载失败');
  } finally {
    if (fid === fetchDetailId) loading.value = false;
  }
}

async function loadPoints() {
  const id = route.params.id as string;
  if (!id) return;
  pointLoading.value = true;
  const fid = ++fetchPointsId;
  try {
    const resp = await fetchGetPointList({ current: 1, size: 100, projectId: id, status: pointStatusFilter.value });
    if (fid !== fetchPointsId) return;
    const data = (resp as any)?.data;
    const error = (resp as any)?.error;
    if (!error && data) {
      pointList.value = data.records || [];
    }
  } catch (_) { /* silent */ }
  finally { if (fid === fetchPointsId) pointLoading.value = false; }
}

function switchTab(key: string) {
  activeTab.value = key;
  if (key === 'points' && pointList.value.length === 0) loadPoints();
  if (key === 'templates') loadTemplateBindings();
}

// --- Template Binding Methods ---
function getBinding(outfallType: string) {
  return bindings.value.find(b => b.outfallType === outfallType) || { outfallType, templateId: undefined, templateVersionId: undefined };
}

function getVersions(templateId?: number) {
  if (!templateId) return [];
  return templateVersionsMap.value[templateId] || [];
}

async function loadTemplateBindings() {
  bindingLoading.value = true;
  try {
    const projectId = Number(route.params.id);
    const tplRes = await fetchGetTemplateSimpleList();
    templates.value = tplRes.data || [];
    const bindRes = await fetchGetBindings(projectId);
    bindings.value = (bindRes.data || []).map((b: any) => ({
      id: b.id,
      outfallType: b.outfallType,
      templateId: b.templateId,
      templateVersionId: b.templateVersionId
    }));
    const boundTemplateIds = [...new Set(bindings.value.map(b => b.templateId).filter(Boolean))];
    for (const tid of boundTemplateIds) {
      if (!templateVersionsMap.value[tid]) {
        const verRes = await fetchGetTemplateVersions(tid);
        templateVersionsMap.value[tid] = verRes.data || [];
      }
    }
  } catch (error) {
    console.error('加载模板绑定失败:', error);
    message.error('加载模板绑定失败');
  } finally {
    bindingLoading.value = false;
  }
}

async function onTemplateChange(outfallType: string, templateId: number) {
  const existing = bindings.value.find(b => b.outfallType === outfallType);
  if (existing) {
    existing.templateId = templateId;
    existing.templateVersionId = undefined;
  } else {
    bindings.value.push({ outfallType, templateId, templateVersionId: undefined });
  }
  if (templateId && !templateVersionsMap.value[templateId]) {
    const verRes = await fetchGetTemplateVersions(templateId);
    templateVersionsMap.value[templateId] = verRes.data || [];
  }
}

function onVersionChange(outfallType: string, versionId: number) {
  const existing = bindings.value.find(b => b.outfallType === outfallType);
  if (existing) {
    existing.templateVersionId = versionId;
  } else {
    bindings.value.push({ outfallType, templateId: undefined, templateVersionId: versionId });
  }
}

async function saveAllBindings() {
  savingBinding.value = true;
  try {
    const projectId = Number(route.params.id);
    const toSave = bindings.value.filter(b => b.templateId && b.templateVersionId);
    for (const b of toSave) {
      await fetchBindOutfallType({
        projectId,
        outfallType: b.outfallType,
        templateId: b.templateId,
        templateVersionId: b.templateVersionId
      });
    }
    message.success('模板绑定已保存');
    loadTemplateBindings();
  } catch (error) {
    console.error('保存模板绑定失败:', error);
    message.error('保存模板绑定失败');
  } finally {
    savingBinding.value = false;
  }
}

async function removeBinding(outfallType: string) {
  const existing = bindings.value.find(b => b.outfallType === outfallType);
  if (!existing?.id) return;
  Modal.confirm({
    title: '确认解除绑定',
    content: `确定要解除「${outfallType}」的模板绑定吗？`,
    onOk: async () => {
      try {
        await fetchDeleteBinding(existing.id);
        message.success('绑定已解除');
        bindings.value = bindings.value.filter(b => b.outfallType !== outfallType);
      } catch {
        message.error('解除绑定失败');
      }
    }
  });
}

function filterPoints(val: number | undefined) {
  pointStatusFilter.value = val;
  loadPoints();
}

function goManagePoints() {
  router.push(`/point/list?projectId=${route.params.id}`);
}

function locatePoint(point: any) {
  router.push(`/point/map?projectId=${route.params.id}&pointId=${point.pointId || point.id}`);
}

function viewPointDetail(point: any) {
  message.info(point.pointName || point.pointCode || '点位详情');
}

function onEditSuccess() {
  loadDetail();
}

watch(() => route.params.id, (newId) => {
  if (newId) {
    projectInfo.value = null;
    pointList.value = [];
    loadDetail();
    loadPoints();
  }
});

onMounted(() => {
  loadDetail();
  loadPoints();
});
</script>

<template>
  <div class="h-full flex-col overflow-y-auto custom-scrollbar project-detail-page">
    <!-- 毛玻璃背景层 -->
    <div class="glass-bg-layer"></div>

    <div class="p-24px relative z-1">
      <!-- Page Header with Title -->
      <div class="mb-24px transition-all duration-300 page-header">
        <div class="flex items-center gap-12px">
          <div class="w-36px h-36px rounded-8px flex items-center justify-center header-icon-wrapper">
            <span class="i-material-symbols:folder-open-outline-rounded text-18px text-[var(--color-primary)]"></span>
          </div>
          <div>
            <div class="flex items-center gap-10px">
              <ASkeleton v-if="loading" active :paragraph="false" style="width:200px;height:24px" />
              <h1 v-else class="text-20px font-700 m-0 header-title">{{ projectInfo?.projectName || '未知项目' }}</h1>
              <div
                v-if="!loading && projectInfo"
                class="px-10px py-3px rounded-4px text-12px font-500"
                :style="{ background: statusTagInfo.bg, color: statusTagInfo.color }"
              >
                {{ statusTagInfo.label }}
              </div>
            </div>
            <p v-if="!loading && projectInfo" class="text-13px text-[var(--color-text-secondary)] m-0 mt-4px">
              {{ projectInfo.projectCode || '--' }} · {{ projectInfo.manager || '未指派' }} · {{ projectInfo.clientName || '--' }}
            </p>
          </div>
        </div>
      </div>

      <!-- Core Metrics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-16px mb-24px">
        <!-- Project Info Card -->
        <div class="glass-card rounded-8px p-20px" v-mouse-glow="{ color: '22,119,255', size: 250, intensity: 0.06 }">
          <div class="flex items-center gap-10px mb-12px">
            <div class="w-28px h-28px rounded-6px flex items-center justify-center" style="background: rgba(22,119,255,0.1)">
              <span class="i-material-symbols:calendar-month-outline-rounded text-16px text-[var(--color-primary)]"></span>
            </div>
            <span class="text-13px text-[var(--color-text-secondary)]">项目周期</span>
          </div>
          <div class="text-16px font-600 text-[var(--color-text-primary)]">
            <ASkeleton v-if="loading" active :paragraph="false" style="width:180px;height:20px" />
            <span v-else>{{ projectInfo?.startDate || '--' }} ~ {{ projectInfo?.endDate || '--' }}</span>
          </div>
        </div>

        <!-- Total Points Card -->
        <div class="glass-card rounded-8px p-20px" v-mouse-glow="{ color: '22,119,255', size: 250, intensity: 0.06 }">
          <div class="flex items-center justify-between mb-12px">
            <div class="flex items-center gap-10px">
              <div class="w-28px h-28px rounded-6px flex items-center justify-center" style="background: rgba(22,119,255,0.1)">
                <span class="i-material-symbols:location-on-outline-rounded text-16px text-[var(--color-primary)]"></span>
              </div>
              <span class="text-13px text-[var(--color-text-secondary)]">总点位数</span>
            </div>
            <span class="px-8px py-2px rounded-4px text-11px" style="background: rgba(22,119,255,0.1); color: var(--color-primary)">
              全部
            </span>
          </div>
          <div class="flex items-end justify-between">
            <div class="text-28px font-700 text-[var(--color-text-primary)]" style="letter-spacing: -0.5px;">
              <ASkeleton v-if="loading" active :paragraph="false" style="width:40px;height:32px" />
              <span v-else>{{ totalCount }}</span>
            </div>
            <div class="flex items-center justify-center w-28px h-28px rounded-6px opacity-0 -translate-x-8px transition-all duration-300 group-hover:opacity-100 group-hover:translate-x-0"
                 style="background: rgba(22,119,255,0.1)">
              <span class="i-material-symbols:arrow-forward-rounded text-16px text-[var(--color-primary)]"></span>
            </div>
          </div>
        </div>

        <!-- Approved Points Card -->
        <div class="glass-card rounded-8px p-20px" v-mouse-glow="{ color: '0,180,42', size: 250, intensity: 0.06 }">
          <div class="flex items-center justify-between mb-12px">
            <div class="flex items-center gap-10px">
              <div class="w-28px h-28px rounded-6px flex items-center justify-center" style="background: rgba(0,180,42,0.1)">
                <span class="i-material-symbols:check-circle-outline-rounded text-16px text-[var(--color-success)]"></span>
              </div>
              <span class="text-13px text-[var(--color-text-secondary)]">已通过点位</span>
            </div>
            <span class="px-8px py-2px rounded-4px text-11px" style="background: rgba(0,180,42,0.1); color: var(--color-success)">
              完成率 {{ progressPercent }}%
            </span>
          </div>
          <div class="flex items-end justify-between">
            <div class="text-28px font-700 text-[var(--color-text-primary)]" style="letter-spacing: -0.5px;">
              <ASkeleton v-if="loading" active :paragraph="false" style="width:40px;height:32px" />
              <span v-else>{{ approvedCount }}</span>
            </div>
            <div class="flex-1 ml-16px">
              <div class="progress-bar-bg">
                <div class="progress-bar-fill" :style="{ width: `${progressPercent}%` }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="flex justify-end gap-8px mb-24px">
        <AButton class="btn-primary" @click="goManagePoints">
          <span class="i-material-symbols:location-on-outline-rounded text-16px mr-6px"></span>
          管理点位
        </AButton>
        <AButton class="btn-secondary" @click="editModalVisible = true">
          <span class="i-material-symbols:edit-outline-rounded text-16px mr-6px"></span>
          编辑项目
        </AButton>
      </div>

      <!-- Content Card -->
      <div class="glass-card rounded-8px overflow-hidden" v-mouse-glow="{ color: '22,119,255', size: 200, intensity: 0.04 }">
        <!-- Tabs Header -->
        <div class="flex items-center justify-between px-24px py-16px header-divider">
          <div class="flex -mb-17px">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              class="tab-btn"
              :class="activeTab === tab.key ? 'tab-active' : ''"
              @click="switchTab(tab.key)"
            >
              <span :class="tab.icon" class="text-16px mr-6px"></span>
              {{ tab.label }}
            </button>
          </div>
        </div>

        <!-- Tab: Points -->
        <div v-show="activeTab === 'points'" class="pt-3">
          <!-- Filter Tags -->
          <div class="flex items-center gap-8px px-24px pb-16px">
            <AButton
              v-for="tag in statusFilterOptions"
              :key="tag.label"
              class="filter-btn"
              :class="pointStatusFilter === tag.value ? 'filter-active' : ''"
              size="small"
              @click="filterPoints(tag.value)"
            >{{ tag.label }}</AButton>
          </div>

          <!-- Points Table -->
          <div class="px-24px pb-24px">
            <div v-if="pointLoading" class="flex flex-col gap-12px">
              <div v-for="i in 3" :key="i" class="flex items-center gap-12px p-14px rounded-8px skeleton-item">
                <div class="w-36px h-36px rounded-6px bg-[var(--bg-card-alt)] animate-pulse flex-shrink-0"></div>
                <div class="flex-1">
                  <div class="h-14px w-3/4 bg-[var(--bg-card-alt)] rounded animate-pulse mb-6px"></div>
                  <div class="h-12px w-1/2 bg-[var(--bg-card-alt)] rounded animate-pulse"></div>
                </div>
              </div>
            </div>

            <div v-else-if="pointList.length > 0" class="point-table">
              <div class="point-table-header">
                <div class="point-th col-name">点位名称</div>
                <div class="point-th col-code">编号</div>
                <div class="point-th col-type">类型</div>
                <div class="point-th col-coord">坐标</div>
                <div class="point-th col-status">状态</div>
                <div class="point-th col-action">操作</div>
              </div>
              <div class="point-table-body">
                <div
                  v-for="point in pointList"
                  :key="point.pointId || point.id"
                  class="point-table-row"
                >
                  <div class="point-td col-name">
                    <div class="flex items-center gap-10px">
                      <div class="point-icon-wrapper">
                        <span class="i-material-symbols:location-on-outline-rounded text-14px"></span>
                      </div>
                      <span class="point-name truncate">{{ point.pointName || '未命名' }}</span>
                    </div>
                  </div>
                  <div class="point-td col-code">
                    <span class="point-code">{{ point.pointCode || '-' }}</span>
                  </div>
                  <div class="point-td col-type">
                    <span class="point-type">{{ point.outfallType || '-' }}</span>
                  </div>
                  <div class="point-td col-coord">
                    <span class="point-coord">
                      {{ point.longitude && point.latitude ? `${Number(point.longitude).toFixed(4)}, ${Number(point.latitude).toFixed(4)}` : '-' }}
                    </span>
                  </div>
                  <div class="point-td col-status">
                    <span
                      class="point-status-badge"
                      :style="{ color: getPointStatusStyle(point.status).color, background: getPointStatusStyle(point.status).bg }"
                    >
                      {{ getPointStatusStyle(point.status).label }}
                    </span>
                  </div>
                  <div class="point-td col-action">
                    <div class="action-buttons">
                      <AButton type="text" size="small" class="action-btn" @click="locatePoint(point)">
                        <span class="i-material-symbols:location-on-outline-rounded text-14px"></span>
                      </AButton>
                      <AButton type="text" size="small" class="action-btn" @click="viewPointDetail(point)">
                        <span class="i-material-symbols:info-outline-rounded text-14px"></span>
                      </AButton>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="py-40px text-center">
              <div class="i-material-symbols:location-on-outline-rounded text-48px mx-auto mb-12px text-[var(--color-text-placeholder)]"></div>
              <p class="text-14px text-[var(--color-text-secondary)] mb-16px">暂无点位数据</p>
              <AButton type="primary" size="small" @click="goManagePoints">添加点位</AButton>
            </div>
          </div>
        </div>

        <!-- Tab: Survey Records -->
        <div v-show="activeTab === 'records'" class="py-60px text-center">
          <div class="i-material-symbols:edit-note-outline-rounded text-48px mx-auto mb-12px text-[var(--color-text-placeholder)]"></div>
          <p class="text-14px text-[var(--color-text-secondary)]">暂无勘查记录</p>
        </div>

        <!-- Tab: Map -->
        <div v-show="activeTab === 'map'" class="p-24px min-h-400px">
          <div class="flex flex-col items-center justify-center h-300px text-[var(--color-text-secondary)]">
            <span class="i-material-symbols:map-outline-rounded text-48px mb-12px text-[var(--color-text-placeholder)]"></span>
            <p class="text-14px">地图加载区</p>
          </div>
        </div>

        <!-- Tab: Template Bindings -->
        <div v-show="activeTab === 'templates'" class="px-24px py-16px">
          <div v-if="bindingLoading" class="flex justify-center py-24px"><ASpin /></div>
          <div v-else class="max-w-800px">
            <div class="flex items-center justify-between mb-16px">
              <div class="text-13px text-[var(--color-text-secondary)]">
                为不同排口类型绑定勘查模板，绑定后移动端将根据点位排口类型自动加载对应模板
              </div>
              <AButton type="primary" size="small" class="btn-primary" :loading="savingBinding" @click="saveAllBindings">
                <span class="i-material-symbols:save-outline-rounded text-14px mr-6px"></span>
                保存配置
              </AButton>
            </div>
            <div v-for="ot in outfallTypes" :key="ot.value" class="template-binding-card">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-10px">
                  <span class="text-14px font-500 text-[var(--color-text-primary)]">{{ ot.label }}</span>
                  <span class="px-8px py-2px rounded-4px text-11px" style="background: rgba(22,119,255,0.1); color: var(--color-primary)">
                    {{ ot.value }}
                  </span>
                </div>
                <AButton
                  v-if="getBinding(ot.value)?.id"
                  type="text"
                  danger
                  size="small"
                  class="text-danger!"
                  @click="removeBinding(ot.value)"
                >
                  <span class="i-material-symbols:link-off-outline-rounded text-14px mr-4px"></span>
                  解除绑定
                </AButton>
              </div>
              <div class="flex gap-12px mt-16px">
                <a-select
                  :value="getBinding(ot.value)?.templateId"
                  placeholder="选择模板"
                  class="w-240px"
                  @change="(val: any) => onTemplateChange(ot.value, val)"
                >
                  <a-select-option v-for="tpl in templates" :key="tpl.id" :value="tpl.id">{{ tpl.templateName }}</a-select-option>
                </a-select>
                <a-select
                  :value="getBinding(ot.value)?.templateVersionId"
                  placeholder="选择版本"
                  class="w-160px"
                  :disabled="!getBinding(ot.value)?.templateId"
                  @change="(val: any) => onVersionChange(ot.value, val)"
                >
                  <a-select-option v-for="ver in getVersions(getBinding(ot.value)?.templateId)" :key="ver.id" :value="ver.id">v{{ ver.versionNo }}</a-select-option>
                </a-select>
              </div>
            </div>
          </div>
        </div>

        <!-- Tab: Activity Logs -->
        <div v-show="activeTab === 'logs'" class="py-60px text-center">
          <div class="i-material-symbols:history-outline-rounded text-48px mx-auto mb-12px text-[var(--color-text-placeholder)]"></div>
          <p class="text-14px text-[var(--color-text-secondary)]">暂无活动日志</p>
        </div>
      </div>
    </div>

    <!-- Loading overlay -->
    <div v-if="loading" class="fixed inset-0 bg-white/60 flex items-center justify-center z-50">
      <ASpin />
    </div>

    <!-- Edit Modal -->
    <ProjectCreateModal v-model:visible="editModalVisible" :editData="projectInfo" @success="onEditSuccess" />
  </div>
</template>

<style scoped>
/* Background Layer */
.glass-bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  background: linear-gradient(135deg,
    rgba(22, 119, 255, 0.06) 0%,
    rgba(22, 119, 255, 0) 50%,
    rgba(16, 185, 129, 0.04) 100%);
  pointer-events: none;
}

.glass-bg-layer::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 20%,
    rgba(22, 119, 255, 0.08) 0%,
    transparent 40%),
    radial-gradient(circle at 80% 60%,
    rgba(16, 185, 129, 0.06) 0%,
    transparent 40%);
  animation: float-bg 20s ease-in-out infinite;
}

@keyframes float-bg {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-2%, -2%); }
}

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--color-divider);
  border-radius: 10px;
  transition: background-color 0.2s ease;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-placeholder);
}

/* Page Header */
.page-header {
  cursor: default;
  padding: 8px 12px;
  margin: -8px -12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.page-header:hover {
  background: rgba(22, 119, 255, 0.03);
}

.header-title {
  position: relative;
  display: inline-block;
}

.header-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--color-primary), transparent);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-header:hover .header-title::after {
  width: 100%;
}

.header-icon-wrapper {
  background: rgba(22, 119, 255, 0.1);
  transition: all 0.3s ease;
}

.page-header:hover .header-icon-wrapper {
  background: rgba(22, 119, 255, 0.15);
  transform: scale(1.05);
}

/* Glass Card */
.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card:hover {
  border-color: rgba(22, 119, 255, 0.25);
  box-shadow: 0 8px 32px rgba(22, 119, 255, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* Progress Bar */
.progress-bar-bg {
  height: 6px;
  background: rgba(0, 180, 42, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-success), #00D42E);
  border-radius: 3px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Buttons */
.btn-primary {
  height: 32px !important;
  border-radius: 6px !important;
  font-size: 13px !important;
  background: var(--color-primary) !important;
  color: white !important;
  border: none !important;
  transition: all 0.2s ease !important;
}

.btn-primary:hover {
  opacity: 0.9 !important;
  transform: translateY(-1px) !important;
}

.btn-secondary {
  height: 32px !important;
  border-radius: 6px !important;
  font-size: 13px !important;
  background: transparent !important;
  color: var(--color-text-primary) !important;
  border: 1px solid var(--color-border) !important;
  transition: all 0.2s ease !important;
}

.btn-secondary:hover {
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
}

/* Tabs */
.header-divider {
  border-bottom: 1px solid var(--color-divider);
}

.tab-btn {
  padding: 10px 16px;
  font-size: 13px;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  background: transparent;
  color: var(--color-text-secondary);
  white-space: nowrap;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  border: none;
  border-radius: 6px 6px 0 0;
}

.tab-btn:hover {
  color: var(--color-text-primary);
  background: rgba(22, 119, 255, 0.04);
}

.tab-active {
  color: var(--color-primary) !important;
  border-bottom-color: var(--color-primary);
  font-weight: 500;
  background: rgba(22, 119, 255, 0.06);
}

/* Filter Buttons */
.filter-btn {
  height: 28px !important;
  border-radius: 14px !important;
  font-size: 12px !important;
  padding: 0 12px !important;
  transition: all 0.2s ease !important;
}

.filter-active {
  background: var(--color-primary) !important;
  color: white !important;
  border-color: var(--color-primary) !important;
}

/* Point Table */
.point-table {
  border: 1px solid var(--color-divider);
  border-radius: 8px;
  overflow: hidden;
}

.point-table-header {
  display: flex;
  align-items: center;
  background: #F7F8FA;
  border-bottom: 1px solid var(--color-divider);
}

.point-th {
  padding: 12px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.point-table-body {
  max-height: 400px;
  overflow-y: auto;
}

.point-table-row {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--color-divider);
  transition: all 0.2s ease;
}

.point-table-row:last-child {
  border-bottom: none;
}

.point-table-row:hover {
  background: rgba(22, 119, 255, 0.04);
}

.point-td {
  padding: 12px 16px;
  flex-shrink: 0;
}

.col-name { flex: 1; min-width: 150px; }
.col-code { width: 100px; }
.col-type { width: 90px; }
.col-coord { width: 160px; }
.col-status { width: 80px; }
.col-action { width: 80px; text-align: right; }

.point-icon-wrapper {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(22, 119, 255, 0.1);
  color: var(--color-primary);
}

.point-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
  max-width: 200px;
}

.point-code,
.point-type,
.point-coord {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.point-status-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
}

.action-btn {
  width: 28px !important;
  height: 28px !important;
  padding: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  border-radius: 6px !important;
  transition: all 0.2s ease !important;
}

.action-btn:hover {
  background: rgba(22, 119, 255, 0.1) !important;
  color: var(--color-primary) !important;
}

/* Template Binding Card */
.template-binding-card {
  border: 1px solid var(--color-divider);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
}

.template-binding-card:hover {
  border-color: rgba(22, 119, 255, 0.2);
  background: rgba(22, 119, 255, 0.02);
}

/* Skeleton */
.skeleton-item {
  background: rgba(247, 248, 250, 0.5);
}
</style>
