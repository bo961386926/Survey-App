<template>
  <div class="h-full flex flex-col bg-[var(--bg-page)] p-6">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-24px font-bold text-[var(--color-text-primary)] mb-1">表单模板</h1>
        <p class="text-14px text-[var(--color-text-secondary)]">配置勘查表单字段，支持多种字段类型与联动逻辑</p>
      </div>
      <a-button v-if="hasPermission('template:create')" type="primary" @click="handleCreate" class="rounded-6px">
        新建模板
      </a-button>
    </div>

    <!-- Template Cards Grid -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <a-spin size="large" />
    </div>
    <div v-else-if="templateList.length === 0" class="text-center py-20 text-[var(--color-text-secondary)]">
      <p class="text-16px mb-4">暂无模板</p>
      <a-button v-if="hasPermission('template:create')" type="primary" @click="handleCreate">创建第一个模板</a-button>
    </div>
    <div v-else class="grid grid-cols-2 gap-6">
      <div
        v-for="template in templateList"
        :key="template.id"
        class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5 shadow-[var(--shadow-card)] hover:shadow-lg transition-all cursor-pointer"
        @click="handleDesign(template.id)"
      >
        <!-- Template Header -->
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-start gap-3">
            <div class="w-40px h-40px rounded-8px bg-[rgba(22,119,255,0.1)] flex items-center justify-center">
              <FileTextOutlined class="text-20px text-[var(--color-primary)]" />
            </div>
            <div>
              <div class="text-16px font-600 text-[var(--color-text-primary)] mb-1">{{ template.name }}</div>
              <div class="text-13px text-[var(--color-text-secondary)]">
                {{ template.fieldsCount }} 个字段 · {{ template.updatedAt }}
              </div>
            </div>
          </div>
          <div v-if="template.showActions" class="flex gap-2">
            <a-button type="text" size="small" @click.stop="handlePreview(template)">
              <template #icon><EyeOutlined class="text-[var(--color-text-secondary)]" /></template>
            </a-button>
            <a-button type="text" size="small" @click.stop="handleVersions(template)">
              <template #icon><HistoryOutlined class="text-[var(--color-text-secondary)]" /></template>
            </a-button>
            <a-button type="text" size="small" @click.stop="handleEdit(template)">
              <template #icon><EditOutlined class="text-[var(--color-text-secondary)]" /></template>
            </a-button>
            <a-button type="text" size="small" danger @click.stop="handleDelete(template)">
              <template #icon><DeleteOutlined /></template>
            </a-button>
          </div>
        </div>

        <!-- Fields Tags -->
        <div class="flex flex-wrap gap-2">
          <a-tag
            v-for="(field, index) in template.fields.slice(0, 5)"
            :key="index"
            class="px-2 py-0.5 text-12px"
          >
            <component :is="field.icon" class="text-12px mr-1" />
            {{ field.name }}
            <span v-if="field.required" class="text-[var(--color-danger)] ml-0.5">*</span>
          </a-tag>
          <a-tag v-if="template.fields.length > 5" class="px-2 py-0.5 text-12px">
            +{{ template.fields.length - 5 }}
          </a-tag>
        </div>
      </div>
    </div>

    <!-- Version History Modal -->
    <a-modal
      v-model:visible="versionModalVisible"
      title="模板版本历史"
      :footer="null"
      width="700px"
    >
      <div v-if="selectedTemplate" class="space-y-4">
        <div class="text-14px text-[var(--color-text-secondary)] mb-4">
          模板：{{ selectedTemplate.name }}
        </div>
        
        <div v-if="versionLoading" class="flex justify-center py-8">
          <a-spin />
        </div>
        <div v-else class="space-y-3">
          <div
            v-for="version in versionList"
            :key="version.versionNo"
            class="border border-[var(--color-border)] rounded-6px p-4 hover:border-[var(--color-primary)] transition-all"
          >
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-3">
                <span class="text-16px font-600 text-[var(--color-text-primary)]">v{{ version.versionNo }}</span>
                <a-tag v-if="version.status === 'published'" color="success" class="text-12px">已发布</a-tag>
                <a-tag v-else color="default" class="text-12px">草稿</a-tag>
                <a-tag v-if="version.isCurrent" color="processing" class="text-12px">当前版本</a-tag>
              </div>
              <span class="text-12px text-[var(--color-text-secondary)]">{{ version.createdAt }}</span>
            </div>
            
            <div class="text-13px text-[var(--color-text-secondary)] mb-3">
              {{ version.fieldCount }} 个字段 · 创建人：{{ version.createdBy }}
            </div>
            
            <div class="flex gap-2">
              <a-button size="small" @click="handleViewVersion(version)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>
              <a-button 
                v-if="version.status === 'draft'"
                size="small" 
                type="primary" 
                @click="handlePublishVersion(version)"
              >
                <template #icon><CloudUploadOutlined /></template>
                发布
              </a-button>
              <a-button 
                v-if="!version.isCurrent && version.status === 'published'"
                size="small"
                @click="handleRollbackVersion(version)"
              >
                <template #icon><RollbackOutlined /></template>
                回退
              </a-button>
            </div>
          </div>
          
          <div v-if="versionList.length === 0" class="text-center py-8 text-[var(--color-text-secondary)]">
            暂无版本历史
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { useAuth } from '@/hooks/common/auth';
import {
  PlusOutlined,
  FileTextOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  HistoryOutlined,
  CloudUploadOutlined,
  RollbackOutlined,
  FontSizeOutlined,
  DownOutlined,
  CheckSquareOutlined,
  NumberOutlined,
  SwitcherOutlined,
  PictureOutlined
} from '@ant-design/icons-vue';

