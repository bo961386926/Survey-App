import { request } from '../request';

/** get audit list */
export function fetchGetAuditList(params?: Api.Audit.AuditSearchParams) {
  return request<Api.Audit.AuditList>({
    url: '/audit/pending',
    method: 'get',
    params: {
      pageNum: params?.current || 1,
      pageSize: params?.size || 10,
      keyword: params?.keyword
    }
  });
}

/** get audit detail */
export function fetchGetAuditDetail(resultId: string | number) {
  return request<Api.Audit.AuditDetail>({
    url: `/audit/detail/${resultId}`,
    method: 'get'
  });
}

/** approve audit */
export function fetchApproveAudit(data: { resultId: number; comment?: string }) {
  return request({
    url: `/audit/pass`,
    method: 'post',
    params: {
      resultId: data.resultId,
      comment: data.comment || ''
    }
  });
}

/** reject audit */
export function fetchRejectAudit(data: { resultId: number; rejectReason: string }) {
  return request({
    url: `/audit/reject`,
    method: 'post',
    params: {
      resultId: data.resultId,
      comment: data.rejectReason
    }
  });
}

/** batch approve audits */
export function fetchBatchApproveAudits(data: { resultIds: number[] }) {
  return request({
    url: '/audit/batch-pass',
    method: 'post',
    params: {
      resultIds: data.resultIds.join(',')
    }
  });
}

/** get version history */
export function fetchGetVersionHistory(pointId: number) {
  return request<Api.Audit.VersionHistory[]>({
    url: `/audit/records?pointId=${pointId}`,
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
