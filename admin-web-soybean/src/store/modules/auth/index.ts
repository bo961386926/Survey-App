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
      error,
      tokenPreview: loginToken?.accessToken ? loginToken.accessToken.substring(0, 20) + '...' : 'none'
    });

    if (!error) {
      const pass = await loginByToken(loginToken);
      
      console.log('✅ [AuthStore] LoginByToken result:', pass);
      console.log('💾 [AuthStore] Token stored:', localStg.get('token') ? 'YES' : 'NO');
      console.log('👤 [AuthStore] UserInfo:', userInfo);

      if (pass) {
        console.log('🚀 [AuthStore] Starting redirect...', { redirect });
        await redirectFromLogin(redirect);
        console.log('✅ [AuthStore] Redirect completed');

        window.$notification?.success({
          message: $t('page.login.common.loginSuccess'),
          description: $t('page.login.common.welcomeBack', { userName: userInfo.userName })
        });
      } else {
        console.error('❌ [AuthStore] LoginByToken failed');
      }
    } else {
      console.error('❌ [AuthStore] Login error:', error);
      // 显示登录失败原因给用户
      const errorMsg = error?.message || error?.msg || '登录失败，请检查用户名密码或验证码';
      window.$message?.error(errorMsg);
      resetStore();
    }

    endLoading();
  }

  async function loginByToken(loginToken: Api.Auth.LoginToken) {
    // 1. stored in the localStorage, the later requests need it in headers
    // 后端返回的字段是token，前端期望accessToken，需要做映射
    const accessToken = loginToken.accessToken || loginToken.token || '';
    localStg.set('token', accessToken);
    localStg.set('refreshToken', loginToken.refreshToken);

    // 2. Map numeric role IDs to role codes (同步 constants/role.ts)
    const roleCodeMap: Record<number, string> = {
      1: ROLE.SUPER,
      2: ROLE.ADMIN,
      3: ROLE.AUDITOR,
      4: ROLE.COLLECTOR,
      5: ROLE.VIEWER,
    };
    
    const numericRole = loginToken.role as number;
    const roleCode = roleCodeMap[numericRole] || 'R_USER';

    // 2. update userInfo from login response
    Object.assign(userInfo, {
      userId: String(loginToken.userId),
      userName: loginToken.username,
      realName: loginToken.realName,
      roles: [roleCode],
      buttons: []
    });

    token.value = accessToken;

    return true;
  }

  async function getUserInfo() {
    const { data: info, error } = await fetchGetUserInfo();

    if (!error) {
      console.log('📥 [AuthStore] getUserInfo response:', info);
      
      // 后端getUserInfo返回的role_code需要映射为前端标准角色码
      // 注意：后端可能返回大写（ADMIN）或小写（admin），需要兼容
      const roleCodeToFrontend: Record<string, string> = {
        // 小写版本
        admin: ROLE.SUPER,
        project_manager: ROLE.ADMIN,
        auditor: ROLE.AUDITOR,
        surveyor: ROLE.COLLECTOR,
        collab: ROLE.VIEWER,
        user: ROLE.VIEWER,
        // 大写版本（兼容）
        ADMIN: ROLE.SUPER,
        PROJECT_MANAGER: ROLE.ADMIN,
        AUDITOR: ROLE.AUDITOR,
        COLLECTOR: ROLE.COLLECTOR,
        THIRD_PARTY: ROLE.VIEWER,
      };

      const backendRoles: string[] = info.roles || [];
      console.log('🔄 [AuthStore] Backend roles:', backendRoles);
      
      const mappedRoles = backendRoles
        .map((r: string) => {
          const mapped = roleCodeToFrontend[r] || r;
          console.log(`  🔄 [AuthStore] Role mapping: ${r} -> ${mapped}`);
          return mapped;
        })
        .filter(Boolean);

      console.log('✅ [AuthStore] Mapped roles:', mappedRoles);

      // ⚠️ 关键修复：先保存旧的角色和按钮，防止丢失
      const oldRoles = userInfo.roles || [];
      const oldButtons = userInfo.buttons || [];
      
      // 只有当映射成功时才更新角色，否则保留旧值
      const newRoles = mappedRoles.length > 0 ? mappedRoles : oldRoles;
      const newButtons = info.buttons?.length > 0 ? info.buttons : oldButtons;
      
      console.log('🔒 [AuthStore] Roles comparison:', {
        oldRoles,
        newRoles,
        willUpdate: newRoles !== oldRoles
      });

      // 更新用户信息，但保留角色和按钮（如果新的为空）
      Object.assign(userInfo, {
        userId: info.userId || userInfo.userId,
        userName: info.userName || userInfo.userName,
        realName: info.realName || userInfo.realName,
        roles: newRoles,
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
