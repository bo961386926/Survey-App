import { request } from '../request';

/** get template list */
export function fetchGetTemplateList(params?: Api.Template.TemplateSearchParams) {
  return request<Api.Template.TemplateList>({
    url: '/template/page',
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
    url: `/template/${templateId}`,
    method: 'get'
  });
}

/** create template */
export function fetchCreateTemplate(data: Api.Template.TemplateEdit) {
  return request<Api.Template.TemplateDetail>({
    url: '/template',
    method: 'post',
    data
  });
}

/** update template */
export function fetchUpdateTemplate(templateId: string | number, data: Api.Template.TemplateEdit) {
  return request<Api.Template.TemplateDetail>({
    url: `/template/${templateId}`,
    method: 'put',
    data
  });
}

/** delete template */
export function fetchDeleteTemplate(templateId: string | number) {
  return request({
    url: `/template/${templateId}`,
    method: 'delete'
  });
}

/** publish template (create version) */
export function fetchPublishTemplate(templateId: number) {
  return request<Api.Template.TemplateVersion>({
    url: `/template/${templateId}/publish`,
    method: 'post'
  });
}

/** get template versions */
export function fetchGetTemplateVersions(templateId: number) {
  return request<Api.Template.TemplateVersion[]>({
    url: `/template/${templateId}/versions`,
    method: 'get'
  });
}

/** preview template */
export function fetchPreviewTemplate(templateId: number) {
  return request<Api.Template.TemplatePreview>({
    url: `/template/${templateId}/preview`,
    method: 'get'
  });
}
