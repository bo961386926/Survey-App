<script setup lang="ts">
import { ref } from 'vue';

defineOptions({ name: 'Dashboard' });

interface ProjectItem {
  id: number;
  name: string;
  points: number;
  members: string;
  status: 'active' | 'pending' | 'new';
  statusText: string;
  progress: number;
  icon: string;
  theme: 'blue' | 'green' | 'orange';
}

const projects = ref<ProjectItem[]>([
  {
    id: 1,
    name: '河道勘察A标段',
    points: 456,
    members: '张工等 5 人',
    status: 'active',
    statusText: '进行中',
    progress: 72,
    icon: '🏞️',
    theme: 'blue'
  },
  {
    id: 2,
    name: '河道勘察B标段',
    points: 312,
    members: '李工等 3 人',
    status: 'pending',
    statusText: '待审核',
    progress: 45,
    icon: '🌊',
    theme: 'green'
  },
  {
    id: 3,
    name: '排污口排查项目',
    points: 189,
    members: '王工等 4 人',
    status: 'new',
    statusText: '新启动',
    progress: 18,
    icon: '🏗️',
    theme: 'orange'
  }
]);

interface ActivityItem {
  id: number;
  title: string;
  desc: string;
  time: string;
  dotColor: 'green' | 'blue' | 'orange' | 'purple';
}

const activities = ref<ActivityItem[]>([
  {
    id: 1,
    title: '张工 提交了 DK0+120 采集数据',
    desc: '河道勘察A标段 · 水质采样',
    time: '5 分钟前',
    dotColor: 'green'
  },
  {
    id: 2,
    title: '审核员 通过了 PTN-003 数据',
    desc: '河道勘察B标段 · 土壤采样',
    time: '15 分钟前',
    dotColor: 'blue'
  },
  {
    id: 3,
    title: '李工 驳回了 DK1+050 数据',
    desc: '排污口排查项目 · 需要补充照片',
    time: '30 分钟前',
    dotColor: 'orange'
  },
  {
    id: 4,
    title: '系统生成了月度报告',
    desc: '2024年3月勘察数据汇总',
    time: '1 小时前',
    dotColor: 'purple'
  },
  {
    id: 5,
    title: '管理员 创建了协作入口',
    desc: '第三方协作 · 有效期72小时',
    time: '2 小时前',
    dotColor: 'green'
  }
]);
</script>

