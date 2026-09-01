import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { projectApi } from '../../services/projectApi';
import { analyticsApi } from '../../services/analyticsApi';
import { notificationApi } from '../../services/notificationApi';
import { BarChart3, CheckCircle2, Clock, AlertTriangle, Folder, Bell, Plus, Loader2 } from 'lucide-react';

interface Stat { label: string; value: number | string; icon: React.ElementType; color: string; }

const StatCard = ({ label, value, icon: Icon, color }: Stat) => (
  <div className="bg-surface-container-lowest rounded-xl border border-outline-variant p-lg flex items-center gap-md hover:border-primary/30 transition-colors">
    <div className={`w-12 h-12 rounded-lg flex items-center justify-center ${color}`}>
      <Icon size={22} />
    </div>
    <div>
      <p className="font-body-sm text-on-surface-variant">{label}</p>
      <p className="font-h2 text-h2 font-bold text-on-surface">{value}</p>
    </div>
  </div>
);

export const Dashboard: React.FC = () => {
  const [analytics, setAnalytics] = useState<any>(null);
  const [projects, setProjects] = useState<any[]>([]);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const [analyticsRes, projectsRes, notifRes] = await Promise.all([
          analyticsApi.getOverview(),
          projectApi.getAll(),
          notificationApi.getAll(),
        ]);
        setAnalytics(analyticsRes.data);
        setProjects(projectsRes.data.slice(0, 5));
        setNotifications(notifRes.data.slice(0, 5));
      } catch (e: any) {
        setError('Failed to load dashboard data.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return (
    <div className="flex items-center justify-center h-full">
      <Loader2 size={32} className="animate-spin text-primary" />
    </div>
  );

  if (error) return (
    <div className="flex items-center justify-center h-full">
      <div className="text-center space-y-sm">
        <AlertTriangle size={48} className="text-error mx-auto" />
        <p className="text-on-surface-variant">{error}</p>
      </div>
    </div>
  );

  const stats: Stat[] = [
    { label: 'Total Tasks', value: analytics?.totalTasks ?? 0, icon: BarChart3, color: 'bg-primary-container text-on-primary-container' },
    { label: 'Completed', value: analytics?.completedTasks ?? 0, icon: CheckCircle2, color: 'bg-green-100 text-green-700' },
    { label: 'In Progress', value: analytics?.inProgressTasks ?? 0, icon: Clock, color: 'bg-blue-100 text-blue-700' },
    { label: 'Overdue', value: analytics?.overdueTasks ?? 0, icon: AlertTriangle, color: 'bg-error-container text-error' },
  ];

  return (
    <div className="p-lg space-y-lg overflow-auto h-full">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-h1 text-h1 font-bold text-on-surface">Dashboard</h1>
          <p className="text-on-surface-variant font-body-sm mt-xs">Welcome back. Here's what's happening.</p>
        </div>
        <Link to="/projects" className="flex items-center gap-xs bg-primary text-on-primary px-md py-sm rounded-lg text-label-md font-label-md hover:bg-primary/90 transition-colors">
          <Plus size={16} /> New Project
        </Link>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-md">
        {stats.map(s => <StatCard key={s.label} {...s} />)}
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-lg">
        {/* Projects */}
        <div className="bg-surface-container-lowest rounded-xl border border-outline-variant p-lg">
          <div className="flex items-center justify-between mb-md">
            <h3 className="font-label-lg text-label-lg font-semibold text-on-surface">Recent Projects</h3>
            <Link to="/projects" className="text-primary font-body-sm hover:underline">View all</Link>
          </div>
          {projects.length === 0 ? (
            <div className="text-center py-xl text-on-surface-variant">
              <Folder size={40} className="mx-auto mb-sm opacity-30" />
              <p>No projects yet</p>
            </div>
          ) : (
            <div className="space-y-sm">
              {projects.map(p => (
                <Link key={p.id} to={`/projects/${p.id}/board`}
                  className="flex items-center gap-sm p-sm rounded-lg hover:bg-surface-container transition-colors">
                  <div className="w-8 h-8 rounded bg-primary-container text-on-primary-container flex items-center justify-center text-label-md font-bold">
                    {p.name.charAt(0)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-label-md text-on-surface truncate">{p.name}</p>
                    <p className="font-body-sm text-on-surface-variant truncate">{p.description || 'No description'}</p>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>

        {/* Notifications */}
        <div className="bg-surface-container-lowest rounded-xl border border-outline-variant p-lg">
          <div className="flex items-center justify-between mb-md">
            <h3 className="font-label-lg text-label-lg font-semibold text-on-surface">Notifications</h3>
            <Link to="/notifications" className="text-primary font-body-sm hover:underline">View all</Link>
          </div>
          {notifications.length === 0 ? (
            <div className="text-center py-xl text-on-surface-variant">
              <Bell size={40} className="mx-auto mb-sm opacity-30" />
              <p>No notifications</p>
            </div>
          ) : (
            <div className="space-y-sm">
              {notifications.map(n => (
                <div key={n.id} className={`p-sm rounded-lg border ${n.read ? 'border-transparent' : 'border-primary/20 bg-primary-container/10'}`}>
                  <p className="font-label-md text-on-surface">{n.title}</p>
                  <p className="font-body-sm text-on-surface-variant text-xs mt-xs">{n.body}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Completion rate */}
      {analytics && (
        <div className="bg-surface-container-lowest rounded-xl border border-outline-variant p-lg">
          <h3 className="font-label-lg font-semibold text-on-surface mb-md">Overall Completion</h3>
          <div className="flex items-center gap-md">
            <div className="flex-1 bg-surface-container rounded-full h-3 overflow-hidden">
              <div className="bg-primary h-full rounded-full transition-all duration-700"
                style={{ width: `${analytics.completionPercentage ?? 0}%` }} />
            </div>
            <span className="font-label-lg text-on-surface font-semibold w-14 text-right">
              {analytics.completionPercentage ?? 0}%
            </span>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
