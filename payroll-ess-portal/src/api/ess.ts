import client from './client';
import type { ApiResponse } from './auth';

export interface PaystubSummary {
  recordId: number;
  period: { id: number; year: number; month: number; payDate: string; status: string } | null;
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
  status: string;
}

export interface PaystubDetail {
  record: {
    id: number; periodId: number; employeeId: number;
    baseSalary: number; totalAllowances: number; overtimePay: number;
    otherEarnings: number; grossPay: number;
    laborInsurance: number; healthInsurance: number; incomeTax: number;
    leaveDeduction: number; otherDeductions: number; totalDeductions: number;
    netPay: number; employerLaborIns: number; employerHealthIns: number;
    employerPension: number; totalEmployerCost: number;
    status: string; remark: string | null;
  };
  period: { id: number; year: number; month: number; startDate: string; endDate: string; payDate: string } | null;
  items: { id: number; itemType: string; name: string; amount: number; remark: string | null }[];
}

export interface UserProfile {
  username: string;
  role: string;
  employeeId: number | null;
  profile?: {
    id: number; name: string; email: string | null; phone: string | null;
    hireDate: string; contractType: string; jobLevel: string | null;
    status: string; department?: { id: number; name: string };
  };
}

export const essApi = {
  me: () => client.get<ApiResponse<UserProfile>>('/ess/me'),
  paystubs: () => client.get<ApiResponse<PaystubSummary[]>>('/ess/paystubs'),
  paystubDetail: (recordId: number) => client.get<ApiResponse<PaystubDetail>>(`/ess/paystubs/${recordId}`),
};
