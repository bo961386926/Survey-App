<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import type { RuleObject } from 'ant-design-vue/es/form';
import dayjs from 'dayjs';
import { 
  RocketOutlined,
  TeamOutlined
} from '@ant-design/icons-vue';
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
const router = useRouter();

const isEdit = computed(() => !!props.editData);

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

// Watch for visibility and editData
watch(
  () => props.visible,
  (val) => {
    if (val && props.editData) {
      formState.value = {
        projectName: props.editData.projectName,
        projectCode: props.editData.projectCode || '',
        clientName: props.editData.clientName || '',
        startDate: props.editData.startDate,
        endDate: props.editData.endDate,
        startDateObj: props.editData.startDate ? dayjs(props.editData.startDate) : undefined,
        endDateObj: props.editData.endDate ? dayjs(props.editData.endDate) : undefined,
        manager: props.editData.manager || undefined,
        description: props.editData.description || ''
      };
    } else if (val && !props.editData) {
      resetForm();
    }
  }
);

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
    :title="isEdit ? '编辑勘察项目' : '创建新勘察项目'"
    width="640px"
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
      <div class="grid grid-cols-1 gap-x-6">
        <!-- Project Name -->
        <AFormItem name="projectName" label="项目名称">
          <AInput
            v-model:value="formState.projectName"
            placeholder="请输入勘察项目全称"
            allow-clear
          />
        </AFormItem>

        <!-- Project Code -->
        <AFormItem name="projectCode" label="项目编号">
          <AInput
            v-model:value="formState.projectCode"
            placeholder="请输入项目编号 (如: PRJ-2024-001)"
            allow-clear
          />
        </AFormItem>

        <!-- Client Name -->
        <AFormItem name="clientName" label="委托单位 / 客户">
          <AInput
            v-model:value="formState.clientName"
            placeholder="输入合作单位名称"
            allow-clear
          >
            <template #prefix>
              <TeamOutlined class="text-[var(--color-text-placeholder)]" />
            </template>
          </AInput>
        </AFormItem>

        <!-- Date Range -->
        <div class="grid grid-cols-2 gap-4">
          <AFormItem name="startDateObj" label="开始日期">
            <ADatePicker
              v-model:value="formState.startDateObj"
              placeholder="选择日期"
              class="w-full"
            />
          </AFormItem>

          <AFormItem name="endDateObj" label="预计结束">
            <ADatePicker
              v-model:value="formState.endDateObj"
              placeholder="选择日期"
              class="w-full"
            />
          </AFormItem>
        </div>

        <!-- Project Manager -->
        <AFormItem name="manager" label="项目负责人">
          <ASelect
            v-model:value="formState.manager"
            placeholder="请指派项目负责人"
            :options="managerOptions"
            allow-clear
          >
            <template #suffixIcon>
              <TeamOutlined class="text-[var(--color-text-placeholder)]" />
            </template>
          </ASelect>
        </AFormItem>

        <!-- Description -->
        <AFormItem name="description" label="项目描述 / 备注">
          <ATextarea
            v-model:value="formState.description"
            placeholder="描述项目的核心目标、安全注意事项或特殊标准..."
            :rows="4"
          />
        </AFormItem>
      </div>
    </AForm>

    <template #footer>
      <div class="flex justify-end gap-3 pt-2">
        <AButton @click="handleCancel">取消</AButton>
        <AButton type="primary" :loading="submitLoading" @click="handleSubmit">
          <template #icon><RocketOutlined /></template>
          {{ isEdit ? '确认修改' : '立即创建' }}
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
