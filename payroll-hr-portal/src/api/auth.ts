import client from './client';
import type { ApiResponse } from '../types';

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

export const authApi = {
  login: (username: string, password: string) =>
    client.post<ApiResponse<LoginResponse>>('/auth/login', { username, password }),
};
