import client from './client';
import type { ApiResponse, Page } from '../types';

export interface LeaveRequest {
  id: number;
  employee: { id: number; name: string } | null;
  leaveType: string;
  startDate: string;
  endDate: string;
  startPeriod: string | null;
  endPeriod: string | null;
  daysCount: number;
  reason: string | null;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  approverId: number | null;
  approvedAt: string | null;
  rejectedReason: string | null;
}

export interface LeaveBalance {
  id: number;
  employee: { id: number; name: string } | null;
  leaveType: string;
  year: number;
  totalDays: number;
  usedDays: number;
}

export const leaveApi = {
  list: (params: {
    employeeId?: number; leaveType?: string; status?: string;
    startDate?: string; endDate?: string; page?: number; size?: number;
  }) => client.get<ApiResponse<Page<LeaveRequest>>>('/leaves', { params }),
  create: (data: {
    employeeId: number; leaveType: string; startDate: string; endDate: string;
    startPeriod?: string; endPeriod?: string; daysCount: number; reason?: string;
  }) => client.post<ApiResponse<LeaveRequest>>('/leaves', data),
  approve: (id: number, approverId: number) =>
    client.put<ApiResponse<LeaveRequest>>(`/leaves/${id}/approve`, null, { params: { approverId } }),
  reject: (id: number, approverId: number, reason: string) =>
    client.put<ApiResponse<LeaveRequest>>(`/leaves/${id}/reject`, { reason }, { params: { approverId } }),
  cancel: (id: number) => client.put<ApiResponse<LeaveRequest>>(`/leaves/${id}/cancel`),
  getBalances: (employeeId: number, year: number) =>
    client.get<ApiResponse<LeaveBalance[]>>('/leaves/balances', { params: { employeeId, year } }),
  initBalances: (year: number) =>
    client.post<ApiResponse<void>>('/leaves/balances/init', null, { params: { year } }),
};
