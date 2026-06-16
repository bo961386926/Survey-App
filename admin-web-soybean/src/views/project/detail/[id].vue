<script setup lang="ts">
import { ref, computed, watch, onMounted, h } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import {
  fetchGetProjectDetail,
  fetchGetPointList,
  fetchGetSectionList,
  fetchCreateSection,
  fetchUpdateSection,
  fetchDeleteSection,
  fetchGetProjectMembers,
  fetchAddProjectMember,
  fetchRemoveProjectMember,
  fetchUpdateProjectMemberRole,
  fetchGetAllUsers,
  fetchUpdateProjectStatus,
  fetchArchiveProject,
  fetchRestoreProject
} from '@/service/api';
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
const projectInfo = ref<Api.Project.ProjectInfo | null>(null);
const pointList = ref<any[]>([]);
const pointLoading = ref(false);
const pointStatusFilter = ref<string | undefined>(undefined);
const activeTab = ref('points');

const tabs = [
  { key: 'points', label: '点位列表', icon: 'i-material-symbols:location-on-outline-rounded' },
  { key: 'sections', label: '标段管理', icon: 'i-material-symbols:grid-view-outline-rounded' },
  { key: 'members', label: '人员管理', icon: 'i-material-symbols:group-outline-rounded' },
  { key: 'templates', label: '模板配置', icon: 'i-material-symbols:settings-outline-rounded' }
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
  return map[s as number] || { label: '未知', color: 'var(--color-text-secondary)', bg: 'rgba(134,144,156,0.1)' };
});

const totalCount = computed(() => pointList.value.length);
const approvedCount = computed(() => pointList.value.filter(p => p.status === 3).length);
const pendingCount = computed(() => pointList.value.filter(p => [0, 1, 2].includes(p.status)).length);
const errorCount = computed(() => pointList.value.filter(p => p.status === 4).length);
const progressPercent = computed(() => totalCount.value === 0 ? 0 : Math.round((approvedCount.value / totalCount.value) * 100));

const currentStage = computed(() => {
  const s = projectInfo.value?.status;
  const map: Record<number, string> = { 0: '草稿', 1: '进行中', 2: '已暂停', 3: '已完成', 4: '已归档' };
  return map[s as number] || '未启动';
});

// --- 状态操作 ---
const statusTransitioning = ref(false);

// 启动检查清单
const startupChecklist = computed(() => {
  const checks = [
    { label: '已创建标段', pass: sectionList.value.length > 0 },
    { label: '已导入点位', pass: pointList.value.length > 0 },
    { label: '已分配采集员', pass: memberList.value.length > 0 },
    { label: '已配置模板', pass: bindings.value.length > 0 || (projectInfo.value as any)?.templateCount > 0 }
  ];
  return checks;
});
const startupReady = computed(() => startupChecklist.value.every(c => c.pass));

// 根据当前状态计算可用操作
const statusActions = computed(() => {
  const s = projectInfo.value?.status as number;
  const actions: { key: string; label: string; icon: string; type: 'primary' | 'default' | 'warning' | 'success' | 'danger' }[] = [];
  switch (s) {
    case 0: // 草稿 → 可启动
      actions.push({ key: 'start', label: '启动项目', icon: 'i-material-symbols:play-arrow-rounded', type: 'primary' });
      break;
    case 1: // 进行中 → 可暂停/完成
      actions.push({ key: 'pause', label: '暂停项目', icon: 'i-material-symbols:pause-rounded', type: 'warning' });
      actions.push({ key: 'complete', label: '完成项目', icon: 'i-material-symbols:check-rounded', type: 'success' });
      break;
    case 2: // 已暂停 → 可恢复
      actions.push({ key: 'resume', label: '恢复项目', icon: 'i-material-symbols:play-arrow-rounded', type: 'primary' });
      break;
    case 3: // 已完成 → 可归档
      actions.push({ key: 'archive', label: '归档项目', icon: 'i-material-symbols:archive-outline-rounded', type: 'default' });
      break;
    case 4: // 已归档 → 可恢复
      actions.push({ key: 'restore', label: '取消归档', icon: 'i-material-symbols:unarchive-rounded', type: 'warning' });
      break;
  }
  return actions;
});

