import { request } from '../request';

export const fetchTaskPage = (params: any) =>
  request({ url: '/task/page', method: 'get', params });

export const fetchTaskById = (id: string | number) =>
  request({ url: `/task/${id}`, method: 'get' });

export const createTask = (data: any) =>
  request({ url: '/task/create', method: 'post', data });

export const updateTask = (data: any) =>
  request({ url: '/task/update', method: 'put', data });

export const changeTaskStatus = (id: string | number, status: number) =>
  request({ url: `/task/${id}/status`, method: 'put', params: { status } });

export const assignTask = (id: string | number, assigneeId: string | number) =>
  request({ url: `/task/${id}/assign`, method: 'put', params: { assigneeId } });

export const deleteTask = (id: string | number) =>
  request({ url: `/task/${id}`, method: 'delete' });
