import re

with open('/Users/wuxb/Desktop/QH-Dev/AIProgects/Survey-App/admin-web-soybean/src/views/task_center/list/index.vue', 'r') as f:
    content = f.read()

content = content.replace("import { useRouter } from 'vue-router';", "import { useRouter } from 'vue-router';\nimport { fetchTaskPage, changeTaskStatus as apiChangeTaskStatus, deleteTask as apiDeleteTask } from '@/service/api/task';\nimport dayjs from 'dayjs';\nimport { onMounted } from 'vue';")

start_marker = "// High-fidelity Mock Data matching design mockups\nconst tasks = ref([\n"
end_marker = "]);\n\n// Summary Stats"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker) + len("]);\n")

if start_idx != -1 and end_idx != -1:
    replacement_tasks = """// Tasks Data
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
"""
    content = content[:start_idx] + replacement_tasks + content[end_idx:]

content = content.replace("""const handleComplete = (task: any) => {
  task.status = 'completed';
  task.dueText = '已完成';
  task.isOverdue = false;
  message.success(`任务 ${task.taskCode} 已标记为已完成`);
};""", """const handleComplete = async (task: any) => {
  try {
    await apiChangeTaskStatus(task.id, 2);
    message.success(`任务 ${task.taskCode} 已标记为已完成`);
    loadTasks();
  } catch (err) {
    message.error('操作失败');
  }
};""")

content = content.replace("""const handleDelete = (id: number) => {
  tasks.value = tasks.value.filter(t => t.id !== id);
  message.success('任务删除成功');
};""", """const handleDelete = async (id: number) => {
  try {
    await apiDeleteTask(id);
    message.success('任务删除成功');
    loadTasks();
  } catch (err) {
    message.error('删除失败');
  }
};""")

with open('/Users/wuxb/Desktop/QH-Dev/AIProgects/Survey-App/admin-web-soybean/src/views/task_center/list/index.vue', 'w') as f:
    f.write(content)