<template>
  <div class="h-full flex-col overflow-y-auto custom-scrollbar dashboard-page">
    <!-- 背景光效 -->
    <div class="bg-effects"></div>
    <div class="grid-pattern"></div>

    <!-- 顶部 Header -->
    <header class="header-section relative z-10 flex-y-center justify-between px-36px py-24px border-b border-[var(--color-divider)] bg-[rgba(255,255,255,0.85)] backdrop-blur-20px">
      <div>
        <h1 class="text-22px font-800 text-[var(--color-text-primary)] tracking-tight">勘察数据仪表板</h1>
        <p class="text-13px text-[var(--color-text-secondary)] mt-4px">实时监控项目进度和数据质量</p>
      </div>
      <div class="flex gap-12px">
        <AButton class="action-btn">📅 今天</AButton>
        <AButton class="action-btn">📥 导出</AButton>
        <AButton type="primary" class="new-project-btn flex-center gap-6px">
          <div class="i-material-symbols:add text-16px"></div>
          新建项目
        </AButton>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="p-36px relative z-10">
      <!-- 统计卡片网格 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-24px mb-32px">
        <!-- 1. 进行中项目 -->
        <div class="stat-card blue-card">
          <div class="stat-header">
            <div class="stat-icon bg-[#E8F2FF] text-[#2563EB]">📋</div>
            <div class="stat-trend up-trend">↑ 12%</div>
          </div>
          <div class="stat-value">28</div>
          <div class="stat-label">进行中项目</div>
        </div>

        <!-- 2. 总点位数量 -->
        <div class="stat-card green-card">
          <div class="stat-header">
            <div class="stat-icon bg-[#E6F9F0] text-[#10B981]">📍</div>
            <div class="stat-trend up-trend">↑ 8%</div>
          </div>
          <div class="stat-value">1,247</div>
          <div class="stat-label">总点位数量</div>
        </div>

        <!-- 3. 待审核数据 -->
        <div class="stat-card orange-card">
          <div class="stat-header">
            <div class="stat-icon bg-[#FFF0E6] text-[#F97316]">✅</div>
            <div class="stat-trend down-trend">↓ 3%</div>
          </div>
          <div class="stat-value">12</div>
          <div class="stat-label">待审核数据</div>
        </div>

        <!-- 4. 已完成采集 -->
        <div class="stat-card purple-card">
          <div class="stat-header">
            <div class="stat-icon bg-[#F3E8FF] text-[#8B5CF6]">📁</div>
            <div class="stat-trend up-trend">↑ 24%</div>
          </div>
          <div class="stat-value">856</div>
          <div class="stat-label">已完成采集</div>
        </div>
      </div>

      <!-- 下方内容网格 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-24px">
        <!-- 左侧: 进行中的项目 -->
        <div class="lg:col-span-2">
          <div class="section-card">
            <div class="section-header">
              <h2 class="section-title">
                <span class="section-title-icon bg-[#E8F2FF]">📋</span>
                进行中的项目
              </h2>
              <a href="#" class="section-action">查看全部 →</a>
            </div>
            <div class="section-body">
              <div class="project-list">
                <div v-for="item in projects" :key="item.id" class="project-item">
                  <div class="project-icon-box" :class="item.theme">
                    {{ item.icon }}
                  </div>
                  <div class="project-info">
                    <h3 class="project-name">{{ item.name }}</h3>
                    <div class="project-meta">
                      <span class="project-meta-item">📍 {{ item.points }} 点位</span>
                      <span class="project-meta-item">👤 {{ item.members }}</span>
                      <span class="status-badge" :class="item.status">
                        {{ item.statusText }}
                      </span>
                    </div>
                  </div>
                  <div class="project-progress">
                    <div class="progress-header">
                      <span class="progress-label">进度</span>
                      <span class="progress-value">{{ item.progress }}%</span>
                    </div>
                    <AProgress :percent="item.progress" :show-info="false" :stroke-width="6" class="m-0!" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧: 快捷操作 + 最近动态 -->
        <div class="lg:col-span-1 flex-col gap-24px">
          <!-- 快捷操作 -->
          <div class="section-card mb-24px">
            <div class="section-header">
              <h2 class="section-title">
                <span class="section-title-icon bg-[#E6F9F0]">⚡</span>
                快捷操作
              </h2>
            </div>
            <div class="section-body">
              <div class="grid grid-cols-2 gap-16px">
                <a href="#" class="quick-action-btn">
                  <div class="quick-action-icon bg-[#E8F2FF] text-[#2563EB]">📋</div>
                  <span class="quick-action-text">新建项目</span>
                </a>
                <a href="#" class="quick-action-btn">
                  <div class="quick-action-icon bg-[#E6F9F0] text-[#10B981]">📍</div>
                  <span class="quick-action-text">导入点位</span>
                </a>
                <a href="#" class="quick-action-btn">
                  <div class="quick-action-icon bg-[#FFF0E6] text-[#F97316]">✅</div>
                  <span class="quick-action-text">审核数据</span>
                </a>
                <a href="#" class="quick-action-btn">
                  <div class="quick-action-icon bg-[#F3E8FF] text-[#8B5CF6]">📁</div>
                  <span class="quick-action-text">导出报告</span>
                </a>
              </div>
            </div>
          </div>

          <!-- 最近动态 -->
          <div class="section-card">
            <div class="section-header">
              <h2 class="section-title">
                <span class="section-title-icon bg-[#F3E8FF]">📅</span>
                最近动态
              </h2>
              <a href="#" class="section-action">查看全部 →</a>
            </div>
            <div class="section-body">
              <div class="activity-list">
                <div v-for="item in activities" :key="item.id" class="activity-item">
                  <div class="activity-dot-container">
                    <div class="activity-dot" :class="item.dotColor"></div>
                    <div class="activity-line"></div>
                  </div>
                  <div class="activity-content">
                    <div class="activity-title">{{ item.title }}</div>
                    <div class="activity-desc">{{ item.desc }}</div>
                    <div class="activity-time">{{ item.time }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--color-divider);
  border-radius: 10px;
}

