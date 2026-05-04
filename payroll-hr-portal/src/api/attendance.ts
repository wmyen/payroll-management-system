import client from './client';
import { ApiResponse, Page } from '../types';

export interface AttendanceRecord {
  id: number;
  employee: { id: number; name: string; department: { id: number; name: string } | null } | null;
  recordDate: string;
  clockIn: string | null;
  clockOut: string | null;
  workHours: number | null;
  status: 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'ABSENT' | 'DAY_OFF' | 'HOLIDAY';
  remark: string | null;
}

export const attendanceApi = {
  search: (params: {
    employeeId?: number; departmentId?: number;
    startDate?: string; endDate?: string; page?: number; size?: number;
  }) => client.get<ApiResponse<Page<AttendanceRecord>>>('/attendance', { params }),
  getById: (id: number) => client.get<ApiResponse<AttendanceRecord>>(`/attendance/${id}`),
  importCsv: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return client.post<ApiResponse<AttendanceRecord[]>>('/attendance/import', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};
