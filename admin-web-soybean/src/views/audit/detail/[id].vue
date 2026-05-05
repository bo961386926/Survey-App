<template>
  <div class="p-6 max-w-1200px mx-auto">
    <!-- Header -->
    <div class="mb-6">
      <a-breadcrumb>
        <a-breadcrumb-item>
          <router-link to="/audit/list">审核管理</router-link>
        </a-breadcrumb-item>
        <a-breadcrumb-item>审核详情</a-breadcrumb-item>
      </a-breadcrumb>
    </div>

    <!-- Review Header -->
    <div class="mb-6">
      <div class="flex items-start justify-between">
        <div>
          <h1 class="text-24px font-bold text-[var(--color-text-primary)] mb-2">滨江路1号排口 - 现场勘察记录</h1>
          <div class="text-13px text-[var(--color-text-secondary)] font-mono">
            ID: REV-2023-08-15-001 • 提交于 2023-08-15 14:30
          </div>
        </div>
        <div class="px-4 py-1.5 rounded-full text-14px font-medium flex items-center gap-2 bg-[rgba(255,125,0,0.1)] text-[var(--color-warning)] border border-[rgba(255,125,0,0.2)]">
          <div class="w-2 h-2 rounded-full bg-current animate-pulse"></div>
          等待审核
        </div>
      </div>
    </div>

    <!-- Meta Grid -->
    <div class="grid grid-cols-4 gap-4 mb-6">
      <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4 shadow-[var(--shadow-card)]">
        <div class="text-12px text-[var(--color-text-secondary)] mb-2">所属项目</div>
        <div class="text-18px font-bold text-[var(--color-text-primary)]">2023年度排污口排查</div>
        <div class="text-11px text-[var(--color-text-secondary)] mt-1">第一标段</div>
      </div>
      <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4 shadow-[var(--shadow-card)]">
        <div class="text-12px text-[var(--color-text-secondary)] mb-2">排口类型</div>
        <div class="text-18px font-bold text-[var(--color-primary)]">雨水排口</div>
        <div class="text-11px text-[var(--color-text-secondary)] mt-1">历史记录：2次勘察</div>
      </div>
      <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4 shadow-[var(--shadow-card)]">
        <div class="text-12px text-[var(--color-text-secondary)] mb-2">现场采集人</div>
        <div class="text-18px font-bold text-[var(--color-text-primary)]">张三</div>
        <div class="text-11px text-[var(--color-text-secondary)] mt-1">联系方式：138****1234</div>
      </div>
      <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-4 shadow-[var(--shadow-card)]">
        <div class="text-12px text-[var(--color-text-secondary)] mb-2">数据版本</div>
        <div class="text-18px font-bold text-[var(--color-text-primary)]">V2.1</div>
        <div class="text-11px text-[var(--color-text-secondary)] mt-1">基于模板：通用排口V1.0</div>
      </div>
    </div>

    <!-- Content Grid -->
    <div class="flex flex-col lg:flex-row gap-6">
      <!-- Main Info -->
      <div class="flex-2 flex flex-col gap-6">
        <!-- Location Details -->
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5 shadow-[var(--shadow-card)]">
          <div class="flex items-center justify-between mb-4">
            <div class="text-16px font-bold text-[var(--color-text-primary)] flex items-center gap-2">
              <div class="w-7 h-7 rounded-md flex items-center justify-center text-14px bg-[rgba(22,119,255,0.1)] text-[var(--color-primary)]">
                <EnvironmentOutlined />
              </div>
              位置信息
            </div>
            <a href="#" class="text-13px text-[var(--color-primary)] hover:underline">在地图中查看</a>
          </div>
          
          <table class="w-full">
            <tbody>
              <tr class="border-b border-[var(--color-divider)]">
                <td class="w-30% py-3 text-13px text-[var(--color-text-secondary)]">登记位置</td>
                <td class="py-3 text-14px text-[var(--color-text-primary)]">滨江区滨江路与江虹路交叉口向东50米</td>
              </tr>
              <tr class="border-b border-[var(--color-divider)]">
                <td class="py-3 text-13px text-[var(--color-text-secondary)]">GPS 坐标</td>
                <td class="py-3 text-13px text-[var(--color-text-primary)] font-mono">120.213456, 30.123456</td>
              </tr>
              <tr>
                <td class="py-3 text-13px text-[var(--color-text-secondary)]">经纬度偏移</td>
                <td class="py-3 text-14px">
                  <span class="inline-flex items-center px-2 py-0.5 rounded-4px text-12px bg-[rgba(0,180,42,0.1)] text-[var(--color-success)]">
                    正常 (偏移 2.5米)
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Form Details -->
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5 shadow-[var(--shadow-card)]">
          <div class="flex items-center justify-between mb-4">
            <div class="text-16px font-bold text-[var(--color-text-primary)] flex items-center gap-2">
              <div class="w-7 h-7 rounded-md flex items-center justify-center text-14px bg-[rgba(139,92,246,0.1)] text-[#8B5CF6]">
                <FileTextOutlined />
              </div>
              表单详情
            </div>
            <a href="#" class="text-13px text-[var(--color-primary)] hover:underline">对比上一版本</a>
          </div>

          <table class="w-full">
            <tbody>
              <tr class="border-b border-[var(--color-divider)]">
                <td class="w-30% py-3 text-13px text-[var(--color-text-secondary)]">排口名称</td>
                <td class="py-3 text-14px text-[var(--color-text-primary)]">滨江路1号排口</td>
              </tr>
              <tr class="border-b border-[var(--color-divider)]">
                <td class="py-3 text-13px text-[var(--color-text-secondary)]">管径 (mm)</td>
                <td class="py-3 text-14px text-[var(--color-text-primary)]">800</td>
              </tr>
              <tr class="border-b border-[var(--color-divider)]">
                <td class="py-3 text-13px text-[var(--color-text-secondary)]">材质</td>
                <td class="py-3 text-14px text-[var(--color-text-primary)]">HDPE双壁波纹管</td>
              </tr>
              <tr class="border-b border-[var(--color-divider)]">
                <td class="py-3 text-13px text-[var(--color-text-secondary)]">排入水体</td>
                <td class="py-3 text-14px text-[var(--color-text-primary)]">钱塘江</td>
              </tr>
              <tr class="border-b border-[var(--color-divider)]">
                <td class="py-3 text-13px text-[var(--color-text-secondary)]">是否有污水混入</td>
                <td class="py-3 text-14px">
                  <span class="inline-flex items-center px-2 py-0.5 rounded-4px text-12px bg-[rgba(255,125,0,0.1)] text-[var(--color-warning)]">
                    是
                  </span>
                </td>
              </tr>
              <tr>
                <td class="py-3 text-13px text-[var(--color-text-secondary)]">周边环境描述</td>
                <td class="py-3 text-14px text-[var(--color-text-primary)]">排口附近有少量生活垃圾堆积，水体呈现轻微浑浊，无明显异味。</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Photos -->
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5 shadow-[var(--shadow-card)]">
          <div class="flex items-center justify-between mb-4">
            <div class="text-16px font-bold text-[var(--color-text-primary)] flex items-center gap-2">
              <div class="w-7 h-7 rounded-md flex items-center justify-center text-14px bg-[rgba(0,180,42,0.1)] text-[var(--color-success)]">
                <CameraOutlined />
              </div>
              现场照片
            </div>
          </div>
          
          <div class="grid grid-cols-3 gap-3">
            <div class="aspect-4/3 rounded-8px overflow-hidden bg-[var(--bg-hover)] border border-dashed border-[var(--color-border)] flex flex-col items-center justify-center text-[var(--color-text-secondary)] gap-2 cursor-pointer hover:border-[var(--color-primary)] transition-all">
              <PictureOutlined class="text-24px" />
              <span class="text-12px">排口正面照片</span>
            </div>
            <div class="aspect-4/3 rounded-8px overflow-hidden bg-[var(--bg-hover)] border border-dashed border-[var(--color-border)] flex flex-col items-center justify-center text-[var(--color-text-secondary)] gap-2 cursor-pointer hover:border-[var(--color-primary)] transition-all">
              <PictureOutlined class="text-24px" />
              <span class="text-12px">周边环境照片</span>
            </div>
            <div class="aspect-4/3 rounded-8px overflow-hidden bg-[var(--bg-hover)] border border-dashed border-[var(--color-border)] flex flex-col items-center justify-center text-[var(--color-text-secondary)] gap-2 cursor-pointer hover:border-[var(--color-primary)] transition-all">
              <PictureOutlined class="text-24px" />
              <span class="text-12px">水质特写照片</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar Info -->
      <div class="flex-1 flex flex-col gap-6">
        <!-- Timeline -->
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5 shadow-[var(--shadow-card)]">
          <div class="text-16px font-bold text-[var(--color-text-primary)] flex items-center gap-2 mb-4">
            <div class="w-7 h-7 rounded-md flex items-center justify-center text-14px bg-[rgba(255,125,0,0.1)] text-[var(--color-warning)]">
              <ClockCircleOutlined />
            </div>
            流转记录
          </div>
          
          <div class="pl-4 relative">
            <div class="absolute left-1.5 top-0 bottom-0 w-0.5 bg-[var(--color-divider)]"></div>
            
            <div class="relative pb-5">
              <div class="absolute -left-15px top-1 w-2.5 h-2.5 rounded-full border-2 border-[var(--bg-card)] bg-[var(--color-warning)]"></div>
              <div class="bg-[var(--bg-hover)] rounded-8px p-3">
                <div class="text-13px font-bold text-[var(--color-text-primary)] mb-1">重新提交审核</div>
                <div class="text-12px text-[var(--color-text-secondary)] mb-1">采集人: 张三</div>
                <div class="text-11px text-[var(--color-text-disabled)]">2023-08-15 14:30:22</div>
              </div>
            </div>
            
            <div class="relative pb-5">
              <div class="absolute -left-15px top-1 w-2.5 h-2.5 rounded-full border-2 border-[var(--bg-card)] bg-[var(--color-danger)]"></div>
              <div class="bg-[var(--bg-hover)] rounded-8px p-3">
                <div class="text-13px font-bold text-[var(--color-text-primary)] mb-1">审核驳回</div>
                <div class="text-12px text-[var(--color-text-secondary)] mb-1">审核人: 李经理<br>原因: 照片不清晰，缺少正面全貌图。</div>
                <div class="text-11px text-[var(--color-text-disabled)]">2023-08-14 16:45:10</div>
              </div>
            </div>
            
            <div class="relative">
              <div class="absolute -left-15px top-1 w-2.5 h-2.5 rounded-full border-2 border-[var(--bg-card)] bg-[var(--color-success)]"></div>
              <div class="bg-[var(--bg-hover)] rounded-8px p-3">
                <div class="text-13px font-bold text-[var(--color-text-primary)] mb-1">首次提交</div>
                <div class="text-12px text-[var(--color-text-secondary)] mb-1">采集人: 张三</div>
                <div class="text-11px text-[var(--color-text-disabled)]">2023-08-14 10:20:05</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Review Form -->
        <div class="bg-[var(--bg-card)] border border-[var(--color-border)] rounded-8px p-5 shadow-[var(--shadow-card)]">
          <div class="text-16px font-bold text-[var(--color-text-primary)] flex items-center gap-2 mb-4">
            <div class="w-7 h-7 rounded-md flex items-center justify-center text-14px bg-[rgba(22,119,255,0.1)] text-[var(--color-primary)]">
              <EditOutlined />
            </div>
            审核操作
          </div>
          
          <a-form layout="vertical">
            <a-form-item label="审核结果">
              <a-radio-group v-model:value="reviewForm.result" class="w-full flex gap-3 custom-radio-group">
                <a-radio-button value="approve" class="flex-1 text-center rounded-8px! h-10 leading-10!">通过</a-radio-button>
                <a-radio-button value="reject" class="flex-1 text-center rounded-8px! h-10 leading-10!">驳回</a-radio-button>
              </a-radio-group>
            </a-form-item>
            
            <a-form-item label="审核意见">
              <a-textarea 
                v-model:value="reviewForm.comments" 
                :rows="4" 
                placeholder="请输入审核意见（驳回时必填）" 
                class="rounded-8px"
              />
            </a-form-item>
            
            <a-form-item v-if="reviewForm.result === 'reject'" label="快捷回复">
              <a-select v-model:value="reviewForm.template" placeholder="选择快捷模板" class="w-full">
                <a-select-option value="1">照片不清晰或缺失</a-select-option>
                <a-select-option value="2">位置定位偏移过大</a-select-option>
                <a-select-option value="3">排口类型判定有误</a-select-option>
                <a-select-option value="4">表单必填项未填写完整</a-select-option>
              </a-select>
            </a-form-item>

            <div class="flex gap-3 mt-6 pt-4 border-t border-[var(--color-divider)]">
              <a-button @click="router.back()" class="h-10 rounded-6px flex-1">返回</a-button>
              <a-button type="primary" @click="submitReview" class="h-10 rounded-6px flex-2" style="background: var(--color-primary)">提交</a-button>
            </div>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { 
  EnvironmentOutlined, 
  FileTextOutlined, 
  CameraOutlined, 
  PictureOutlined,
  ClockCircleOutlined,
  EditOutlined 
} from '@ant-design/icons-vue';

defineOptions({ name: 'AuditDetail' });

const router = useRouter();

const reviewForm = ref({
  result: 'approve',
  comments: '',
  template: undefined
});

const submitReview = () => {
  console.log('Submitting review:', reviewForm.value);
  router.push('/audit/list');
};
</script>

<style scoped>
.custom-radio-group :deep(.ant-radio-button-wrapper) {
  background: var(--bg-input);
  border-color: var(--color-border);
  color: var(--color-text-secondary);
}

.custom-radio-group :deep(.ant-radio-button-wrapper-checked) {
  border-color: var(--color-primary);
  background: rgba(22,119,255,0.1);
  color: var(--color-primary);
}

.custom-radio-group :deep(.ant-radio-button-wrapper-checked[value="approve"]) {
  border-color: var(--color-success);
  background: rgba(0,180,42,0.1);
  color: var(--color-success);
}

.custom-radio-group :deep(.ant-radio-button-wrapper-checked[value="reject"]) {
  border-color: var(--color-danger);
  background: rgba(245,63,63,0.1);
  color: var(--color-danger);
}
</style>
