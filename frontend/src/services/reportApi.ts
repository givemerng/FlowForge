import api from './api';

export const reportApi = {
  getAll: () => api.get('/reports'),
  getById: (id: number) => api.get(`/reports/${id}`),
  generate: (projectId: number, type: string = 'PROJECT_SUMMARY') => api.post('/reports', { projectId, type }),
};
