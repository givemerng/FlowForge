import React, { useEffect, useState } from 'react';
import { Loader2, CheckCircle2, Clock, Filter } from 'lucide-react';

const PriorityBadge = ({ p }: { p: string }) => {
  const map: Record<string, string> = {
    CRITICAL: 'bg-red-100 text-red-700',
    HIGH: 'bg-orange-100 text-orange-700',
    MEDIUM: 'bg-yellow-100 text-yellow-700',
    LOW: 'bg-green-100 text-green-700',
  };
  return <span className={`px-xs py-[2px] rounded text-label-md font-label-md ${map[p] || ''}`}>{p}</span>;
};

const StatusBadge = ({ s }: { s: string }) => {
  const map: Record<string, string> = {
    DONE: 'bg-green-100 text-green-700',
    IN_PROGRESS: 'bg-blue-100 text-blue-700',
    TODO: 'bg-surface-container text-on-surface-variant',
    BLOCKED: 'bg-error-container text-error',
    REVIEW: 'bg-purple-100 text-purple-700',
  };
  return <span className={`px-xs py-[2px] rounded text-label-md font-label-md ${map[s] || ''}`}>{s.replace('_', ' ')}</span>;
};

export const MyTasks: React.FC = () => {
  const [tasks, setTasks] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    // Fetch all tasks from all projects - simplified: fetch from /api/tasks/my
    // For now show from the first project or all visible tasks
    const fetchTasks = async () => {
      try {
        const res = await fetch(
          `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/tasks`,
          { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } }
        );
        if (res.ok) {
          const data = await res.json();
          setTasks(data);
        }
      } catch {} finally { setLoading(false); }
    };
    fetchTasks();
  }, []);

  const filtered = filter === 'ALL' ? tasks : tasks.filter(t => t.status === filter);

  return (
    <div className="p-lg h-full overflow-auto space-y-lg">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-h1 text-h1 font-bold text-on-surface">My Tasks</h1>
          <p className="text-on-surface-variant font-body-sm">{tasks.length} task{tasks.length !== 1 ? 's' : ''} assigned to you</p>
        </div>
        <div className="flex items-center gap-xs">
          <Filter size={14} className="text-on-surface-variant" />
          <select value={filter} onChange={e => setFilter(e.target.value)}
            className="border border-outline-variant px-sm py-xs rounded-lg text-body-sm focus:outline-none focus:border-primary bg-surface-container-lowest">
            <option value="ALL">All</option>
            <option value="TODO">Todo</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="REVIEW">Review</option>
            <option value="BLOCKED">Blocked</option>
            <option value="DONE">Done</option>
          </select>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-xl"><Loader2 size={32} className="animate-spin text-primary" /></div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-2xl border-2 border-dashed border-outline-variant rounded-xl">
          <CheckCircle2 size={48} className="mx-auto text-on-surface-variant opacity-30 mb-sm" />
          <p className="font-label-lg text-on-surface-variant">No tasks</p>
        </div>
      ) : (
        <div className="space-y-sm">
          {filtered.map(t => (
            <div key={t.id} className="bg-surface-container-lowest border border-outline-variant rounded-xl p-md flex items-center gap-md hover:border-primary/30 transition-colors">
              <div className="flex-1 min-w-0">
                <p className="font-label-md font-semibold text-on-surface truncate">{t.title}</p>
                {t.description && <p className="font-body-sm text-on-surface-variant truncate mt-xs">{t.description}</p>}
                {t.deadline && (
                  <div className="flex items-center gap-xs mt-xs text-on-surface-variant">
                    <Clock size={12} />
                    <span className="font-body-sm text-xs">{new Date(t.deadline).toLocaleDateString()}</span>
                  </div>
                )}
              </div>
              <div className="flex items-center gap-sm shrink-0">
                <PriorityBadge p={t.priority} />
                <StatusBadge s={t.status} />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
