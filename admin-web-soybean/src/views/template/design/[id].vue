<script setup lang="ts">
import { ref, computed } from 'vue';
import { VueDraggable } from 'vue-draggable-plus';
import { message } from 'ant-design-vue';

defineOptions({ name: 'TemplateDesign' });

interface Props {
  id: string;
}
const props = defineProps<Props>();

// --- State Definitions ---
const templateName = ref('市政管网普查标准模板');
const currentVersion = ref('v3');

// Field palette organized by categories
const fieldCategories = ref([
  {
    title: '基础字段',
    fields: [
      { type: 'input', label: '单行文本', icon: 'i-material-symbols:text-fields-rounded' },
      { type: 'textarea', label: '多行文本', icon: 'i-material-symbols:notes-rounded' },
      { type: 'number', label: '数字输入', icon: 'i-material-symbols:pin-outline' },
      { type: 'select', label: '下拉选择', icon: 'i-material-symbols:arrow-drop-down-circle-outline' },
      { type: 'radio', label: '单选框', icon: 'i-material-symbols:radio-button-checked' },
      { type: 'checkbox', label: '多选框', icon: 'i-material-symbols:check-box-outline-rounded' },
      { type: 'switch', label: '开关', icon: 'i-material-symbols:toggle-on-outline-rounded' }
    ]
  },
  {
    title: '高级字段',
    fields: [
      { type: 'date', label: '日期选择', icon: 'i-material-symbols:calendar-today-outline-rounded' },
      { type: 'time', label: '时间选择', icon: 'i-material-symbols:schedule-outline-rounded' },
      { type: 'image', label: '图片上传', icon: 'i-material-symbols:image-outline' },
      { type: 'file', label: '附件上传', icon: 'i-material-symbols:attach-file-rounded' },
      { type: 'location', label: '位置定位', icon: 'i-material-symbols:location-on-outline-rounded' },
      { type: 'cascader', label: '级联选择', icon: 'i-material-symbols:account-tree-outline-rounded' }
    ]
  },
  {
    title: '布局组件',
    fields: [
      { type: 'divider', label: '分割线', icon: 'i-material-symbols:horizontal-rule-rounded' },
      { type: 'grid', label: '栅格布局', icon: 'i-material-symbols:grid-view-outline-rounded' }
    ]
  }
]);

interface Field {
  id: string;
  type: string;
  label: string;
  icon: string;
  placeholder?: string;
  required?: boolean;
}

const canvasFields = ref<Field[]>([]);
const activeFieldId = ref<string | null>(null);

const templateList = [
  { id: '1', name: '农村排污口普查模板', active: true },
  { id: '2', name: '城市雨水管网排查', active: false },
  { id: '3', name: '工业废水排查模板', active: false }
];

const versions = [
  { version: 'v3', status: '当前发布版', active: true },
  { version: 'v2', status: '2026.04.16', active: false },
  { version: 'v1', status: '2026.04.03', active: false }
];

// --- Methods ---
const generateId = () => Math.random().toString(36).substring(2, 9);

const selectField = (id: string) => {
  activeFieldId.value = id;
};

const deleteField = (id: string) => {
  canvasFields.value = canvasFields.value.filter(f => f.id !== id);
  if (activeFieldId.value === id) activeFieldId.value = null;
};

const handlePublish = () => {
  message.loading('正在发布...', 1).then(() => message.success('发布成功'));
};

const activeField = computed(() => canvasFields.value.find(f => f.id === activeFieldId.value));

const cloneField = (field: any) => {
  return {
    ...field,
    id: generateId(),
    placeholder: `请输入${field.label}`,
    required: false
  };
};
</script>

