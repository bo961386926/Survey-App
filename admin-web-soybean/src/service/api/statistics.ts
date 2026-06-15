import { request } from '../request';

/** get system overview statistics */
export function fetchGetSystemOverview() {
  return request<Api.Statistics.SystemOverview>({
    url: '/statistics/overview',
    method: 'get'
  });
}

/** get user statistics */
export function fetchGetUserStatistics() {
  return request<Api.Statistics.UserStats>({
    url: '/statistics/users',
    method: 'get'
  });
}

/** get project statistics */
export function fetchGetProjectStatistics() {
  return request<Api.Statistics.ProjectStats>({
    url: '/statistics/projects',
    method: 'get'
  });
}
