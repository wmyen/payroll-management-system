import client from './client';
import { ApiResponse, Page } from '../types';

export interface Holiday {
  id: number;
  holidayDate: string;
  name: string;
  holidayType: 'HOLIDAY' | 'MAKEUP_WORKDAY';
  year: number;
}

export interface HolidayRequest {
  holidayDate: string;
  name: string;
  holidayType: string;
  year: number;
}

export const holidayApi = {
  list: (year: number) => client.get<ApiResponse<Holiday[]>>('/holidays', { params: { year } }),
  getById: (id: number) => client.get<ApiResponse<Holiday>>(`/holidays/${id}`),
  create: (data: HolidayRequest) => client.post<ApiResponse<Holiday>>('/holidays', data),
  update: (id: number, data: HolidayRequest) => client.put<ApiResponse<Holiday>>(`/holidays/${id}`, data),
  delete: (id: number) => client.delete<ApiResponse<void>>(`/holidays/${id}`),
};
