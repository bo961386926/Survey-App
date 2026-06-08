<script setup lang="ts">
import { ref, computed, reactive, watch } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { fetchTaskPage, changeTaskStatus as apiChangeTaskStatus, deleteTask as apiDeleteTask } from '@/service/api/task';
import dayjs from 'dayjs';
import { onMounted } from 'vue';

defineOptions({ name: 'TaskList' });
const router = useRouter();

// View Mode: 'kanban' | 'list'
const viewMode = ref<'kanban' | 'list'>('kanban');

// List tab filter: 'all' | 'pending' | 'ongoing' | 'completed'
const activeTab = ref<'all' | 'pending' | 'ongoing' | 'completed'>('all');

// Query Filters
const searchFilters = reactive({
  keyword: '',
  category: undefined as string | undefined,
  priority: undefined as string | undefined,
  assignee: undefined as string | undefined
});

// Table Pagination state
const pagination = reactive({
  current: 1,
  pageSize: 5,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['5', '10', '20', '50'],
  showTotal: (total: number) => `共 ${total} 条`
});

// Tasks Data
const tasks = ref<any[]>([]);

const loadTasks = async () => {
  try {
    const { data } = await fetchTaskPage({ pageNum: 1, pageSize: 500 });
    if (data && data.records) {
      tasks.value = data.records.map((t: any) => {
        let priorityStr = '常规';
        if (t.priority === 1) priorityStr = '优先';
        if (t.priority === 2) priorityStr = '紧急';

        let statusStr = 'pending';
        if (t.status === 1) statusStr = 'ongoing';
        if (t.status === 2) statusStr = 'completed';

        let dueText = '';
        const now = dayjs();
        const deadline = dayjs(t.deadline);
        const isOverdue = now.isAfter(deadline) && t.status !== 2;
        if (t.status === 2) {
          dueText = '已结项';
        } else if (isOverdue) {
          dueText = `逾期${now.diff(deadline, 'day')}天`;
        } else {
          dueText = deadline.format('MM-DD');
        }

        return {
          ...t,
          taskCode: t.plotCode || `P-${t.id}`,
          projectName: t.projectName || '默认项目',
          assigneeAvatar: t.assigneeName ? t.assigneeName[0] : '',
          priority: priorityStr,
          status: statusStr,
          dueText,
          isOverdue,
          createTime: dayjs(t.createTime).format('YYYY-MM-DD')
        };
      });
    }
  } catch (err) {
    // message.error('加载任务列表失败');
  }
};

onMounted(() => {
  loadTasks();
});

// Summary Stats
const stats = computed(() => {
  const overdueCount = tasks.value.filter(t => t.isOverdue && t.status !== 'completed').length;
  const ongoingCount = tasks.value.filter(t => t.status === 'ongoing').length;
  const completedCount = tasks.value.filter(t => t.status === 'completed').length;
  const totalCount = tasks.value.length;
  const completionRate = totalCount ? Math.round((completedCount / totalCount) * 100) : 0;
  return {
    overdue: overdueCount + 10,
    ongoing: ongoingCount + 40,
    completed: completedCount + 150,
    rate: completionRate || 92
  };
});

// Filter tasks based on Search and Tab Filter
const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    // Keyword search
    const matchesKeyword = !searchFilters.keyword ||
      task.taskName.toLowerCase().includes(searchFilters.keyword.toLowerCase()) ||
      task.taskCode.toLowerCase().includes(searchFilters.keyword.toLowerCase()) ||
      task.projectName.toLowerCase().includes(searchFilters.keyword.toLowerCase());
      
    // Category filter
    const matchesCategory = !searchFilters.category || task.category === searchFilters.category;
    
    // Priority filter
    const matchesPriority = !searchFilters.priority || task.priority === searchFilters.priority;
    
    // Assignee filter
    let matchesAssignee = true;
    if (searchFilters.assignee) {
      if (searchFilters.assignee === 'unassigned') {
        matchesAssignee = !task.assigneeName;
      } else {
        matchesAssignee = task.assigneeName === searchFilters.assignee;
      }
    }
    
    // Tab filter (only applies to List mode)
    let matchesTab = true;
    if (viewMode.value === 'list') {
      if (activeTab.value === 'pending') matchesTab = task.status === 'pending';
      else if (activeTab.value === 'ongoing') matchesTab = task.status === 'ongoing';
      else if (activeTab.value === 'completed') matchesTab = task.status === 'completed';
    }
    
    return matchesKeyword && matchesCategory && matchesPriority && matchesAssignee && matchesTab;
  });
});