// 字段类型到图标的映射
const fieldIconMap: Record<string, any> = {
  input: FontSizeOutlined,
  textarea: FileTextOutlined,
  number: NumberOutlined,
  select: DownOutlined,
  radio: CheckSquareOutlined,
  checkbox: CheckSquareOutlined,
  switch: SwitcherOutlined,
  date: HistoryOutlined,
  image: PictureOutlined
};
import { fetchGetTemplateList, fetchGetTemplateVersions, fetchPublishTemplate, fetchDeleteTemplate, fetchCreateTemplate } from '@/service/api';

defineOptions({ name: 'TemplateList' });

const router = useRouter();
const { hasPermission } = useAuth();

const loading = ref(false);
const versionModalVisible = ref(false);
const versionLoading = ref(false);
const selectedTemplate = ref<any>(null);

const templateList = ref<any[]>([]);
const versionList = ref<Array<{
  versionNo: number;
  status: 'draft' | 'published';
  isCurrent: boolean;
  fieldCount: number;
  createdBy: string;
  createdAt: string;
}>>([]);

// Load template list
const loadTemplateList = async () => {
  loading.value = true;
  try {
    const response = await fetchGetTemplateList({ current: 1, size: 100 });
    
    if (response.data) {
      templateList.value = (response.data.records || []).map((tpl: any) => {
        let fields: any[] = [];
        try {
          if (tpl.fieldsJson) fields = JSON.parse(tpl.fieldsJson);
        } catch { /* ignore parse error */ }
        return {
          id: tpl.id,
          name: tpl.templateName,
          fieldsCount: fields.length,
          updatedAt: tpl.updateTime ? new Date(tpl.updateTime).toLocaleDateString() : '-',
          showActions: true,
          fields: fields.map((f: any) => ({ name: f.label, required: f.required, icon: fieldIconMap[f.type] || FileTextOutlined }))
        };
      });
    }
  } catch (error) {
    console.error('Failed to load templates:', error);
    message.error('加载模板列表失败');
  } finally {
    loading.value = false;
  }
};

