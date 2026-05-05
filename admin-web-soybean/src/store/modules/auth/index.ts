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

    // 2. Map numeric role IDs to role codes expected by frontend
    // Backend returns: 1=SUPER_ADMIN, 2=PROJECT_MANAGER, 3=AUDITOR, 4=COLLECTOR
    // Frontend expects: R_SUPER, R_ADMIN, R_AUDITOR, etc.
    const roleCodeMap: Record<number, string> = {
      1: 'R_SUPER',      // 超级管理员
      2: 'R_ADMIN',      // 项目负责人
      3: 'R_AUDITOR',    // 审核员
      4: 'R_COLLECTOR',  // 采集员
      5: 'R_VIEWER'      // 查看者
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
      // update store
      Object.assign(userInfo, info);

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
