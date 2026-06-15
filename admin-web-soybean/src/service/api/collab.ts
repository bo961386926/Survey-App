import { request } from '../request';

/** get collab entry list */
export function fetchGetCollabList(params?: Api.Collab.CollabSearchParams) {
  return request<Api.Collab.CollabList>({
    url: '/collab/page',
    method: 'get',
    params: {
      pageNum: params?.current || 1,
      pageSize: params?.size || 10,
      keyword: params?.keyword
    }
  });
}

/** get collab entry detail */
export function fetchGetCollabDetail(id: number | string) {
  return request<Api.Collab.CollabEntryInfo>({
    url: `/collab/${id}`,
    method: 'get'
  });
}

/** create collab entry */
export function fetchCreateCollab(data: Partial<Api.Collab.CollabEntryInfo>) {
  return request<Api.Collab.CollabEntryInfo>({
    url: '/collab',
    method: 'post',
    data
  });
}

/** update collab entry */
export function fetchUpdateCollab(id: number | string, data: Partial<Api.Collab.CollabEntryInfo>) {
  return request<Api.Collab.CollabEntryInfo>({
    url: `/collab/${id}`,
    method: 'put',
    data
  });
}

/** revoke collab entry */
export function fetchRevokeCollab(id: number | string) {
  return request({
    url: `/collab/${id}/revoke`,
    method: 'put'
  });
}

/** reset collab entry token */
export function fetchResetCollabToken(id: number | string) {
  return request<string>({
    url: `/collab/${id}/reset-token`,
    method: 'put'
  });
}

/** get collab entry by token */
export function fetchGetCollabByToken(token: string) {
  return request<Api.Collab.CollabEntryInfo>({
    url: '/collab/by-token',
    method: 'get',
    params: { token }
  });
}

/** get collab access logs */
export function fetchGetCollabAccessLogs(entryId: number | string) {
  return request<any[]>({
    url: `/collab/${entryId}/logs`,
    method: 'get'
  });
}

/** issue collab JWT token */
export function fetchIssueCollabToken(id: number | string) {
  return request<string>({
    url: `/collab/${id}/issue-token`,
    method: 'post'
  });
}
