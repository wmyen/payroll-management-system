import client from './client';
import { ApiResponse } from '../types';

export interface PayrollPeriod {
  id: number;
  year: number;
  month: number;
  startDate: string;
  endDate: string;
  payDate: string;
  status: 'DRAFT' | 'PROCESSING' | 'CONFIRMED' | 'LOCKED';
}

export interface PayrollRecord {
  id: number;
  periodId: number;
  employeeId: number;
  baseSalary: number;
  totalAllowances: number;
  overtimePay: number;
  otherEarnings: number;
  grossPay: number;
  laborInsurance: number;
  healthInsurance: number;
  incomeTax: number;
  leaveDeduction: number;
  otherDeductions: number;
  totalDeductions: number;
  netPay: number;
  employerLaborIns: number;
  employerHealthIns: number;
  employerPension: number;
  totalEmployerCost: number;
  status: 'DRAFT' | 'CONFIRMED';
  remark: string | null;
}

export interface PayrollItem {
  id: number;
  payrollRecordId: number;
  itemType: 'EARNING' | 'DEDUCTION';
  name: string;
  amount: number;
  remark: string | null;
}

export interface PayrollRecordDetail {
  record: PayrollRecord;
  employee: { id: number; name: string; department: { name: string } | null };
  items: PayrollItem[];
}

export interface TaxBracket {
  id: number;
  year: number;
  bracketStart: number;
  bracketEnd: number | null;
  rate: number;
  quickDeduction: number;
}

export const payrollApi = {
  // Periods
  listPeriods: () => client.get<ApiResponse<PayrollPeriod[]>>('/payroll/periods'),
  createPeriod: (data: { year: number; month: number; startDate: string; endDate: string; payDate: string }) =>
    client.post<ApiResponse<PayrollPeriod>>('/payroll/periods', data),
  updatePeriod: (id: number, data: { startDate: string; endDate: string; payDate: string }) =>
    client.put<ApiResponse<PayrollPeriod>>(`/payroll/periods/${id}`, data),
  calculate: (id: number) => client.post<ApiResponse<PayrollRecord[]>>(`/payroll/periods/${id}/calculate`),
  confirm: (id: number) => client.post<ApiResponse<void>>(`/payroll/periods/${id}/confirm`),

  // Records
  listRecords: (periodId: number) => client.get<ApiResponse<PayrollRecord[]>>('/payroll/records', { params: { periodId } }),
  getRecord: (id: number) => client.get<ApiResponse<PayrollRecordDetail>>(`/payroll/records/${id}`),
  updateRecord: (id: number, data: { otherEarnings?: number; otherDeductions?: number; remark?: string }) =>
    client.put<ApiResponse<PayrollRecord>>(`/payroll/records/${id}`, data),

  // Items
  listItems: (recordId: number) => client.get<ApiResponse<PayrollItem[]>>(`/payroll/records/${recordId}/items`),
  addItem: (recordId: number, data: { itemType: string; name: string; amount: number; remark?: string }) =>
    client.post<ApiResponse<PayrollItem>>(`/payroll/records/${recordId}/items`, data),
  deleteItem: (id: number) => client.delete<ApiResponse<void>>(`/payroll/items/${id}`),

  // Tax
  getTaxBrackets: (year: number) => client.get<ApiResponse<TaxBracket[]>>('/payroll/tax-brackets', { params: { year } }),
  createTaxBrackets: (data: { year: number; brackets: { bracketStart: number; bracketEnd: number | null; rate: number; quickDeduction: number }[] }) =>
    client.post<ApiResponse<TaxBracket[]>>('/payroll/tax-brackets', data),
};
