<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { VueDraggable } from 'vue-draggable-plus';
import { message } from 'ant-design-vue';
import { useAuth } from '@/hooks/common/auth';
import { fetchGetTemplateDetail, fetchSaveTemplateDraft, fetchPublishTemplate, fetchGetTemplateVersions, type FieldSchema, type FieldOption, type TemplateSaveDraft } from '@/service/api/template';

defineOptions({ name: 'TemplateDesign' });

const route = useRoute();
const router = useRouter();
const templateId = route.params.id as string;

const { hasPermission } = useAuth();

// --- State ---
const templateName = ref('未命名模板');
const loading = ref(true);
const saving = ref(false);
const canvasFields = ref<FieldSchema[]>([]);
const activeFieldId = ref<string | null>(null);
const versions = ref<any[]>([]);
const currentVersion = ref('');
const hasUnsavedChanges = ref(false);

// --- Field Palette - 点击添加组件，不用 VueDraggable ---
const fieldPalette = [
  { type: 'input', label: '单行文本', icon: 'i-material-symbols:text-fields-rounded', category: '基础字段' },
  { type: 'textarea', label: '多行文本', icon: 'i-material-symbols:notes-rounded', category: '基础字段' },
  { type: 'number', label: '数字输入', icon: 'i-material-symbols:pin-outline', category: '基础字段' },
  { type: 'select', label: '下拉选择', icon: 'i-material-symbols:arrow-drop-down-circle-outline', category: '基础字段' },
  { type: 'radio', label: '单选框', icon: 'i-material-symbols:radio-button-checked', category: '基础字段' },
  { type: 'checkbox', label: '多选框', icon: 'i-material-symbols:check-box-outline-rounded', category: '基础字段' },
  { type: 'switch', label: '开关', icon: 'i-material-symbols:toggle-on-outline-rounded', category: '基础字段' },
  { type: 'date', label: '日期选择', icon: 'i-material-symbols:calendar-today-outline-rounded', category: '高级字段' },
  { type: 'image', label: '图片上传', icon: 'i-material-symbols:image-outline', category: '高级字段' },
  { type: 'location', label: '位置定位', icon: 'i-material-symbols:location-on-outline-rounded', category: '高级字段' },
  { type: 'divider', label: '分割线', icon: 'i-material-symbols:horizontal-rule-rounded', category: '布局组件' },
];

const paletteCategories = computed(() => {
  const map: Record<string, typeof fieldPalette> = {};
  fieldPalette.forEach(item => {
    if (!map[item.category]) map[item.category] = [];
    map[item.category].push(item);
  });
  return Object.entries(map).map(([title, items]) => ({ title, items }));
});

// --- Computed ---
const activeField = computed(() => canvasFields.value.find(f => f.id === activeFieldId.value));

// --- Methods ---
const generateId = () => 'f_' + Date.now().toString(36) + Math.random().toString(36).substring(2, 6);

const createField = (type: string, label: string): FieldSchema => ({
  id: generateId(),
  type: type as FieldSchema['type'],
  label,
  placeholder: `请输入${label}`,
  required: false,
  disabled: false,
  hidden: false,
  defaultValue: undefined,
  validation: {},
  ...(type === 'image' ? { imageConfig: { cameraOnly: false, maxCount: 9, maxSize: 10 } } : {}),
  ...(type === 'select' || type === 'radio' || type === 'checkbox' ? {
    options: [{ label: '选项一', value: 'option_1' }, { label: '选项二', value: 'option_2' }],
    optionSource: { type: 'static' }
  } : {}),
  ...(type === 'location' ? {
    mapZoom: 15,
    autoFillLngFieldId: undefined as string | undefined,
    autoFillLatFieldId: undefined as string | undefined
  } : {})
});

const addFieldToCanvas = (item: { type: string; label: string }) => {
  canvasFields.value.push(createField(item.type, item.label));
  markDirty();
};

const selectField = (id: string | null) => { activeFieldId.value = id; };

const deleteField = (id: string) => {
  canvasFields.value = canvasFields.value.filter(f => f.id !== id);
  if (activeFieldId.value === id) activeFieldId.value = null;
  markDirty();
};

