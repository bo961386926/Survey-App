<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchTaskById, updateTask, changeTaskStatus, deleteTask } from '@/service/api/task';
import { message } from 'ant-design-vue';
import { localStg } from '@/utils/storage';
import dayjs from 'dayjs';

defineOptions({ name: 'TaskDetail' });

const route = useRoute();
const router = useRouter();
const taskId = route.params.id as string;

const task = ref<any>(null);
const loading = ref(false);
const subtasks = ref<any[]>([]);

const currentUser = (localStg as any).get('userInfo') as any || {};

// 权限控制
const isCreator = computed(() => task.value && task.value.ownerUserId === currentUser.id);
const isAssignee = computed(() => task.value && task.value.assigneeId === currentUser.id);

const getPriorityColor = (p: number) => {
  const map: Record<number, string> = {
    0: 'default',
    1: 'warning',
    2: 'error'
  };
  return map[p] || 'default';
};

const getPriorityText = (p: number) => {
  const map: Record<number, string> = {
    0: '普通',
    1: '重要',
    2: '紧急'
  };
  return map[p] || '未知';
};

const getStatusInfo = (status: number) => {
  const map: Record<number, { color: string; text: string }> = {
    0: { color: 'default', text: '待分配' },
    1: { color: 'processing', text: '进行中' },
    2: { color: 'success', text: '已完成' },
    3: { color: 'error', text: '已逾期' },
    4: { color: 'warning', text: '已终止' }
  };
  return map[status] || map[0];
};