function handleStatusAction(action: string) {
  switch (action) {
    case 'start':
      if (!startupReady.value) {
        const passCount = startupChecklist.value.filter(c => c.pass).length;
        const totalCount2 = startupChecklist.value.length;
        Modal.confirm({
          title: '启动检查',
          width: 420,
          content: () => h('div', { style: 'padding: 8px 0' }, [
            ...startupChecklist.value.map(c =>
              h('div', {
                style: `display:flex;align-items:center;gap:10px;padding:10px 14px;margin:6px 0;border-radius:8px;background:${c.pass ? 'rgba(0,180,42,0.06)' : 'rgba(245,63,63,0.06)'};border:1px solid ${c.pass ? 'rgba(0,180,42,0.15)' : 'rgba(245,63,63,0.15)'}`
              }, [
                h('span', {
                  style: `width:22px;height:22px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:12px;color:white;background:${c.pass ? '#00B42A' : '#F53F3F'};flex-shrink:0`
                }, c.pass ? '✓' : '✗'),
                h('span', {
                  style: `font-size:14px;font-weight:500;color:${c.pass ? '#00B42A' : '#F53F3F'}`
                }, c.label),
                h('span', {
                  style: `margin-left:auto;font-size:12px;color:${c.pass ? '#00B42A' : '#86909C'}`
                }, c.pass ? '就绪' : '未就绪')
              ])
            ),
            h('div', {
              style: 'display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding:10px 14px;background:rgba(255,125,0,0.06);border-radius:8px;border:1px solid rgba(255,125,0,0.15)'
            }, [
              h('span', { style: 'font-size:13px;color:#FA8C16;font-weight:500' }, `${passCount}/${totalCount2} 项就绪`),
              h('span', { style: 'font-size:12px;color:#86909C' }, '部分条件未满足，仍可强制启动')
            ])
          ]),
          okText: '仍然启动',
          okType: 'primary',
          cancelText: '取消',
          onOk: () => doStatusTransition(1, '启动项目')
        });
      } else {
        doStatusTransition(1, '启动项目');
      }
      break;
    case 'pause':
      Modal.confirm({
        title: '暂停项目',
        content: `确定要暂停「${projectInfo.value?.projectName}」吗？暂停后采集员将收到通知。`,
        okText: '确认暂停',
        cancelText: '取消',
        onOk: () => doStatusTransition(2, '暂停项目')
      });
      break;
    case 'resume':
      doStatusTransition(1, '恢复项目');
      break;
    case 'complete':
      Modal.confirm({
        title: '完成项目',
        width: 400,
        content: () => h('div', { style: 'padding: 8px 0' }, [
          h('div', { style: 'text-align:center;margin-bottom:16px' }, [
            h('div', { style: 'font-size:36px;font-weight:700;color:var(--color-text-primary)' }, `${progressPercent.value}%`),
            h('div', { style: 'font-size:13px;color:var(--color-text-secondary);margin-top:4px' }, '当前完成率')
          ]),
          h('div', { style: 'display:flex;gap:12px;margin-bottom:16px' }, [
            h('div', { style: 'flex:1;text-align:center;padding:10px;background:rgba(0,180,42,0.06);border-radius:8px' }, [
              h('div', { style: 'font-size:20px;font-weight:700;color:#00B42A' }, String(approvedCount.value)),
              h('div', { style: 'font-size:12px;color:#86909C' }, '已完成')
            ]),
            h('div', { style: 'flex:1;text-align:center;padding:10px;background:rgba(255,125,0,0.06);border-radius:8px' }, [
              h('div', { style: 'font-size:20px;font-weight:700;color:#FA8C16' }, String(totalCount.value - approvedCount.value)),
              h('div', { style: 'font-size:12px;color:#86909C' }, '待处理')
            ])
          ]),
          progressPercent.value < 100
            ? h('div', { style: 'padding:10px 14px;background:rgba(245,63,63,0.06);border-radius:8px;border:1px solid rgba(245,63,63,0.15)' }, [
                h('span', { style: 'font-size:13px;color:#F53F3F;font-weight:500' }, `仍有 ${totalCount.value - approvedCount.value} 个点位未完成，确认强制完成？`)
              ])
            : h('div', { style: 'padding:10px 14px;background:rgba(0,180,42,0.06);border-radius:8px;border:1px solid rgba(0,180,42,0.15)' }, [
                h('span', { style: 'font-size:13px;color:#00B42A;font-weight:500' }, '所有点位已完成，确认标记为已完成？')
              ])
        ]),
        okText: progressPercent.value < 100 ? '强制完成' : '确认完成',
        okType: progressPercent.value < 100 ? 'danger' : 'primary',
        cancelText: '取消',
        onOk: () => doStatusTransition(3, '完成项目')
      });
      break;
    case 'archive':
      Modal.confirm({
        title: '归档项目',
        width: 400,
        content: () => h('div', { style: 'padding: 8px 0' }, [
          h('div', { style: 'display:flex;align-items:center;gap:10px;padding:14px;background:rgba(255,125,0,0.06);border-radius:8px;border:1px solid rgba(255,125,0,0.15);margin-bottom:14px' }, [
            h('span', { style: 'font-size:24px' }, '🔒'),
            h('div', {}, [
              h('div', { style: 'font-size:14px;font-weight:600;color:var(--color-text-primary)' }, '项目将变为只读'),
              h('div', { style: 'font-size:12px;color:#86909C;margin-top:2px' }, '所有人无法修改数据，仅可查看和导出')
            ])
          ]),
          h('div', { style: 'font-size:13px;color:var(--color-text-secondary)' }, `确定归档「${projectInfo.value?.projectName}」？`)
        ]),
        okText: '确认归档',
        cancelText: '取消',
        onOk: async () => {
          statusTransitioning.value = true;
          try {
            await fetchArchiveProject(route.params.id as string);
            message.success('项目已归档');
            loadDetail();
          } catch (e: any) {
            message.error(e?.message || '归档失败');
          } finally {
            statusTransitioning.value = false;
          }
        }
      });
      break;
    case 'restore':
      Modal.confirm({
        title: '取消归档',
        width: 400,
        content: () => h('div', { style: 'padding: 8px 0' }, [
          h('div', { style: 'display:flex;align-items:center;gap:10px;padding:14px;background:rgba(22,119,255,0.06);border-radius:8px;border:1px solid rgba(22,119,255,0.15);margin-bottom:14px' }, [
            h('span', { style: 'font-size:24px' }, '📂'),
            h('div', {}, [
              h('div', { style: 'font-size:14px;font-weight:600;color:var(--color-text-primary)' }, '恢复为已完成状态'),
              h('div', { style: 'font-size:12px;color:#86909C;margin-top:2px' }, '项目将重新可编辑，操作会记录审计日志')
            ])
          ]),
          h('div', { style: 'font-size:13px;color:var(--color-text-secondary)' }, `确定取消归档「${projectInfo.value?.projectName}」？`)
        ]),
        okText: '确认恢复',
        cancelText: '取消',
        onOk: async () => {
          statusTransitioning.value = true;
          try {
            await fetchRestoreProject(route.params.id as string);
            message.success('项目已恢复');
            loadDetail();
          } catch (e: any) {
            message.error(e?.message || '恢复失败');
          } finally {
            statusTransitioning.value = false;
          }
        }
      });
      break;
  }
}