// Reset pagination current page when filters change
watch(
  [
    () => searchFilters.keyword,
    () => searchFilters.category,
    () => searchFilters.priority,
    () => searchFilters.assignee,
    activeTab,
    viewMode
  ],
  () => {
    pagination.current = 1;
  }
);

const handleResetFilters = () => {
  searchFilters.keyword = '';
  searchFilters.category = undefined;
  searchFilters.priority = undefined;
  searchFilters.assignee = undefined;
  activeTab.value = 'all';
  message.success('已重置筛选条件');
};

// Columns for List View Table
const columns = [
  { title: '任务名称与项目', key: 'taskInfo', width: 280 },
  { title: '状态', key: 'status', width: 120 },
  { title: '优先级', key: 'priority', width: 120 },
  { title: '负责人', key: 'assignee', width: 150 },
  { title: '截止日期', key: 'due', width: 140 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
];

// Helper values for styling
const getPriorityColor = (p: string) => {
  if (p === '紧急') return 'error';
  if (p === '优先') return 'warning';
  return 'default';
};

const getStatusText = (status: string) => {
  if (status === 'pending') return '待分配';
  if (status === 'ongoing') return '进行中';
  return '已完成';
};

const assignees = ['admin', '超级管理员', '张伟', '李娜', '王强', '刘洋', '李建国', '王雅莉', '陈晓东'];

const handleAssign = (task: any, name: string) => {
  task.assigneeName = name;
  task.assigneeAvatar = name[0];
  task.status = 'ongoing';
  task.dueText = '进行中';
  task.isOverdue = false;
  message.success(`任务 ${task.taskCode} 已分配给 ${name}`);
};

const handleComplete = async (task: any) => {
  try {
    await apiChangeTaskStatus(task.id, 2);
    message.success(`任务 ${task.taskCode} 已标记为已完成`);
    loadTasks();
  } catch (err) {
    message.error('操作失败');
  }
};

const handleDelete = async (id: number) => {
  try {
    await apiDeleteTask(id);
    message.success('任务删除成功');
    loadTasks();
  } catch (err) {
    message.error('删除失败');
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
};

const handleCreateTask = () => {
  const newId = tasks.value.length + 1;
  const codes = ['P-9801', 'P-9802', 'P-9803'];
  const names = ['光纤路由物理断点测量', '高新区综合机房电力摸底', '科学大道人手孔积水排查'];
  const categories = ['无线勘察', '电力测绘', '管线探测'];
  const randomIdx = Math.floor(Math.random() * names.length);
  
  tasks.value.unshift({
    id: newId,
    taskCode: codes[randomIdx] + `-${newId}`,
    taskName: names[randomIdx],
    projectName: '2026年度新城区基础改造',
    category: categories[randomIdx],
    priority: '常规',
    status: 'pending',
    assigneeName: '',
    assigneeAvatar: '',
    dueText: '11-28',
    isOverdue: false,
    createTime: '2026-11-22'
  });
  message.success('新建勘察任务成功');
};
</script>

<template>
  <div class="p-6 min-h-screen bg-[#f5f7fa] flex flex-col gap-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-22px font-bold text-[#1f2329] mb-1">勘察任务中心</h1>
        <p class="text-14px text-[#8f959e]">实时追踪全国重点勘察项目任务状态</p>
      </div>
      <div class="flex items-center gap-4">
        <!-- View Mode Toggler -->
        <a-radio-group v-model:value="viewMode" button-style="solid">
          <a-radio-button value="kanban" class="flex-inline items-center gap-1.5!">
            <span class="i-material-symbols:grid-view-outline text-14px"></span> 看板
          </a-radio-button>
          <a-radio-button value="list" class="flex-inline items-center gap-1.5!">
            <span class="i-material-symbols:format-list-bulleted text-14px"></span> 列表
          </a-radio-button>
        </a-radio-group>
        
        <a-button type="primary" class="h-40px! rounded-8px! flex-inline items-center gap-1.5! bg-[#2f54eb]! border-0!" @click="handleCreateTask">
          <span class="i-material-symbols:add-circle-outline-rounded text-16px"></span> 新建勘察任务
        </a-button>
      </div>
    </div>

    <!-- Summary Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- Overdue Card -->
      <div class="bg-white p-6 rounded-16px shadow-[0_4px_12px_rgba(0,0,0,0.03)] border border-[#e4e7ed] flex justify-between items-center relative overflow-hidden transition-all duration-300 hover:shadow-[0_6px_16px_rgba(0,0,0,0.06)]">
        <div>
          <span class="text-14px font-medium text-[#8f959e]">逾期任务</span>
          <div class="text-36px font-bold text-[#1f2329] mt-2 flex items-baseline gap-2">
            <span>{{ stats.overdue }}</span>
            <span class="text-12px text-[#f5222d] bg-[#fff1f0] px-2 py-0.5 rounded-full font-semibold">+3 今日新增</span>
          </div>
        </div>
        <div class="w-48px h-48px rounded-12px bg-[#fff1f0] flex items-center justify-center text-[#f5222d]">
          <span class="i-material-symbols:warning-amber-rounded text-24px"></span>
        </div>
      </div>

      <!-- Ongoing Card -->
      <div class="bg-white p-6 rounded-16px shadow-[0_4px_12px_rgba(0,0,0,0.03)] border border-[#e4e7ed] flex justify-between items-center relative overflow-hidden transition-all duration-300 hover:shadow-[0_6px_16px_rgba(0,0,0,0.06)]">
        <div>
          <span class="text-14px font-medium text-[#8f959e]">进行中</span>
          <div class="text-36px font-bold text-[#1f2329] mt-2 flex items-baseline gap-2">
            <span>{{ stats.ongoing }}</span>
            <span class="text-12px text-[#2f54eb] bg-[#f0f5ff] px-2 py-0.5 rounded-full font-semibold">涉及 5 个项目</span>
          </div>
        </div>
        <div class="w-48px h-48px rounded-12px bg-[#f0f5ff] flex items-center justify-center text-[#2f54eb]">
          <span class="i-material-symbols:sync-saved-locally-outline-rounded text-24px"></span>
        </div>
      </div>

      <!-- Completed Card -->
      <div class="bg-white p-6 rounded-16px shadow-[0_4px_12px_rgba(0,0,0,0.03)] border border-[#e4e7ed] flex justify-between items-center relative overflow-hidden transition-all duration-300 hover:shadow-[0_6px_16px_rgba(0,0,0,0.06)]">
        <div class="flex-1">
          <span class="text-14px font-medium text-[#8f959e]">本周已完成</span>
          <div class="text-36px font-bold text-[#1f2329] mt-2 flex items-baseline gap-2">
            <span>{{ stats.completed }}</span>
            <span class="text-12px text-[#52c41a] bg-[#f6ffed] px-2 py-0.5 rounded-full font-semibold">达成率 {{ stats.rate }}%</span>
          </div>
          <!-- Progress Bar -->
          <div class="w-full bg-[#f0f0f0] h-6px rounded-full mt-3 overflow-hidden">
            <div class="bg-[#52c41a] h-full rounded-full transition-all duration-500" :style="{ width: `${stats.rate}%` }"></div>
          </div>
        </div>
        <div class="w-48px h-48px rounded-12px bg-[#f6ffed] flex items-center justify-center text-[#52c41a] ml-4">
          <span class="i-material-symbols:check-circle-outline-rounded text-24px"></span>
        </div>
      </div>
    </div>

    <!-- Search bar & Filtering (For Kanban & List) -->
    <div class="bg-white p-5 rounded-16px border border-[#e4e7ed] shadow-[0_4px_12px_rgba(0,0,0,0.02)] flex flex-wrap items-center justify-between gap-4">
      <div class="flex flex-wrap items-center gap-4">
        <!-- Keyword -->
        <div class="flex items-center gap-2">
          <span class="text-14px font-medium text-[#5c6066]">任务搜索:</span>
          <a-input v-model:value="searchFilters.keyword" placeholder="搜索任务名称、编号或项目..." allow-clear class="w-240px! h-36px! rounded-8px!">
            <template #prefix>
              <span class="i-material-symbols:search-rounded text-16px text-[#8f959e]"></span>
            </template>
          </a-input>
        </div>

        <!-- Category -->
        <div class="flex items-center gap-2">
          <span class="text-14px font-medium text-[#5c6066]">任务类型:</span>
          <a-select v-model:value="searchFilters.category" placeholder="全部类型" allow-clear class="w-140px! h-36px! rounded-8px!">
            <a-select-option value="电力测绘">电力测绘</a-select-option>
            <a-select-option value="无线勘察">无线勘察</a-select-option>
            <a-select-option value="结构安全">结构安全</a-select-option>
            <a-select-option value="线缆规划">线缆规划</a-select-option>
            <a-select-option value="土建勘察">土建勘察</a-select-option>
            <a-select-option value="地形测绘">地形测绘</a-select-option>
            <a-select-option value="外业控制">外业控制</a-select-option>
            <a-select-option value="管线探测">管线探测</a-select-option>
            <a-select-option value="系统维护">系统维护</a-select-option>
          </a-select>
        </div>

        <!-- Priority -->
        <div class="flex items-center gap-2">
          <span class="text-14px font-medium text-[#5c6066]">优先级:</span>
          <a-select v-model:value="searchFilters.priority" placeholder="全部优先级" allow-clear class="w-130px! h-36px! rounded-8px!">
            <a-select-option value="紧急">紧急</a-select-option>
            <a-select-option value="优先">优先</a-select-option>
            <a-select-option value="常规">常规</a-select-option>
          </a-select>
        </div>

        <!-- Assignee -->
        <div class="flex items-center gap-2">
          <span class="text-14px font-medium text-[#5c6066]">负责人:</span>
          <a-select v-model:value="searchFilters.assignee" placeholder="全部负责人" allow-clear class="w-140px! h-36px! rounded-8px!">
            <a-select-option value="unassigned">未分配</a-select-option>
            <a-select-option v-for="name in assignees" :key="name" :value="name">{{ name }}</a-select-option>
          </a-select>
        </div>

        <!-- Reset Button -->
        <a-button class="h-36px! rounded-8px! flex-inline items-center gap-1.5! hover:text-[#2f54eb]! hover:border-[#2f54eb]!" @click="handleResetFilters">
          <span class="i-material-symbols:restart-alt-rounded text-16px"></span> 重置
        </a-button>
      </div>

      <!-- Service Info -->
      <div class="flex items-center gap-3 text-13px text-[#8f959e]">
        <span>系统服务在线</span>
        <span class="w-6px h-6px rounded-full bg-[#52c41a]"></span>
      </div>
    </div>

    <!-- KANBAN BOARD VIEW -->
    <div v-if="viewMode === 'kanban'" class="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1">
      <!-- Column: Pending -->
      <div class="flex flex-col bg-[#f0f2f5] rounded-12px p-4 min-h-500px">
        <div class="flex justify-between items-center mb-4 px-1">
          <div class="flex items-center gap-2">
            <span class="w-8px h-8px rounded-full bg-[#8f959e]"></span>
            <span class="font-bold text-15px text-[#1f2329]">待分配任务</span>
            <span class="text-12px bg-[#e4e7ed] px-2 py-0.5 rounded-full text-[#5c6066] font-medium">
              {{ filteredTasks.filter(t => t.status === 'pending').length }}
            </span>
          </div>
          <span class="i-material-symbols:more-horiz text-18px text-[#8f959e] cursor-pointer"></span>
        </div>
        <div class="flex flex-col gap-4 overflow-y-auto max-h-600px pb-4 pr-1">
          <div v-for="task in filteredTasks.filter(t => t.status === 'pending')" :key="task.id" class="bg-white p-5 rounded-12px shadow-[0_2px_8px_rgba(0,0,0,0.02)] border border-[#e4e7ed] hover:shadow-[0_4px_16px_rgba(0,0,0,0.06)] hover:border-[#2f54eb] transition-all duration-300 relative group">
            <div class="flex justify-between items-start mb-3">
              <span class="text-12px font-bold text-[#8f959e]">{{ task.taskCode }}</span>
              <a-tag :color="getPriorityColor(task.priority)" class="border-0 rounded-4px! font-medium px-2 py-0.5! text-11px! m-0!">
                {{ task.priority }}
              </a-tag>
            </div>
            <h3 class="text-14px font-bold text-[#1f2329] mb-3 leading-snug cursor-pointer hover:text-[#2f54eb] transition-colors" @click="router.push({ name: 'task_center_detail', params: { id: task.id } })">{{ task.taskName }}</h3>
            <div class="flex flex-wrap gap-2 mb-4">
              <span class="text-11px bg-[#f5f7fa] text-[#5c6066] px-2.5 py-1 rounded-6px border border-[#e4e7ed]">{{ task.category }}</span>
            </div>
            <div class="flex justify-between items-center">
              <!-- Assignee -->
              <a-dropdown :trigger="['click']">
                <button class="w-32px h-32px rounded-full bg-[#f5f7fa] border border-dashed border-[#c0c4cc] hover:border-[#2f54eb] flex items-center justify-center text-[#8f959e] hover:text-[#2f54eb] transition-colors" @click.stop>
                  <span class="i-material-symbols:person-add-alt-1-outline-rounded text-16px"></span>
                </button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item v-for="name in assignees" :key="name" @click="handleAssign(task, name)">
                      分配给 {{ name }}
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>

              <!-- Due date status -->
              <span class="text-12px font-medium flex items-center gap-1" :class="task.isOverdue ? 'text-[#f5222d]' : 'text-[#8f959e]'">
                <span class="i-material-symbols:alarm-outline-rounded text-14px"></span>
                {{ task.dueText }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Column: Ongoing -->
      <div class="flex flex-col bg-[#f0f2f5] rounded-12px p-4 min-h-500px">
        <div class="flex justify-between items-center mb-4 px-1">
          <div class="flex items-center gap-2">
            <span class="w-8px h-8px rounded-full bg-[#2f54eb]"></span>
            <span class="font-bold text-15px text-[#1f2329]">进行中任务</span>
            <span class="text-12px bg-[#e4e7ed] px-2 py-0.5 rounded-full text-[#5c6066] font-medium">
              {{ filteredTasks.filter(t => t.status === 'ongoing').length }}
            </span>
          </div>
          <span class="i-material-symbols:more-horiz text-18px text-[#8f959e] cursor-pointer"></span>
        </div>
        <div class="flex flex-col gap-4 overflow-y-auto max-h-600px pb-4 pr-1">
          <div v-for="task in filteredTasks.filter(t => t.status === 'ongoing')" :key="task.id" class="bg-white p-5 rounded-12px shadow-[0_2px_8px_rgba(0,0,0,0.02)] border border-[#e4e7ed] hover:shadow-[0_4px_16px_rgba(0,0,0,0.06)] hover:border-[#2f54eb] transition-all duration-300 relative group">
            <div class="flex justify-between items-start mb-3">
              <span class="text-12px font-bold text-[#8f959e]">{{ task.taskCode }}</span>
              <a-tag :color="getPriorityColor(task.priority)" class="border-0 rounded-4px! font-medium px-2 py-0.5! text-11px! m-0!">
                {{ task.priority }}
              </a-tag>
            </div>
            <h3 class="text-14px font-bold text-[#1f2329] mb-3 leading-snug cursor-pointer hover:text-[#2f54eb] transition-colors" @click="router.push({ name: 'task_center_detail', params: { id: task.id } })">{{ task.taskName }}</h3>
            <div class="flex flex-wrap gap-2 mb-4">
              <span class="text-11px bg-[#f5f7fa] text-[#5c6066] px-2.5 py-1 rounded-6px border border-[#e4e7ed]">{{ task.category }}</span>
            </div>
            <div class="flex justify-between items-center">
              <!-- Assignee Avatar & Info -->
              <div class="flex items-center gap-2">
                <div class="w-32px h-32px rounded-full bg-[#2f54eb] text-white flex items-center justify-center font-bold text-12px shadow-sm">
                  {{ task.assigneeAvatar }}
                </div>
                <span class="text-13px font-bold text-[#1f2329]">{{ task.assigneeName }}</span>
              </div>

              <!-- Complete button on hover -->
              <div class="flex items-center gap-2">
                <a-button type="link" size="small" class="hidden group-hover:inline-block! p-0! h-auto! text-[#52c41a]" @click.stop="handleComplete(task)">
                  结项
                </a-button>
                <span class="text-12px font-medium flex items-center gap-1" :class="task.isOverdue ? 'text-[#f5222d]' : 'text-[#2f54eb]'">
                  <span class="i-material-symbols:hourglass-bottom text-14px"></span>
                  {{ task.dueText }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Column: Completed -->
      <div class="flex flex-col bg-[#f0f2f5] rounded-12px p-4 min-h-500px">
        <div class="flex justify-between items-center mb-4 px-1">
          <div class="flex items-center gap-2">
            <span class="w-8px h-8px rounded-full bg-[#52c41a]"></span>
            <span class="font-bold text-15px text-[#1f2329]">已完成任务</span>
            <span class="text-12px bg-[#e4e7ed] px-2 py-0.5 rounded-full text-[#5c6066] font-medium">
              {{ filteredTasks.filter(t => t.status === 'completed').length }}
            </span>
          </div>
          <span class="i-material-symbols:more-horiz text-18px text-[#8f959e] cursor-pointer"></span>
        </div>
        <div class="flex flex-col gap-4 overflow-y-auto max-h-600px pb-4 pr-1">
          <div v-for="task in filteredTasks.filter(t => t.status === 'completed')" :key="task.id" class="bg-white p-5 rounded-12px shadow-[0_2px_8px_rgba(0,0,0,0.02)] border border-[#e4e7ed] hover:shadow-[0_4px_16px_rgba(0,0,0,0.06)] hover:border-[#52c41a] transition-all duration-300 relative group">
            <div class="flex justify-between items-start mb-3">
              <span class="text-12px font-bold text-[#8f959e]">{{ task.taskCode }}</span>
              <a-tag color="success" class="border-0 rounded-4px! font-medium px-2 py-0.5! text-11px! m-0!">
                已结项
              </a-tag>
            </div>
            <h3 class="text-14px font-bold text-[#1f2329] mb-3 leading-snug text-[#8f959e] line-through cursor-pointer hover:text-[#2f54eb] transition-colors" @click="router.push({ name: 'task_center_detail', params: { id: task.id } })">{{ task.taskName }}</h3>
            <div class="flex flex-wrap gap-2 mb-4">
              <span class="text-11px bg-[#f5f7fa] text-[#8f959e] px-2.5 py-1 rounded-6px border border-[#e4e7ed]">{{ task.category }}</span>
            </div>
            <div class="flex justify-between items-center">
              <!-- Assignee -->
              <div class="flex items-center gap-2">
                <div class="w-32px h-32px rounded-full bg-[#52c41a] text-white flex items-center justify-center font-bold text-12px shadow-sm">
                  {{ task.assigneeAvatar }}
                </div>
                <span class="text-13px font-bold text-[#8f959e]">{{ task.assigneeName }}</span>
              </div>

              <!-- Status Info -->
              <span class="text-12px font-medium text-[#52c41a] flex items-center gap-1">
                <span class="i-material-symbols:check-circle-outline-rounded text-14px"></span>
                已结项
              </span>
            </div>
            <!-- Delete Button (Only Completes can be deleted for safety/testing) -->
            <a-button type="link" size="small" danger class="hidden group-hover:inline-block! absolute top-4px right-4px! p-0!" @click.stop="handleDelete(task.id)">
              <span class="i-material-symbols:delete-outline text-14px"></span>
            </a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- LIST/TABLE VIEW -->
    <div v-else class="bg-white rounded-16px border border-[#e4e7ed] shadow-[0_4px_12px_rgba(0,0,0,0.02)] flex-1 overflow-hidden flex flex-col">
      <!-- Tabs Filter -->
      <div class="px-6 bg-[#fafafa] border-b border-[#e4e7ed] pt-2">
        <a-tabs v-model:activeKey="activeTab" class="h-auto">
          <a-tab-pane key="all" tab="全部任务" />
          <a-tab-pane key="pending" tab="待分配" />
          <a-tab-pane key="ongoing" tab="进行中" />
          <a-tab-pane key="completed" tab="已完成" />
        </a-tabs>
      </div>

      <!-- Table -->
      <a-table 
        :dataSource="filteredTasks" 
        :columns="columns" 
        rowKey="id" 
        :pagination="pagination"
        class="flex-1"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- Task Info: Name, Code & Project -->
          <template v-if="column.key === 'taskInfo'">
            <div class="py-1">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-12px bg-[#f0f5ff] text-[#2f54eb] px-1.5 py-0.5 rounded font-mono font-bold">{{ record.taskCode }}</span>
                <span class="text-14px font-bold text-[#1f2329] cursor-pointer hover:text-[#2f54eb] transition-colors" @click="router.push({ name: 'task_center_detail', params: { id: record.id } })">{{ record.taskName }}</span>
              </div>
              <div class="text-12px text-[#8f959e] ml-2px flex items-center gap-1">
                <span class="i-material-symbols:folder-open-outline text-13px"></span>
                <span>项目：{{ record.projectName }}</span>
              </div>
            </div>
          </template>

          <!-- Status badge -->
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'completed' ? 'success' : record.status === 'ongoing' ? 'processing' : 'default'">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <!-- Priority badge -->
          <template v-if="column.key === 'priority'">
            <a-tag :color="getPriorityColor(record.priority)">
              {{ record.priority }}
            </a-tag>
          </template>

          <!-- Assignee information -->
          <template v-if="column.key === 'assignee'">
            <div v-if="record.assigneeName" class="flex items-center gap-2">
              <div class="w-24px h-24px rounded-full bg-[#2f54eb] text-white flex items-center justify-center text-11px font-bold">
                {{ record.assigneeAvatar }}
              </div>
              <span class="text-13px text-[#1f2329] font-medium">{{ record.assigneeName }}</span>
            </div>
            <div v-else>
              <a-dropdown :trigger="['click']">
                <a-button type="dashed" size="small" class="flex items-center gap-1 rounded-6px!">
                  <span class="i-material-symbols:person-add-alt-1-outline-rounded text-12px"></span> 分配
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item v-for="name in assignees" :key="name" @click="handleAssign(record, name)">
                      {{ name }}
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </template>

          <!-- Due Date status -->
          <template v-if="column.key === 'due'">
            <span class="text-13px flex items-center gap-1 font-medium" :class="record.isOverdue ? 'text-[#f5222d]' : 'text-[#5c6066]'">
              <span class="i-material-symbols:alarm-outline-rounded text-14px"></span>
              {{ record.dueText }}
            </span>
          </template>

          <!-- Action operations -->
          <template v-if="column.key === 'action'">
            <div class="flex gap-2">
                <a-button v-if="record.status !== 'completed'" type="link" size="small" class="text-[#52c41a] p-0!" @click="handleComplete(record)">
                  结项
                </a-button>
                <a-button type="link" size="small" class="text-[#1890ff] p-0!" @click="() => router.push({ name: 'task_center_detail', params: { id: record.id } })">
                  详情
                </a-button>
                <a-popconfirm title="确定删除此任务吗？" @confirm="() => handleDelete(record.id)">
                  <a-button type="link" size="small" danger class="p-0!">删除</a-button>
                </a-popconfirm>
              </div>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<style scoped>
:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa !important;
  color: #5c6066 !important;
  font-weight: 700 !important;
  border-bottom: 1px solid #e4e7ed !important;
}

:deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid #f0f0f0 !important;
}

/* Custom Scrollbar for Kanban Columns */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #909399;
}
</style>

