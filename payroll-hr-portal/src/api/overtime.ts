import client from './client';
import type { ApiResponse, Page } from '../types';

export interface OvertimeRecord {
  id: number;
  employee: { id: number; name: string; department: { id: number; name: string } | null } | null;
  overtimeDate: string;
  startTime: string;
  endTime: string;
  hours: number;
  overtimeType: 'WORKDAY' | 'REST_DAY' | 'HOLIDAY';
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  approverId: number | null;
  overtimePay: number | null;
}

export const overtimeApi = {
  list: (params: {
    employeeId?: number; departmentId?: number;
    startDate?: string; endDate?: string; page?: number; size?: number;
  }) => client.get<ApiResponse<Page<OvertimeRecord>>>('/overtime', { params }),
  create: (data: {
    employeeId: number; overtimeDate: string;
    startTime: string; endTime: string; overtimeType: string;
  }) => client.post<ApiResponse<OvertimeRecord>>('/overtime', data),
  approve: (id: number, approverId: number) =>
    client.put<ApiResponse<OvertimeRecord>>(`/overtime/${id}/approve`, null, { params: { approverId } }),
  reject: (id: number, approverId: number) =>
    client.put<ApiResponse<OvertimeRecord>>(`/overtime/${id}/reject`, null, { params: { approverId } }),
};
