import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/layout/Layout';

const Dashboard = lazy(() => import('./pages/Dashboard/Dashboard').then(({ Dashboard }) => ({ default: Dashboard })));
const Login = lazy(() => import('./pages/Auth/Login').then(({ Login }) => ({ default: Login })));
const Register = lazy(() => import('./pages/Auth/Register').then(({ Register }) => ({ default: Register })));
const ProjectBoard = lazy(() => import('./pages/Projects/ProjectBoard').then(({ ProjectBoard }) => ({ default: ProjectBoard })));
const ProjectList = lazy(() => import('./pages/Projects/ProjectList').then(({ ProjectList }) => ({ default: ProjectList })));
const MyTasks = lazy(() => import('./pages/Tasks/MyTasks').then(({ MyTasks }) => ({ default: MyTasks })));
const Analytics = lazy(() => import('./pages/Analytics/Analytics').then(({ Analytics }) => ({ default: Analytics })));
const Insights = lazy(() => import('./pages/Insights/Insights').then(({ Insights }) => ({ default: Insights })));
const Reports = lazy(() => import('./pages/Reports/Reports').then(({ Reports }) => ({ default: Reports })));
const Jobs = lazy(() => import('./pages/Jobs/Jobs').then(({ Jobs }) => ({ default: Jobs })));
const Monitoring = lazy(() => import('./pages/Monitoring/Monitoring').then(({ Monitoring }) => ({ default: Monitoring })));
const Notifications = lazy(() => import('./pages/Notifications/Notifications').then(({ Notifications }) => ({ default: Notifications })));
const Settings = lazy(() => import('./pages/Settings/Settings').then(({ Settings }) => ({ default: Settings })));
const AdminDashboard = lazy(() => import('./pages/Admin/AdminDashboard').then(({ AdminDashboard }) => ({ default: AdminDashboard })));
const AuditLogs = lazy(() => import('./pages/Admin/AuditLogs').then(({ AuditLogs }) => ({ default: AuditLogs })));

// Protected route guard
const RequireAuth = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
};

const RequireRole = ({ roles, children }: { roles: string[]; children: React.ReactNode }) => {
  const role = localStorage.getItem('role');
  return role && roles.includes(role) ? <>{children}</> : <Navigate to="/dashboard" replace />;
};

function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<div className="min-h-screen bg-slate-950" />}>
        <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }>
          <Route path="/dashboard" element={<Dashboard />} />

          <Route path="/projects" element={<ProjectList />} />
          <Route path="/projects/:projectId/board" element={<ProjectBoard />} />

          <Route path="/my-tasks" element={<MyTasks />} />

          <Route path="/analytics" element={<Analytics />} />
          <Route path="/insights" element={<Insights />} />
          <Route path="/reports" element={<Reports />} />

          <Route path="/jobs" element={<RequireRole roles={['ADMIN', 'MANAGER']}><Jobs /></RequireRole>} />

          <Route path="/monitoring" element={<RequireRole roles={['ADMIN', 'MANAGER']}><Monitoring /></RequireRole>} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/settings" element={<Settings />} />

          <Route path="/admin" element={<RequireRole roles={['ADMIN']}><AdminDashboard /></RequireRole>} />
          <Route path="/audit-logs" element={<RequireRole roles={['ADMIN']}><AuditLogs /></RequireRole>} />
        </Route>

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}

export default App;
