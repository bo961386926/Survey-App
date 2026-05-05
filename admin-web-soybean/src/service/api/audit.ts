import { request } from '../request';

/** get audit list */
export function fetchGetAuditList(params?: Api.Audit.AuditSearchParams) {
  const statusMap: Record<string, number> = {
    pending: 0,
    approved: 1,
    rejected: 2
  };

  return request<Api.Audit.AuditList>({
    url: '/result/audit/page',
    method: 'get',
    params: {
      pageNum: params?.current || 1,
      pageSize: params?.size || 10,
      status: params?.status ? statusMap[params.status] : undefined,
      projectId: params?.projectId,
      keyword: params?.keyword
    }
  });
}

/** get audit detail */
export function fetchGetAuditDetail(resultId: string | number) {
  return request<Api.Audit.AuditDetail>({
    url: `/result/${resultId}`,
    method: 'get'
  });
}

/** approve audit */
export function fetchApproveAudit(data: { resultId: number; comment?: string }) {
  return request({
    url: `/result/audit/${data.resultId}/pass`,
    method: 'post',
    params: {
      auditRemark: data.comment
    }
  });
}

/** reject audit */
export function fetchRejectAudit(data: { resultId: number; rejectReason: string }) {
  return request({
    url: `/result/audit/${data.resultId}/reject`,
    method: 'post',
    params: {
      auditRemark: data.rejectReason
    }
  });
}

/** batch approve audits */
export function fetchBatchApproveAudits(data: { resultIds: number[] }) {
  return request({
    url: '/audit/batch-approve',
    method: 'post',
    data
  });
}

/** get version history */
export function fetchGetVersionHistory(pointId: number) {
  return request<Api.Audit.VersionHistory[]>({
    url: `/audit/versions/${pointId}`,
    method: 'get'
  });
}

/** get version diff */
export function fetchGetVersionDiff(params: { pointId: number; version1: number; version2: number }) {
  return request<Api.Audit.VersionDiff>({
    url: '/audit/version-diff',
    method: 'get',
    params
  });
}
