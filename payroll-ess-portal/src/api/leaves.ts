import client from './client';
import type { ApiResponse } from './auth';

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

interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const leaveApi = {
  list: (params: { employeeId: number; page?: number; size?: number }) =>
    client.get<ApiResponse<Page<LeaveRequest>>>('/leaves', { params }),
  create: (data: {
    employeeId: number; leaveType: string; startDate: string; endDate: string;
    startPeriod?: string; endPeriod?: string; daysCount: number; reason?: string;
  }) => client.post<ApiResponse<LeaveRequest>>('/leaves', data),
  cancel: (id: number) => client.put<ApiResponse<LeaveRequest>>(`/leaves/${id}/cancel`),
  getBalances: (employeeId: number, year: number) =>
    client.get<ApiResponse<LeaveBalance[]>>('/leaves/balances', { params: { employeeId, year } }),
};
