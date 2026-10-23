import api from './api';
import type { RiskResult } from '../types';

export const insightApi = {
  analyzeTask: (taskId: number) => api.get<RiskResult>(`/insights/tasks/${taskId}/risk`),
};
