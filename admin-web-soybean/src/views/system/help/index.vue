<template>
  <div class="h-full flex flex-col bg-[var(--bg-page)] p-6 overflow-y-auto">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-24px font-bold text-[var(--color-text-primary)] mb-1">帮助文档</h1>
      <p class="text-14px text-[var(--color-text-secondary)]">了解各功能模块的使用方式与操作指引</p>
    </div>

    <!-- Quick Access -->
    <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5 mb-6 shadow-[var(--shadow-card)]">
      <div class="text-14px font-500 text-[var(--color-text-primary)] mb-4">快速入口</div>
      <div class="flex gap-3 flex-wrap">
        <a-button class="rounded-6px" @click="navigateTo('/project')">
          <template #icon><FolderOutlined style="color: var(--color-primary)" /></template>
          新建项目
          <RightOutlined class="text-12px" />
        </a-button>
        <a-button class="rounded-6px" @click="navigateTo('/point/list')">
          <template #icon><EnvironmentOutlined style="color: var(--color-primary)" /></template>
          新增点位
          <RightOutlined class="text-12px" />
        </a-button>
        <a-button class="rounded-6px" @click="navigateTo('/audit/list')">
          <template #icon><FileTextOutlined style="color: var(--color-warning)" /></template>
          待审核记录
          <RightOutlined class="text-12px" />
        </a-button>
        <a-button class="rounded-6px" @click="navigateTo('/template/list')">
          <template #icon><FormOutlined style="color: var(--color-success)" /></template>
          表单模板
          <RightOutlined class="text-12px" />
        </a-button>
        <a-button class="rounded-6px" @click="navigateTo('/system/user')">
          <template #icon><TeamOutlined style="color: var(--color-danger)" /></template>
          用户管理
          <RightOutlined class="text-12px" />
        </a-button>
      </div>
    </div>

    <!-- Content Area -->
    <div class="flex gap-6 flex-1">
      <!-- Left Sidebar -->
      <div class="w-240px bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px overflow-hidden shadow-[var(--shadow-card)]">
        <div class="p-4 border-b border-[var(--color-border)]">
          <div class="text-13px font-500 text-[var(--color-text-secondary)]">功能模块</div>
        </div>
        <div class="p-2">
          <div
            v-for="module in modules"
            :key="module.key"
            class="flex items-center gap-2 px-3 py-2.5 rounded-6px cursor-pointer transition-all mb-1"
            :class="activeModule === module.key ? 'bg-[rgba(22,119,255,0.1)] text-[var(--color-primary)]' : 'text-[var(--color-text-secondary)] hover:bg-[var(--bg-hover)]'"
            @click="activeModule = module.key"
          >
            <component :is="module.icon" class="text-16px" />
            <span class="text-14px">{{ module.label }}</span>
          </div>
        </div>
      </div>

      <!-- Right Content -->
      <div class="flex-1 space-y-4">
        <!-- Module Header -->
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5 shadow-[var(--shadow-card)]">
          <div class="flex items-center gap-3">
            <div class="w-40px h-40px rounded-8px bg-[rgba(22,119,255,0.1)] flex items-center justify-center">
              <FolderOutlined class="text-20px text-[var(--color-primary)]" />
            </div>
            <div>
              <div class="text-18px font-600 text-[var(--color-text-primary)] mb-1">{{ currentModule.title }}</div>
              <div class="text-13px text-[var(--color-text-secondary)]">{{ currentModule.description }}</div>
            </div>
          </div>
        </div>

        <!-- Documentation Items -->
        <div class="space-y-4">
          <div
            v-for="(doc, index) in currentModule.docs"
            :key="index"
            class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5 shadow-[var(--shadow-card)]"
          >
            <div class="flex items-center gap-2 mb-3">
              <div class="w-28px h-28px rounded-full bg-[rgba(22,119,255,0.1)] flex items-center justify-center text-13px font-600 text-[var(--color-primary)]">
                {{ Number(index) + 1 }}
              </div>
              <span class="text-16px font-600 text-[var(--color-text-primary)]">{{ doc.title }}</span>
            </div>
            
            <div class="space-y-3 ml-10">
              <p class="text-14px text-[var(--color-text-secondary)] leading-relaxed">{{ doc.content }}</p>
              
              <div v-if="doc.tip" class="bg-[rgba(255,193,7,0.05)] border border-[rgba(255,193,7,0.2)] rounded-6px p-3 flex items-start gap-2">
                <BulbOutlined class="text-16px text-[var(--color-warning)] mt-0.5 flex-shrink-0" />
                <span class="text-13px text-[var(--color-warning)]">{{ doc.tip }}</span>
              </div>
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
import {
  FolderOutlined,
  EnvironmentOutlined,
  FileTextOutlined,
  FormOutlined,
  TeamOutlined,
  RightOutlined,
  BulbOutlined,
  UserOutlined,
  AuditOutlined,
  AppstoreOutlined,
  MobileOutlined,
  BellOutlined,
  HistoryOutlined
} from '@ant-design/icons-vue';

