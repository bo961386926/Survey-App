import { request } from '../request';

/** get announcement list */
export function fetchGetAnnouncementList(params?: Api.Announcement.AnnouncementSearchParams) {
  return request<Api.Announcement.AnnouncementList>({
    url: '/announcement/page',
    method: 'get',
    params: {
      pageNum: params?.current || 1,
      pageSize: params?.size || 10,
      keyword: params?.keyword,
      type: params?.type,
      status: params?.status
    }
  });
}

/** get announcement detail */
export function fetchGetAnnouncementDetail(id: number | string) {
  return request<Api.Announcement.AnnouncementInfo>({
    url: `/announcement/${id}`,
    method: 'get'
  });
}

/** create announcement */
export function fetchCreateAnnouncement(data: Partial<Api.Announcement.AnnouncementInfo>) {
  return request<Api.Announcement.AnnouncementInfo>({
    url: '/announcement',
    method: 'post',
    data
  });
}

/** update announcement */
export function fetchUpdateAnnouncement(id: number | string, data: Partial<Api.Announcement.AnnouncementInfo>) {
  return request<Api.Announcement.AnnouncementInfo>({
    url: `/announcement/${id}`,
    method: 'put',
    data
  });
}

/** publish announcement */
export function fetchPublishAnnouncement(id: number | string) {
  return request({
    url: `/announcement/${id}/publish`,
    method: 'put'
  });
}

/** recall announcement */
export function fetchRecallAnnouncement(id: number | string) {
  return request({
    url: `/announcement/${id}/recall`,
    method: 'put'
  });
}

/** delete announcement */
export function fetchDeleteAnnouncement(id: number | string) {
  return request({
    url: `/announcement/${id}`,
    method: 'delete'
  });
}