const loadTask = async () => {
  loading.value = true;
  try {
    const { data } = await fetchTaskById(taskId);
    if (data) {
      task.value = data;
      if (data.subtasks) {
        try {
          subtasks.value = JSON.parse(data.subtasks);
        } catch(e) {
          subtasks.value = [];
        }
      } else {
        subtasks.value = [];
      }
    }
  } catch (err: any) {
    message.error('加载任务详情失败');
    console.error(err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  if (taskId) loadTask();
});

const handleBack = () => {
  router.back();
};

const handleCompleteSubtask = async (item: any, checked: boolean) => {
  if (!isAssignee.value && !isCreator.value) {
    message.warning('只有负责人或创建人可以修改任务进度');
    return;
  }
  item.status = checked ? 'completed' : 'pending';
  
  // 更新后端
  try {
    const updatedSubtasksJson = JSON.stringify(subtasks.value);
    await updateTask({ id: task.value.id, subtasks: updatedSubtasksJson });
    message.success('子任务状态已更新');
  } catch (error) {
    message.error('更新失败');
    item.status = checked ? 'pending' : 'completed'; // revert
  }
};

const handleCompleteTask = async () => {
  try {
    await changeTaskStatus(task.value.id, 2);
    message.success('任务已标记为结项');
    loadTask();
  } catch (err) {
    message.error('操作失败');
  }
};

const handleDeleteTask = async () => {
  try {
    await deleteTask(task.value.id);
    message.success('任务已删除');
    router.back();
  } catch (err) {
    message.error('删除失败');
  }
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-';
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm');
};
</script>

<template>
  <div class="p-6 min-h-screen bg-[#f5f7fa] flex flex-col gap-6">
    <div class="flex items-center">
      <a-button type="link" @click="handleBack" class="mb-2 px-0! text-[#5c6066] hover:text-[#2f54eb]">
        <span class="i-material-symbols:arrow-back-rounded text-18px mr-1"></span>
        返回列表
      </a-button>
    </div>

    <a-card v-if="task" :loading="loading" class="glass-card flex-1 shadow-sm border-0!">
      <!-- 头部 -->
      <template #title>
        <div class="flex items-center justify-between py-2">
          <div class="flex flex-col gap-2">
            <div class="flex items-center gap-3">
              <span class="text-13px bg-[#f0f5ff] text-[#2f54eb] px-2 py-0.5 rounded font-mono font-bold border border-[#adc6ff]">
                {{ task.plotCode }}
              </span>
              <span class="text-22px font-bold text-[#1f2329]">{{ task.taskName }}</span>
              <a-tag :color="getPriorityColor(task.priority)" class="m-0 border-0 rounded font-medium">
                {{ getPriorityText(task.priority) }}
              </a-tag>
              <a-tag :color="getStatusInfo(task.status).color" class="m-0 border-0 rounded font-medium">
                {{ getStatusInfo(task.status).text }}
              </a-tag>
            </div>
          </div>
          
          <div class="flex gap-3">
            <template v-if="isCreator || isAssignee">
              <a-button v-if="task.status !== 2" type="primary" class="bg-[#52c41a] hover:bg-[#73d13d] border-0" @click="handleCompleteTask">
                <template #icon><span class="i-material-symbols:check-circle-outline-rounded mr-1"></span></template>
                标记结项
              </a-button>
            </template>
            <template v-if="isCreator">
              <a-popconfirm title="确定要删除这个任务吗？" @confirm="handleDeleteTask" ok-text="删除" cancel-text="取消" ok-type="danger">
                <a-button danger>删除任务</a-button>
              </a-popconfirm>
            </template>
          </div>
        </div>
      </template>

      <div class="grid grid-cols-1 xl:grid-cols-3 gap-6 mt-2">
        
        <!-- 左侧信息区 -->
        <div class="xl:col-span-2 flex flex-col gap-6">
          <!-- 核心规格 -->
          <div class="bg-[#fafbfc] rounded-12px p-5 border border-[#e4e7ed]">
            <h3 class="text-15px font-bold text-[#1f2329] mb-4 flex items-center gap-2">
              <span class="w-1 h-4 bg-[#2f54eb] rounded-full"></span>
              任务基本信息
            </h3>
            <a-descriptions :column="{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }" size="small" class="detail-descriptions">
              <a-descriptions-item label="所属项目">
                <span class="font-medium text-[#1f2329]">{{ task.projectName }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="任务类别">
                <span class="text-[#5c6066]">{{ task.category }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="地块编号">
                <span class="font-mono text-[#5c6066]">{{ task.plotCode }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="精度要求">
                <span class="text-[#5c6066]">{{ task.precisionRequirement || '常规' }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="传感器类型">
                <span class="text-[#5c6066]">{{ task.sensorType || '无限制' }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="截止日期">
                <span class="text-[#f5222d] font-medium">{{ formatDate(task.deadline) }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="创建人">
                <span class="text-[#5c6066]">{{ task.ownerUserName || '系统' }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="创建时间">
                <span class="text-[#8f959e]">{{ formatDate(task.createTime) }}</span>
              </a-descriptions-item>
            </a-descriptions>
          </div>

          <!-- 子任务清单 -->
          <div class="bg-white rounded-12px p-5 border border-[#e4e7ed]">
            <h3 class="text-15px font-bold text-[#1f2329] mb-4 flex items-center gap-2">
              <span class="w-1 h-4 bg-[#2f54eb] rounded-full"></span>
              执行子任务清单
            </h3>
            <div v-if="subtasks.length > 0">
              <a-list :dataSource="subtasks" itemLayout="horizontal" class="subtask-list">
                <template #renderItem="{ item, index }">
                  <a-list-item class="hover:bg-[#f5f7fa] px-4 py-3 rounded-8px transition-colors">
                    <div class="flex items-center gap-3 w-full">
                      <a-checkbox 
                        :checked="item.status === 'completed'" 
                        @change="(e) => handleCompleteSubtask(item, e.target.checked)"
                        :disabled="!isAssignee && !isCreator"
                      />
                      <span 
                        class="text-14px flex-1 transition-all" 
                        :class="item.status === 'completed' ? 'text-[#8f959e] line-through' : 'text-[#1f2329] font-medium'"
                      >
                        {{ index + 1 }}. {{ item.title }}
                      </span>
                      <a-tag :color="item.status === 'completed' ? 'success' : 'default'" class="m-0 border-0">
                        {{ item.status === 'completed' ? '已完成' : '未完成' }}
                      </a-tag>
                    </div>
                  </a-list-item>
                </template>
              </a-list>
            </div>
            <div v-else class="text-[#8f959e] py-4 text-center bg-[#fafbfc] rounded-lg border border-dashed border-[#d9d9d9]">暂无子任务安排</div>
          </div>
          
          <!-- 任务描述 -->
          <div class="bg-white rounded-12px p-5 border border-[#e4e7ed]">
            <h3 class="text-15px font-bold text-[#1f2329] mb-4 flex items-center gap-2">
              <span class="w-1 h-4 bg-[#2f54eb] rounded-full"></span>
              任务描述
            </h3>
            <p class="text-[#5c6066] leading-relaxed whitespace-pre-wrap">{{ task.description || '无任务描述' }}</p>
          </div>

        </div>

        <!-- 右侧：地图与指派信息 -->
        <div class="flex flex-col gap-6">
          <!-- 负责人卡片 -->
          <div class="bg-[#fafbfc] rounded-12px p-5 border border-[#e4e7ed]">
            <h3 class="text-15px font-bold text-[#1f2329] mb-4 flex items-center gap-2">
              <span class="i-material-symbols:person-outline-rounded text-[#2f54eb] text-18px"></span>
              任务负责人
            </h3>
            <div class="flex items-center gap-4">
              <div class="w-48px h-48px rounded-full bg-[#2f54eb] text-white flex items-center justify-center font-bold text-18px shadow-sm">
                {{ task.assigneeName ? task.assigneeName[0] : '无' }}
              </div>
              <div class="flex flex-col">
                <span class="text-16px font-bold text-[#1f2329]">{{ task.assigneeName || '暂未分配' }}</span>
                <span class="text-13px text-[#8f959e] mt-1">{{ task.assigneeName ? '项目执行专员' : '等待管理员分配' }}</span>
              </div>
            </div>
          </div>

          <!-- 地图占位 -->
          <div class="bg-[#f0f2f5] rounded-12px border border-[#e4e7ed] overflow-hidden flex flex-col min-h-300px relative">
            <div class="absolute inset-0 flex flex-col items-center justify-center bg-[url('https://api.mapbox.com/styles/v1/mapbox/light-v10/static/116.40,39.90,12,0,0/600x400?access_token=placeholder')] bg-cover bg-center opacity-40 grayscale"></div>
            <div class="relative z-10 flex flex-col items-center justify-center h-full gap-3 py-10 bg-white/60 backdrop-blur-sm">
              <span class="i-material-symbols:location-on-rounded text-40px text-[#2f54eb]"></span>
              <span class="text-15px font-bold text-[#1f2329]">地图定位信息</span>
              <span class="text-12px text-[#5c6066]">实际接入坐标后在此展示目标点位</span>
              <a-button type="primary" size="small" ghost class="mt-2">查看坐标点位</a-button>
            </div>
          </div>
        </div>

      </div>
    </a-card>

    <a-card v-else-if="loading" :loading="true" class="glass-card flex-1 shadow-sm border-0!">
      <template #title>加载中...</template>
    </a-card>

    <a-card v-else class="glass-card flex-1 shadow-sm border-0!">
      <template #title>暂无任务数据或已被删除</template>
    </a-card>
  </div>
</template>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 16px;
}
:deep(.detail-descriptions .ant-descriptions-item-label) {
  color: #8f959e;
  width: 100px;
}
:deep(.subtask-list .ant-list-item) {
  border-bottom: 1px solid #f0f0f0;
}
:deep(.subtask-list .ant-list-item:last-child) {
  border-bottom: none;
}
</style>