<template>
  <div class="h-full flex flex-col bg-[var(--bg-page)] overflow-hidden">
    <!-- Header -->
    <header class="bg-[var(--bg-card)] border-b border-[var(--color-divider)] px-6 py-3 flex items-center justify-between shadow-sm z-10">
      <div class="flex items-center gap-2">
        <div class="i-material-symbols:dynamic-form-outline text-24px text-primary"></div>
        <div>
          <h1 class="text-16px font-700 text-[var(--color-text-primary)]">动态模板设计器</h1>
          <p class="text-12px text-[var(--color-text-secondary)]">可视化表单构建与逻辑配置</p>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <a-button size="small">联动预览</a-button>
        <a-button type="primary" size="small" @click="handlePublish">发布模板</a-button>
      </div>
    </header>

    <main class="flex-1 flex overflow-hidden">
      <!-- Left -->
      <aside class="w-260px bg-[var(--bg-card)] border-r border-[var(--color-divider)] flex flex-col shrink-0">
        <div class="p-4 flex-1 overflow-y-auto custom-scrollbar">
          <div class="mb-4 flex items-center gap-2 text-13px font-600 text-[var(--color-text-primary)]">
            <div class="i-material-symbols:widgets-outline-rounded text-16px text-primary"></div>
            组件库
          </div>
          
          <div v-for="category in fieldCategories" :key="category.title" class="mb-6">
            <div class="mb-3 text-11px font-600 text-[var(--color-text-placeholder)] uppercase tracking-wider">{{ category.title }}</div>
            <VueDraggable
              v-model="category.fields"
              :animation="150"
              :group="{ name: 'fields', pull: 'clone', put: false }"
              :sort="false"
              :clone="cloneField"
              class="grid grid-cols-2 gap-2"
            >
              <template #item="{ element }">
                <div class="flex flex-col items-center justify-center gap-2 p-3 bg-[var(--bg-page)] border border-[var(--color-border)] rd-6px cursor-grab hover:border-primary hover:shadow-sm transition-all group">
                  <div :class="element.icon" class="text-20px text-[var(--color-text-secondary)] group-hover:text-primary"></div>
                  <span class="text-12px text-[var(--color-text-primary)]">{{ element.label }}</span>
                </div>
              </template>
            </VueDraggable>
          </div>
        </div>
        <div class="p-4 border-t border-[var(--color-divider)] bg-[var(--bg-card-alt)]">
          <div class="mb-2 text-12px font-600 flex items-center gap-2">
            <div class="i-material-symbols:history-rounded text-14px"></div>
            最近模板
          </div>
          <div class="space-y-1">
            <div v-for="t in templateList" :key="t.id" class="px-2 py-1.5 text-12px text-[var(--color-text-secondary)] hover:text-primary hover:bg-[var(--bg-hover)] rd-4px cursor-pointer transition-colors truncate">
              {{ t.name }}
            </div>
          </div>
        </div>
      </aside>


      <!-- Center -->
      <section class="flex-1 p-8 overflow-y-auto flex flex-col items-center bg-[var(--bg-card-alt)]">
        <div class="w-full max-w-720px">
          <!-- Template Title Card -->
          <div class="bg-[var(--bg-card)] p-6 rd-12px shadow-sm border border-[var(--color-border)] mb-8 transition-all hover:shadow-md">
            <div class="flex items-center gap-3 mb-2">
              <div class="i-material-symbols:edit-document-outline-rounded text-20px text-primary"></div>
              <a-input v-model:value="templateName" class="text-18px font-700 !border-none !p-0 !bg-transparent !shadow-none hover:!bg-[var(--bg-hover)] rd-4px px-2 -ml-2" />
            </div>
            <div class="flex items-center gap-4 text-12px text-[var(--color-text-secondary)]">
              <span class="flex items-center gap-1"><div class="i-material-symbols:version-rounded text-14px"></div>版本: {{ currentVersion }}</span>
              <span class="flex items-center gap-1"><div class="i-material-symbols:calendar-month-rounded text-14px"></div>最后更新: 2026-05-05</span>
            </div>
          </div>

          <!-- Canvas -->
          <div class="min-h-500px rd-12px border-2 border-dashed border-[var(--color-divider)] p-6 bg-[var(--bg-card)] shadow-inner relative">
            <VueDraggable
              v-model="canvasFields"
              group="fields"
              :animation="150"
              class="space-y-4 min-h-400px"
            >
              <template #item="{ element }">
                <div 
                  class="relative p-5 rd-8px border-2 transition-all cursor-pointer group bg-[var(--bg-card)]"
                  :class="activeFieldId === element.id ? 'border-primary ring-2 ring-primary ring-opacity-10' : 'border-transparent hover:border-[var(--color-border)] hover:shadow-sm'"
                  @click="selectField(element.id)"
                >
                  <!-- Field Label & Actions -->
                  <div class="flex justify-between items-center mb-4">
                    <div class="flex items-center gap-2">
                      <span class="text-13px font-600 text-[var(--color-text-primary)]">
                        {{ element.label }}
                        <span v-if="element.required" class="text-danger ml-1">*</span>
                      </span>
                      <div :class="element.icon" class="text-14px text-[var(--color-text-placeholder)]"></div>
                    </div>
                    <div class="opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-1">
                      <a-button type="text" size="small" class="!flex-center">
                        <template #icon><div class="i-material-symbols:content-copy-outline-rounded text-14px"></div></template>
                      </a-button>
                      <a-button type="text" danger size="small" @click.stop="deleteField(element.id)" class="!flex-center">
                        <template #icon><div class="i-material-symbols:delete-outline-rounded text-14px"></div></template>
                      </a-button>
                    </div>
                  </div>

                  <!-- Field Preview -->
                  <div class="pointer-events-none">
                    <template v-if="element.type === 'input'">
                      <a-input :placeholder="element.placeholder" />
                    </template>
                    <template v-else-if="element.type === 'textarea'">
                      <a-textarea :placeholder="element.placeholder" :rows="3" />
                    </template>
                    <template v-else-if="element.type === 'select'">
                      <a-select :placeholder="element.placeholder" class="w-full" />
                    </template>
                    <template v-else-if="element.type === 'radio'">
                      <a-radio-group><a-radio>选项一</a-radio><a-radio>选项二</a-radio></a-radio-group>
                    </template>
                    <template v-else-if="element.type === 'checkbox'">
                      <a-checkbox-group><a-checkbox>选项一</a-checkbox><a-checkbox>选项二</a-checkbox></a-checkbox-group>
                    </template>
                    <template v-else-if="element.type === 'switch'">
                      <a-switch />
                    </template>
                    <template v-else-if="element.type === 'date'">
                      <a-date-picker class="w-full" />
                    </template>
                    <template v-else-if="element.type === 'image'">
                      <div class="w-100px h-100px rd-4px border border-dashed border-[var(--color-border)] flex-center flex-col gap-1 text-[var(--color-text-placeholder)]">
                        <div class="i-material-symbols:add-rounded text-24px"></div>
                        <span class="text-11px">上传图片</span>
                      </div>
                    </template>
                    <template v-else-if="element.type === 'divider'">
                      <div class="h-1px bg-[var(--color-divider)] w-full my-2"></div>
                    </template>
                    <template v-else>
                      <div class="h-10 bg-[var(--bg-page)] rd-4px border border-[var(--color-border)] opacity-60 px-3 flex items-center text-12px italic text-[var(--color-text-placeholder)]">
                        {{ element.label }} 预览占位
                      </div>
                    </template>
                  </div>
                </div>
              </template>
              <template #header>
                <div v-if="canvasFields.length === 0" class="absolute inset-0 flex-center flex-col gap-4 text-[var(--color-text-placeholder)]">
                  <div class="i-material-symbols:drag-pan-rounded text-48px opacity-20"></div>
                  <div class="text-14px">拖拽左侧组件到此处开始设计</div>
                </div>
              </template>
            </VueDraggable>
          </div>
        </div>
      </section>

      <!-- Right -->
      <aside class="w-300px bg-[var(--bg-card)] border-l border-[var(--color-divider)] flex flex-col shrink-0 overflow-hidden">
        <div class="flex-1 p-5 overflow-y-auto custom-scrollbar">
          <div class="mb-6 flex items-center gap-2 text-14px font-600 text-[var(--color-text-primary)]">
            <div class="i-material-symbols:settings-suggest-outline-rounded text-18px text-primary"></div>
            属性配置
          </div>

          <template v-if="activeField">
            <a-form layout="vertical" class="premium-form">
              <a-form-item label="组件 ID">
                <a-input :value="activeField.id" disabled class="!bg-[var(--bg-page)]" />
              </a-form-item>
              <a-form-item label="字段标题">
                <a-input v-model:value="activeField.label" placeholder="请输入标题" />
              </a-form-item>
              <a-form-item label="提示文字" v-if="['input', 'textarea', 'select', 'date'].includes(activeField.type)">
                <a-input v-model:value="activeField.placeholder" placeholder="请输入提示文字" />
              </a-form-item>
              
              <div class="my-6 border-t border-[var(--color-divider)]"></div>
              
              <div class="space-y-4">
                <div class="flex justify-between items-center">
                  <span class="text-13px font-500">是否必填</span>
                  <a-switch v-model:checked="activeField.required" size="small" />
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-13px font-500">是否禁用</span>
                  <a-switch size="small" />
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-13px font-500">隐藏字段</span>
                  <a-switch size="small" />
                </div>
              </div>

              <template v-if="activeField.type === 'select' || activeField.type === 'radio' || activeField.type === 'checkbox'">
                <div class="mt-6">
                  <div class="text-13px font-500 mb-3 flex justify-between items-center">
                    <span>选项配置</span>
                    <a-button type="link" size="small" class="!p-0">+ 添加</a-button>
                  </div>
                  <div class="space-y-2">
                    <div v-for="i in 2" :key="i" class="flex items-center gap-2">
                      <a-input :value="'选项' + i" size="small" />
                      <a-button type="text" danger size="small" class="!p-0"><div class="i-material-symbols:close-rounded"></div></a-button>
                    </div>
                  </div>
                </div>
              </template>
            </a-form>
          </template>
          <div v-else class="h-full flex-center flex-col gap-4 text-[var(--color-text-placeholder)]">
            <div class="i-material-symbols:ads-click-rounded text-40px opacity-10"></div>
            <div class="text-12px">选中画布中的字段进行详细配置</div>
          </div>
        </div>

        <!-- Versions -->
        <div class="p-5 border-t border-[var(--color-divider)] bg-[var(--bg-card-alt)]">
          <div class="text-12px font-600 mb-4 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="i-material-symbols:history-rounded text-16px text-primary"></div>
              历史版本
            </div>
            <a-button type="link" size="small" class="!text-11px">查看全部</a-button>
          </div>
          <div class="space-y-3">
            <div v-for="v in versions" :key="v.version" class="flex items-center justify-between group cursor-pointer">
              <div class="flex flex-col">
                <span class="text-12px font-500 group-hover:text-primary transition-colors">{{ v.version }}</span>
                <span class="text-11px opacity-50">{{ v.status }}</span>
              </div>
              <div class="i-material-symbols:chevron-right-rounded text-16px opacity-0 group-hover:opacity-40"></div>
            </div>
          </div>
        </div>
      </aside>

    </main>
  </div>
</template>

<style scoped>
:deep(.ant-input), :deep(.ant-select-selector), :deep(.ant-picker), :deep(.ant-input-number) {
  border-radius: 4px !important;
  border-color: var(--color-border) !important;
}

:deep(.ant-input:focus), :deep(.ant-select-selector:focus) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1) !important;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--color-divider);
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.premium-form :deep(.ant-form-item-label > label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.ghost-class {
  opacity: 0.5;
  background: var(--bg-hover) !important;
  border: 2px dashed var(--color-primary) !important;
}
</style>
