import { computed } from 'vue';
import { useAuthStore } from '@/store/modules/auth';
import { ROLE, RoleType } from '@/constants/role';

/** 权限检查 Composables */
export function useAuth() {
  const authStore = useAuthStore();
  const roles = authStore.userInfo.roles || [];

  /** 检查是否有指定角色 */
  function hasRole(role: RoleType | RoleType[]): boolean {
    const targetRoles = Array.isArray(role) ? role : [role];
    return targetRoles.some(r => roles.includes(r));
  }

  /** 是否是超级管理员或管理员（拥有所有权限） */
  const isAdmin = computed(() => hasRole([ROLE.SUPER, ROLE.ADMIN]));

  /** 是否是管理员或项目负责人（可管理项目） */
  const canManageProject = computed(() => hasRole([ROLE.SUPER, ROLE.ADMIN]));

  /** 是否是审核员 */
  const isAuditor = computed(() => hasRole(ROLE.AUDITOR));

  /** 是否是采集员 */
  const isCollector = computed(() => hasRole(ROLE.COLLECTOR));

  return { hasRole, isAdmin, canManageProject, isAuditor, isCollector, roles };
}