async function doStatusTransition(targetStatus: number, actionLabel: string) {
  statusTransitioning.value = true;
  try {
    const resp = await fetchUpdateProjectStatus(route.params.id as string, targetStatus);
    if ((resp as any)?.error) {
      message.error((resp as any).error?.message || `${actionLabel}失败`);
    } else {
      message.success(`${actionLabel}成功`);
      loadDetail();
    }
  } catch (e: any) {
    message.error(e?.message || `${actionLabel}失败`);
  } finally {
    statusTransitioning.value = false;
  }
}

const formattedUpdateTime = computed(() => {
  const t = (projectInfo.value as any)?.updateTime;
  if (!t) return '--';
  try {
    const d = new Date(t);
    return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  } catch { return t; }
});

// Mock recent activities (will be replaced by real API later)
const recentActivities = ref([
  { icon: 'i-material-symbols:upload-rounded', color: '#1677FF', desc: 'Sarah Chen 上传了 12 个点位', time: '今天 10:45' },
  { icon: 'i-material-symbols:check-circle-outline-rounded', color: '#00B42A', desc: '#ST-092 数据审核通过', time: '昨天 16:20' },
  { icon: 'i-material-symbols:warning-outline-rounded', color: '#F53F3F', desc: '检测到水质数据异常 - 地点 #W-12', time: '2024-05-19 09:15' },
  { icon: 'i-material-symbols:person-add-outline-rounded', color: '#86909C', desc: 'Marcus Holloway 加入项目组', time: '2024-05-18 14:00' }
]);

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
    const resp = await fetchGetPointList({ current: 1, size: 100, projectId: id, status: pointStatusFilter.value as any });
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
  if (key === 'sections') loadSections();
  if (key === 'members') {
    loadMembers();
    loadUsers();
  }
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

function filterPoints(val: any) {
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

// --- Section Management ---
const sectionList = ref<any[]>([]);
const sectionLoading = ref(false);
const sectionModalVisible = ref(false);
const editingSection = ref<any>(null);
const sectionForm = ref({ sectionName: '', sectionCode: '', description: '' });

const loadSections = async () => {
  sectionLoading.value = true;
  try {
    const projectId = route.params.id as string;
    const { data, error } = await fetchGetSectionList(projectId);
    if (!error && data) {
      sectionList.value = data;
    }
  } catch (e) {
    message.error('加载标段列表失败');
  } finally {
    sectionLoading.value = false;
  }
};

const handleAddSection = () => {
  editingSection.value = null;
  sectionForm.value = { sectionName: '', sectionCode: '', description: '' };
  sectionModalVisible.value = true;
};

const handleEditSection = (section: any) => {
  editingSection.value = section;
  sectionForm.value = {
    sectionName: section.sectionName,
    sectionCode: section.sectionCode,
    description: section.description || ''
  };
  sectionModalVisible.value = true;
};

const handleSaveSection = async () => {
  if (!sectionForm.value.sectionName || !sectionForm.value.sectionCode) {
    message.warning('请填写标段名称和编号');
    return;
  }
  try {
    const projectId = route.params.id as string;
    if (editingSection.value) {
      const { error } = await fetchUpdateSection(editingSection.value.id, {
        projectId,
        ...sectionForm.value
      });
      if (!error) {
        message.success('更新标段成功');
        sectionModalVisible.value = false;
        loadSections();
      }
    } else {
      const { error } = await fetchCreateSection({
        projectId,
        ...sectionForm.value
      });
      if (!error) {
        message.success('创建标段成功');
        sectionModalVisible.value = false;
        loadSections();
      }
    }
  } catch (e) {
    message.error('保存标段失败');
  }
};

const handleDeleteSection = (id: number | string) => {
  Modal.confirm({
    title: '确认删除该标段？',
    content: '删除后，该标段的信息将不可恢复。',
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      const { error } = await fetchDeleteSection(id);
      if (!error) {
        message.success('删除标段成功');
        loadSections();
      }
    }
  });
};

