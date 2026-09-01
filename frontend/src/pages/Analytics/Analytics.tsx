import React, { useEffect, useState } from 'react';
import { analyticsApi } from '../../services/analyticsApi';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import { Loader2, BarChart3 } from 'lucide-react';

const COLORS = ['#6366f1', '#3b82f6', '#f59e0b', '#ef4444', '#10b981'];

export const Analytics: React.FC = () => {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    analyticsApi.getOverview()
      .then(r => setData(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div className="flex items-center justify-center h-full">
      <Loader2 size={32} className="animate-spin text-primary" />
    </div>
  );

  if (!data) return (
    <div className="flex items-center justify-center h-full text-on-surface-variant">No data available.</div>
  );

  const statusData = data.tasksByStatus
    ? Object.entries(data.tasksByStatus).map(([name, value]) => ({ name, value }))
    : [];

  const priorityData = data.tasksByPriority
    ? Object.entries(data.tasksByPriority).map(([name, value]) => ({ name, value }))
    : [];

  return (
    <div className="p-lg h-full overflow-auto space-y-lg">
      <div>
        <h1 className="font-h1 text-h1 font-bold text-on-surface">Analytics</h1>
        <p className="text-on-surface-variant font-body-sm">Real-time task and project metrics</p>
      </div>

      {/* KPI Row */}
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-md">
        {[
          { label: 'Total Tasks', value: data.totalTasks },
          { label: 'Completed', value: data.completedTasks },
          { label: 'Overdue', value: data.overdueTasks },
          { label: 'Completion %', value: `${data.completionPercentage}%` },
        ].map(k => (
          <div key={k.label} className="bg-surface-container-lowest border border-outline-variant rounded-xl p-lg">
            <p className="font-body-sm text-on-surface-variant">{k.label}</p>
            <p className="font-h2 text-h2 font-bold text-on-surface mt-xs">{k.value}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-lg">
        {/* Status Bar */}
        <div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-lg">
          <h3 className="font-label-lg font-semibold text-on-surface mb-md">Tasks by Status</h3>
          {statusData.length === 0 ? (
            <div className="flex items-center justify-center h-[200px] text-on-surface-variant">
              <BarChart3 size={40} className="opacity-30" />
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={statusData} margin={{ left: -20 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.05)" />
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="value" fill="#6366f1" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Priority Pie */}
        <div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-lg">
          <h3 className="font-label-lg font-semibold text-on-surface mb-md">Tasks by Priority</h3>
          {priorityData.length === 0 ? (
            <div className="flex items-center justify-center h-[200px] text-on-surface-variant">
              <BarChart3 size={40} className="opacity-30" />
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie data={priorityData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={70} label>
                  {priorityData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </div>
  );
};
