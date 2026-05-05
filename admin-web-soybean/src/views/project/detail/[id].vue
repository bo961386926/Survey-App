<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { fetchGetProjectDetail } from '@/service/api';
import ProjectCreateModal from '../modules/create-modal.vue';

defineOptions({ name: 'ProjectDetail' });

interface Props {
  id: string;
}
const props = defineProps<Props>();

const loading = ref(true);
const editModalVisible = ref(false);

const overview = ref({
  pointsCount: 0,
  completedPoints: 0,
  abnormalPoints: 0,
  auditPending: 0
});

const projectInfo = ref<Api.Project.ProjectInfo | null>(null);

const risks = ref([
  { id: 1, type: 'warning', text: '标段A 进度滞后于计划 15%', time: '2026-05-01 10:20' },
  { id: 2, type: 'danger', text: '连续 3 个点位发现管网破损，需要重点复核', time: '2026-04-30 16:45' }
]);

const sections = ref<Api.Project.ProjectSection[]>([]);
const members = ref<Api.Project.ProjectMember[]>([]);

const loadDetail = async () => {
  loading.value = true;
  try {
    const { data, error } = await fetchGetProjectDetail(props.id);
    if (!error && data) {
      projectInfo.value = data;
      overview.value = {
        pointsCount: data.pointTotal || 0,
        completedPoints: data.pointDone || 0,
        abnormalPoints: 0, // Mock for now
        auditPending: data.auditPending || 0
      };
      sections.value = data.sections || [];
      members.value = data.members || [];
    }
  } catch (err) {
    console.error('Failed to fetch project detail:', err);
  } finally {
    loading.value = false;
  }
};

const handleEdit = () => {
  editModalVisible.value = true;
};

const handleExport = () => {
  message.loading('正在生成勘察项目报表...', 1.5).then(() => {
    message.success('报表导出成功');
  });
};

const handleEditSuccess = () => {
  loadDetail();
};

onMounted(() => {
  loadDetail();
});
</script>

