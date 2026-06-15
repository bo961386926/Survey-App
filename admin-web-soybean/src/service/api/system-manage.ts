import { request } from '../request';

/** get role list */
export function fetchGetRoleList(params?: Api.SystemManage.RoleSearchParams) {
  return request<Api.SystemManage.RoleList>({
    url: '/systemManage/getRoleList',
    method: 'get',
    params
  });
}

/**
 * get all roles
 *
 * these roles are all enabled
 */
export function fetchGetAllRoles() {
  return request<Api.SystemManage.AllRole[]>({
    url: '/role/list',
    method: 'get'
  });
}

/** get user list */
export function fetchGetUserList(params?: Api.SystemManage.UserSearchParams) {
  return request<Api.SystemManage.UserList>({
    url: '/user/page',
    method: 'get',
    params
  });
}

/** get all users */
export function fetchGetAllUsers() {
  return request<Api.SystemManage.User[]>({
    url: '/user/list',
    method: 'get'
  });
}

/** create user */
export function fetchCreateUser(data: any) {
  return request({
    url: '/user/create',
    method: 'post',
    data
  });
}

/** update user */
export function fetchUpdateUser(data: any) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  });
}

/** delete user */
export function fetchDeleteUser(id: number | string) {
  return request({
    url: `/user/delete/${id}`,
    method: 'delete'
  });
}

/** update user status */
export function fetchUpdateUserStatus(id: number | string, status: number) {
  return request({
    url: `/user/status/${id}`,
    method: 'put',
    params: { status }
  });
}

/** get user detail by id */
export function fetchGetUserDetailById(id: string | number) {
  return request<Api.SystemManage.User>({
    url: `/user/${id}`,
    method: 'get'
  });
}

/** reset password */
export function fetchResetPassword(id: number | string, password?: string) {
  const defaultPwd = password || 'Survey@2026';
  return request({
    url: `/user/reset-password/${id}`,
    method: 'put',
    data: { newPassword: defaultPwd }
  });
}

/** get menu list */
export function fetchGetMenuList() {
  return request<Api.SystemManage.MenuList>({
    url: '/systemManage/getMenuList/v2',
    method: 'get'
  });
}

/** get all pages */
export function fetchGetAllPages() {
  return request<string[]>({
    url: '/systemManage/getAllPages',
    method: 'get'
  });
}

/** get menu tree */
export function fetchGetMenuTree() {
  return request<Api.SystemManage.MenuTree[]>({
    url: '/systemManage/getMenuTree',
    method: 'get'
  });
}

/** get operation log list */
export function fetchGetOperationLogList(params?: { current?: number; size?: number; module?: string; keyword?: string }) {
  return request<any>({
    url: '/log/operation/page',
    method: 'get',
    params: {
      pageNum: params?.current || 1,
      pageSize: params?.size || 10,
      module: params?.module,
      keyword: params?.keyword
    }
  });
}

/** get operation log trend */
export function fetchGetOperationLogTrend(params?: { startDate?: string; endDate?: string }) {
  return request<Record<string, number>>({
    url: '/log/operation/statistics/trend',
    method: 'get',
    params
  });
}
