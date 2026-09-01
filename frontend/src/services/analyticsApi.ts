import api from './api';

export const analyticsApi = {
  getOverview: () => api.get('/analytics/overview'),
  getProjectAnalytics: (projectId: number) => api.get(`/analytics/project/${projectId}`),
};