<template>
  <div class="p-6 h-full flex flex-col gap-4 overflow-y-auto">
    <!-- Header -->
    <div class="header-card">
      <a-breadcrumb class="mb-2">
        <a-breadcrumb-item>
          <router-link to="/project">项目管理</router-link>
        </a-breadcrumb-item>
        <a-breadcrumb-item>项目详情</a-breadcrumb-item>
      </a-breadcrumb>
      <div class="flex justify-between items-center">
        <div class="flex items-center gap-4">
          <h1 class="text-18px font-600 m-0 title-text">
            <a-skeleton active :loading="loading" :paragraph="false" style="width: 200px" />
            <span v-if="!loading">{{ projectInfo?.projectName || '未知项目' }}</span>
          </h1>
          <span
            v-if="!loading && projectInfo"
            :class="[
              'px-12px py-2px rd-full text-11px font-bold inline-flex items-center gap-6px',
              projectInfo.status == 1 ? 'bg-primary/10 text-primary' : 
              projectInfo.status == 2 ? 'bg-warning/10 text-warning' : 
              projectInfo.status == 3 ? 'bg-success/10 text-success' : 
              projectInfo.status == 4 ? 'bg-secondary/10 text-secondary' : 'bg-info/10 text-info'
            ]"
          >
            <span v-if="projectInfo.status == 1" class="size-6px rd-full bg-primary animate-pulse"></span>
            <span v-else class="size-6px rd-full bg-current"></span>
            {{ 
              projectInfo.status == 0 ? '草稿' : 
              projectInfo.status == 1 ? '进行中' : 
              projectInfo.status == 2 ? '已暂停' : 
              projectInfo.status == 3 ? '已完成' : 
              projectInfo.status == 4 ? '已归档' : '未知状态'
            }}
          </span>
        </div>
        <div class="flex gap-2">
          <a-button type="primary" ghost @click="handleExport">导出报表</a-button>
          <a-button type="primary" @click="handleEdit">编辑项目</a-button>
        </div>
      </div>
      <div v-if="!loading && projectInfo" class="mt-2 flex items-center gap-6 desc-text text-13px">
        <div class="flex items-center gap-1">
          <div class="i-material-symbols:person-outline text-16px"></div>
          负责人：{{ projectInfo.manager || '未指派' }}
        </div>
        <div class="flex items-center gap-1">
          <div class="i-material-symbols:calendar-month-outline text-16px"></div>
          周期：{{ projectInfo.startDate || '--' }} 至 {{ projectInfo.endDate || '--' }}
        </div>
        <div class="flex items-center gap-1">
          <div class="i-material-symbols:label-outline text-16px"></div>
          编号：{{ projectInfo.projectCode || '--' }}
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <a-skeleton active :loading="loading">
      <a-row :gutter="[16, 16]">
        <!-- Left Column -->
        <a-col :span="16">
          <!-- Overview Stats -->
          <a-row :gutter="[16, 16]" class="mb-4">
            <a-col :span="6">
              <div class="stat-card">
                <div class="stat-title">总点位数</div>
                <div class="stat-value text-primary">{{ overview.pointsCount }}</div>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="stat-card">
                <div class="stat-title">已完成</div>
                <div class="stat-value text-success">{{ overview.completedPoints }}</div>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="stat-card">
                <div class="stat-title">待审核</div>
                <div class="stat-value text-warning">{{ overview.auditPending }}</div>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="stat-card">
                <div class="stat-title">异常点位</div>
                <div class="stat-value text-danger">{{ overview.abnormalPoints }}</div>
              </div>
            </a-col>
          </a-row>

          <!-- Section Progress -->
          <div class="content-card">
            <div class="card-title">标段进度</div>
            <div class="flex flex-col gap-4 mt-4" v-if="sections.length > 0">
              <div v-for="sec in sections" :key="sec.name" class="section-item">
                <div class="flex justify-between mb-2">
                  <span class="font-500 title-text">{{ sec.name }}</span>
                  <span class="desc-text">{{ sec.completed }} / {{ sec.points }} 点位 | 负责人：{{ sec.manager }}</span>
                </div>
                <a-progress :percent="sec.progress" :status="sec.progress === 100 ? 'success' : 'active'" strokeColor="var(--color-primary)" />
              </div>
            </div>
            <div v-else class="text-center py-4 desc-text">暂无标段数据</div>
          </div>
        </a-col>

        <!-- Right Column -->
        <a-col :span="8">
          <!-- Risks -->
          <div class="content-card mb-4">
            <div class="flex items-center gap-2 mb-4">
              <icon-lucide-alert-triangle class="text-danger" />
              <div class="card-title m-0">风险提示</div>
            </div>
            <div class="flex flex-col gap-3">
              <div v-for="risk in risks" :key="risk.id" class="risk-item" :class="`risk-${risk.type}`">
                <div class="text-13px">{{ risk.text }}</div>
                <div class="text-12px mt-1 opacity-70">{{ risk.time }}</div>
              </div>
            </div>
          </div>

          <!-- Members -->
          <div class="content-card">
            <div class="card-title mb-4">项目成员</div>
            <div class="flex flex-col gap-4" v-if="members.length > 0">
              <div v-for="member in members" :key="member.name" class="flex items-center gap-3">
                <img :src="member.avatar || `https://api.dicebear.com/7.x/adventurer-neutral/svg?seed=${member.name}`" class="w-10 h-10 rounded-full bg-gray-100" />
                <div>
                  <div class="text-14px title-text">{{ member.name }}</div>
                  <div class="text-12px desc-text mt-1">{{ member.role }}</div>
                </div>
              </div>
            </div>
            <div v-else class="text-center py-4 desc-text">暂无成员</div>
          </div>
        </a-col>
      </a-row>
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
.header-card, .content-card, .stat-card {
  background-color: var(--bg-card);
  border-radius: 8px;
  box-shadow: var(--shadow-card);
}

.header-card {
  padding: 16px 20px;
}

.content-card {
  padding: 20px;
}

.stat-card {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title-text {
  color: var(--color-text-primary);
}

.desc-text {
  color: var(--color-text-secondary);
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.stat-title {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
}

.text-primary { color: var(--color-primary); }
.text-success { color: var(--color-success); }
.text-warning { color: var(--color-warning); }
.text-danger { color: var(--color-danger); }

.section-item {
  padding: 12px;
  border-radius: 6px;
  background-color: var(--bg-card-alt);
  border: 1px solid var(--color-divider);
}

.risk-item {
  padding: 10px 12px;
  border-radius: 6px;
  border-left: 3px solid;
  background-color: var(--bg-card-alt);
  color: var(--color-text-primary);
}

.risk-warning {
  border-left-color: var(--color-warning);
}

.risk-danger {
  border-left-color: var(--color-danger);
}
</style>


