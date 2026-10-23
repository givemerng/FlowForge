import api from './api';
import type { Project } from '../types';

export const projectApi = {
  getAll: () => api.get<Project[]>('/projects'),
  getById: (id: number) => api.get<Project>(`/projects/${id}`),
  create: (data: { name: string; description: string }) => api.post<Project>('/projects', data),
  update: (id: number, data: { name: string; description: string }) => api.put<Project>(`/projects/${id}`, data),
  delete: (id: number) => api.delete(`/projects/${id}`),
};
