import api from './api';

export const projectApi = {
  getAll: () => api.get('/projects'),
  getById: (id: number) => api.get(`/projects/${id}`),
  create: (data: { name: string; description: string }) => api.post('/projects', data),
  update: (id: number, data: { name: string; description: string }) => api.put(`/projects/${id}`, data),
  delete: (id: number) => api.delete(`/projects/${id}`),
};
