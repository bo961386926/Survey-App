/** 角色常量 - 与后端 sys_role.role_code 保持一致 */
export const ROLE = {
  /** 超级管理员 */
  SUPER: 'admin',
  /** 项目负责人 */
  ADMIN: 'project_manager',
  /** 审核员 */
  AUDITOR: 'auditor',
  /** 采集员 */
  COLLECTOR: 'surveyor',
  /** 查看者/第三方协作 */
  VIEWER: 'collab',
} as const;

export type RoleType = (typeof ROLE)[keyof typeof ROLE];