// --- Project Members ---
const memberList = ref<any[]>([]);
const memberLoading = ref(false);
const memberModalVisible = ref(false);
const memberForm = ref({ userId: undefined as number | string | undefined, role: 'collector' });
const usersList = ref<any[]>([]);

const loadUsers = async () => {
  const { data, error } = await fetchGetAllUsers();
  if (!error && data) {
    usersList.value = data;
  }
};

const loadMembers = async () => {
  memberLoading.value = true;
  try {
    const projectId = route.params.id as string;
    const { data, error } = await fetchGetProjectMembers(projectId);
    if (!error && data) {
      memberList.value = data;
    }
  } catch (e) {
    message.error('加载成员列表失败');
  } finally {
    memberLoading.value = false;
  }
};

const handleAddMember = () => {
  memberForm.value = { userId: undefined, role: 'collector' };
  memberModalVisible.value = true;
};

const handleSaveMember = async () => {
  if (!memberForm.value.userId) {
    message.warning('请选择用户');
    return;
  }
  try {
    const projectId = route.params.id as string;
    const { error } = await fetchAddProjectMember(projectId, {
      userId: memberForm.value.userId,
      role: memberForm.value.role
    });
    if (!error) {
      message.success('添加成员成功');
      memberModalVisible.value = false;
      loadMembers();
    }
  } catch (e) {
    message.error('添加成员失败');
  }
};

const handleRemoveMember = (userId: number | string) => {
  Modal.confirm({
    title: '确认移除该成员？',
    content: '被移除后，该成员将失去对该项目的访问与采集权限。',
    okText: '确认移除',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      const projectId = route.params.id as string;
      const { error } = await fetchRemoveProjectMember(projectId, userId);
      if (!error) {
        message.success('移除成员成功');
        loadMembers();
      }
    }
  });
};

const handleRoleChange = async (userId: number | string, newRole: string) => {
  try {
    const projectId = route.params.id as string;
    const { error } = await fetchUpdateProjectMemberRole(projectId, userId, newRole);
    if (!error) {
      message.success('更新角色成功');
      loadMembers();
    }
  } catch (e) {
    message.error('更新角色失败');
  }
};

const getUserRealName = (userId: number | string) => {
  const user = usersList.value.find(u => String(u.id) === String(userId));
  return user ? user.realName : `用户ID: ${userId}`;
};

const getUserUsername = (userId: number | string) => {
  const user = usersList.value.find(u => String(u.id) === String(userId));
  return user ? user.username : '';
};

