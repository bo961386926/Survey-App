<template>
  <AForm ref="formRef" :model="model" :rules="rules" layout="vertical" @keyup.enter="handleSubmit" class="space-y-20px">
    <!-- 用户名 -->
    <AFormItem name="userName" class="mb-0!">
      <div class="relative w-full">
        <div class="absolute left-16px top-1/2 -translate-y-1/2 z-10 flex-y-center pointer-events-none">
          <div class="i-material-symbols:person-outline text-[var(--color-text-secondary)] text-20px"></div>
        </div>
        <AInput
          v-model:value="model.userName"
          class="w-full h-52px! pl-48px! rd-12px! bg-[var(--bg-card-alt)]! border-1! border-solid! border-[var(--color-border)]! hover:border-[var(--color-primary)]! focus:border-[var(--color-primary)]! focus:bg-[var(--bg-card)]! focus:shadow-[0_0_0_2px_rgba(22,119,255,0.1)]! transition-all!"
          :placeholder="$t('page.login.common.userNamePlaceholder')"
        />
      </div>
    </AFormItem>

    <!-- 密码 -->
    <AFormItem name="password" class="mb-0!">
      <div class="relative w-full">
        <div class="absolute left-16px top-1/2 -translate-y-1/2 z-10 flex-y-center pointer-events-none">
          <div class="i-material-symbols:lock-outline text-[var(--color-text-secondary)] text-20px"></div>
        </div>
        <AInputPassword
          v-model:value="model.password"
          class="w-full h-52px! pl-48px! rd-12px! bg-[var(--bg-card-alt)]! border-1! border-solid! border-[var(--color-border)]! hover:border-[var(--color-primary)]! focus:border-[var(--color-primary)]! focus:bg-[var(--bg-card)]! focus:shadow-[0_0_0_2px_rgba(22,119,255,0.1)]! transition-all!"
          :placeholder="$t('page.login.common.passwordPlaceholder')"
        />
      </div>
    </AFormItem>

    <!-- 验证码 -->
    <AFormItem name="captcha" class="mb-0!">
      <div class="flex gap-12px w-full">
        <div class="relative flex-1">
          <div class="absolute left-16px top-1/2 -translate-y-1/2 z-10 flex-y-center pointer-events-none">
            <div class="i-material-symbols:key-outline text-[var(--color-text-secondary)] text-20px"></div>
          </div>
          <AInput
            v-model:value="model.captcha"
            class="w-full h-52px! pl-48px! rd-12px! bg-[var(--bg-card-alt)]! border-1! border-solid! border-[var(--color-border)]! hover:border-[var(--color-primary)]! focus:border-[var(--color-primary)]! focus:bg-[var(--bg-card)]! focus:shadow-[0_0_0_2px_rgba(22,119,255,0.1)]! transition-all!"
            placeholder="请输入验证码"
          />
        </div>
        <div class="captcha-box" @click="refreshCaptcha">
          <img v-if="captchaImage" :src="captchaImage" alt="验证码" class="captcha-img" />
          <span v-else class="captcha-placeholder">点击获取</span>
        </div>
      </div>
    </AFormItem>

    <!-- 记住我 / 忘记密码 -->
    <div class="flex-y-center justify-between pt-4px">
      <ACheckbox v-model:checked="rememberMe" class="text-13px" style="color: var(--color-text-secondary);">
        {{ $t('page.login.pwdLogin.rememberMe') }}
      </ACheckbox>
      <AButton type="link" size="small" class="text-13px p-0!" style="color: var(--color-primary);" @click="toggleLoginModule('reset-pwd')">
        {{ $t('page.login.pwdLogin.forgetPassword') }}
      </AButton>
    </div>

    <!-- 登录按钮 -->
    <div class="space-y-16px pt-8px">
      <AButton
        type="primary"
        block
        class="h-52px! rd-12px! text-16px! font-bold! shadow-lg! shadow-primary/20!"
        :loading="authStore.loginLoading"
        @click="handleSubmit"
      >
        立即登录
      </AButton>
      <div class="flex-y-center justify-between">
        <AButton class="login-btn-secondary flex-1" @click="toggleLoginModule('code-login')">
          {{ $t(loginModuleRecord['code-login']) }}
        </AButton>
        <div class="w-12px"></div>
        <AButton class="login-btn-secondary flex-1" @click="toggleLoginModule('register')">
          {{ $t(loginModuleRecord.register) }}
        </AButton>
      </div>
    </div>

    <!-- 社交登录分割线 -->
    <div class="mt-32px mb-16px relative">
      <div class="absolute inset-0 flex-y-center">
        <div class="w-full h-1px" style="background-color: var(--color-divider);"></div>
      </div>
      <div class="relative flex justify-center">
        <span class="px-12px text-12px font-500" style="background-color: var(--bg-card); color: var(--color-text-secondary);">或使用其他方式登录</span>
      </div>
    </div>

    <!-- 社交登录按钮 -->
    <div class="grid grid-cols-2 gap-12px">
      <AButton class="social-login-btn">
        <div class="i-ic:baseline-wechat text-16px" style="color: #07C160;"></div>
        <span class="text-13px">微信登录</span>
      </AButton>
      <AButton class="social-login-btn">
        <div class="i-logos:microsoft-icon text-14px"></div>
        <span class="text-13px">AD 域账号</span>
      </AButton>
    </div>
  </AForm>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { $t } from '@/locales';
