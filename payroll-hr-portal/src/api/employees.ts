import client from './client';
import type { ApiResponse, Employee, Page } from '../types';

export const employeeApi = {
  search: (params: { name?: string; departmentId?: number; status?: string; page?: number; size?: number }) =>
    client.get<ApiResponse<Page<Employee>>>('/employees', { params }),
  getById: (id: number) =>
    client.get<ApiResponse<Employee>>(`/employees/${id}`),
  create: (data: Partial<Employee>) =>
    client.post<ApiResponse<Employee>>('/employees', data),
  update: (id: number, data: Partial<Employee>) =>
    client.put<ApiResponse<Employee>>(`/employees/${id}`, data),
};