/* 背景模糊与网格 */
.bg-effects {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}
.bg-effects::before {
  content: '';
  position: absolute;
  top: -30%;
  left: -20%;
  width: 80%;
  height: 80%;
  background: radial-gradient(ellipse at center, rgba(37, 99, 235, 0.05) 0%, transparent 60%);
  animation: float1 20s ease-in-out infinite;
}
@keyframes float1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(10%, 10%) scale(1.05); }
}
.grid-pattern {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(226, 232, 240, 0.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(226, 232, 240, 0.3) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
  z-index: 0;
}

/* Header UI */
.action-btn {
  height: 40px !important;
  border-radius: 10px !important;
  background-color: var(--bg-card-alt) !important;
  border: 1px solid var(--color-border) !important;
  color: var(--color-text-secondary) !important;
  font-weight: 500 !important;
}
.action-btn:hover {
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
}
.new-project-btn {
  height: 40px !important;
  border-radius: 10px !important;
  background: linear-gradient(135deg, #2563EB, #1D4ED8) !important;
  border: none !important;
  color: white !important;
  font-weight: 600 !important;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25) !important;
}
.new-project-btn:hover {
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.35) !important;
}

/* Bento Grid Stats Cards */
.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 16px;
  padding: 24px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
  border-color: rgba(37, 99, 235, 0.4);
}
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
}
.blue-card::before { background: linear-gradient(90deg, #2563EB, #60A5FA); }
.green-card::before { background: linear-gradient(90deg, #10B981, #34D399); }
.orange-card::before { background: linear-gradient(90deg, #F97316, #FB923C); }
.purple-card::before { background: linear-gradient(90deg, #8B5CF6, #A78BFA); }

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.stat-trend {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.up-trend {
  background: rgba(16, 185, 129, 0.12);
  color: #10B981;
}
.down-trend {
  background: rgba(239, 68, 68, 0.12);
  color: #EF4444;
}
.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: var(--color-text-primary);
  letter-spacing: -1px;
  margin-bottom: 4px;
}
.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

/* Sections */
.section-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.02);
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px 20px;
  border-b: 1px solid var(--color-divider);
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-title-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.section-action {
  font-size: 13px;
  color: #3B82F6;
  font-weight: 500;
  text-decoration: none;
}
.section-action:hover {
  color: #2563EB;
}
.section-body {
  padding: 24px 28px;
}

/* Projects */
.project-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.project-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: var(--bg-card-alt);
  border: 1px solid var(--color-border);
  border-radius: 14px;
  transition: all 0.2s ease;
  cursor: pointer;
}
.project-item:hover {
  background: var(--bg-card);
  border-color: var(--color-primary);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}
.project-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.project-icon-box.blue { background: rgba(37, 99, 235, 0.12); }
.project-icon-box.green { background: rgba(16, 185, 129, 0.12); }
.project-icon-box.orange { background: rgba(249, 115, 22, 0.12); }

.project-info {
  flex: 1;
  min-width: 0;
}
.project-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}
.project-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
.project-meta-item {
  display: flex;
  align-items: center;
}
.status-badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}
.status-badge.active { background: rgba(16, 185, 129, 0.12); color: #10B981; }
.status-badge.pending { background: rgba(245, 158, 11, 0.12); color: #D97706; }
.status-badge.new { background: rgba(37, 99, 235, 0.12); color: #2563EB; }

.project-progress {
  width: 140px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.progress-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.progress-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}
.progress-value {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-primary);
}

/* Quick Actions */
.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 18px 12px;
  background: var(--bg-card-alt);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  text-decoration: none;
  transition: all 0.2s ease;
}
.quick-action-btn:hover {
  background: var(--bg-card);
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.03);
}
.quick-action-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.quick-action-text {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
}

/* Activity List */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
}
.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  position: relative;
}
.activity-dot-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 12px;
  height: 100%;
}
.activity-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #CBD5E1;
  z-index: 2;
  margin-top: 5px;
}
.activity-dot.green { background: #10B981; box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2); }
.activity-dot.blue { background: #2563EB; box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.2); }
.activity-dot.orange { background: #F97316; box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.2); }
.activity-dot.purple { background: #8B5CF6; box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.2); }

.activity-line {
  width: 2px;
  background: var(--color-divider);
  position: absolute;
  top: 15px;
  bottom: -25px;
  left: 5px;
  z-index: 1;
}
.activity-item:last-child .activity-line {
  display: none;
}

.activity-content {
  flex: 1;
  min-width: 0;
}
.activity-title {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}
.activity-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}
.activity-time {
  font-size: 11px;
  color: var(--color-text-placeholder);
}
</style>
