<script lang="ts" setup>
import { computed } from 'vue';
import { $t } from '@/locales';
import { useRouterPush } from '@/hooks/common/router';

defineOptions({ name: 'ExceptionBase' });

type ExceptionType = '403' | '404' | '500';

interface Props {
  /**
   * Exception type
   *
   * - 403: no permission
   * - 404: not found
   * - 500: service error
   */
  type: ExceptionType;
  /**
   * Custom title
   */
  title?: string;
  /**
   * Custom description
   */
  description?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  description: ''
});

const { routerPushByKey } = useRouterPush();

const iconMap: Record<ExceptionType, string> = {
  '403': 'no-permission',
  '404': 'not-found',
  '500': 'service-error'
};

const titleMap: Record<ExceptionType, string> = {
  '403': '抱歉，您没有权限访问此页面',
  '404': '抱歉，您访问的页面不存在',
  '500': '抱歉，服务器出现了一些问题'
};

const descMap: Record<ExceptionType, string> = {
  '403': '请联系管理员获取访问权限，或返回上一页',
  '404': '页面可能已被删除或移动，请检查URL是否正确',
  '500': '我们的工程师正在紧急处理，请稍后重试'
};

const icon = computed(() => iconMap[props.type]);
const pageTitle = computed(() => props.title || titleMap[props.type]);
const pageDesc = computed(() => props.description || descMap[props.type]);

const goBack = () => {
  window.history.back();
};
</script>

<template>
  <div class="size-full min-h-520px flex-col-center gap-24px overflow-hidden px-24px">
    <div class="flex text-400px text-primary">
      <SvgIcon :local-icon="icon" />
    </div>
    <div class="text-center">
      <h1 class="text-24px font-600 text-[var(--color-text-primary)] mb-8px">
        {{ pageTitle }}
      </h1>
      <p class="text-14px text-[var(--color-text-secondary)] mb-24px">
        {{ pageDesc }}
      </p>
      <div class="flex gap-12px justify-center">
        <AButton @click="routerPushByKey('root')">
          {{ $t('common.backToHome') }}
        </AButton>
        <AButton @click="goBack">
          返回上一页
        </AButton>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
