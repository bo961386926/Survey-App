import { request } from '../request';

/** get export task list */
export function fetchGetExportList(params?: Api.Export.ExportSearchParams) {
  return request<Api.Export.ExportList>({
    url: '/export/page',
    method: 'get',
    params: {
      pageNum: params?.current || 1,
      pageSize: params?.size || 10,
      taskType: params?.taskType,
      status: params?.status
    }
  });
}

/** create export task */
export function fetchCreateExport(data: Api.Export.ExportCreate) {
  return request<Api.Export.ExportTask>({
    url: '/export',
    method: 'post',
    data
  });
}

/** get export task detail */
export function fetchGetExportDetail(taskId: string | number) {
  return request<Api.Export.ExportTask>({
    url: `/export/${taskId}`,
    method: 'get'
  });
}

/** download export file */
export function fetchDownloadExport(taskId: string | number) {
  return request({
    url: `/export/${taskId}/download`,
    method: 'get',
    responseType: 'blob'
  });
}

/** delete export task */
export function fetchDeleteExport(taskId: string | number) {
  return request({
    url: `/export/${taskId}`,
    method: 'delete'
  });
}

/** export point list */
export function fetchExportPointList(params?: { projectId?: number; status?: number }) {
  return request({
    url: '/export/points',
    method: 'post',
    data: params,
    responseType: 'blob'
  });
}

/** export audit results */
export function fetchExportAuditResults(params?: { projectId?: number; status?: number }) {
  return request({
    url: '/export/audits',
    method: 'post',
    data: params,
    responseType: 'blob'
  });
}

/** generate PDF report */
export function fetchGeneratePDF(pointId: number) {
  return request({
    url: `/export/pdf/${pointId}`,
    method: 'post',
    responseType: 'blob'
  });
}
