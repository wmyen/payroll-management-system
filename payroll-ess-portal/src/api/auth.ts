import client from './client';

export interface ApiResponse<T> {
  code: number;
  data: T;
  message: string;
}

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  employeeId: number | null;
  role: string;
}

export const authApi = {
  login: (username: string, password: string) =>
    client.post<ApiResponse<LoginResponse>>('/auth/login', { username, password }),
};
