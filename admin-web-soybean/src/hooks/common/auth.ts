import { computed } from 'vue';
import { useAuthStore } from '@/store/modules/auth';
import { ROLE, RoleType } from '@/constants/role';

/** 权限检查 Composables */
export function useAuth() {
  const authStore = useAuthStore();
  const roles = authStore.userInfo.roles || [];
  const permissions = authStore.userInfo.permissions || [];

  /** 检查是否有指定角色 */
  function hasRole(role: RoleType | RoleType[]): boolean {
    const targetRoles = Array.isArray(role) ? role : [role];
    return targetRoles.some(r => roles.includes(r));
  }

  /**
   * 检查是否有指定权限码
   * @param permission 权限码，如 'point:view', 'project:create'
   */
  function hasPermission(permission: string): boolean {
    if (!permission) return true;
    // admin/super 角色拥有所有权限
    if (roles.includes(ROLE.SUPER) || roles.includes(ROLE.ADMIN)) return true;
    return permissions.includes(permission);
  }

  /**
   * 检查是否有任意一个权限
   * @param perms 权限码列表
   */
  function hasAnyPermission(perms: string[]): boolean {
    if (!perms || perms.length === 0) return true;
    return perms.some(p => hasPermission(p));
  }

  /** 是否是超级管理员或管理员（拥有所有权限） */
  const isAdmin = computed(() => hasRole([ROLE.SUPER, ROLE.ADMIN]));

  /** 是否是管理员或项目负责人（可管理项目） */
  const canManageProject = computed(() => hasRole([ROLE.SUPER, ROLE.ADMIN]));

  /** 是否是审核员 */
  const isAuditor = computed(() => hasRole(ROLE.AUDITOR));

  /** 是否是采集员 */
  const isCollector = computed(() => hasRole(ROLE.COLLECTOR));

  return {
    hasRole,
    hasPermission,
    hasAnyPermission,
    isAdmin,
    canManageProject,
    isAuditor,
    isCollector,
    roles,
    permissions
  };
}
