import api from './api';
import type { Notification } from '../types';

export const notificationApi = {
  getAll: () => api.get<Notification[]>('/notifications'),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markRead: (id: number) => api.put(`/notifications/${id}/read`),
  markAllRead: () => api.put('/notifications/read-all'),
};