// Open version history modal
const handleVersions = async (template: any) => {
  selectedTemplate.value = template;
  versionModalVisible.value = true;
  
  // Load version history
  versionLoading.value = true;
  try {
    const templateId = Number(template.id) || template.id;
    const response = await fetchGetTemplateVersions(templateId);
    
    if (response.data) {
      versionList.value = (response.data || []).map((v: any) => ({
        versionNo: v.versionNo || 1,
        status: v.status || 'draft',
        isCurrent: v.isCurrent || false,
        fieldCount: v.fieldCount || 0,
        createdBy: v.createdBy || '-',
        createdAt: v.createdAt || '-'
      }));
    }
  } catch (error) {
    console.error('Failed to load versions:', error);
    message.error('加载版本历史失败');
  } finally {
    versionLoading.value = false;
  }
};

// View version details
const handleViewVersion = (version: any) => {
  message.info(`查看版本 v${version.versionNo} 详情`);
  // In production, navigate to version detail or open preview
};

// Publish version
const handlePublishVersion = async (version: any) => {
  Modal.confirm({
    title: '确认发布',
    content: `确定要发布模板版本 v${version.versionNo} 吗？发布后将作为当前使用版本。`,
    okText: '确认发布',
    cancelText: '取消',
    onOk: async () => {
      try {
        const templateId = Number(selectedTemplate.value.id.replace('TPL-', '')) || selectedTemplate.value.id;
        const response = await fetchPublishTemplate(templateId, {
          templateName: selectedTemplate.value.name,
          fields: version.fields || [],
          rules: '{}',
          linkageRules: '[]'
        });
        
        if (!response.error) {
          message.success('发布成功');
          // Reload versions
          handleVersions(selectedTemplate.value);
          // Reload template list
          loadTemplateList();
        } else {
          message.error('发布失败');
        }
      } catch (error) {
        console.error('Publish failed:', error);
        message.error('发布操作失败');
      }
    }
  });
};

// Rollback to version
const handleRollbackVersion = (version: any) => {
  Modal.confirm({
    title: '确认回退',
    content: `确定要回退到版本 v${version.versionNo} 吗？当前版本将被替换。`,
    okText: '确认回退',
    cancelText: '取消',
    okType: 'danger',
    onOk: () => {
      message.success(`已回退到版本 v${version.versionNo}`);
      // In production, call rollback API
      handleVersions(selectedTemplate.value);
      loadTemplateList();
    }
  });
};

const handleDesign = (id: string) => {
  router.push(`/template/design/${id}`);
};

const handleCreate = async () => {
  // 先创建模板记录
  loading.value = true;
  try {
    const response = await fetchCreateTemplate({
      templateName: '新模板',
      templateCode: `TPL_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      description: '新创建的模板'
    } as any);
    
    if (!response.error && response.data) {
      const templateId = response.data.id || (response.data as any).templateId;
      message.success('模板创建成功，进入设计器');
      // 跳转到新创建的模板设计器
      router.push(`/template/design/${templateId}`);
    } else {
      message.error((response as any).message || '创建模板失败');
      loading.value = false;
    }
  } catch (error) {
    console.error('Create template failed:', error);
    message.error('创建模板失败：' + ((error as any).message || '未知错误'));
    loading.value = false;
  }
};

const handlePreview = (template: any) => {
  message.info(`预览模板：${template.name}`);
  // In production, open preview modal
};

const handleEdit = (template: any) => {
  router.push(`/template/design/${template.id}`);
};

const handleDelete = (template: any) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除模板「${template.name}」吗？此操作不可恢复。`,
    okText: '确认删除',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        const response = await fetchDeleteTemplate(template.id);
        
        if (!response.error) {
          message.success('删除成功');
          loadTemplateList();
        } else {
          message.error('删除失败');
        }
      } catch (error) {
        console.error('Delete failed:', error);
        message.error('删除操作失败');
      }
    }
  });
};

onMounted(() => {
  loadTemplateList();
});
</script>
