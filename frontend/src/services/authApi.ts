import api from './api';

export interface LoginPayload { username: string; password: string; }
export interface RegisterPayload { username: string; email: string; password: string; }

export const authApi = {
  login: (data: LoginPayload) => api.post('/auth/login', data),
  register: (data: RegisterPayload) => api.post('/auth/register', data),
};
