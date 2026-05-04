export interface Employee {
  id: number;
  name: string;
  idNumber: string;
  bankAccount: string | null;
  hireDate: string;
  leaveDate: string | null;
  department: Department | null;
  contractType: 'REGULAR' | 'CONTRACT' | 'PART_TIME' | 'INTERN';
  jobLevel: string | null;
  status: 'ACTIVE' | 'SUSPENDED' | 'LEFT';
  email: string | null;
  phone: string | null;
}

export interface Department {
  id: number;
  name: string;
  parent: Department | null;
  children: Department[];
}

export interface SalaryStructure {
  id: number;
  employeeId: number;
  baseSalary: string;
  effectiveDate: string;
  allowances: Allowance[];
}

export interface Allowance {
  id: number;
  type: 'TRANSPORT' | 'MEAL' | 'HOUSING' | 'POSITION' | 'OTHER';
  amount: string;
}

export interface ApiResponse<T> {
  code: number;
  data: T;
  message: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
