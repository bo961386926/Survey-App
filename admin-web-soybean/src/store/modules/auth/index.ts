import { computed, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { defineStore } from 'pinia';
import { useLoading } from '@sa/hooks';
import { SetupStoreId } from '@/enum';
import { useRouterPush } from '@/hooks/common/router';
import { fetchGetUserInfo, fetchLogin } from '@/service/api';
import { localStg } from '@/utils/storage';
import { $t } from '@/locales';
import { useRouteStore } from '../route';
import { useTabStore } from '../tab';
import { clearAuthStorage, getToken } from './shared';
import { ROLE } from '@/constants/role';

// 后端 role_code 直接使用，无需映射
function normalizeRoles(backendRoles: string[]): string[] {
  if (!backendRoles || backendRoles.length === 0) return [];
  // 统一转换为小写，并兼容 ROLE_ADMIN / R_ADMIN 等旧角色编码
  return backendRoles
    .map(role => role.toLowerCase().replace(/^role_/, '').replace(/^r_/, ''))
    .filter(Boolean);
}

export const useAuthStore = defineStore(SetupStoreId.Auth, () => {
  const route = useRoute();
  const routeStore = useRouteStore();
  const tabStore = useTabStore();
  const { toLogin, redirectFromLogin } = useRouterPush(false);
  const { loading: loginLoading, startLoading, endLoading } = useLoading();

  const token = ref(getToken());

  const userInfo: Api.Auth.UserInfo = reactive({
    userId: '',
    userName: '',
    realName: '',
    roles: [],
    permissions: [],
    buttons: []
  });

  /** is super role in static route */
  const isStaticSuper = computed(() => {
    const { VITE_AUTH_ROUTE_MODE, VITE_STATIC_SUPER_ROLE } = import.meta.env;

    return VITE_AUTH_ROUTE_MODE === 'static' && userInfo.roles.includes(VITE_STATIC_SUPER_ROLE);
  });

  /** Is login */
  const isLogin = computed(() => Boolean(token.value));

  /** Reset auth store */
  async function resetStore() {
    const authStore = useAuthStore();

    clearAuthStorage();

    authStore.$reset();

    if (!route.meta.constant) {
      await toLogin();
    }

    tabStore.cacheTabs();
    routeStore.resetStore();
  }

  /**
   * Login
   *
   * @param userName User name
   * @param password Password
   * @param captcha Captcha code
   * @param captchaKey Captcha key
   * @param [redirect=true] Whether to redirect after login. Default is `true`
   */
  async function login(userName: string, password: string, captcha: string, captchaKey: string, redirect = true) {
    console.log('🔐 [AuthStore] Login started', { userName, redirect });
    startLoading();

    const { data: loginToken, error } = await fetchLogin(userName, password, captcha, captchaKey);

    console.log('📥 [AuthStore] Login response:', {
      hasData: !!loginToken,
      hasError: !!error,
      tokenPreview: loginToken?.accessToken ? loginToken.accessToken.substring(0, 20) + '...' : 'none'
    });

    if (!error) {
      const pass = await loginByToken(loginToken);

      console.log('✅ [AuthStore] LoginByToken result:', pass);

      if (pass) {
        await redirectFromLogin(redirect);

        window.$notification?.success({
          message: $t('page.login.common.loginSuccess'),
          description: $t('page.login.common.welcomeBack', { userName: userInfo.userName })
        });
      } else {
        console.error('❌ [AuthStore] LoginByToken failed');
      }
    } else {
      console.error('❌ [AuthStore] Login error:', error);
      // 登录失败时让 request 拦截器自动处理错误提示
    }

    endLoading();
  }

  async function loginByToken(loginToken: Api.Auth.LoginToken) {
    // 1. stored in the localStorage, the later requests need it in headers
    const accessToken = loginToken.accessToken || loginToken.token || '';
    localStg.set('token', accessToken);
    localStg.set('refreshToken', loginToken.refreshToken);

    // 2. 从登录响应获取角色编码列表
    //    优先使用后端返回的 roleCodes（新字段，从 sys_role 表查询）
    //    兼容旧版 loginToken.role（数字，已废弃）
    // 2. 直接使用后端 role_code
    const mappedRoles = normalizeRoles(loginToken.roleCodes || []);

    // 3. 更新用户信息（包含权限列表）
    const permissions = loginToken.permissions || [];

    Object.assign(userInfo, {
      userId: String(loginToken.userId),
      userName: loginToken.username,
      realName: loginToken.realName,
      roles: mappedRoles.length > 0 ? mappedRoles : ['user'],
      permissions,
      buttons: []
    });

    console.log('🔑 [AuthStore] Permissions loaded:', permissions);

    token.value = accessToken;

    return true;
  }

  async function getUserInfo() {
    const { data: info, error } = await fetchGetUserInfo();

    if (!error) {
      console.log('📥 [AuthStore] getUserInfo response:', info);

      const backendRoles: string[] = info.roles || [];
      console.log('🔄 [AuthStore] Backend roles:', backendRoles);

      const mappedRoles = normalizeRoles(backendRoles);
      console.log('✅ [AuthStore] Mapped roles:', mappedRoles);

      // 更新用户信息（包含权限列表）
      const oldRoles = userInfo.roles || [];
      const oldPermissions = userInfo.permissions || [];
      const oldButtons = userInfo.buttons || [];

      const newRoles = mappedRoles.length > 0 ? mappedRoles : oldRoles;
      // 优先使用 getUserInfo 返回的权限，如果后端有返回的话
      const newPermissions = (info as any).permissions?.length > 0 ? (info as any).permissions : oldPermissions;
      const newButtons = info.buttons?.length > 0 ? info.buttons : oldButtons;

      Object.assign(userInfo, {
        userId: info.userId || userInfo.userId,
        userName: info.userName || userInfo.userName,
        realName: info.realName || userInfo.realName,
        roles: newRoles,
        permissions: newPermissions,
        buttons: newButtons
      });

      console.log('👤 [AuthStore] Final userInfo:', userInfo);

      return true;
    }

    return false;
  }

  async function initUserInfo() {
    const hasToken = getToken();

    if (hasToken) {
      const pass = await getUserInfo();

      if (!pass) {
        resetStore();
      }
    }
  }

  return {
    token,
    userInfo,
    isStaticSuper,
    isLogin,
    loginLoading,
    resetStore,
    login,
    initUserInfo
  };
});
