import client from './client';
import type { ApiResponse, Department } from '../types';

export const departmentApi = {
  getTree: () => client.get<ApiResponse<Department[]>>('/departments'),
  create: (data: { name: string; parentId: number | null }) =>
    client.post<ApiResponse<Department>>('/departments', data),
  delete: (id: number) => client.delete(`/departments/${id}`),
};
