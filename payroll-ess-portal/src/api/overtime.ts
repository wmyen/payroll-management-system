import client from './client';
import type { ApiResponse } from './auth';

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

interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const overtimeApi = {
  list: (params: { employeeId: number; page?: number; size?: number }) =>
    client.get<ApiResponse<Page<OvertimeRecord>>>('/overtime', { params }),
  create: (data: {
    employeeId: number; overtimeDate: string;
    startTime: string; endTime: string; overtimeType: string;
  }) => client.post<ApiResponse<OvertimeRecord>>('/overtime', data),
};
