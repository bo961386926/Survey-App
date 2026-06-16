import { request } from '../request';

/** get project list */
export function fetchGetProjectList(params?: Api.Project.ProjectSearchParams) {
  // Map frontend params to backend expected params
  const backendParams = {
    pageNum: params?.current || 1,
    pageSize: params?.size || 10,
    projectName: params?.keyword || undefined,
    status: params?.status ? parseInt(String(params.status)) : undefined
  };
  
  return request<Api.Project.ProjectList>({
    url: '/project/page',
    method: 'get',
    params: backendParams
  });
}

/** get project detail */
export function fetchGetProjectDetail(projectId: string | number) {
  return request<Api.Project.ProjectDetail>({
    url: `/project/${projectId}`,
    method: 'get'
  });
}

/** create project */
export function fetchCreateProject(data: Api.Project.ProjectEditParams) {
  return request<Api.Project.ProjectDetail>({
    url: '/project',
    method: 'post',
    data
  });
}

/** update project */
export function fetchUpdateProject(projectId: string | number, data: Api.Project.ProjectEditParams) {
  return request<Api.Project.ProjectDetail>({
    url: `/project/${projectId}`,
    method: 'put',
    data
  });
}

/** delete project */
export function fetchDeleteProject(projectId: string | number) {
  return request({
    url: `/project/${projectId}`,
    method: 'delete'
  });
}

/** update project status */
export function fetchUpdateProjectStatus(projectId: string | number, status: number) {
  return request({
    url: `/project/${projectId}/status`,
    method: 'put',
    params: { targetStatus: status }
  });
}

/** archive project */
export function fetchArchiveProject(projectId: string | number) {
  return request({
    url: `/project/${projectId}/archive`,
    method: 'put'
  });
}

/** restore project (unarchive) */
export function fetchRestoreProject(projectId: string | number) {
  return request({
    url: `/project/${projectId}/restore`,
    method: 'put'
  });
}

/** get section list by projectId */
export function fetchGetSectionList(projectId: string | number) {
  return request<any[]>({
    url: '/section/list',
    method: 'get',
    params: { projectId }
  });
}

/** create section */
export function fetchCreateSection(data: { projectId: number | string; sectionName: string; sectionCode: string; description?: string }) {
  return request<any>({
    url: '/section',
    method: 'post',
    data
  });
}

/** update section */
export function fetchUpdateSection(id: number | string, data: { projectId: number | string; sectionName: string; sectionCode: string; description?: string }) {
  return request<any>({
    url: `/section/${id}`,
    method: 'put',
    data
  });
}

/** delete section */
export function fetchDeleteSection(id: number | string) {
  return request<any>({
    url: `/section/${id}`,
    method: 'delete'
  });
}

/** toggle section status */
export function fetchToggleSectionStatus(id: number | string, status: number) {
  return request<any>({
    url: `/section/${id}/status`,
    method: 'put',
    params: { status }
  });
}

/** set section manager */
export function fetchSetSectionManager(id: number | string, managerId: number | string) {
  return request<any>({
    url: `/section/${id}/manager`,
    method: 'put',
    params: { managerId }
  });
}

/** bind points to section */
export function fetchBindPointsToSection(id: number | string, pointIds: (number | string)[]) {
  return request<any>({
    url: `/section/${id}/bind-points`,
    method: 'post',
    data: pointIds
  });
}

/** unbind points from section */
export function fetchUnbindPointsFromSection(id: number | string, pointIds: (number | string)[]) {
  return request<any>({
    url: `/section/${id}/unbind-points`,
    method: 'post',
    data: pointIds
  });
}

/** get project member list */
export function fetchGetProjectMembers(projectId: string | number) {
  return request<any[]>({
    url: `/project/${projectId}/members`,
    method: 'get'
  });
}

/** add project member */
export function fetchAddProjectMember(projectId: string | number, params: { userId: string | number; role: string }) {
  return request<any>({
    url: `/project/${projectId}/members`,
    method: 'post',
    params
  });
}

/** remove project member */
export function fetchRemoveProjectMember(projectId: string | number, userId: string | number) {
  return request<any>({
    url: `/project/${projectId}/members/${userId}`,
    method: 'delete'
  });
}

/** update project member role */
export function fetchUpdateProjectMemberRole(projectId: string | number, userId: string | number, role: string) {
  return request<any>({
    url: `/project/${projectId}/members/${userId}/role`,
    method: 'put',
    params: { role }
  });
}

