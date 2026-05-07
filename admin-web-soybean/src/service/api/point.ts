import { request } from '../request';

/** get point list */
export function fetchGetPointList(params?: Api.Point.PointSearchParams) {
  return request<Api.Point.PointList>({
    url: '/point/page',
    method: 'get',
    params: {
      pageNum: params?.current || 1,
      pageSize: params?.size || 10,
      projectId: params?.projectId,
      sectionId: params?.sectionId,
      status: params?.status,
      keyword: params?.keyword
    }
  });
}

/** get point detail */
export function fetchGetPointDetail(pointId: string | number) {
  return request<Api.Point.PointDetail>({
    url: `/point/${pointId}`,
    method: 'get'
  });
}

/** create point */
export function fetchCreatePoint(data: Api.Point.PointEdit) {
  return request<Api.Point.PointDetail>({
    url: '/point',
    method: 'post',
    data
  });
}

/** update point */
export function fetchUpdatePoint(pointId: string | number, data: Api.Point.PointEdit) {
  return request<Api.Point.PointDetail>({
    url: `/point/${pointId}`,
    method: 'put',
    data
  });
}

/** delete point */
export function fetchDeletePoint(pointId: string | number) {
  return request({
    url: `/point/${pointId}`,
    method: 'delete'
  });
}

/** batch assign points */
export function fetchBatchAssignPoints(data: { pointIds: number[]; assigneeId: number }) {
  return request({
    url: '/point/batch-assign',
    method: 'post',
    data
  });
}

/** import points from Excel */
export function fetchImportPoints(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request({
    url: '/point/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/** get point map data */
export function fetchGetPointMapData(params?: { projectId?: number; status?: number }) {
  return request<Api.Point.PointMapData[]>({
    url: '/point/map-data',
    method: 'get',
    params
  });
}
