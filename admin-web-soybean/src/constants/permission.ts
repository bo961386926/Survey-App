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
  /** 系统操作日志 */
  SYSTEM_LOG: 'system:log',

  // ============================
  // 导出 (export)
  // ============================
  /** 导出项目数据 */
  EXPORT_PROJECT: 'export:project',
  /** 导出审核数据 */
  EXPORT_AUDIT: 'export:audit',
} as const;

export type PermissionType = (typeof PERMISSION)[keyof typeof PERMISSION];
