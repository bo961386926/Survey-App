<template>
  <AForm ref="formRef" :model="model" :rules="rules" @keyup.enter="handleSubmit" class="space-y-16px">
    <AFormItem name="userName" class="mb-0!">
      <div class="relative group">
        <div class="absolute inset-y-0 left-16px flex-y-center pointer-events-none">
          <div class="i-material-symbols:person-outline text-secondary text-20px group-focus-within:text-primary transition-colors"></div>
        </div>
        <AInput
          v-model:value="model.userName"
          class="pl-40px! h-32px! rd-4px! bg-card! border-border! focus:border-primary! transition-all!"
          :placeholder="$t('page.login.common.userNamePlaceholder')"
        />
      </div>
    </AFormItem>
    <AFormItem name="password" class="mb-0!">
      <div class="relative group">
        <div class="absolute inset-y-0 left-16px flex-y-center pointer-events-none">
          <div class="i-material-symbols:lock-outline text-secondary text-20px group-focus-within:text-primary transition-colors"></div>
        </div>
        <AInputPassword
          v-model:value="model.password"
          class="pl-40px! h-32px! rd-4px! bg-card! border-border! focus:border-primary! transition-all!"
          :placeholder="$t('page.login.common.passwordPlaceholder')"
        />
      </div>
    </AFormItem>
    <AFormItem name="captcha" class="mb-0!">
      <div class="flex gap-12px">
        <div class="relative group flex-1">
          <div class="absolute inset-y-0 left-16px flex-y-center pointer-events-none">
            <div class="i-material-symbols:key-outline text-secondary text-20px group-focus-within:text-primary transition-colors"></div>
          </div>
          <AInput
            v-model:value="model.captcha"
            class="pl-40px! h-32px! rd-4px! bg-card! border-border! focus:border-primary! transition-all!"
            placeholder="请输入验证码"
          />
        </div>
        <div class="h-32px w-100px rd-4px bg-card border border-border flex-center cursor-pointer overflow-hidden" @click="refreshCaptcha">
          <img v-if="captchaImage" :src="captchaImage" alt="验证码" class="h-full w-full object-cover" />
          <span v-else class="text-secondary text-12px">点击获取</span>
        </div>
      </div>
    </AFormItem>
    <div class="flex-y-center justify-between pt-8px">
      <ACheckbox v-model:checked="rememberMe" class="text-13px">{{ $t('page.login.pwdLogin.rememberMe') }}</ACheckbox>
      <AButton type="link" size="small" class="text-13px p-0!" @click="toggleLoginModule('reset-pwd')">
        {{ $t('page.login.pwdLogin.forgetPassword') }}
      </AButton>
    </div>
    <div class="space-y-16px pt-8px">
      <AButton
        type="primary"
        block
        class="h-32px! rd-6px! text-14px! font-600! shadow-sm!"
        :loading="authStore.loginLoading"
        @click="handleSubmit"
      >
        立即登录
      </AButton>
      <div class="flex-y-center justify-between">
        <AButton class="h-32px rd-6px flex-1" @click="toggleLoginModule('code-login')">
          {{ $t(loginModuleRecord['code-login']) }}
        </AButton>
        <div class="w-12px"></div>
        <AButton class="h-32px rd-6px flex-1" @click="toggleLoginModule('register')">
          {{ $t(loginModuleRecord.register) }}
        </AButton>
      </div>
    </div>

    <!-- Social Login Divider -->
    <div class="mt-32px mb-16px relative">
      <div class="absolute inset-0 flex-y-center">
        <div class="w-full h-1px bg-divider/50"></div>
      </div>
      <div class="relative flex justify-center">
        <span class="px-12px bg-transparent text-12px text-secondary font-500">或使用其他方式登录</span>
      </div>
    </div>
    <div class="grid grid-cols-2 gap-12px">
      <AButton class="h-32px rd-6px flex-center gap-8px bg-card! border-border!">
        <div class="i-ic:baseline-wechat text-16px text-[#07C160]"></div>
        <span class="text-13px">微信登录</span>
      </AButton>
      <AButton class="h-32px rd-6px flex-center gap-8px bg-card! border-border!">
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
