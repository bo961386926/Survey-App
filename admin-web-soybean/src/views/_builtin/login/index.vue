<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import { getColorPalette, mixColor } from '@sa/utils';
import { loginModuleRecord } from '@/constants/app';
import { useAppStore } from '@/store/modules/app';
import { useThemeStore } from '@/store/modules/theme';
import { $t } from '@/locales';
import PwdLogin from './modules/pwd-login.vue';
import CodeLogin from './modules/code-login.vue';
import Register from './modules/register.vue';
import ResetPwd from './modules/reset-pwd.vue';
import BindWechat from './modules/bind-wechat.vue';

interface Props {
  /** The login module */
  module?: UnionKey.LoginModule;
}

const props = defineProps<Props>();

const appStore = useAppStore();
const themeStore = useThemeStore();

interface LoginModule {
  label: App.I18n.I18nKey;
  component: Component;
}

const moduleMap: Record<UnionKey.LoginModule, LoginModule> = {
  'pwd-login': { label: loginModuleRecord['pwd-login'], component: PwdLogin },
  'code-login': { label: loginModuleRecord['code-login'], component: CodeLogin },
  register: { label: loginModuleRecord.register, component: Register },
  'reset-pwd': { label: loginModuleRecord['reset-pwd'], component: ResetPwd },
  'bind-wechat': { label: loginModuleRecord['bind-wechat'], component: BindWechat }
};

const activeModule = computed(() => moduleMap[props.module || 'pwd-login']);

const bgThemeColor = computed(() =>
  themeStore.darkMode ? getColorPalette(themeStore.themeColor, 7) : themeStore.themeColor
);

const bgColor = computed(() => {
  const COLOR_WHITE = '#ffffff';
  const ratio = themeStore.darkMode ? 0.5 : 0.2;
  return mixColor(COLOR_WHITE, themeStore.themeColor, ratio);
});

const isLogin = computed(() => props.module === 'pwd-login' || props.module === 'code-login' || !props.module);
</script>

<template>
  <div class="relative size-full flex-center overflow-hidden bg-page" :style="{ backgroundColor: bgColor }">
    <div class="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-primary/10 rounded-full blur-[100px] pointer-events-none"></div>
    <div class="absolute bottom-[-10%] right-[-10%] w-[60%] h-[60%] bg-primary/5 rounded-full blur-[120px] pointer-events-none"></div>

    <main class="w-full max-w-[1100px] min-h-[650px] grid grid-cols-1 lg:grid-cols-2 rounded-16px overflow-hidden glass-panel relative z-10 mx-24px">
      <!-- Left Side: Brand Visual -->
      <section class="hidden lg:flex flex-col justify-between p-48px relative overflow-hidden border-r border-divider bg-primary/5">
        <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: radial-gradient(circle at 2px 2px, var(--color-text-primary) 1px, transparent 0); background-size: 24px 24px;"></div>
        
        <div class="relative z-10">
          <header class="flex-y-center gap-12px mb-48px">
            <SystemLogo class="size-48px" />
            <h1 class="text-24px font-800 tracking-tight text-primary">{{ $t('system.title') }}</h1>
          </header>
          
          <h2 class="text-36px font-800 leading-tight mb-24px text-primary">
            专业实地调研<br />
            及巡检作业平台
          </h2>
          <p class="text-16px text-secondary leading-relaxed max-w-360px">
            通过高精度地理位置服务与智能表单系统，为您提供权威、稳固、不间断的外勤作业支撑。
          </p>
        </div>

        <div class="relative z-10">
          <div class="p-24px bg-card/40 backdrop-blur-md rounded-8px border border-border/60 shadow-sm">
            <div class="flex-y-center gap-16px mb-12px">
              <div class="size-40px rounded-full bg-primary/20 flex-center">
                <div class="i-material-symbols:person text-primary text-20px"></div>
              </div>
              <div>
                <p class="text-14px font-bold">高级巡检工程师</p>
                <p class="text-12px text-secondary">数字化外勤管理专家</p>
              </div>
            </div>
            <p class="text-13px text-secondary italic font-500 leading-relaxed">
              "在恶劣环境下的数据采集，我们需要的是绝对的可靠性。系统平台从未让我失望。"
            </p>
          </div>
        </div>
      </section>

      <!-- Right Side: Login Form -->
      <section class="p-32px md:p-48px lg:p-64px flex flex-col justify-center bg-card/20">
        <header class="flex-y-center justify-between mb-32px">
          <div class="lg:hidden flex-y-center gap-8px">
            <SystemLogo class="size-32px" />
            <span class="text-18px font-800 text-primary">{{ $t('system.title') }}</span>
          </div>
          <div class="flex gap-12px ml-auto">
            <ThemeSchemaSwitch
              :theme-schema="themeStore.themeScheme"
              :show-tooltip="false"
              class="text-20px"
              @switch="themeStore.toggleThemeScheme"
            />
            <LangSwitch
              :lang="appStore.locale"
              :lang-options="appStore.localeOptions"
              :show-tooltip="false"
              @change-lang="appStore.changeLocale"
            />
          </div>
        </header>

        <div class="mb-32px">
          <h2 class="text-28px font-700 mb-8px">{{ $t(activeModule.label) }}</h2>
          <p class="text-secondary">{{ isLogin ? '欢迎回来，请输入您的凭据开始作业' : '请填写以下信息完成注册' }}</p>
        </div>

        <main class="animation-slide-in-left">
          <Transition :name="themeStore.page.animateMode" mode="out-in" appear>
            <component :is="activeModule.component" />
          </Transition>
        </main>
      </section>
    </main>

    <!-- Footer Meta -->
    <div class="fixed bottom-24px text-center w-full px-24px pointer-events-none z-20">
      <p class="text-12px text-secondary/80 font-500">
        © 2024 {{ $t('system.title') }}. 安全、稳固、精准的行业数据标准。
        <span class="mx-8px hidden md:inline">|</span> 
        隐私条款 <span class="mx-8px">|</span> 服务协议
      </p>
    </div>
  </div>
</template>

<style scoped>
.glass-panel {
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-card);
}
</style>
