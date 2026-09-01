import React, { useEffect, useState } from 'react';
import axios from 'axios';

interface AuditLog {
  id: number;
  user: { username: string };
  action: string;
  resource: string;
  resourceId: number;
  metadata: string;
  createdAt: string;
}

export const AuditLogs: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchLogs = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get<AuditLog[]>('http://localhost:8080/api/audit-logs', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setLogs(response.data);
    } catch (err) {
      setError('Failed to fetch audit logs or you do not have permission.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  if (loading) return <div className="p-8 text-white">Loading logs...</div>;
  if (error) return <div className="p-8 text-red-500">{error}</div>;

  return (
    <div className="p-8 max-w-6xl mx-auto text-white">
      <h1 className="text-3xl font-bold mb-8">System Audit Logs</h1>
      <div className="bg-gray-900 rounded-lg shadow-lg border border-gray-800 p-6 space-y-4">
        {logs.map(log => (
          <div key={log.id} className="flex flex-col md:flex-row md:items-center justify-between p-4 bg-gray-800 rounded-lg border border-gray-700 hover:border-gray-500 transition-colors">
            <div className="space-y-1">
              <div className="flex items-center space-x-3">
                <span className="text-blue-400 font-semibold">{log.user.username}</span>
                <span className="text-gray-400 text-sm bg-gray-700 px-2 py-0.5 rounded">{log.action}</span>
              </div>
              <div className="text-gray-300 text-sm">
                Target: {log.resource} <span className="text-gray-500">#{log.resourceId}</span>
              </div>
              <div className="text-gray-500 text-xs font-mono">{log.metadata}</div>
            </div>
            <div className="mt-2 md:mt-0 text-right">
              <div className="text-xs text-gray-500">
                {new Date(log.createdAt).toLocaleString()}
              </div>
            </div>
          </div>
        ))}
        {logs.length === 0 && (
          <div className="text-gray-500 text-center py-8">No audit logs found.</div>
        )}
      </div>
    </div>
  );
};