defineOptions({ name: 'SystemHelp' });

const router = useRouter();
const activeModule = ref('project');

const modules = [
  { key: 'project', label: '项目管理', icon: 'FolderOutlined', color: 'var(--color-primary)' },
  { key: 'point', label: '点位管理', icon: 'EnvironmentOutlined', color: 'var(--color-primary)' },
  { key: 'audit', label: '勘查审核', icon: 'AuditOutlined', color: 'var(--color-warning)' },
  { key: 'template', label: '表单模板', icon: 'FormOutlined', color: 'var(--color-success)' },
  { key: 'user', label: '用户管理', icon: 'UserOutlined', color: 'var(--color-danger)' },
  { key: 'mobile', label: '移动端勘查', icon: 'MobileOutlined', color: 'var(--color-primary)' },
  { key: 'message', label: '消息中心', icon: 'BellOutlined', color: 'var(--color-primary)' },
  { key: 'log', label: '操作日志', icon: 'HistoryOutlined', color: 'var(--color-text-secondary)' }
];

const moduleDocs: Record<string, any> = {
  project: {
    title: '项目管理',
    description: '创建和管理勘查项目，分配负责人与点位',
    docs: [
      {
        title: '新建项目',
        content: '进入「项目管理」页面，点击右上角「新建项目」按钮，填写项目名称、项目编号、负责人等基本信息后保存。',
        tip: '项目编号建议使用统一格式，如 PRJ-2024-001，便于后续检索。'
      },
      {
        title: '查看项目详情',
        content: '在项目列表中点击任意项目行，可进入项目详情页，查看该项目下所有点位的勘查进度与状态分布。',
        tip: ''
      },
      {
        title: '编辑与删除项目',
        content: '在项目列表每行右侧，点击「编辑」图标可修改项目信息；点击「删除」图标可删除项目（删除前请确认该项目下无关联数据）。',
        tip: '删除操作不可恢复，请谨慎操作。'
      }
    ]
  },
  point: {
    title: '点位管理',
    description: '管理排污口点位信息，支持地图可视化查看',
    docs: [
      {
        title: '新增点位',
        content: '进入「点位管理」页面，点击「新增点位」，填写点位名称、所属项目、经纬度坐标、排口类型等信息。坐标可手动输入，也可在地图上点击定位。',
        tip: '经纬度格式为十进制度数，如：经度 121.4737，纬度 31.2304。'
      },
      {
        title: '地图视图',
        content: '点击右上角「地图视图」切换按钮，可在地图上查看所有点位的分布情况，点击地图上的标记可查看点位详情。',
        tip: ''
      },
      {
        title: '筛选与搜索',
        content: '支持按项目、点位状态（待勘查/已勘查/已审核）进行筛选，也可通过搜索框按点位名称快速定位。',
        tip: ''
      },
      {
        title: '点位状态说明',
        content: '点位共有三种状态：「待勘查」表示尚未提交勘查记录；「已提交」表示勘查员已提交待审核；「已通过」表示审核通过；「已驳回」表示审核未通过需重新勘查。',
        tip: ''
      }
    ]
  },
  audit: {
    title: '勘查审核',
    description: '审核勘查员提交的勘查记录，支持通过或驳回',
    docs: [
      {
        title: '查看待审核记录',
        content: '进入「勘查审核」页面，默认展示所有待审核的勘查记录。侧边栏「勘查审核」菜单上的红色数字徽标表示当前待审核数量。',
        tip: ''
      },
      {
        title: '审核勘查记录',
        content: '点击任意记录进入详情页，可查看勘查员填写的完整表单数据、现场照片（含水印信息）、GPS 定位等。确认无误后点击「通过」，有问题则点击「驳回」并填写驳回原因。',
        tip: '驳回时请填写具体原因，勘查员会收到通知并了解需要补充或修改的内容。'
      },
      {
        title: '查看历史记录',
        content: '在勘查详情页可查看该点位的历史勘查记录，了解每次提交与审核的完整流程。',
        tip: ''
      },
      {
        title: '导出报告',
        content: '在勘查详情页点击「导出 PDF」按钮，可生成包含所有勘查数据和照片的标准报告文件。',
        tip: ''
      }
    ]
  },
  template: {
    title: '表单模板',
    description: '自定义勘查表单字段，支持字段联动配置',
    docs: [
      {
        title: '查看与使用模板',
        content: '进入「表单模板」页面，可查看所有已发布的表单模板。勘查员在移动端填写勘查记录时，将使用对应项目绑定的模板。',
        tip: ''
      },
      {
        title: '创建新模板（管理员）',
        content: '管理员点击右上角「快速新建」可创建基础模板；点击「动态设计器」可进入可视化设计界面，拖拽添加字段、配置选项。',
        tip: '只有管理员角色才能看到「动态设计器」和「快速新建」按钮。'
      },
      {
        title: '配置字段联动',
        content: '在动态设计器中，选中一个字段后，在右侧面板找到「联动规则」区域，点击「新增规则条件」。设置触发条件（如：当「排口类型」= 工业时），选择目标字段执行显示/隐藏操作。',
        tip: '联动规则支持多条件叠加，可实现复杂的表单逻辑控制。'
      },
      {
        title: '预览与发布',
        content: '配置完成后点击「联动预览」可实时测试联动效果；确认无误后点击「发布模板」保存并生效。',
        tip: ''
      }
    ]
  },
  user: {
    title: '用户管理',
    description: '管理系统用户账号、角色权限与安全设置',
    docs: [
      {
        title: '用户角色说明',
        content: '系统共有三种角色：「管理员」拥有全部权限；「审核员」可查看和审核勘查记录；「勘查员」只能通过移动端提交勘查记录。',
        tip: ''
      },
      {
        title: '新增用户',
        content: '点击「新增用户」，填写用户名、邮箱、初始密码，并选择对应角色。用户创建后会收到邮件通知，可使用邮箱登录系统。',
        tip: '初始密码建议设置为强密码，并提醒用户登录后及时修改。'
      },
      {
        title: '修改角色与权限',
        content: '在用户列表中点击「编辑」，可修改用户的角色和基本信息。角色变更立即生效，用户下次操作时将受新权限约束。',
        tip: ''
      },
      {
        title: '账号安全',
        content: '在用户详情中可查看账号安全信息，包括最近登录时间、登录 IP 等。如发现异常可立即禁用账号。',
        tip: ''
      }
    ]
  },
  mobile: {
    title: '移动端勘查',
    description: '勘查员使用手机完成现场勘查记录的完整流程',
    docs: [
      {
        title: '登录移动端',
        content: '勘查员在手机浏览器中打开系统链接（路径以 /m/ 开头），使用管理员分配的账号密码登录。建议将页面添加到手机桌面快捷方式，方便日常使用。',
        tip: ''
      },
      {
        title: '查看待勘查点位',
        content: '登录后进入「我的点位」页面，可看到分配给自己的所有待勘查点位列表，点击任意点位开始填写勘查记录。',
        tip: ''
      },
      {
        title: '填写勘查表单',
        content: '按照表单字段逐项填写，支持文字输入、下拉选择、开关切换等多种字段类型。部分字段配置了联动规则，选择特定值后会自动显示或隐藏相关字段。',
        tip: '表单支持草稿保存，填写中途可暂存，下次打开继续填写。'
      },
      {
        title: '拍摄现场照片',
        content: '在照片上传字段中，点击拍照按钮调用手机相机拍摄现场照片。照片会自动添加水印（包含 GPS 坐标、拍摄时间、勘查员姓名），确保数据真实性。',
        tip: ''
      },
      {
        title: '提交勘查记录',
        content: '所有必填字段填写完成后，点击「提交」按钮。提交后记录进入待审核状态，等待管理员或审核员审核。',
        tip: ''
      }
    ]
  },
  message: {
    title: '消息中心',
    description: '查看系统通知，了解审核结果与重要提醒',
    docs: [
      {
        title: '查看通知',
        content: '点击侧边栏「消息中心」进入通知列表，可查看所有系统通知，包括审核结果通知、系统公告等。未读通知会在菜单上显示红色数字徽标。',
        tip: ''
      },
      {
        title: '标记已读',
        content: '点击通知条目可将其标记为已读；也可点击右上角「全部已读」按钮一键清除所有未读状态。',
        tip: ''
      }
    ]
  },
  log: {
    title: '操作日志',
    description: '查看系统所有操作记录，追溯数据变更历史',
    docs: [
      {
        title: '查看操作日志',
        content: '进入「操作日志」页面，可查看系统内所有用户的操作记录，包括登录、数据新增/修改/删除等操作，每条记录包含操作人、操作时间、操作内容等详细信息。',
        tip: '操作日志仅管理员可见，用于系统安全审计和问题排查。'
      },
      {
        title: '筛选日志',
        content: '支持按操作类型、操作人、时间范围进行筛选，快速定位特定操作记录。',
        tip: ''
      }
    ]
  }
};

const currentModule = computed(() => {
  return moduleDocs[activeModule.value] || moduleDocs.project;
});

const navigateTo = (path: string) => {
  router.push(path);
};
</script>

<style scoped>
</style>
