/**
 * 权限码常量定义
 *
 * 命名规范: MODULE_PERMISSION = 'module:permission'
 * 必须与后端 com.qhiot.survey.common.constant.Permissions 保持一致
 */
export const PERMISSION = {
  // ============================
  // 项目管理 (project)
  // ============================
  /** 查看项目 */
  PROJECT_VIEW: 'project:view',
  /** 编辑项目 */
  PROJECT_EDIT: 'project:edit',
  /** 模板绑定 */
  TEMPLATE_BIND: 'template:bind',
  /** 查看模板 */
  TEMPLATE_VIEW: 'template:view',
  /** 编辑模板 */
  TEMPLATE_EDIT: 'template:edit',

  // ============================
  // 点位管理 (point)
  // ============================
  /** 查看点位 */
  POINT_VIEW: 'point:view',
  /** 编辑点位 */
  POINT_EDIT: 'point:edit',

  // ============================
  // 勘查管理 (survey)
  // ============================
  /** 创建勘查 */
  SURVEY_CREATE: 'survey:create',
  /** 编辑勘查 */
  SURVEY_EDIT: 'survey:edit',
  /** 提交勘查 */
  SURVEY_SUBMIT: 'survey:submit',
  /** 协作勘查 */
  SURVEY_ASSIST: 'survey:assist',

  // ============================
  // 任务管理 (task)
  // ============================
  /** 查看任务 */
  TASK_VIEW: 'task:view',
  /** 编辑任务 */
  TASK_EDIT: 'task:edit',
  /** 指派任务 */
  TASK_ASSIGN: 'task:assign',

  // ============================
  // 审核管理 (audit)
  // ============================
  /** 查看审核 */
  AUDIT_VIEW: 'audit:view',
  /** 审核通过 */
  AUDIT_PASS: 'audit:pass',
  /** 审核驳回 */
  AUDIT_REJECT: 'audit:reject',

  // ============================
  // 系统管理 (system)
  // ============================
  /** 用户管理 */
  SYSTEM_USER: 'system:user',
  /** 角色管理 */
  SYSTEM_ROLE: 'system:role',
  /** 字典管理 */
  SYSTEM_DICT: 'system:dict',
  /** 系统操作日志 */
  SYSTEM_LOG: 'system:log',

  // ============================
  // 消息管理 (message)
  // ============================
  /** 推送消息 */
  MESSAGE_PUSH: 'message:push',

  // ============================
  // 导出 (export)
  // ============================
  /** 导出项目数据 */
  EXPORT_PROJECT: 'export:project',
  /** 导出审核数据 */
  EXPORT_AUDIT: 'export:audit',
} as const;

export type PermissionType = (typeof PERMISSION)[keyof typeof PERMISSION];
