import api from './api';
import type { Task, TaskPriority } from '../types';

export interface CreateTaskPayload {
  title: string;
  description?: string;
  priority: TaskPriority;
  assignedToId?: number;
  deadline?: string;
}

export const taskApi = {
  getAllAssigned: () => api.get<Task[]>('/tasks'),
  getProjectTasks: (projectId: number) => api.get<Task[]>(`/projects/${projectId}/tasks`),
  getById: (id: number) => api.get<Task>(`/tasks/${id}`),
  create: (projectId: number, data: CreateTaskPayload) => api.post<Task>(`/projects/${projectId}/tasks`, data),
  updateStatus: (id: number, status: string) => api.put<Task>(`/tasks/${id}/status`, { status }),
  assign: (id: number, userId: number) => api.put<Task>(`/tasks/${id}/assign`, { userId }),
  delete: (id: number) => api.delete(`/tasks/${id}`),
  getComments: (taskId: number) => api.get(`/tasks/${taskId}/comments`),
  addComment: (taskId: number, body: string) => api.post(`/tasks/${taskId}/comments`, { body }),
};
