<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import type { RuleObject } from 'ant-design-vue/es/form';
import dayjs from 'dayjs';
import { fetchCreateProject, fetchUpdateProject, fetchGetAllUsers } from '@/service/api';

defineOptions({ name: 'ProjectCreateModal' });

interface Props {
  visible: boolean;
  editData?: Api.Project.ProjectInfo | null;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'success'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
const isEdit = computed(() => !!props.editData);
const showAdvanced = ref(false);

// Form state
const formRef = ref();
const formState = ref<Api.Project.ProjectEditParams & { startDateObj?: dayjs.Dayjs; endDateObj?: dayjs.Dayjs }>({
  projectName: '',
  projectCode: '',
  clientName: '',
  startDate: undefined,
  endDate: undefined,
  manager: undefined,
  description: ''
});

// Watch for visibility and editData — fill form when modal opens
watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.editData) {
        fillEditForm(props.editData);
      } else {
        resetForm();
      }
    }
  }
);

// Also watch editData directly — handle case where editData changes while modal is open
watch(
  () => props.editData,
  (val) => {
    if (props.visible && val) {
      fillEditForm(val);
    }
  }
);

function fillEditForm(data: Api.Project.ProjectInfo) {
  formState.value = {
    projectName: data.projectName,
    projectCode: data.projectCode || '',
    clientName: data.clientName || '',
    startDate: data.startDate,
    endDate: data.endDate,
    startDateObj: data.startDate ? dayjs(data.startDate) : undefined,
    endDateObj: data.endDate ? dayjs(data.endDate) : undefined,
    manager: data.manager || undefined,
    description: data.description || ''
  };
}

// Manager options
const managerOptions = ref<{ label: string; value: string }[]>([]);

async function getManagerOptions() {
  const { data, error } = await fetchGetAllUsers();
  if (!error && data) {
    managerOptions.value = data.map(user => ({
      label: `${user.realName} (${user.username})`,
      value: user.realName // Backend expects manager name as string
    }));
  }
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      getManagerOptions();
    }
  }
);

const submitLoading = ref(false);

// Validation rules
const rules: Record<string, RuleObject | RuleObject[]> = {
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度应在2-100个字符之间', trigger: 'blur' }
  ],
  projectCode: [
    { required: true, message: '请输入项目编号', trigger: 'blur' }
  ],
  startDateObj: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ]
};

const handleClose = () => {
  emit('update:visible', false);
};

const resetForm = () => {
  formState.value = {
    projectName: '',
    projectCode: '',
    clientName: '',
    startDate: undefined,
    endDate: undefined,
    startDateObj: undefined,
    endDateObj: undefined,
    manager: undefined,
    description: ''
  };
  formRef.value?.resetFields();
};

const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    submitLoading.value = true;

    const params: Api.Project.ProjectEditParams = {
      projectName: formState.value.projectName,
      projectCode: formState.value.projectCode,
      clientName: formState.value.clientName || undefined,
      startDate: formState.value.startDateObj ? dayjs(formState.value.startDateObj).format('YYYY-MM-DD') : undefined,
      endDate: formState.value.endDateObj ? dayjs(formState.value.endDateObj).format('YYYY-MM-DD') : undefined,
      manager: formState.value.manager, // Correctly sending the manager name string
      description: formState.value.description || undefined
    };

    const response = isEdit.value 
      ? await fetchUpdateProject(props.editData!.id, params)
      : await fetchCreateProject(params);

    if (!response.error) {
      message.success(isEdit.value ? '项目更新成功' : '项目创建成功');
      emit('success');
      handleClose();
    }
  } catch (error: any) {
    if (error.errorFields) return;
    console.error('Operation failed:', error);
    message.error(isEdit.value ? '项目更新失败' : '项目创建失败');
  } finally {
    submitLoading.value = false;
  }
};

const handleCancel = () => {
  handleClose();
};
</script>

<template>
  <AModal
    :open="props.visible"
    :title="isEdit ? `编辑项目：${props.editData?.projectName || ''}` : '新建项目'"
    width="520px"
    :maskClosable="false"
    :confirmLoading="submitLoading"
    @cancel="handleCancel"
    @ok="handleSubmit"
    class="harmonized-modal"
  >
    <!-- Form Container -->
    <AForm
      ref="formRef"
      :model="formState"
      :rules="rules"
      layout="vertical"
      class="mt-4"
    >
      <!-- 基础信息（始终可见） -->
      <div class="grid grid-cols-2 gap-12px">
        <AFormItem name="projectName" label="项目名称" class="col-span-2">
          <AInput
            v-model:value="formState.projectName"
            placeholder="请输入项目名称"
            allow-clear
          />
        </AFormItem>

        <AFormItem name="projectCode" label="项目编号">
          <AInput
            v-model:value="formState.projectCode"
            placeholder="如: PRJ-2024-001"
            allow-clear
          />
        </AFormItem>

        <AFormItem name="clientName" label="委托单位">
          <AInput
            v-model:value="formState.clientName"
            placeholder="合作单位"
            allow-clear
          />
        </AFormItem>
      </div>

      <!-- 折叠：更多信息 -->
      <div class="mt-4">
        <button
          type="button"
          class="flex items-center gap-4px text-12px text-[var(--color-text-secondary)] hover:text-primary transition-colors cursor-pointer bg-transparent border-none p-0"
          @click="showAdvanced = !showAdvanced"
        >
          <div :class="['i-material-symbols:chevron-right-rounded text-14px transition-transform', showAdvanced ? 'rotate-90' : '']"></div>
          {{ showAdvanced ? '收起更多信息' : '展开更多信息' }}
        </button>

        <div v-show="showAdvanced" class="mt-12px grid grid-cols-2 gap-12px">
          <AFormItem name="startDateObj" label="开始日期">
            <ADatePicker
              v-model:value="formState.startDateObj"
              placeholder="选择日期"
              class="w-full"
            />
          </AFormItem>

          <AFormItem name="endDateObj" label="结束日期">
            <ADatePicker
              v-model:value="formState.endDateObj"
              placeholder="选择日期"
              class="w-full"
            />
          </AFormItem>

          <AFormItem name="manager" label="项目负责人" class="col-span-2">
            <ASelect
              v-model:value="formState.manager"
              placeholder="指派负责人（可选）"
              :options="managerOptions"
              allow-clear
              class="w-full"
            />
          </AFormItem>

          <AFormItem name="description" label="备注" class="col-span-2">
            <ATextarea
              v-model:value="formState.description"
              placeholder="核心目标、安全注意事项..."
              :rows="2"
            />
          </AFormItem>
        </div>
      </div>
    </AForm>

    <template #footer>
      <div class="flex justify-end gap-8px">
        <AButton @click="handleCancel">取消</AButton>
        <AButton type="primary" :loading="submitLoading" class="h-32px!" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建项目' }}
        </AButton>
      </div>
    </template>
  </AModal>
</template>

<style scoped>
:deep(.harmonized-modal .ant-modal-content) {
  border-radius: 8px;
  padding: 24px;
}

:deep(.harmonized-modal .ant-modal-header) {
  margin-bottom: 24px;
  border-bottom: none;
}

:deep(.harmonized-modal .ant-modal-title) {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
}

:deep(.ant-input),
:deep(.ant-select-selector),
:deep(.ant-picker) {
  border-radius: 4px !important;
  height: 32px !important;
}

:deep(.ant-btn) {
  border-radius: 6px !important;
  height: 32px !important;
}

:deep(.ant-form-item-label > label) {
  font-size: 14px !important;
  color: var(--color-text-primary) !important;
  font-weight: 500 !important;
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}
</style>
