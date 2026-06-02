<template>
  <AForm ref="formRef" :model="model" :rules="rules" layout="vertical" @keyup.enter="handleSubmit" class="space-y-16px">
    <AFormItem name="phone" class="mb-0!">
      <div class="relative w-full">
        <div class="absolute left-16px top-1/2 -translate-y-1/2 z-10 flex-y-center pointer-events-none">
          <div class="i-material-symbols:smartphone-outline text-[var(--color-text-secondary)] text-20px"></div>
        </div>
        <AInput
          v-model:value="model.phone"
          class="w-full h-52px! pl-48px! rd-12px! bg-[var(--bg-card-alt)]! border-1! border-solid! border-[var(--color-border)]! hover:border-[var(--color-primary)]! focus:border-[var(--color-primary)]! focus:bg-[var(--bg-card)]! focus:shadow-[0_0_0_2px_rgba(22,119,255,0.1)]! transition-all!"
          :placeholder="$t('page.login.common.phonePlaceholder')"
        />
      </div>
    </AFormItem>
    <AFormItem name="code" class="mb-0!">
      <div class="w-full flex-y-center gap-12px">
        <div class="relative flex-1">
          <div class="absolute left-16px top-1/2 -translate-y-1/2 z-10 flex-y-center pointer-events-none">
            <div class="i-material-symbols:verified-user-outline text-[var(--color-text-secondary)] text-20px"></div>
          </div>
          <AInput
            v-model:value="model.code"
            class="w-full h-52px! pl-48px! rd-12px! bg-[var(--bg-card-alt)]! border-1! border-solid! border-[var(--color-border)]! hover:border-[var(--color-primary)]! focus:border-[var(--color-primary)]! focus:bg-[var(--bg-card)]! focus:shadow-[0_0_0_2px_rgba(22,119,255,0.1)]! transition-all!"
            :placeholder="$t('page.login.common.codePlaceholder')"
          />
        </div>
        <AButton
          class="h-52px! rd-12px! px-16px! min-w-120px!"
          :disabled="isCounting"
          :loading="loading"
          @click="getCaptcha(model.phone)"
        >
          {{ label }}
        </AButton>
      </div>
    </AFormItem>
    <div class="space-y-16px pt-16px">
      <AButton
        type="primary"
        block
        class="h-52px! rd-12px! text-16px! font-bold! shadow-lg! shadow-primary/20!"
        @click="handleSubmit"
      >
        {{ $t('common.confirm') }}
      </AButton>
      <AButton block class="h-40px rd-8px" @click="toggleLoginModule('pwd-login')">
        {{ $t('page.login.common.back') }}
      </AButton>
    </div>
  </AForm>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue';
import { $t } from '@/locales';
import { useRouterPush } from '@/hooks/common/router';
import { useAntdForm, useFormRules } from '@/hooks/common/form';
import { useCaptcha } from '@/hooks/business/captcha';

defineOptions({
  name: 'CodeLogin'
});

const { toggleLoginModule } = useRouterPush();
const { formRef, validate } = useAntdForm();
const { label, isCounting, loading, getCaptcha } = useCaptcha();

interface FormModel {
  phone: string;
  code: string;
}

const model: FormModel = reactive({
  phone: '',
  code: ''
});

const rules = computed<Record<keyof FormModel, App.Global.FormRule[]>>(() => {
  const { formRules } = useFormRules();

  return {
    phone: formRules.phone,
    code: formRules.code
  };
});

async function handleSubmit() {
  await validate();
  // request
  window.$message?.success($t('page.login.common.validateSuccess'));
}
</script>

<style scoped>
</style>