const copyField = (field: FieldSchema) => {
  const copy = JSON.parse(JSON.stringify(field)) as FieldSchema;
  copy.id = generateId();
  canvasFields.value.splice(canvasFields.value.findIndex(f => f.id === field.id) + 1, 0, copy);
  markDirty();
};

const markDirty = () => { hasUnsavedChanges.value = true; };

// --- Options Management ---
const addOption = (field: FieldSchema) => {
  if (!field.options) field.options = [];
  const idx = field.options.length + 1;
  field.options.push({ label: `选项${idx}`, value: `option_${idx}` });
  markDirty();
};

const removeOption = (field: FieldSchema, index: number) => {
  field.options?.splice(index, 1);
  markDirty();
};

// --- Linkage Rules ---
const addLinkageRule = (field: FieldSchema) => {
  if (!field.linkageRules) field.linkageRules = [];
  field.linkageRules.push({ action: 'show', conditions: [{ fieldId: '', operator: 'eq', value: '' }] });
  markDirty();
};
const removeLinkageRule = (field: FieldSchema, ruleIdx: number) => { field.linkageRules?.splice(ruleIdx, 1); markDirty(); };
const addCondition = (field: FieldSchema, ruleIdx: number) => {
  field.linkageRules?.[ruleIdx]?.conditions.push({ fieldId: '', operator: 'eq', value: '' });
  markDirty();
};
const removeCondition = (field: FieldSchema, ruleIdx: number, condIdx: number) => {
  field.linkageRules?.[ruleIdx]?.conditions.splice(condIdx, 1);
  markDirty();
};
const otherFields = computed(() => canvasFields.value.filter(f => f.id !== activeFieldId.value));

// --- Mobile Preview ---
const showPreview = ref(false);
const previewFields = computed(() => canvasFields.value);

// --- Save / Load / Publish ---
const loadTemplate = async () => {
  if (!templateId || templateId === 'new') { loading.value = false; return; }
  loading.value = true;
  try {
    const { data, error } = await fetchGetTemplateDetail(templateId);
    if (!error && data) templateName.value = (data as any).templateName || '未命名模板';
    const { data: versionsData } = await fetchGetTemplateVersions(Number(templateId));
    if (versionsData) {
      versions.value = versionsData as any[];
      const latest = versionsData[0];
      if (latest) {
        currentVersion.value = `v${latest.versionNo}`;
        if (latest.fieldsJson) {
          try { const parsed = JSON.parse(latest.fieldsJson); if (Array.isArray(parsed)) canvasFields.value = parsed; } catch {}
        }
      }
    }
  } catch (err) { console.error('加载模板失败', err); }
  finally { loading.value = false; }
};

const saveDraft = async () => {
  if (!templateId || templateId === 'new') return;
  saving.value = true;
  try {
    const { error } = await fetchSaveTemplateDraft(templateId, {
      templateName: templateName.value, fields: canvasFields.value, rules: '{}', linkageRules: '[]'
    });
    if (!error) { hasUnsavedChanges.value = false; message.success('草稿已保存'); }
  } catch { message.error('保存失败'); }
  finally { saving.value = false; }
};

const handlePublish = async () => {
  if (canvasFields.value.length === 0) { message.warning('请至少添加一个字段'); return; }
  if (!templateId || templateId === 'new') { message.warning('请先创建模板'); return; }
  saving.value = true;
  try {
    const { error } = await fetchPublishTemplate(Number(templateId), {
      templateName: templateName.value, fields: canvasFields.value, rules: '{}', linkageRules: '[]'
    });
    if (!error) {
      hasUnsavedChanges.value = false; message.success('模板发布成功');
      const { data: vd } = await fetchGetTemplateVersions(Number(templateId));
      if (vd) { versions.value = vd as any[]; currentVersion.value = `v${vd[0]?.versionNo}`; }
    }
  } catch { message.error('发布失败'); }
  finally { saving.value = false; }
};

watch(canvasFields, () => { hasUnsavedChanges.value = true; }, { deep: true });
onMounted(() => { loadTemplate(); });
</script>

