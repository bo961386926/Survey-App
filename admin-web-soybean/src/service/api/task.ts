import { request } from '../request';

export const fetchTaskPage = (params: any) =>
  request({ url: '/task/page', method: 'get', params });

export const fetchTaskById = (id: number) =>
  request({ url: `/task/${id}`, method: 'get' });

export const createTask = (data: any) =>
  request({ url: '/task/create', method: 'post', data });

export const updateTask = (data: any) =>
  request({ url: '/task/update', method: 'put', data });

export const changeTaskStatus = (id: number, status: number) =>
  request({ url: `/task/${id}/status`, method: 'put', data: { status } });

export const assignTask = (id: number, assigneeId: number) =>
  request({ url: `/task/${id}/assign`, method: 'put', data: { assigneeId } });

export const deleteTask = (id: number) =>
  request({ url: `/task/${id}`, method: 'delete' });