import { loginModuleRecord } from '@/constants/app';
import { useRouterPush } from '@/hooks/common/router';
import { useAntdForm, useFormRules } from '@/hooks/common/form';
import { useAuthStore } from '@/store/modules/auth';
import { fetchGetCaptcha } from '@/service/api/auth';

defineOptions({
  name: 'PwdLogin'
});

const authStore = useAuthStore();
const { toggleLoginModule } = useRouterPush();
const { formRef, validate } = useAntdForm();

const rememberMe = ref(false);

interface FormModel {
  userName: string;
  password: string;
  captcha: string;
}

const model: FormModel = reactive({
  userName: '',
  password: '',
  captcha: ''
});

const rules = computed<Record<keyof FormModel, App.Global.FormRule[]>>(() => {
  const { formRules } = useFormRules();

  return {
    userName: formRules.userName,
    password: formRules.pwd,
    captcha: [{ required: true, message: '请输入验证码' }]
  };
});

const captchaImage = ref('');
const captchaKey = ref('');

async function refreshCaptcha() {
  try {
    const { data, error } = await fetchGetCaptcha();
    if (!error && data) {
      captchaImage.value = data.image;
      captchaKey.value = data.key;
    }
  } catch (err) {
    console.error('获取验证码失败:', err);
  }
}

// 初始化时获取验证码
refreshCaptcha();

async function handleSubmit() {
  try {
    await validate();
    await authStore.login(model.userName, model.password, model.captcha, captchaKey.value);
    // 登录成功后刷新验证码
    refreshCaptcha();
  } catch (err) {
    // 表单验证失败时不做额外处理
  }
}
</script>

<style scoped>
/* 验证码区域 */
.captcha-box {
  height: 52px;
  width: 120px;
  border-radius: 12px;
  background-color: var(--bg-card);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  flex-shrink: 0;
  transition: border-color 0.2s ease;
}

.captcha-box:hover {
  border-color: var(--color-primary);
}

.captcha-img {
  height: 100%;
  width: 100%;
  object-fit: cover;
}

.captcha-placeholder {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* 次要按钮 */
.login-btn-secondary {
  height: 40px !important;
  border-radius: 8px !important;
  background-color: var(--bg-card-alt) !important;
  border: 1px solid var(--color-border) !important;
  color: var(--color-text-primary) !important;
}

.login-btn-secondary:hover {
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
}

/* 社交登录按钮 */
.social-login-btn {
  height: 40px !important;
  border-radius: 8px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 8px !important;
  background-color: var(--bg-card) !important;
  border: 1px solid var(--color-border) !important;
  color: var(--color-text-primary) !important;
}

.social-login-btn:hover {
  border-color: var(--color-primary) !important;
}
</style>
