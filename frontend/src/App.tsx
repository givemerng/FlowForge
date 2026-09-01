import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/layout/Layout';
import { Dashboard } from './pages/Dashboard/Dashboard';
import { Login } from './pages/Auth/Login';
import { Register } from './pages/Auth/Register';
import { ProjectBoard } from './pages/Projects/ProjectBoard';
import { ProjectList } from './pages/Projects/ProjectList';
import { MyTasks } from './pages/Tasks/MyTasks';
import { Analytics } from './pages/Analytics/Analytics';
import { Insights } from './pages/Insights/Insights';
import { Reports } from './pages/Reports/Reports';
import { Jobs } from './pages/Jobs/Jobs';
import { Monitoring } from './pages/Monitoring/Monitoring';
import { Notifications } from './pages/Notifications/Notifications';
import { Settings } from './pages/Settings/Settings';
import { AdminDashboard } from './pages/Admin/AdminDashboard';
import { AuditLogs } from './pages/Admin/AuditLogs';

// Protected route guard
const RequireAuth = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
};

function App() {
  return (
    <BrowserRouter>
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

          <Route path="/jobs" element={<Jobs />} />

          <Route path="/monitoring" element={<Monitoring />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/settings" element={<Settings />} />

          <Route path="/admin" element={<AdminDashboard />} />
          <Route path="/audit-logs" element={<AuditLogs />} />
        </Route>

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
