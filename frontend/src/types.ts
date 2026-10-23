export interface UserSummary {
  id: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'MANAGER' | 'MEMBER';
}

export interface NotificationPreferences {
  taskAssigned: boolean;
  taskCompleted: boolean;
  jobFailed: boolean;
  reportReady: boolean;
}

export interface Project {
  id: number;
  name: string;
  description?: string;
  owner?: UserSummary;
  createdAt: string;
  updatedAt: string;
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'BLOCKED' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface Label {
  id: number;
  name: string;
  color: string;
}

export interface Task {
  id: number;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  projectId: number;
  assignedTo?: UserSummary;
  deadline?: string;
  createdAt: string;
  updatedAt: string;
  labels: Label[];
}

export interface AnalyticsOverview {
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  blockedTasks: number;
  overdueTasks: number;
  completionPercentage: number;
  tasksByStatus: Record<string, number>;
  tasksByPriority: Record<string, number>;
}

export interface Notification {
  id: number;
  title: string;
  body?: string;
  read: boolean;
  createdAt: string;
}

export interface Job {
  id: number;
  type: string;
  status: 'QUEUED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  attemptCount: number;
  lastError?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Report {
  id: number;
  project: Project;
  createdBy: UserSummary;
  job?: Job;
  type: string;
  content?: string;
  createdAt: string;
}

export interface RiskResult {
  taskId?: number;
  risk?: 'LOW' | 'MEDIUM' | 'HIGH';
  riskScore?: number;
  reasons?: string[];
  error?: string;
}
