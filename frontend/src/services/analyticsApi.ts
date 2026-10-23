import api from './api';
import type { AnalyticsOverview } from '../types';

export const analyticsApi = {
  getOverview: () => api.get<AnalyticsOverview>('/analytics/overview'),
  getProjectAnalytics: (projectId: number) => api.get<AnalyticsOverview>(`/analytics/project/${projectId}`),
};
