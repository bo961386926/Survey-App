import { request } from '../request';

/** Base URL prefix */
const TEMPLATE_BASE = '/template';

/** Field JSON schema type for design */
export interface FieldOption {
  label: string;
  value: string;
}

export interface LinkageCondition {
  fieldId: string;
  operator: 'eq' | 'neq' | 'in' | 'contains';
  value: any;
}

export interface LinkageRule {
  action: 'show' | 'hide' | 'clear' | 'require';
  conditions: LinkageCondition[];
}

export interface FieldValidation {
  min?: number;
  max?: number;
  pattern?: string;
  maxLength?: number;
  minLength?: number;
}

export interface OptionSource {
  type: 'static' | 'dict';
  dictCode?: string;
}

export interface ImageConfig {
  cameraOnly: boolean;
  maxCount: number;
  maxSize: number;
}

export interface FieldSchema {
  id: string;
  type: 'input' | 'textarea' | 'number' | 'select' | 'radio' | 'checkbox' | 'switch' | 'date' | 'image' | 'location' | 'divider' | 'grid';
  label: string;
  placeholder?: string;
  required: boolean;
  disabled: boolean;
  hidden: boolean;
  defaultValue?: any;
  validation?: FieldValidation;
  options?: FieldOption[];
  optionSource?: OptionSource;
  imageConfig?: ImageConfig;
  linkageRules?: LinkageRule[];
  /** Location field: default map zoom level */
  mapZoom?: number;
  /** Location field: auto-fill longitude to this field ID */
  autoFillLngFieldId?: string;
  /** Location field: auto-fill latitude to this field ID */
  autoFillLatFieldId?: string;
  /** Grid Layout: columns config */
  columns?: Array<{ span: number; fields: FieldSchema[] }>;
  /** Inline Input: prefix text */
  prefix?: string;
  /** Inline Input: suffix text */
  suffix?: string;
}

export interface TemplateSaveDraft {
  templateName: string;
  fields: FieldSchema[];
  rules?: string;
  linkageRules?: string;
}

/** get template list */
export function fetchGetTemplateList(params?: Api.Template.TemplateSearchParams) {
  return request<Api.Template.TemplateList>({
    url: `${TEMPLATE_BASE}/page`,
    method: 'get',
    params: {
      pageNum: params?.current || 1,
      pageSize: params?.size || 10,
      templateName: params?.templateName
    }
  });
}

/** get template detail */
export function fetchGetTemplateDetail(templateId: string | number) {
  return request<Api.Template.TemplateDetail>({
    url: `${TEMPLATE_BASE}/detail/${templateId}`,
    method: 'get'
  });
}

/** create template */
export function fetchCreateTemplate(data: Api.Template.TemplateEdit) {
  return request<Api.Template.TemplateDetail>({
    url: `${TEMPLATE_BASE}/create`,
    method: 'post',
    data
  });
}

/** update template */
export function fetchUpdateTemplate(templateId: string | number, data: Api.Template.TemplateEdit) {
  return request<Api.Template.TemplateDetail>({
    url: `${TEMPLATE_BASE}/update/${templateId}`,
    method: 'put',
    data
  });
}

/** delete template */
export function fetchDeleteTemplate(templateId: string | number) {
  return request({
    url: `${TEMPLATE_BASE}/delete/${templateId}`,
    method: 'delete'
  });
}

/** save template draft (save design state) */
export function fetchSaveTemplateDraft(templateId: string | number, data: TemplateSaveDraft) {
  return request({
    url: `${TEMPLATE_BASE}/draft/${templateId}`,
    method: 'put',
    data
  });
}

/** publish template (create version) */
export function fetchPublishTemplate(templateId: number, data: TemplateSaveDraft) {
  return request<Api.Template.TemplateVersion>({
    url: `${TEMPLATE_BASE}/${templateId}/publish`,
    method: 'post',
    data
  });
}

/** get template versions */
export function fetchGetTemplateVersions(templateId: number) {
  return request<Api.Template.TemplateVersion[]>({
    url: `${TEMPLATE_BASE}/${templateId}/versions`,
    method: 'get'
  });
}

/** preview template (mobile form preview data) */
export function fetchPreviewTemplate(templateId: number) {
  return request<{fields: FieldSchema[]}>({
    url: `${TEMPLATE_BASE}/${templateId}/preview`,
    method: 'get'
  });
}

/** get template list (simple) */
export function fetchGetTemplateSimpleList() {
  return request<Api.Template.TemplateInfo[]>({
    url: `${TEMPLATE_BASE}/list`,
    method: 'get'
  });
}

/** bind template to outfall type */
export function fetchBindOutfallType(data: {
  projectId: number;
  sectionId?: number;
  outfallType: string;
  templateId: number;
  templateVersionId: number;
}) {
  return request({
    url: `${TEMPLATE_BASE}/bind-outfall`,
    method: 'post',
    params: data
  });
}

/** get binding by outfall type */
export function fetchGetBinding(params: { projectId: number; sectionId?: number; outfallType: string }) {
  return request<{
    id: number;
    projectId: number;
    sectionId?: number;
    outfallType: string;
    templateId: number;
    templateVersionId: number;
  } | null>({
    url: `${TEMPLATE_BASE}/binding`,
    method: 'get',
    params
  });
}

/** get all bindings for project */
export function fetchGetBindings(projectId: number) {
  return request<Array<{
    id: number;
    projectId: number;
    sectionId?: number;
    outfallType: string;
    templateId: number;
    templateVersionId: number;
  }>>({
    url: `${TEMPLATE_BASE}/bindings`,
    method: 'get',
    params: { projectId }
  });
}

/** delete binding */
export function fetchDeleteBinding(bindingId: number) {
  return request({
    url: `${TEMPLATE_BASE}/binding/${bindingId}`,
    method: 'delete'
  });
}