<template>
  <div class="h-full flex flex-col bg-[var(--bg-page)] overflow-hidden">
    <header class="bg-[var(--bg-card)] border-b border-[var(--color-divider)] px-6 py-3 flex items-center justify-between shadow-sm z-10">
      <div class="flex items-center gap-3">
        <div class="i-material-symbols:dynamic-form-outline text-24px text-primary"></div>
        <div>
          <div class="flex items-center gap-3">
            <h1 class="text-16px font-700 text-[var(--color-text-primary)]">动态模板设计器</h1>
            <a-tag v-if="currentVersion" color="blue">{{ currentVersion }}</a-tag>
            <a-tag v-if="hasUnsavedChanges" color="orange">未保存</a-tag>
          </div>
          <p class="text-12px text-[var(--color-text-secondary)]">可视化表单构建与逻辑配置</p>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <a-button size="small" @click="showPreview = true"><div class="i-material-symbols:smartphone-outline-rounded text-16px mr-1"></div>预览</a-button>
        <a-button size="small" :loading="saving" @click="saveDraft" :disabled="!hasUnsavedChanges">{{ saving ? '保存中...' : '保存草稿' }}</a-button>
        <a-button type="primary" size="small" :loading="saving" @click="handlePublish">发布模板</a-button>
      </div>
    </header>

    <main v-if="!loading" class="flex-1 flex overflow-hidden">
      <!-- Left: Component Palette (click to add) -->
      <aside class="w-260px bg-[var(--bg-card)] border-r border-[var(--color-divider)] flex flex-col shrink-0">
        <div class="p-4 flex-1 overflow-y-auto custom-scrollbar">
          <div class="mb-4 flex items-center gap-2 text-13px font-600 text-[var(--color-text-primary)]">
            <div class="i-material-symbols:widgets-outline-rounded text-16px text-primary"></div>
            组件库
          </div>
          <div v-for="cat in paletteCategories" :key="cat.title" class="mb-6">
            <div class="mb-3 text-11px font-600 text-[var(--color-text-placeholder)] uppercase tracking-wider">{{ cat.title }}</div>
            <div class="grid grid-cols-2 gap-2">
              <div v-for="item in cat.items" :key="item.type"
                class="flex flex-col items-center justify-center gap-2 p-3 bg-[var(--bg-page)] border border-[var(--color-border)] rd-6px cursor-pointer hover:border-primary hover:shadow-sm hover:bg-primary/5 transition-all group"
                @click="addFieldToCanvas(item)"
              >
                <div :class="item.icon" class="text-20px text-[var(--color-text-secondary)] group-hover:text-primary"></div>
                <span class="text-11px text-[var(--color-text-primary)] text-center">{{ item.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- Center: Canvas -->
      <section class="flex-1 p-8 overflow-y-auto flex flex-col items-center bg-[var(--bg-card-alt)]">
        <div class="w-full max-w-720px">
          <div class="bg-[var(--bg-card)] p-6 rd-12px shadow-sm border border-[var(--color-border)] mb-8">
            <div class="flex items-center gap-3 mb-2">
              <div class="i-material-symbols:edit-document-outline-rounded text-20px text-primary"></div>
              <a-input v-model:value="templateName" class="text-18px font-700 !border-none !p-0 !bg-transparent !shadow-none hover:!bg-[var(--bg-hover)] rd-4px px-2 -ml-2" @input="markDirty" />
            </div>
          </div>

          <div class="min-h-500px rd-12px border-2 border-dashed border-[var(--color-divider)] p-6 bg-[var(--bg-card)] shadow-inner relative">
            <VueDraggable v-model="canvasFields" group="fields" :animation="150" class="space-y-4 min-h-400px">
              <div
                v-for="element in canvasFields"
                :key="element.id"
                class="relative p-5 rd-8px border-2 transition-all cursor-pointer group bg-[var(--bg-card)]"
                :class="activeFieldId === element.id ? 'border-primary ring-2 ring-primary/10' : 'border-transparent hover:border-[var(--color-border)] hover:shadow-sm'"
                @click="selectField(element.id)"
              >
                <div class="flex justify-between items-center mb-4">
                  <div class="flex items-center gap-2">
                    <span class="text-13px font-600 text-[var(--color-text-primary)]">
                      {{ element.label }}<span v-if="element.required" class="text-danger ml-1">*</span>
                    </span>
                  </div>
                  <div class="opacity-60 group-hover:opacity-100 transition-opacity flex items-center gap-1">
                    <a-button type="text" size="small" class="!flex-center" @click.stop="copyField(element)"><template #icon><div class="i-material-symbols:content-copy-outline-rounded text-14px"></div></template></a-button>
                    <a-button type="text" danger size="small" @click.stop="deleteField(element.id)" class="!flex-center" title="删除此字段"><template #icon><div class="i-material-symbols:delete-outline-rounded text-14px"></div></template></a-button>
                  </div>
                </div>
                <div class="pointer-events-none">
                  <template v-if="element.type === 'input'"><a-input :placeholder="element.placeholder" /></template>
                  <template v-else-if="element.type === 'textarea'"><a-textarea :placeholder="element.placeholder" :rows="3" /></template>
                  <template v-else-if="element.type === 'number'"><a-input-number class="w-full" :placeholder="element.placeholder" /></template>
                  <template v-else-if="element.type === 'select'"><a-select :placeholder="element.placeholder" class="w-full" /></template>
                  <template v-else-if="element.type === 'radio'"><a-radio-group><a-radio>选项一</a-radio><a-radio>选项二</a-radio></a-radio-group></template>
                  <template v-else-if="element.type === 'checkbox'"><a-checkbox-group><a-checkbox>选项一</a-checkbox><a-checkbox>选项二</a-checkbox></a-checkbox-group></template>
                  <template v-else-if="element.type === 'switch'"><a-switch /></template>
                  <template v-else-if="element.type === 'date'"><a-date-picker class="w-full" /></template>
                  <template v-else-if="element.type === 'image'">
                    <div class="w-100px h-100px rd-4px border border-dashed border-[var(--color-border)] flex-center flex-col gap-1 text-[var(--color-text-placeholder)]">
                      <div class="i-material-symbols:add-rounded text-24px"></div><span class="text-11px">上传图片</span>
                    </div>
                  </template>
                  <template v-else-if="element.type === 'location'">
                    <div class="h-10 bg-[var(--bg-page)] rd-4px border border-[var(--color-border)] flex items-center px-3 text-12px text-[var(--color-text-secondary)]">
                      <div class="i-material-symbols:location-on-outline-rounded text-16px mr-2"></div>点击选择位置
                    </div>
                  </template>
                  <template v-else-if="element.type === 'divider'"><div class="h-1px bg-[var(--color-divider)] w-full my-2"></div></template>
                </div>
              </div>
            </VueDraggable>
            <div v-if="canvasFields.length === 0" class="absolute inset-0 flex-center flex-col gap-4 text-[var(--color-text-placeholder)] pointer-events-none">
              <div class="i-material-symbols:drag-pan-rounded text-48px opacity-20"></div>
              <div class="text-14px">点击左侧组件添加到表单</div>
            </div>
          </div>
        </div>
      </section>

      <!-- Right: Property Panel -->
      <aside class="w-320px bg-[var(--bg-card)] border-l border-[var(--color-divider)] flex flex-col shrink-0 overflow-hidden">
        <div class="flex-1 p-5 overflow-y-auto custom-scrollbar">
          <div class="mb-6 flex items-center gap-2 text-14px font-600 text-[var(--color-text-primary)]">
            <div class="i-material-symbols:settings-suggest-outline-rounded text-18px text-primary"></div>属性配置
          </div>

          <template v-if="activeField">
            <a-form layout="vertical" class="premium-form">
              <a-form-item label="字段标题"><a-input v-model:value="activeField.label" placeholder="请输入标题" @input="markDirty" /></a-form-item>
              <a-form-item label="提示文字" v-if="['input', 'textarea', 'number', 'select', 'date'].includes(activeField.type)">
                <a-input v-model:value="activeField.placeholder" placeholder="请输入提示文字" @input="markDirty" />
              </a-form-item>
              <a-form-item label="默认值" v-if="['input', 'textarea', 'number', 'select', 'switch'].includes(activeField.type)">
                <a-input v-if="activeField.type === 'input' || activeField.type === 'textarea'" v-model:value="activeField.defaultValue" @input="markDirty" />
                <a-input-number v-else-if="activeField.type === 'number'" v-model:value="activeField.defaultValue" class="w-full" @change="markDirty" />
                <a-switch v-else-if="activeField.type === 'switch'" v-model:checked="activeField.defaultValue" @change="markDirty" />
                <a-select v-else-if="activeField.type === 'select'" v-model:value="activeField.defaultValue" allow-clear class="w-full" @change="markDirty">
                  <a-select-option v-for="opt in activeField.options || []" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
              </a-form-item>
              <div class="my-4 border-t border-[var(--color-divider)]"></div>
              <div class="space-y-4">
                <div class="flex justify-between items-center"><span class="text-13px font-500">是否必填</span><a-switch v-model:checked="activeField.required" @change="markDirty" /></div>
                <div class="flex justify-between items-center"><span class="text-13px font-500">禁用编辑</span><a-switch v-model:checked="activeField.disabled" @change="markDirty" /></div>
              </div>

              <div class="my-4 border-t border-[var(--color-divider)]"></div>
              <div class="text-13px font-500 mb-3">校验规则</div>
              <a-form-item label="正则表达式" v-if="['input', 'textarea'].includes(activeField.type)">
                <a-input v-model:value="(activeField.validation || (activeField.validation = {})).pattern" placeholder="例如: ^[0-9]+$" @input="markDirty" />
              </a-form-item>
              <div v-if="activeField.type === 'number'" class="flex gap-3">
                <a-form-item label="最小值"><a-input-number v-model:value="(activeField.validation || (activeField.validation = {})).min" class="w-full" @change="markDirty" /></a-form-item>
                <a-form-item label="最大值"><a-input-number v-model:value="(activeField.validation || (activeField.validation = {})).max" class="w-full" @change="markDirty" /></a-form-item>
              </div>
              <div v-if="['input', 'textarea'].includes(activeField.type)" class="flex gap-3">
                <a-form-item label="最小长度"><a-input-number v-model:value="(activeField.validation || (activeField.validation = {})).minLength" class="w-full" :min="0" @change="markDirty" /></a-form-item>
                <a-form-item label="最大长度"><a-input-number v-model:value="(activeField.validation || (activeField.validation = {})).maxLength" class="w-full" :min="0" @change="markDirty" /></a-form-item>
              </div>

              <template v-if="activeField.type === 'image'">
                <div class="my-4 border-t border-[var(--color-divider)]"></div>
                <div class="text-13px font-500 mb-3">图片配置</div>
                <div class="space-y-4">
                  <div class="flex justify-between items-center"><span class="text-13px font-500">仅允许相机拍摄</span><a-switch v-model:checked="(activeField.imageConfig || (activeField.imageConfig = { cameraOnly: false, maxCount: 9, maxSize: 10 })).cameraOnly" @change="markDirty" /></div>
                  <a-form-item label="最大上传张数"><a-input-number v-model:value="(activeField.imageConfig || {}).maxCount" :min="1" :max="50" class="w-full" @change="markDirty" /></a-form-item>
                </div>
              </template>

              <!-- Location Field Config (Amap) -->
              <template v-if="activeField.type === 'location'">
                <div class="my-4 border-t border-[var(--color-divider)]"></div>
                <div class="text-13px font-500 mb-3">高德地图定位配置</div>
                <div class="space-y-4">
                  <a-form-item label="地图缩放级别">
                    <a-input-number v-model:value="activeField.mapZoom" :min="3" :max="18" class="w-full" @change="markDirty" />
                    <div class="text-11px text-[var(--color-text-placeholder)] mt-1">数值越大地图越精细 (3-18)</div>
                  </a-form-item>
                  <div class="my-3 border-t border-[var(--color-divider)]"></div>
                  <div class="mb-2">
                    <div class="text-12px font-500 mb-3">经纬度回填配置</div>
                    <div class="text-11px text-[var(--color-text-placeholder)] mb-3">选择位置后自动将经纬度填入指定的文本字段</div>
                  </div>
                  <a-form-item label="经度回填字段">
                    <a-select
                      v-model:value="activeField.autoFillLngFieldId"
                      allow-clear
                      class="w-full"
                      @change="markDirty"
                    >
                      <a-select-option
                        v-for="f in otherFields"
                        :key="f.id"
                        :value="f.id"
                      >{{ f.label }} ({{ f.id }})</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item label="纬度回填字段">
                    <a-select
                      v-model:value="activeField.autoFillLatFieldId"
                      allow-clear
                      class="w-full"
                      @change="markDirty"
                    >
                      <a-select-option
                        v-for="f in otherFields"
                        :key="f.id"
                        :value="f.id"
                      >{{ f.label }} ({{ f.id }})</a-select-option>
                    </a-select>
                  </a-form-item>
                </div>
              </template>

              <template v-if="activeField.type === 'select' || activeField.type === 'radio' || activeField.type === 'checkbox'">
                <div class="my-4 border-t border-[var(--color-divider)]"></div>
                <div class="text-13px font-500 mb-3 flex justify-between items-center">
                  <span>选项配置</span>
                  <div class="flex gap-2">
                    <a-select v-model:value="(activeField.optionSource || (activeField.optionSource = { type: 'static' })).type" size="small" style="width:90px" @change="markDirty">
                      <a-select-option value="static">静态</a-select-option>
                      <a-select-option value="dict">数据字典</a-select-option>
                    </a-select>
                    <a-button v-if="activeField.optionSource?.type === 'static'" type="link" size="small" class="!p-0" @click="addOption(activeField!)">+ 添加</a-button>
                  </div>
                </div>
                <div v-if="activeField.optionSource?.type === 'dict'">
                  <a-input placeholder="字典编码" v-model:value="activeField.optionSource.dictCode" @input="markDirty" />
                  <div class="text-11px text-[var(--color-text-placeholder)] mt-1">运行时自动从系统字典加载选项</div>
                </div>
                <div v-if="activeField.optionSource?.type === 'static'" class="space-y-2">
                  <div v-for="(opt, idx) in activeField.options" :key="idx" class="flex items-center gap-2">
                    <a-input v-model:value="opt.label" placeholder="显示值" size="small" style="width:100px" @input="markDirty" />
                    <a-input v-model:value="opt.value" placeholder="实际值" size="small" style="width:100px" @input="markDirty" />
                    <a-button type="text" danger size="small" class="!p-0" @click="removeOption(activeField!, idx)"><div class="i-material-symbols:close-rounded text-16px"></div></a-button>
                  </div>
                </div>
              </template>

              <!-- Linkage Rules -->
              <template v-if="activeField && otherFields.length > 0">
                <div class="my-4 border-t border-[var(--color-divider)]"></div>
                <div class="text-13px font-500 mb-3 flex justify-between items-center">
                  <span>联动规则</span><a-button type="link" size="small" class="!p-0" @click="addLinkageRule(activeField!)">+ 添加规则</a-button>
                </div>
                <div v-if="!activeField.linkageRules || activeField.linkageRules.length === 0" class="text-12px text-[var(--color-text-placeholder)] mb-2">设置本字段的显示/隐藏条件</div>
                <div v-for="(rule, rIdx) in activeField.linkageRules" :key="rIdx" class="mb-4 p-3 bg-[var(--bg-page)] rd-6px border border-[var(--color-border)]">
                  <div class="flex justify-between items-center mb-2">
                    <span class="text-12px font-500">规则 {{ rIdx + 1 }}</span>
                    <a-button type="text" danger size="small" class="!p-0" @click="removeLinkageRule(activeField!, rIdx)"><div class="i-material-symbols:delete-outline-rounded text-14px"></div></a-button>
                  </div>
                  <div class="flex gap-2 mb-2">
                    <span class="text-12px">当</span>
                    <a-select v-if="rule.conditions[0]" v-model:value="rule.conditions[0].fieldId" size="small" style="width:100px" @change="markDirty">
                      <a-select-option v-for="f in otherFields" :key="f.id" :value="f.id">{{ f.label }}</a-select-option>
                    </a-select>
                    <a-select v-if="rule.conditions[0]" v-model:value="rule.conditions[0].operator" size="small" style="width:70px" @change="markDirty">
                      <a-select-option value="eq">等于</a-select-option><a-select-option value="neq">不等于</a-select-option>
                    </a-select>
                    <a-input v-if="rule.conditions[0]" v-model:value="rule.conditions[0].value" size="small" style="width:70px" placeholder="值" @input="markDirty" />
                  </div>
                  <div class="flex gap-2 items-center"><span class="text-12px">则</span>
                    <a-select v-model:value="rule.action" size="small" style="width:90px" @change="markDirty">
                      <a-select-option value="show">显示</a-select-option><a-select-option value="hide">隐藏</a-select-option>
                      <a-select-option value="clear">清空</a-select-option><a-select-option value="require">设为必填</a-select-option>
                    </a-select><span class="text-12px">本字段</span>
                  </div>
                </div>
              </template>
            </a-form>
          </template>
          <div v-else class="h-full flex-center flex-col gap-4 text-[var(--color-text-placeholder)]">
            <div class="i-material-symbols:ads-click-rounded text-40px opacity-10"></div>
            <div class="text-12px">选中画布中的字段进行配置</div>
          </div>
        </div>

        <div class="p-5 border-t border-[var(--color-divider)] bg-[var(--bg-card-alt)] max-h-[180px] overflow-y-auto custom-scrollbar shrink-0">
          <div class="text-12px font-600 mb-4 flex items-center gap-2">
            <div class="i-material-symbols:history-rounded text-16px text-primary"></div>历史版本 ({{ versions.length }})
          </div>
          <div class="space-y-3">
            <div v-for="v in versions" :key="v.versionNo" class="flex items-center justify-between group cursor-pointer">
              <div class="flex flex-col">
                <span class="text-12px font-500 group-hover:text-primary transition-colors">v{{ v.versionNo }}</span>
                <span class="text-11px opacity-50">{{ v.publishTime ? new Date(v.publishTime).toLocaleDateString() : '草稿' }}</span>
              </div>
              <div v-if="v.status === 1" class="i-material-symbols:check-circle-rounded text-14px text-success"></div>
            </div>
          </div>
        </div>
      </aside>
    </main>

    <main v-else class="flex-1 flex-center"><a-spin size="large" /></main>
  </div>

  <!-- Mobile Preview Modal -->
  <a-modal v-model:open="showPreview" title="移动端预览" width="420px" :footer="null" destroy-on-close>
    <div class="flex justify-center py-4">
      <div class="w-320px bg-white rd-12px shadow-lg overflow-hidden border border-gray-200">
        <div class="bg-gray-900 text-white text-center py-3 text-13px font-500">表单预览</div>
        <div class="p-4 space-y-4 max-h-550px overflow-y-auto">
          <template v-if="previewFields.length === 0"><div class="text-center text-gray-400 py-8 text-13px">暂无字段</div></template>
          <template v-for="field in previewFields" :key="field.id">
            <div class="space-y-1">
              <label class="text-13px font-500 text-gray-700">{{ field.label }}<span v-if="field.required" class="text-red-500 ml-1">*</span></label>
              <template v-if="field.type === 'input'"><a-input size="small" :placeholder="field.placeholder" /></template>
              <template v-else-if="field.type === 'textarea'"><a-textarea size="small" :rows="2" :placeholder="field.placeholder" /></template>
              <template v-else-if="field.type === 'number'"><a-input-number size="small" class="w-full" :placeholder="field.placeholder" /></template>
              <template v-else-if="field.type === 'select'">
                <a-select size="small" class="w-full" :placeholder="field.placeholder">
                  <a-select-option v-for="opt in field.options || []" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
              </template>
              <template v-else-if="field.type === 'radio'"><a-radio-group><a-radio v-for="opt in field.options || []" :key="opt.value" :value="opt.value">{{ opt.label }}</a-radio></a-radio-group></template>
              <template v-else-if="field.type === 'checkbox'"><a-checkbox-group><a-checkbox v-for="opt in field.options || []" :key="opt.value" :value="opt.value">{{ opt.label }}</a-checkbox></a-checkbox-group></template>
              <template v-else-if="field.type === 'switch'"><a-switch /></template>
              <template v-else-if="field.type === 'date'"><a-date-picker size="small" class="w-full" /></template>
              <template v-else-if="field.type === 'image'">
                <div class="w-full h-80px rd-4px border border-dashed border-gray-300 flex-center text-gray-400 text-12px">
                  <div class="i-material-symbols:camera-alt-outline text-20px mr-1"></div>
                  {{ field.imageConfig?.cameraOnly ? '拍照上传' : '点击上传' }}
                </div>
              </template>
              <template v-else-if="field.type === 'location'">
                <div class="h-36px rd-4px border border-gray-200 flex items-center px-3 text-12px text-gray-400">
                  <div class="i-material-symbols:location-on-outline-rounded text-16px mr-1"></div>点击选择位置
                </div>
              </template>
              <template v-else-if="field.type === 'divider'"><div class="h-1px bg-gray-200 w-full my-2"></div></template>
            </div>
          </template>
        </div>
      </div>
    </div>
  </a-modal>
</template>
