/** 角色常量 - 与后端角色映射保持一致 */
export const ROLE = {
  /** 超级管理员 (role=1) */
  SUPER: 'R_SUPER',
  /** 项目负责人 (role=2) */
  ADMIN: 'R_ADMIN',
  /** 审核员 (role=3) */
  AUDITOR: 'R_AUDITOR',
  /** 采集员 (role=4) */
  COLLECTOR: 'R_COLLECTOR',
  /** 查看者 (role=5) */
  VIEWER: 'R_VIEWER',
} as const;

export type RoleType = (typeof ROLE)[keyof typeof ROLE];
