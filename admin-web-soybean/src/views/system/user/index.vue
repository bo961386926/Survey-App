<template>
  <div class="h-full flex flex-col bg-[var(--bg-page)] p-6 overflow-auto">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-20px font-bold text-[var(--color-text-primary)] mb-1">用户与角色管理</h1>
        <p class="text-13px text-[var(--color-text-secondary)]">账号、角色、权限边界、项目范围与启停状态统一管理</p>
      </div>
      <a-button v-if="isAdmin" type="primary" @click="handleAddUser" class="rounded-6px">
        <template #icon><PlusOutlined /></template>
        新增用户
      </a-button>
    </div>

    <!-- Statistics Cards -->
    <div class="grid grid-cols-4 gap-4 mb-6">
      <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4">
        <div class="flex items-center justify-between mb-2">
          <span class="text-14px text-[var(--color-text-secondary)]">总用户数</span>
          <div class="w-32px h-32px rounded-full bg-[rgba(22,119,255,0.1)] flex items-center justify-center">
            <UsergroupAddOutlined class="text-16px text-[var(--color-primary)]" />
          </div>
        </div>
        <div class="text-28px font-bold text-[var(--color-text-primary)]">{{ stats.total }}</div>
      </div>
      <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4">
        <div class="flex items-center justify-between mb-2">
          <span class="text-14px text-[var(--color-text-secondary)]">管理员</span>
          <div class="w-32px h-32px rounded-full bg-[rgba(250,173,20,0.1)] flex items-center justify-center">
            <SafetyCertificateOutlined class="text-16px text-[var(--color-warning)]" />
          </div>
        </div>
        <div class="text-28px font-bold text-[var(--color-text-primary)]">{{ stats.admin }}</div>
      </div>
      <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4">
        <div class="flex items-center justify-between mb-2">
          <span class="text-14px text-[var(--color-text-secondary)]">审核员</span>
          <div class="w-32px h-32px rounded-full bg-[rgba(22,119,255,0.1)] flex items-center justify-center">
            <SecurityScanOutlined class="text-16px text-[var(--color-primary)]" />
          </div>
        </div>
        <div class="text-28px font-bold text-[var(--color-text-primary)]">{{ stats.auditor }}</div>
      </div>
      <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4">
        <div class="flex items-center justify-between mb-2">
          <span class="text-14px text-[var(--color-text-secondary)]">勘查员</span>
          <div class="w-32px h-32px rounded-full bg-[rgba(82,196,26,0.1)] flex items-center justify-center">
            <UserOutlined class="text-16px text-[var(--color-success)]" />
          </div>
        </div>
        <div class="text-28px font-bold text-[var(--color-text-primary)]">{{ stats.collector }}</div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex gap-6">
      <!-- Left: Role List -->
      <div class="w-280px flex flex-col gap-6">
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5">
          <h3 class="text-15px font-600 text-[var(--color-text-primary)] mb-1">角色列表</h3>
          <p class="text-12px text-[var(--color-text-secondary)] mb-4">角色决定页面可见性与可执行动作</p>

          <div class="space-y-3">
            <div
              v-for="role in roleList"
              :key="role.key"
              :class="[
                'rounded-8px p-4 cursor-pointer transition-all',
                selectedRole === role.key
                  ? 'bg-[var(--color-primary)] text-white'
                  : 'bg-[var(--bg-card-alt)] border border-[var(--color-border)] hover:border-[var(--color-primary)]'
              ]"
              @click="selectedRole = role.key"
            >
              <div class="flex items-center justify-between mb-2">
                <span :class="['text-14px font-600', selectedRole === role.key ? 'text-white' : 'text-[var(--color-text-primary)]']">
                  {{ role.name }}
                </span>
                <span :class="[
                  'text-12px px-2 py-0.5 rounded-full',
                  selectedRole === role.key
                    ? 'bg-white/20 text-white'
                    : 'bg-[rgba(22,119,255,0.1)] text-[var(--color-primary)]'
                ]">
                  {{ role.count }}人
                </span>
              </div>
              <div :class="['text-12px', selectedRole === role.key ? 'text-white/80' : 'text-[var(--color-text-secondary)]']">
                {{ role.permissions }}
              </div>
            </div>
          </div>
        </div>

        <!-- Permission Details -->
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5">
          <h3 class="text-15px font-600 text-[var(--color-text-primary)] mb-1">权限明细</h3>
          <p class="text-12px text-[var(--color-text-secondary)] mb-4">
            当前选中角色：<span class="font-600">{{ currentRoleName }}</span>
          </p>

          <div class="space-y-4">
            <div>
              <div class="text-13px font-500 text-[var(--color-text-primary)] mb-2">可见页面</div>
              <div class="grid grid-cols-2 gap-2">
                <div
                  v-for="page in visiblePages"
                  :key="page"
                  class="flex items-center gap-1 text-12px text-[var(--color-text-secondary)]"
                >
                  <CheckCircleOutlined class="text-[var(--color-primary)] text-12px" />
                  {{ page }}
                </div>
              </div>
            </div>

            <div>
              <div class="text-13px font-500 text-[var(--color-text-primary)] mb-2">敏感动作</div>
              <div class="flex flex-wrap gap-2">
                <a-tag
                  v-for="action in sensitiveActions"
                  :key="action"
                  color="warning"
                  class="text-12px"
                >
                  <WarningOutlined class="mr-1" />
                  {{ action }}
                </a-tag>
              </div>
            </div>

            <div>
              <div class="text-13px font-500 text-[var(--color-text-primary)] mb-2">项目范围</div>
              <div class="bg-[var(--bg-card-alt)] border border-[var(--color-border)] rounded-6px p-3">
                <div class="flex items-center gap-2 text-13px text-[var(--color-text-secondary)]">
                  <FolderOutlined />
                  全部项目
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: User List & Security -->
      <div class="flex-1 flex flex-col gap-6">
        <!-- User List -->
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5">
          <div class="flex items-center justify-between mb-4">
            <div>
              <h3 class="text-15px font-600 text-[var(--color-text-primary)] mb-1">用户列表</h3>
              <p class="text-12px text-[var(--color-text-secondary)]">共 {{ userList.length }} 位用户</p>
            </div>
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索用户..."
              style="width: 280px"
              allow-clear
              @search="initData"
            />
          </div>

          <a-table
            :dataSource="userList"
            :columns="columns"
            rowKey="id"
            :pagination="{ pageSize: 20, showSizeChanger: false }"
            :scroll="{ y: 300 }"
            :loading="loading"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'user'">
                <div class="flex items-center gap-3">
                  <div
                    :class="[
                      'w-36px h-36px rounded-full flex items-center justify-center text-white font-600 bg-[var(--color-primary)]',
                    ]"
                  >
                    {{ record.realName.substring(0, 1) }}
                  </div>
                  <div>
                    <div class="text-14px font-500 text-[var(--color-text-primary)]">{{ record.realName }}</div>
                    <div class="text-12px text-[var(--color-text-secondary)]">{{ record.email }}</div>
                  </div>
                </div>
              </template>

              <template v-if="column.key === 'role'">
                <a-tag :color="getRoleColor(record.role)" class="text-12px">{{ getRoleName(record.role) }}</a-tag>
              </template>

              <template v-if="column.key === 'status'">
                <div class="flex items-center gap-1">
                  <div :class="['w-6px h-6px rounded-full', record.status === 1 ? 'bg-[var(--color-success)]' : 'bg-[var(--color-text-placeholder)]']"></div>
                  <span :class="['text-13px', record.status === 1 ? 'text-[var(--color-success)]' : 'text-[var(--color-text-secondary)]']">
                    {{ record.status === 1 ? '启用中' : '已停用' }}
                  </span>
                </div>
              </template>

              <template v-if="column.key === 'action'">
                <div class="flex gap-1">
                  <a-button v-if="isAdmin" type="link" size="small" @click="handleEdit(record)">编辑</a-button>
                  <a-button v-if="isAdmin" type="link" size="small" @click="handleResetPassword(record)">重置密码</a-button>
                  <a-button v-if="isAdmin" type="link" size="small" danger @click="handleDisable(record)">停用</a-button>
                </div>
              </template>
            </template>
          </a-table>
        </div>

        <!-- Account Security -->
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5">
          <h3 class="text-15px font-600 text-[var(--color-text-primary)] mb-1">账号安全</h3>
          <p class="text-12px text-[var(--color-text-secondary)] mb-4">登录保护与账号生命周期管理</p>

          <div class="space-y-4">
            <!-- Password Policy -->
            <div class="bg-[var(--bg-card-alt)] border border-[var(--color-border)] rounded-8px p-4">
              <div class="flex items-start gap-3">
                <div class="w-32px h-32px rounded-6px bg-[rgba(22,119,255,0.1)] flex items-center justify-center flex-shrink-0">
                  <LockOutlined class="text-16px text-[var(--color-primary)]" />
                </div>
                <div class="flex-1">
                  <div class="text-14px font-600 text-[var(--color-text-primary)] mb-1">密码策略</div>
                  <div class="text-12px text-[var(--color-text-secondary)] mb-2">
                    支持重置密码、强制首次登录修改、异常登录报警
                  </div>
                  <div class="flex gap-2">
                    <a-tag class="text-11px">首次登录强制改密</a-tag>
                    <a-tag class="text-11px">8位以上混合密码</a-tag>
                    <a-tag class="text-11px">异常登录告警</a-tag>
                  </div>
                </div>
              </div>
            </div>

            <!-- Session Management -->
            <div class="bg-[var(--bg-card-alt)] border border-[var(--color-border)] rounded-8px p-4">
              <div class="flex items-start gap-3">
                <div class="w-32px h-32px rounded-6px bg-[rgba(22,119,255,0.1)] flex items-center justify-center flex-shrink-0">
                  <ClockCircleOutlined class="text-16px text-[var(--color-primary)]" />
                </div>
                <div class="flex-1">
                  <div class="text-14px font-600 text-[var(--color-text-primary)] mb-1">会话管理</div>
                  <div class="text-12px text-[var(--color-text-secondary)] mb-2">
                    Token 有效期 72h，过期自动登出，支持手动吊销
                  </div>
                  <div class="flex gap-2">
                    <a-tag color="warning" class="text-11px">72h 有效</a-tag>
                    <a-tag class="text-11px">自动续期</a-tag>
                  </div>
                </div>
              </div>
            </div>

            <!-- Risk Protection -->
            <div class="bg-gradient-to-r from-[var(--color-primary)] to-[rgba(22,119,255,0.8)] rounded-8px p-4 text-white">
              <div class="flex items-start gap-3 mb-3">
                <div class="w-32px h-32px rounded-6px bg-white/20 flex items-center justify-center flex-shrink-0">
                  <SecurityScanOutlined class="text-16px" />
                </div>
                <div class="flex-1">
                  <div class="text-14px font-600 mb-1">风险操作保护</div>
                  <div class="text-12px opacity-90">
                    停用账号、修改角色、开放导出权限均需二次确认并记录日志
                  </div>
                </div>
              </div>
              <a-button
                block
                class="bg-white/20 border-white/30 text-white hover:bg-white/30 hover:border-white/50"
                @click="showRiskModal = true"
              >
                <template #icon><SettingOutlined /></template>
                配置风险策略
              </a-button>
            </div>

            <!-- Recent Login Records -->
            <div>
              <div class="flex items-center justify-between mb-3">
                <h4 class="text-14px font-600 text-[var(--color-text-primary)]">近期登录记录</h4>
                <a-button type="link" size="small">查看全部</a-button>
              </div>
              <div class="space-y-3">
                <div
                  v-for="(login, index) in recentLogins"
                  :key="index"
                  class="flex items-center justify-between"
                >
                  <div class="flex items-center gap-2">
                    <div :class="[
                      'w-6px h-6px rounded-full',
                      login.type === 'normal' ? 'bg-[var(--color-success)]' : 'bg-[var(--color-danger)]'
                    ]"></div>
                    <span class="text-13px text-[var(--color-text-primary)] font-500">{{ login.user }}</span>
                    <span class="text-12px text-[var(--color-text-secondary)]">{{ login.device }}</span>
                  </div>
                  <span class="text-12px text-[var(--color-text-secondary)]">{{ login.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add User Modal -->
    <a-modal
      v-model:open="showAddUserModal"
      title="新增用户"
      :footer="null"
      width="720px"
      :closable="true"
      centered
    >
      <div class="-mx-6 -mt-4 bg-gradient-to-r from-[var(--color-primary)] to-[rgba(22,119,255,0.7)] px-6 py-4 mb-4">
        <div class="flex items-center gap-3 text-white">
          <div class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center">
            <UserAddOutlined class="text-xl" />
          </div>
          <div>
            <div class="font-600">创建新账号</div>
            <div class="text-12px text-white/80">为新成员分配系统访问权限</div>
          </div>
        </div>
      </div>

      <div class="flex gap-6">
        <!-- Left: Basic Info -->
        <div class="flex-1">
          <div class="text-14px font-600 text-[var(--color-text-primary)] mb-3 flex items-center gap-2">
            <div class="w-1 h-4 bg-[var(--color-primary)] rounded-full"></div>
            基本信息
          </div>
          
          <a-form layout="vertical" class="space-y-4">
            <!-- Username -->
            <a-form-item label="登录账号" required>
              <a-input
                v-model:value="newUserForm.username"
                placeholder="用于登录系统的唯一标识"
                size="large"
              >
                <template #prefix><UserOutlined class="text-[var(--color-text-placeholder)]" /></template>
              </a-input>
            </a-form-item>

            <!-- Name -->
            <a-form-item label="姓名" required>
              <a-input
                v-model:value="newUserForm.realName"
                placeholder="成员的真实姓名"
                size="large"
              >
                <template #prefix><IdcardOutlined class="text-[var(--color-text-placeholder)]" /></template>
              </a-input>
            </a-form-item>

            <!-- Password -->
            <a-form-item label="初始密码" required>
              <a-input-password
                v-model:value="newUserForm.password"
                placeholder="登录密码（至少8位）"
                size="large"
              >
                <template #prefix><LockOutlined class="text-[var(--color-text-placeholder)]" /></template>
              </a-input-password>
            </a-form-item>
          </a-form>

          <div class="text-14px font-600 text-[var(--color-text-primary)] mb-3 mt-6 flex items-center gap-2">
            <div class="w-1 h-4 bg-[var(--color-primary)] rounded-full"></div>
            联系方式
          </div>

          <a-form layout="vertical" class="space-y-4">
            <!-- Email -->
            <a-form-item label="邮箱">
              <a-input
                v-model:value="newUserForm.email"
                placeholder="用于接收通知"
                size="large"
              >
                <template #prefix><MailOutlined class="text-[var(--color-text-placeholder)]" /></template>
              </a-input>
            </a-form-item>

            <!-- Phone -->
            <a-form-item label="手机号">
              <a-input
                v-model:value="newUserForm.phone"
                placeholder="紧急联系号码"
                size="large"
              >
                <template #prefix><PhoneOutlined class="text-[var(--color-text-placeholder)]" /></template>
              </a-input>
            </a-form-item>
          </a-form>
        </div>

        <!-- Right: Role Selection -->
        <div class="w-280px">
          <div class="text-14px font-600 text-[var(--color-text-primary)] mb-3 flex items-center gap-2">
            <div class="w-1 h-4 bg-[var(--color-warning)] rounded-full"></div>
            分配角色
          </div>

          <div class="space-y-2">
            <div
              v-for="role in roleOptions"
              :key="role.value"
              :class="[
                'border rounded-lg p-3 cursor-pointer transition-all',
                newUserForm.roleIds.includes(role.value)
                  ? 'border-[var(--color-primary)] bg-[rgba(22,119,255,0.08)] shadow-sm'
                  : 'border-[var(--color-border)] hover:border-[var(--color-primary)] hover:bg-[var(--bg-card-alt)]'
              ]"
              @click="toggleNewUserRole(role.value)"
            >
              <div class="flex items-center gap-3">
                <a-checkbox 
                  :checked="newUserForm.roleIds.includes(role.value)"
                  @change="() => toggleNewUserRole(role.value)"
                  @click.stop
                />
                <div :class="[
                  'w-8 h-8 rounded-lg flex items-center justify-center',
                  newUserForm.roleIds.includes(role.value) ? 'bg-[var(--color-primary)] text-white' : 'bg-[var(--bg-card-alt)] text-[var(--color-text-secondary)]'
                ]">
                  <component :is="getRoleIcon(role.key)" />
                </div>
                <div class="flex-1">
                  <div class="text-13px font-500 text-[var(--color-text-primary)]">
                    {{ role.name }}
                  </div>
                  <div class="text-11px text-[var(--color-text-secondary)] truncate">
                    {{ role.description }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Role Permission Tips -->
          <div class="mt-4 p-3 bg-[rgba(250,173,20,0.08)] rounded-lg border border-[rgba(250,173,20,0.2)]">
            <div class="text-11px text-[var(--color-warning)] flex items-start gap-2">
              <ExclamationCircleOutlined class="mt-0.5" />
              <div>
                <div class="font-500 mb-1">角色权限说明</div>
                <div class="text-[var(--color-text-secondary)]">不同角色拥有不同的功能权限，创建后可随时修改</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="flex gap-3 mt-6 pt-4 border-t border-[var(--color-border)]">
        <a-button block size="large" @click="showAddUserModal = false">取消</a-button>
        <a-button type="primary" block size="large" @click="handleCreateUser">创建用户</a-button>
      </div>
    </a-modal>

    <!-- Risk Strategy Modal -->
    <a-modal
      v-model:open="showRiskModal"
      title="风险策略配置"
      :footer="null"
      width="500px"
    >
      <div class="mb-4">
        <p class="text-13px text-[var(--color-text-secondary)]">当前已启用全部保护策略</p>
      </div>

      <div class="space-y-4">
        <div class="flex items-center justify-between py-3 border-b border-[var(--color-border)]">
          <span class="text-14px text-[var(--color-text-primary)]">停用账号需二次确认</span>
          <a-switch v-model:checked="riskSettings.confirmDisable" />
        </div>
        <div class="flex items-center justify-between py-3 border-b border-[var(--color-border)]">
          <span class="text-14px text-[var(--color-text-primary)]">修改角色记录日志</span>
          <a-switch v-model:checked="riskSettings.logRoleChange" />
        </div>
        <div class="flex items-center justify-between py-3 border-b border-[var(--color-border)]">
          <span class="text-14px text-[var(--color-text-primary)]">导出操作需审批</span>
          <a-switch v-model:checked="riskSettings.approveExport" />
        </div>
        <div class="flex items-center justify-between py-3">
          <span class="text-14px text-[var(--color-text-primary)]">异常登录自动锁定</span>
          <a-switch v-model:checked="riskSettings.autoLock" />
        </div>
      </div>

      <div class="mt-6">
        <a-button type="primary" block @click="handleConfirmRiskSettings">确认</a-button>
      </div>
    </a-modal>

    <!-- Edit User Modal -->
    <a-modal
      v-model:open="showEditUserModal"
      title="编辑用户"
      :footer="null"
      width="720px"
      :closable="true"
      centered
    >
      <div class="-mx-6 -mt-4 bg-gradient-to-r from-[var(--color-primary)] to-[rgba(22,119,255,0.7)] px-6 py-4 mb-4">
        <div class="flex items-center gap-3 text-white">
          <div class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center">
            <EditOutlined class="text-xl" />
          </div>
          <div>
            <div class="font-600">编辑用户信息</div>
            <div class="text-12px text-white/80">修改成员的基本信息和权限</div>
          </div>
        </div>
      </div>

      <div class="flex gap-6">
        <!-- Left: Basic Info -->
        <div class="flex-1">
          <div class="text-14px font-600 text-[var(--color-text-primary)] mb-3 flex items-center gap-2">
            <div class="w-1 h-4 bg-[var(--color-primary)] rounded-full"></div>
            基本信息
          </div>
          
          <a-form layout="vertical" class="space-y-4">
            <!-- Username (readonly) -->
            <a-form-item label="登录账号">
              <a-input
                v-model:value="editUserForm.username"
                size="large"
                disabled
              >
                <template #prefix><UserOutlined class="text-[var(--color-text-placeholder)]" /></template>
              </a-input>
              <div class="text-11px text-[var(--color-text-placeholder)] mt-1">登录账号不可修改</div>
            </a-form-item>

            <!-- Name -->
            <a-form-item label="姓名" required>
              <a-input
                v-model:value="editUserForm.realName"
                placeholder="成员的真实姓名"
                size="large"
              >
                <template #prefix><IdcardOutlined class="text-[var(--color-text-placeholder)]" /></template>
              </a-input>
            </a-form-item>

            <!-- Password -->
            <a-form-item label="重置密码">
              <a-input-password
                v-model:value="editUserForm.password"
                placeholder="留空则保持原密码不变"
                size="large"
              >
                <template #prefix><LockOutlined class="text-[var(--color-text-placeholder)]" /></template>
              </a-input-password>
              <div class="text-11px text-[var(--color-text-secondary)] mt-1">输入新密码将覆盖原密码</div>
            </a-form-item>
          </a-form>

          <div class="text-14px font-600 text-[var(--color-text-primary)] mb-3 mt-6 flex items-center gap-2">
            <div class="w-1 h-4 bg-[var(--color-primary)] rounded-full"></div>
            联系方式
          </div>

          <a-form layout="vertical" class="space-y-4">
            <!-- Email -->
            <a-form-item label="邮箱">
              <a-input
                v-model:value="editUserForm.email"
                placeholder="用于接收通知"
                size="large"
              >
                <template #prefix><MailOutlined class="text-[var(--color-text-placeholder)]" /></template>
              </a-input>
            </a-form-item>

            <!-- Phone -->
            <a-form-item label="手机号">
              <a-input
                v-model:value="editUserForm.phone"
                placeholder="紧急联系号码"
                size="large"
              >
                <template #prefix><PhoneOutlined class="text-[var(--color-text-placeholder)]" /></template>
              </a-input>
            </a-form-item>
          </a-form>
        </div>

        <!-- Right: Role Selection -->
        <div class="w-280px">
          <div class="text-14px font-600 text-[var(--color-text-primary)] mb-3 flex items-center gap-2">
            <div class="w-1 h-4 bg-[var(--color-warning)] rounded-full"></div>
            分配角色
          </div>

          <div class="space-y-2">
            <div
              v-for="role in roleOptions"
              :key="role.value"
              :class="[
                'border rounded-lg p-3 cursor-pointer transition-all',
                editUserForm.roleIds.includes(role.value)
                  ? 'border-[var(--color-primary)] bg-[rgba(22,119,255,0.08)] shadow-sm'
                  : 'border-[var(--color-border)] hover:border-[var(--color-primary)] hover:bg-[var(--bg-card-alt)]'
              ]"
              @click="toggleEditUserRole(role.value)"
            >
              <div class="flex items-center gap-3">
                <a-checkbox 
                  :checked="editUserForm.roleIds.includes(role.value)"
                  @change="() => toggleEditUserRole(role.value)"
                  @click.stop
                />
                <div :class="[
                  'w-8 h-8 rounded-lg flex items-center justify-center',
                  editUserForm.roleIds.includes(role.value) ? 'bg-[var(--color-primary)] text-white' : 'bg-[var(--bg-card-alt)] text-[var(--color-text-secondary)]'
                ]">
                  <component :is="getRoleIcon(role.key)" />
                </div>
                <div class="flex-1">
                  <div class="text-13px font-500 text-[var(--color-text-primary)]">
                    {{ role.name }}
                  </div>
                  <div class="text-11px text-[var(--color-text-secondary)] truncate">
                    {{ role.description }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Role Permission Tips -->
          <div class="mt-4 p-3 bg-[rgba(250,173,20,0.08)] rounded-lg border border-[rgba(250,173,20,0.2)]">
            <div class="text-11px text-[var(--color-warning)] flex items-start gap-2">
              <ExclamationCircleOutlined class="mt-0.5" />
              <div>
                <div class="font-500 mb-1">权限变更提示</div>
                <div class="text-[var(--color-text-secondary)]">修改角色将影响用户的系统访问权限</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="flex gap-3 mt-6 pt-4 border-t border-[var(--color-border)]">
        <a-button block size="large" @click="showEditUserModal = false">取消</a-button>
        <a-button type="primary" block size="large" @click="handleUpdateUser">保存修改</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { useAuth } from '@/hooks/common/auth';
import {
  PlusOutlined,
  UsergroupAddOutlined,
  SafetyCertificateOutlined,
  SecurityScanOutlined,
  UserOutlined,
  CheckCircleOutlined,
  CheckCircleFilled,
  WarningOutlined,
  FolderOutlined,
  LockOutlined,
  ClockCircleOutlined,
  SettingOutlined,
  EditOutlined,
  UserAddOutlined,
  IdcardOutlined,
  MailOutlined,
  PhoneOutlined,
  ExclamationCircleOutlined,
  CrownOutlined,
  TeamOutlined,
  AuditOutlined,
  EnvironmentOutlined
} from '@ant-design/icons-vue';
import { 
  fetchGetUserList, 
  fetchCreateUser, 
  fetchUpdateUser,
  fetchUpdateUserStatus, 
  fetchResetPassword,
  fetchDeleteUser 
} from '@/service/api';

defineOptions({ name: 'SystemUser' });

const { isAdmin } = useAuth();

const loading = ref(false);
const selectedRole = ref('admin');
const searchKeyword = ref('');
const showRiskModal = ref(false);
const showAddUserModal = ref(false);
const showEditUserModal = ref(false);

const newUserForm = ref({
  username: '',
  realName: '',
  password: '',
  email: '',
  phone: '',
  roleIds: [] as number[], // 支持多角色，默认为空数组
});

const editUserForm = ref({
  id: null as number | null,
  username: '',
  realName: '',
  password: '',
  email: '',
  phone: '',
  roleIds: [] as number[], // 支持多角色，默认为空数组
});

const userList = ref<Api.SystemManage.User[]>([]);

// Statistics computed from userList
const stats = computed(() => {
  const allUsers = userList.value || [];
  return {
    total: allUsers.length,
    admin: allUsers.filter(u => u.role === 1).length,
    auditor: allUsers.filter(u => u.role === 3).length,
    collector: allUsers.filter(u => u.role === 4).length
  };
});

const roleList = computed(() => [
  {
    key: 'all',
    name: '全部用户',
    count: stats.value.total,
    permissions: '查看所有用户信息'
  },
  {
    key: 'admin',
    name: '管理员',
    count: stats.value.admin,
    permissions: '项目创建 / 模板配置 / 全局导出 / 权限配置'
  },
  {
    key: 'manager',
    name: '项目经理',
    count: userList.value.filter(u => u.role === 2).length,
    permissions: '项目进度监控 / 成员管理 / 任务指派'
  },
  {
    key: 'auditor',
    name: '审核员',
    count: stats.value.auditor,
    permissions: '审核反馈 / 版本对比 / PDF 生成'
  },
  {
    key: 'collector',
    name: '采集员',
    count: stats.value.collector,
    permissions: '地图打点 / 表单填报 / 拍照上传 / 草稿保存'
  }
]);

const roleOptions = computed(() => [
  {
    key: 'admin',
    value: 1,
    name: '管理员',
    description: '项目创建、模板配置、全局导出、权限配置'
  },
  {
    key: 'manager',
    value: 2,
    name: '项目经理',
    description: '项目进度监控、成员管理、任务指派'
  },
  {
    key: 'auditor',
    value: 3,
    name: '审核员',
    description: '审核反馈、版本对比、PDF 生成'
  },
  {
    key: 'collector',
    value: 4,
    name: '采集员',
    description: '地图打点、表单填报、拍照上传'
  }
]);

const visiblePages = ref([
  '项目管理',
  '模板设计',
  '地图中心',
  '审核详情',
  '导出中心',
  '日志中心'
]);

const sensitiveActions = ref([
  '全局导出',
  '权限修改',
  '撤销协作链接'
]);



const columns = [
  {
    title: '用户',
    key: 'user',
    width: 250
  },
  {
    title: '角色',
    key: 'role',
    width: 120
  },
  {
    title: '负责项目',
    dataIndex: 'project',
    key: 'project',
    width: 150
  },
  {
    title: '状态',
    key: 'status',
    width: 100
  },
  {
    title: '注册时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 120,
    sorter: true
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right' as const
  }
];

const recentLogins = ref([
  { user: '李航', device: 'Chrome / Windows', time: '今天 10:24', type: 'normal' },
  { user: '张敏', device: 'Safari / macOS', time: '今天 09:15', type: 'normal' },
  { user: '刘晨', device: 'Mobile / iOS', time: '昨天 18:42', type: 'normal' },
  { user: '未知设备', device: '异常 IP 登录', time: '昨天 03:11', type: 'abnormal' }
]);

const riskSettings = ref({
  confirmDisable: true,
  logRoleChange: true,
  approveExport: false,
  autoLock: true
});

const getRoleName = (role: number) => {
  const roleMap: Record<number, string> = {
    1: '超级管理员',
    2: '项目经理',
    3: '审核员',
    4: '采集员'
  };
  return roleMap[role] || '未知';
};

const getRoleColor = (role: number) => {
  const colorMap: Record<number, string> = {
    1: 'red',
    2: 'blue',
    3: 'purple',
    4: 'green'
  };
  return colorMap[role] || 'default';
};

// 角色图标映射
const getRoleIcon = (roleKey: string) => {
  const iconMap: Record<string, any> = {
    'admin': CrownOutlined,
    'manager': TeamOutlined,
    'auditor': AuditOutlined,
    'collector': EnvironmentOutlined
  };
  return iconMap[roleKey] || UserOutlined;
};

const initData = async () => {
  loading.value = true;
  
  // Map role key to number if needed (only for specific roles)
  let roleNum: number | undefined = undefined;
  if (selectedRole.value === 'admin') roleNum = 1;
  else if (selectedRole.value === 'manager') roleNum = 2;
  else if (selectedRole.value === 'auditor') roleNum = 3;
  else if (selectedRole.value === 'collector') roleNum = 4;
  // 'all' role will fetch all users (no role filter)

  const { data, error } = await fetchGetUserList({
    pageNum: 1,
    pageSize: 100,
    username: searchKeyword.value || undefined,
    role: roleNum
  });
  if (!error && data) {
    userList.value = data.records || [];
  }
  loading.value = false;
};

// Watch for changes to role or search keyword
import { watch } from 'vue';
watch([selectedRole, searchKeyword], () => {
  initData();
});

onMounted(() => {
  initData();
});

const handleAddUser = () => {
  showAddUserModal.value = true;
  newUserForm.value = {
    username: '',
    realName: '',
    password: '',
    email: '',
    phone: '',
    roleIds: [], // 重置为空数组
  };
};

const handleCreateUser = async () => {
  if (!newUserForm.value.username || !newUserForm.value.realName || !newUserForm.value.password) {
    message.warning('请填写必填项');
    return;
  }

  const { error } = await fetchCreateUser(newUserForm.value);
  if (!error) {
    message.success('用户创建成功');
    showAddUserModal.value = false;
    initData();
  }
};

const handleEdit = (record: any) => {
  console.log('====== [前端] 编辑用户 - record:', record);
  console.log('====== [前端] record.id:', record.id, 'type:', typeof record.id);
  
  showEditUserModal.value = true;
  editUserForm.value = {
    id: record.id,
    username: record.username,
    realName: record.realName || record.real_name || '',
    password: '', // Don't pre-fill password
    email: record.email || '',
    phone: record.phone || '',
    roleIds: record.roleIds || [] // 使用角色ID数组
  };
  
  console.log('====== [前端] editUserForm:', editUserForm.value);
};

const handleUpdateUser = async () => {
  if (!editUserForm.value.realName) {
    message.warning('请填写姓名');
    return;
  }
  
  if (!editUserForm.value.id) {
    message.error('用户ID不存在，请刷新页面后重试');
    console.error('====== [前端] 用户ID为空:', editUserForm.value);
    return;
  }
  
  console.log('====== [前端] 提交更新用户:', editUserForm.value);

  const { error } = await fetchUpdateUser(editUserForm.value);
  if (!error) {
    message.success('用户信息已更新');
    showEditUserModal.value = false;
    initData();
  }
};

const handleResetPassword = (record: any) => {
  Modal.confirm({
    title: '重置密码',
    content: `确定要重置用户 ${record.realName} 的密码吗？`,
    async onOk() {
      const { error } = await fetchResetPassword(record.id);
      if (!error) {
        message.success('密码已重置为初始密码');
      }
    }
  });
};

const handleDisable = (record: any) => {
  const newStatus = record.status === 1 ? 0 : 1;
  const actionText = newStatus === 1 ? '启用' : '停用';
  
  Modal.confirm({
    title: `${actionText}用户`,
    content: `确定要${actionText}用户 ${record.realName} 吗？`,
    async onOk() {
      const { error } = await fetchUpdateUserStatus(record.id, newStatus);
      if (!error) {
        message.success(`用户已${actionText}`);
        initData();
      }
    }
  });
};

const handleConfirmRiskSettings = () => {
  showRiskModal.value = false;
};

// 切换新用户角色（多选）
const toggleNewUserRole = (roleId: number) => {
  const index = newUserForm.value.roleIds.indexOf(roleId);
  if (index > -1) {
    newUserForm.value.roleIds.splice(index, 1);
  } else {
    newUserForm.value.roleIds.push(roleId);
  }
};

// 切换编辑用户角色（多选）
const toggleEditUserRole = (roleId: number) => {
  const index = editUserForm.value.roleIds.indexOf(roleId);
  if (index > -1) {
    editUserForm.value.roleIds.splice(index, 1);
  } else {
    editUserForm.value.roleIds.push(roleId);
  }
};
</script>
