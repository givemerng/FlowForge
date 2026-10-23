import React, { useCallback, useEffect, useState } from 'react';
import { jobApi } from '../../services/jobApi';
import { Loader2, RefreshCw, CheckCircle2, XCircle, Clock, AlertTriangle } from 'lucide-react';
import { useWebSocket } from '../../hooks/useWebSocket';
import type { Job } from '../../types';

const StatusBadge = ({ status }: { status: string }) => {
  const map: Record<string, string> = {
    COMPLETED: 'bg-green-100 text-green-700',
    FAILED: 'bg-error-container text-error',
    PROCESSING: 'bg-blue-100 text-blue-700',
    QUEUED: 'bg-surface-container text-on-surface-variant',
  };
  const icons: Record<string, React.ElementType> = {
    COMPLETED: CheckCircle2, FAILED: XCircle, PROCESSING: Loader2, QUEUED: Clock,
  };
  const Icon = icons[status] || Clock;
  return (
    <span className={`flex items-center gap-xs px-sm py-[2px] rounded-full text-label-md font-label-md ${map[status] || ''}`}>
      <Icon size={12} className={status === 'PROCESSING' ? 'animate-spin' : ''} />
      {status}
    </span>
  );
};

export const Jobs: React.FC = () => {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [retrying, setRetrying] = useState<number | null>(null);
  const { connected, subscribe } = useWebSocket();
  const jobIds = jobs.map(job => job.id).sort((a, b) => a - b).join(',');

  const load = useCallback(async () => {
    try {
      const res = await jobApi.getAll();
      setJobs(res.data);
    } catch { setError('Unable to load background jobs.'); } finally { setLoading(false); }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const hasPendingJob = jobs.some(job => job.status === 'QUEUED' || job.status === 'PROCESSING');

  useEffect(() => {
    if (!hasPendingJob) return;
    const timer = window.setInterval(() => { void load(); }, 2000);
    return () => window.clearInterval(timer);
  }, [hasPendingJob, load]);

  useEffect(() => {
    if (!connected) return;
    const subscriptions = jobIds.split(',').filter(Boolean).map(jobId =>
      subscribe<Job>(`/topic/jobs/${jobId}`, (updatedJob) => {
        setJobs(prev => prev.map(job => job.id === updatedJob.id ? updatedJob : job));
      })
    );
    return () => subscriptions.forEach(subscription => subscription?.unsubscribe());
  }, [connected, subscribe, jobIds]);

  const handleRetry = async (id: number) => {
    setRetrying(id);
    try { await jobApi.retry(id); await load(); }
    catch { setError('Unable to retry this job.'); } finally { setRetrying(null); }
  };

  return (
    <div className="p-lg h-full overflow-auto">
      <div className="flex items-center justify-between mb-lg">
        <div>
          <h1 className="font-h1 text-h1 font-bold text-on-surface">Background Jobs</h1>
          <p className="text-on-surface-variant font-body-sm flex items-center gap-2">
            {jobs.length} total jobs 
            {connected ? (
               <span className="text-green-600 text-xs flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-green-500 animate-pulse"></span> Live</span>
            ) : (
               <span className="text-outline text-xs">Offline</span>
            )}
          </p>
        </div>
        <button onClick={load} className="flex items-center gap-xs border border-outline-variant px-md py-sm rounded-lg hover:bg-surface-container transition-colors text-label-md">
          <RefreshCw size={14} /> Refresh
        </button>
      </div>

      {error && <div className="mb-md p-sm rounded-lg bg-error-container text-on-error-container">{error}</div>}

      {loading ? (
        <div className="flex justify-center py-xl"><Loader2 size={32} className="animate-spin text-primary" /></div>
      ) : jobs.length === 0 ? (
        <div className="text-center py-2xl border-2 border-dashed border-outline-variant rounded-xl">
          <AlertTriangle size={48} className="mx-auto text-on-surface-variant opacity-30 mb-sm" />
          <p className="text-on-surface-variant">No jobs found.</p>
        </div>
      ) : (
        <div className="overflow-hidden rounded-xl border border-outline-variant">
          <table className="w-full text-sm">
            <thead className="bg-surface-container">
              <tr>
                {['ID', 'Type', 'Status', 'Attempts', 'Created', 'Actions'].map(h => (
                  <th key={h} className="px-md py-sm text-left font-label-md text-on-surface-variant">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-outline-variant">
              {jobs.map(j => (
                <tr key={j.id} className="hover:bg-surface-container/50 transition-colors">
                  <td className="px-md py-sm font-mono text-on-surface-variant">#{j.id}</td>
                  <td className="px-md py-sm font-label-md text-on-surface">{j.type}</td>
                  <td className="px-md py-sm"><StatusBadge status={j.status} /></td>
                  <td className="px-md py-sm text-on-surface-variant">{j.attemptCount}/3</td>
                  <td className="px-md py-sm text-on-surface-variant">{new Date(j.createdAt).toLocaleString()}</td>
                  <td className="px-md py-sm">
                    {j.status === 'FAILED' && (
                      <button onClick={() => handleRetry(j.id)} disabled={retrying === j.id}
                        className="flex items-center gap-xs text-primary hover:underline text-label-md disabled:opacity-50">
                        {retrying === j.id ? <Loader2 size={12} className="animate-spin" /> : <RefreshCw size={12} />}
                        Retry
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
