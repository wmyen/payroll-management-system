import { create } from 'zustand';

interface AuthState {
  token: string | null;
  employeeId: number | null;
  role: string | null;
  isAuthenticated: boolean;
  login: (token: string, employeeId: number | null, role: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('access_token'),
  employeeId: localStorage.getItem('employee_id') ? Number(localStorage.getItem('employee_id')) : null,
  role: localStorage.getItem('user_role'),
  isAuthenticated: !!localStorage.getItem('access_token'),
  login: (token: string, employeeId: number | null, role: string) => {
    localStorage.setItem('access_token', token);
    if (employeeId !== null) localStorage.setItem('employee_id', String(employeeId));
    localStorage.setItem('user_role', role);
    set({ token, employeeId, role, isAuthenticated: true });
  },
  logout: () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('employee_id');
    localStorage.removeItem('user_role');
    set({ token: null, employeeId: null, role: null, isAuthenticated: false });
  },
}));
