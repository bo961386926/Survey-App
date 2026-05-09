import type { Directive, DirectiveBinding } from 'vue';
import { useAuthStore } from '@/store/modules/auth';

/**
 * v-permission 指令
 *
 * 用于按钮级别的权限控制
 *
 * 用法:
 * <button v-permission="'user:create'">新增用户</button>
 * <button v-permission="['user:create', 'user:update']">操作</button>
 * <button v-permission="'system:role:delete'">删除角色</button>
 *
 * 原理:
 * 检查用户权限列表中是否包含指定权限
 * 如果不包含，则移除该 DOM 元素
 */
const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const authStore = useAuthStore();
    const userPermissions = authStore.userInfo.permissions || [];

    // 超级管理员拥有所有权限
    const isSuper = authStore.isStaticSuper;
    if (isSuper) {
      return;
    }

    // 获取需要的权限
    const requiredPermissions: string | string[] = binding.value;

    if (!requiredPermissions) {
      return;
    }

    // 判断是否有权限
    let hasPermission = false;

    if (Array.isArray(requiredPermissions)) {
      // 数组形式：满足任意一个权限即可
      hasPermission = requiredPermissions.some(perm => userPermissions.includes(perm));
    } else {
      // 字符串形式：必须包含该权限
      hasPermission = userPermissions.includes(requiredPermissions);
    }

    // 没有权限则移除元素
    if (!hasPermission && el.parentNode) {
      el.parentNode.removeChild(el);
    }
  }
};

export default permission;