// Avatar Initials
const getInitials = (name: string) => {
  if (!name) return '??';
  if (/[\u4e00-\u9fa5]/.test(name)) {
    return name.length > 2 ? name.substring(name.length - 2) : name;
  }
  const parts = name.split(' ');
  if (parts.length > 1) {
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }
  return name.substring(0, 2).toUpperCase();
};

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
    <div class="p-24px">
      <!-- Page Header -->
      <div class="mb-24px">
        <div class="flex items-center justify-between flex-wrap gap-12px">
          <div>
            <div class="flex items-center gap-10px mb-6px">
              <span v-if="!loading && projectInfo" class="project-code-pill">{{ projectInfo.projectCode || '--' }}</span>
            </div>
            <div class="flex items-center gap-12px">
              <ASkeleton v-if="loading" active :paragraph="false" style="width:300px;height:28px" />
              <h1 v-else class="text-22px font-700 m-0 text-[var(--color-text-primary)]">{{ projectInfo?.projectName || '未知项目' }}</h1>
              <div v-if="!loading && projectInfo" class="flex items-center gap-6px">
                <span class="status-dot" :style="{ background: statusTagInfo.color }"></span>
                <span class="text-13px text-[var(--color-text-secondary)]">{{ statusTagInfo.label }}</span>
              </div>
            </div>
            <p v-if="!loading && projectInfo" class="text-12px text-[var(--color-text-placeholder)] m-0 mt-6px">
              最后更新: {{ formattedUpdateTime }}
            </p>
          </div>
          <div class="flex items-center gap-8px flex-shrink-0 flex-wrap">
            <!-- 状态操作按钮 -->
            <template v-if="!loading && projectInfo">
              <AButton
                v-for="action in statusActions"
                :key="action.key"
                :loading="statusTransitioning"
                :class="['status-action-btn', `status-action-btn--${action.type}`]"
                @click="handleStatusAction(action.key)"
              >
                <span :class="[action.icon, 'text-14px mr-4px']"></span>
                {{ action.label }}
              </AButton>
            </template>
            <!-- 编辑按钮 -->
            <AButton
              v-if="!loading && projectInfo && projectInfo.status !== 4"
              class="btn-outline"
              @click="editModalVisible = true"
            >
              <span class="i-material-symbols:edit-outline-rounded text-14px mr-6px"></span>
              编辑
            </AButton>
            <!-- 返回按钮 -->
            <AButton class="btn-ghost" @click="router.push('/project/list')">
              <span class="i-material-symbols:arrow-back-rounded text-14px mr-4px"></span>
              返回
            </AButton>
          </div>
        </div>
      </div>

      <!-- Info Cards Row -->
      <div class="grid grid-cols-2 md:grid-cols-5 gap-16px mb-24px">
        <!-- 项目经理 -->
        <div class="info-card">
          <div class="flex items-center gap-10px mb-10px">
            <div class="info-icon" style="background: #E6F0FF">
              <span class="i-material-symbols:person-outline-rounded text-16px" style="color: #1677FF"></span>
            </div>
            <span class="text-12px text-[var(--color-text-secondary)]">项目经理</span>
          </div>
          <ASkeleton v-if="loading" active :paragraph="false" style="width:100px;height:18px" />
          <div v-else class="text-14px font-600 text-[var(--color-text-primary)]">{{ projectInfo?.manager || '未指派' }}</div>
        </div>
        <!-- 客户单位 -->
        <div class="info-card">
          <div class="flex items-center gap-10px mb-10px">
            <div class="info-icon" style="background: #EDE6FF">
              <span class="i-material-symbols:business-outline-rounded text-16px" style="color: #7B61FF"></span>
            </div>
            <span class="text-12px text-[var(--color-text-secondary)]">客户单位</span>
          </div>
          <ASkeleton v-if="loading" active :paragraph="false" style="width:120px;height:18px" />
          <div v-else class="text-14px font-600 text-[var(--color-text-primary)]">{{ projectInfo?.clientName || '--' }}</div>
        </div>
        <!-- 项目周期 -->
        <div class="info-card">
          <div class="flex items-center gap-10px mb-10px">
            <div class="info-icon" style="background: #F0F2F5">
              <span class="i-material-symbols:calendar-month-outline-rounded text-16px" style="color: #666"></span>
            </div>
            <span class="text-12px text-[var(--color-text-secondary)]">项目周期</span>
          </div>
          <ASkeleton v-if="loading" active :paragraph="false" style="width:160px;height:18px" />
          <div v-else class="text-13px font-500 text-[var(--color-text-primary)]">
            <div>{{ projectInfo?.startDate || '--' }}</div>
            <div class="text-[var(--color-text-secondary)]">至 {{ projectInfo?.endDate || '--' }}</div>
          </div>
        </div>
        <!-- 当前阶段 -->
        <div class="info-card">
          <div class="flex items-center gap-10px mb-10px">
            <div class="info-icon" style="background: #FFF5E6">
              <span class="i-material-symbols:approval-delegation-outline-rounded text-16px" style="color: #FA8C16"></span>
            </div>
            <span class="text-12px text-[var(--color-text-secondary)]">当前阶段</span>
          </div>
          <ASkeleton v-if="loading" active :paragraph="false" style="width:100px;height:18px" />
          <div v-else class="text-14px font-600 text-[var(--color-text-primary)]">{{ currentStage }}</div>
        </div>
        <!-- 完成率 -->
        <div class="info-card">
          <div class="flex items-center gap-10px mb-10px">
            <div class="info-icon" style="background: #E6FFE6">
              <span class="i-material-symbols:check-circle-outline-rounded text-16px" style="color: #00B42A"></span>
            </div>
            <span class="text-12px text-[var(--color-text-secondary)]">完成率</span>
          </div>
          <ASkeleton v-if="loading" active :paragraph="false" style="width:50px;height:18px" />
          <div v-else class="text-22px font-700 text-[var(--color-success)]">{{ progressPercent }}%</div>
        </div>
      </div>

      <!-- Progress + Recent Activity Dual Column -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-16px mb-24px">
        <!-- Left: Progress Details (2/3 width) -->
        <div class="md:col-span-2 info-card">
          <div class="flex items-center justify-between mb-16px">
            <span class="text-15px font-600 text-[var(--color-text-primary)]">进度详情</span>
            <a class="text-13px text-[var(--color-primary)] cursor-pointer flex items-center gap-4px hover:opacity-80" @click="router.push(`/point/map?projectId=${route.params.id}`)">
              <span class="i-material-symbols:map-outline-rounded text-14px"></span>
              查看地图
            </a>
          </div>
          <div class="flex items-center gap-24px">
            <!-- Ring Progress Chart -->
            <div class="flex-shrink-0 relative" style="width:120px;height:120px">
              <svg viewBox="0 0 120 120" class="w-full h-full" style="transform: rotate(-90deg)">
                <circle cx="60" cy="60" r="50" fill="none" stroke="#F0F2F5" stroke-width="10" />
                <circle cx="60" cy="60" r="50" fill="none" stroke="#1677FF" stroke-width="10"
                  stroke-linecap="round"
                  :stroke-dasharray="`${progressPercent * 3.14} ${314 - progressPercent * 3.14}`"
                />
              </svg>
              <div class="absolute inset-0 flex flex-col items-center justify-center">
                <span class="text-28px font-700 text-[var(--color-text-primary)]">{{ progressPercent }}%</span>
                <span class="text-11px text-[var(--color-text-secondary)]">总完成度</span>
              </div>
            </div>
            <!-- Stat Cards -->
            <div class="flex-1 grid grid-cols-1 sm:grid-cols-3 gap-12px">
              <div class="stat-card">
                <div class="text-24px font-700" style="color: #0052CC">{{ approvedCount }}</div>
                <div class="text-12px text-[var(--color-text-secondary)] mt-4px">个点位已完成</div>
              </div>
              <div class="stat-card">
                <div class="text-24px font-700" style="color: #FA8C16">{{ pendingCount }}</div>
                <div class="text-12px text-[var(--color-text-secondary)] mt-4px">个点位待处理</div>
              </div>
              <div class="stat-card stat-card-alert">
                <div class="flex items-center gap-4px">
                  <span class="i-material-symbols:warning-outline-rounded text-14px text-white"></span>
                  <span class="text-24px font-700 text-white">{{ errorCount }}</span>
                </div>
                <div class="text-12px text-white/80 mt-4px">项警报</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: Recent Activity (1/3 width) -->
        <div class="info-card">
          <div class="text-15px font-600 text-[var(--color-text-primary)] mb-16px">最近动态</div>
          <div class="flex flex-col gap-0">
            <div v-for="(act, idx) in recentActivities" :key="idx" class="activity-item" :class="{ 'border-b border-[var(--color-divider)] pb-12px mb-12px': idx < recentActivities.length - 1 }">
              <div class="flex items-start gap-10px">
                <div class="w-28px h-28px rounded-full flex items-center justify-center flex-shrink-0 mt-2px" :style="{ background: act.color + '15' }">
                  <span :class="act.icon" class="text-14px" :style="{ color: act.color }"></span>
                </div>
                <div class="flex-1 min-w-0">
                  <div class="text-13px text-[var(--color-text-primary)] leading-20px">{{ act.desc }}</div>
                  <div class="text-11px text-[var(--color-text-placeholder)] mt-4px">{{ act.time }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Content Card (Tabs) -->
      <div class="info-card overflow-hidden">
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

        <!-- Tab: Sections -->
        <div v-show="activeTab === 'sections'" class="px-24px py-16px">
          <div class="flex justify-between items-center mb-16px">
            <div class="text-13px text-[var(--color-text-secondary)]">
              创建和管理该项目下的工程标段，可为各标段分配独立负责人及点位范围。
            </div>
            <AButton type="primary" size="small" class="btn-primary" @click="handleAddSection">
              <span class="i-material-symbols:add-circle-outline-rounded text-14px mr-6px"></span>
              新增标段
            </AButton>
          </div>

          <div v-if="sectionLoading" class="flex justify-center py-24px"><ASpin /></div>
          <div v-else-if="sectionList.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-16px">
            <div v-for="sec in sectionList" :key="sec.id" class="info-card p-16px flex flex-col justify-between">
              <div>
                <div class="flex justify-between items-start mb-8px">
                  <div class="flex items-center gap-8px">
                    <span class="i-material-symbols:grid-view-outline-rounded text-18px text-[var(--color-primary)]"></span>
                    <span class="text-15px font-600 text-[var(--color-text-primary)]">{{ sec.sectionName }}</span>
                  </div>
                  <span class="px-8px py-2px rounded-4px text-11px font-mono bg-slate-100 dark:bg-slate-800 text-[var(--color-text-secondary)]">
                    {{ sec.sectionCode }}
                  </span>
                </div>
                <p class="text-12px text-[var(--color-text-secondary)] min-h-36px mb-12px">
                  {{ sec.description || '暂无描述信息' }}
                </p>
              </div>
              <div class="flex justify-between items-center pt-12px border-t border-[var(--color-divider)]">
                <div class="flex items-center gap-6px text-12px text-[var(--color-text-secondary)]">
                  <span class="i-material-symbols:person-outline-rounded text-14px"></span>
                  负责人: <span class="font-500 text-[var(--color-text-primary)]">{{ getUserRealName(sec.managerId) || '未指派' }}</span>
                </div>
                <div class="flex items-center gap-4px">
                  <AButton type="text" size="small" class="action-btn" @click="handleEditSection(sec)">
                    <span class="i-material-symbols:edit-outline-rounded text-14px text-blue-500"></span>
                  </AButton>
                  <AButton type="text" size="small" class="action-btn" @click="handleDeleteSection(sec.id)">
                    <span class="i-material-symbols:delete-outline-rounded text-14px text-red-500"></span>
                  </AButton>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="py-40px text-center">
            <div class="i-material-symbols:grid-view-outline-rounded text-48px mx-auto mb-12px text-[var(--color-text-placeholder)]"></div>
            <p class="text-14px text-[var(--color-text-secondary)]">暂无标段数据</p>
          </div>
        </div>

        <!-- Tab: Members -->
        <div v-show="activeTab === 'members'" class="px-24px py-16px">
          <div class="flex justify-between items-center mb-16px">
            <div class="text-13px text-[var(--color-text-secondary)]">
              管理参与此项目的成员列表，分配相应的操作角色权限（管理员、采集员、审核员）。
            </div>
            <AButton type="primary" size="small" class="btn-primary" @click="handleAddMember">
              <span class="i-material-symbols:person-add-outline-rounded text-14px mr-6px"></span>
              添加成员
            </AButton>
          </div>

          <div v-if="memberLoading" class="flex justify-center py-24px"><ASpin /></div>
          <div v-else-if="memberList.length > 0" class="point-table">
            <div class="point-table-header bg-slate-50 dark:bg-slate-900 border-b border-[var(--color-divider)]">
              <div class="point-th flex-1">成员姓名</div>
              <div class="point-th w-180px">账号用户名</div>
              <div class="point-th w-180px">项目内角色</div>
              <div class="point-th w-120px text-right">操作</div>
            </div>
            <div class="point-table-body">
              <div v-for="mbr in memberList" :key="mbr.id" class="point-table-row hover:bg-slate-50/50 dark:hover:bg-slate-900/50">
                <div class="point-td flex-1 flex items-center gap-8px">
                  <div class="w-24px h-24px rounded-full bg-primary/10 flex items-center justify-center text-10px font-600 text-primary">
                    {{ getInitials(getUserRealName(mbr.userId)) }}
                  </div>
                  <span class="text-13px font-500 text-[var(--color-text-primary)]">{{ getUserRealName(mbr.userId) }}</span>
                </div>
                <div class="point-td w-180px text-12px text-[var(--color-text-secondary)]">
                  {{ getUserUsername(mbr.userId) }}
                </div>
                <div class="point-td w-180px">
                  <a-select
                    :value="mbr.role"
                    size="small"
                    class="w-140px"
                    @change="(val: any) => handleRoleChange(mbr.userId, val)"
                  >
                    <a-select-option value="admin">项目管理员</a-select-option>
                    <a-select-option value="collector">采集员</a-select-option>
                    <a-select-option value="auditor">审核员</a-select-option>
                    <a-select-option value="viewer">查看者</a-select-option>
                  </a-select>
                </div>
                <div class="point-td w-120px text-right">
                  <AButton type="text" size="small" class="action-btn inline-flex" @click="handleRemoveMember(mbr.userId)">
                    <span class="i-material-symbols:delete-outline-rounded text-14px text-red-500"></span>
                  </AButton>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="py-40px text-center">
            <div class="i-material-symbols:group-outline-rounded text-48px mx-auto mb-12px text-[var(--color-text-placeholder)]"></div>
            <p class="text-14px text-[var(--color-text-secondary)]">暂无成员数据</p>
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
      </div>
    </div>

    <!-- Loading overlay -->
    <div v-if="loading" class="fixed inset-0 bg-white/60 flex items-center justify-center z-50">
      <ASpin />
    </div>

    <!-- Edit Modal -->
    <ProjectCreateModal v-model:visible="editModalVisible" :editData="projectInfo" @success="onEditSuccess" />

    <!-- Section Create/Edit Modal -->
    <AModal
      v-model:open="sectionModalVisible"
      :title="editingSection ? '编辑标段' : '新增标段'"
      width="480px"
      :maskClosable="false"
      @ok="handleSaveSection"
    >
      <div class="flex flex-col gap-16px pt-12px">
        <div>
          <div class="text-12px font-500 text-[var(--color-text-secondary)] mb-6px">标段名称</div>
          <AInput v-model:value="sectionForm.sectionName" placeholder="如: 亳州排口整治一标段" />
        </div>
        <div>
          <div class="text-12px font-500 text-[var(--color-text-secondary)] mb-6px">标段编号</div>
          <AInput v-model:value="sectionForm.sectionCode" placeholder="如: SEC-01" />
        </div>
        <div>
          <div class="text-12px font-500 text-[var(--color-text-secondary)] mb-6px">备注描述</div>
          <ATextarea v-model:value="sectionForm.description" placeholder="请输入标段备注描述..." :rows="3" />
        </div>
      </div>
    </AModal>

    <!-- Member Add Modal -->
    <AModal
      v-model:open="memberModalVisible"
      title="添加项目成员"
      width="400px"
      :maskClosable="false"
      @ok="handleSaveMember"
    >
      <div class="flex flex-col gap-16px pt-12px">
        <div>
          <div class="text-12px font-500 text-[var(--color-text-secondary)] mb-6px">选择系统用户</div>
          <ASelect
            v-model:value="memberForm.userId"
            placeholder="请选择要添加的用户"
            class="w-full"
            show-search
            option-filter-prop="label"
          >
            <a-select-option
              v-for="user in usersList"
              :key="user.id"
              :value="user.id"
              :label="`${user.realName} (${user.username})`"
            >
              {{ user.realName }} ({{ user.username }})
            </a-select-option>
          </ASelect>
        </div>
        <div>
          <div class="text-12px font-500 text-[var(--color-text-secondary)] mb-6px">项目内角色</div>
          <ASelect v-model:value="memberForm.role" class="w-full">
            <a-select-option value="admin">项目管理员</a-select-option>
            <a-select-option value="collector">采集员</a-select-option>
            <a-select-option value="auditor">审核员</a-select-option>
            <a-select-option value="viewer">查看者</a-select-option>
          </ASelect>
        </div>
      </div>
    </AModal>
  </div>
</template>

<style scoped>
/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: var(--color-divider); border-radius: 10px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: var(--color-text-placeholder); }

