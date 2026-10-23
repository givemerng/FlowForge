import api from './api';
import type { NotificationPreferences, UserSummary } from '../types';

export const userApi = {
  me: () => api.get<UserSummary>('/users/me'),
  updateMe: (username: string, email: string) => api.put<UserSummary>('/users/me', { username, email }),
  changePassword: (currentPassword: string, newPassword: string) =>
    api.put('/users/me/password', { currentPassword, newPassword }),
  settings: () => api.get<NotificationPreferences>('/users/me/settings'),
  updateSettings: (settings: NotificationPreferences) =>
    api.put<NotificationPreferences>('/users/me/settings', settings),
};
