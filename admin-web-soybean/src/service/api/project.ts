import { request } from '../request';

/** get project list */
export function fetchGetProjectList(params?: Api.Project.ProjectSearchParams) {
  // Map frontend params to backend expected params
  const backendParams = {
    pageNum: params?.current || 1,
    pageSize: params?.size || 10,
    projectName: params?.keyword || undefined,
    status: params?.status ? parseInt(params.status) : undefined
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