/* Project Code Pill */
.project-code-pill {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: #6B5CE7;
}

/* Status Dot */
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

/* Info Card */
.info-card {
  background: #fff;
  border: 1px solid var(--color-divider);
  border-radius: 8px;
  padding: 20px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.info-card:hover {
  border-color: rgba(22, 119, 255, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* Info Icon */
.info-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* Stat Card */
.stat-card {
  background: #F7F8FA;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.stat-card-alert {
  background: #FF4D4F !important;
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

/* Status Action Buttons */
.status-action-btn {
  height: 32px !important;
  border-radius: 6px !important;
  font-size: 13px !important;
  font-weight: 500 !important;
  display: inline-flex !important;
  align-items: center !important;
  transition: all 0.2s ease !important;
}
.status-action-btn--primary {
  background: var(--color-primary) !important;
  color: white !important;
  border: none !important;
}
.status-action-btn--primary:hover { opacity: 0.9 !important; }
.status-action-btn--warning {
  background: #FF7D00 !important;
  color: white !important;
  border: none !important;
}
.status-action-btn--warning:hover { opacity: 0.9 !important; }
.status-action-btn--success {
  background: #00B42A !important;
  color: white !important;
  border: none !important;
}
.status-action-btn--success:hover { opacity: 0.9 !important; }
.status-action-btn--default {
  background: #F2F3F5 !important;
  color: var(--color-text-primary) !important;
  border: 1px solid var(--color-divider) !important;
}
.status-action-btn--default:hover { background: #E8E9EB !important; }
.status-action-btn--danger {
  background: #F53F3F !important;
  color: white !important;
  border: none !important;
}
.status-action-btn--danger:hover { opacity: 0.9 !important; }

.btn-outline {
  height: 32px !important;
  border-radius: 6px !important;
  font-size: 13px !important;
  background: transparent !important;
  color: var(--color-text-primary) !important;
  border: 1px solid var(--color-divider) !important;
  transition: all 0.2s ease !important;
}
.btn-outline:hover {
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
}

.btn-ghost {
  height: 32px !important;
  border-radius: 6px !important;
  font-size: 13px !important;
  background: transparent !important;
  color: var(--color-text-secondary) !important;
  border: none !important;
  transition: all 0.2s ease !important;
}
.btn-ghost:hover {
  background: rgba(0,0,0,0.04) !important;
  color: var(--color-text-primary) !important;
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

.point-table-row:last-child { border-bottom: none; }
.point-table-row:hover { background: rgba(22, 119, 255, 0.04); }

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

.point-code, .point-type, .point-coord {
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
.skeleton-item { background: rgba(247, 248, 250, 0.5); }

/* ===== 大屏适配 (2K+) ===== */
@media (min-width: 1920px) {
  .info-card {
    padding: 20px 24px;
  }
  .stat-card .text-24px {
    font-size: 28px;
  }
  .text-22px {
    font-size: 24px;
  }
  .project-code-pill {
    font-size: 13px;
    padding: 3px 12px;
  }
  .status-action-btn {
    height: 36px !important;
    font-size: 14px !important;
    padding: 0 18px !important;
  }
  .btn-outline, .btn-ghost {
    height: 36px !important;
    font-size: 14px !important;
  }
}

@media (min-width: 2200px) {
  .info-card {
    padding: 24px 28px;
  }
  .stat-card .text-24px {
    font-size: 32px;
  }
  .text-22px {
    font-size: 26px;
  }
}
</style>
