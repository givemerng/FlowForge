import api from './api';

export const taskApi = {
  getProjectTasks: (projectId: number) => api.get(`/projects/${projectId}/tasks`),
  getById: (id: number) => api.get(`/tasks/${id}`),
  create: (projectId: number, data: any) => api.post(`/projects/${projectId}/tasks`, data),
  updateStatus: (id: number, status: string) => api.patch(`/tasks/${id}/status`, { status }),
  assign: (id: number, userId: number) => api.patch(`/tasks/${id}/assign`, { userId }),
  delete: (id: number) => api.delete(`/tasks/${id}`),
  getComments: (taskId: number) => api.get(`/tasks/${taskId}/comments`),
  addComment: (taskId: number, body: string) => api.post(`/tasks/${taskId}/comments`, { body }),
};
