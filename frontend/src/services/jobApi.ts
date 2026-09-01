import api from './api';

export const jobApi = {
  getAll: () => api.get('/jobs'),
  getById: (id: number) => api.get(`/jobs/${id}`),
  retry: (id: number) => api.post(`/jobs/${id}/retry`),
};
