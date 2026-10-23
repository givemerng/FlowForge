import api from './api';
import type { Report } from '../types';

export const reportApi = {
  getAll: () => api.get<Report[]>('/reports'),
  getById: (id: number) => api.get<Report>(`/reports/${id}`),
  generate: (projectId: number, type: string = 'PROJECT_SUMMARY') => api.post('/reports', { projectId, type }),
};
