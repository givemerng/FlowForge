import api from './api';
import type { Job } from '../types';

export const jobApi = {
  getAll: () => api.get<Job[]>('/jobs'),
  getById: (id: number) => api.get<Job>(`/jobs/${id}`),
  retry: (id: number) => api.post<Job>(`/jobs/${id}/retry`),
};
