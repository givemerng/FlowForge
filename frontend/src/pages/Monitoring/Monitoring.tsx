import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import { Activity, Server, Database, MessageSquare, Zap, CheckCircle2, XCircle, AlertTriangle } from 'lucide-react';

interface ServiceStatus {
  name: string;
  url: string;
  status: 'HEALTHY' | 'OFFLINE' | 'UNKNOWN';
  icon: React.ElementType;
}

const StatusIcon = ({ s }: { s: string }) => {
  if (s === 'HEALTHY') return <CheckCircle2 size={18} className="text-green-500" />;
  if (s === 'OFFLINE') return <XCircle size={18} className="text-error" />;
  return <AlertTriangle size={18} className="text-yellow-500" />;
};

export const Monitoring: React.FC = () => {
  const [services, setServices] = useState<ServiceStatus[]>([
    { name: 'Java API', url: '/actuator/health', status: 'UNKNOWN', icon: Server },
    { name: 'MySQL (via API)', url: '/actuator/health', status: 'UNKNOWN', icon: Database },
    { name: 'RabbitMQ (via API)', url: '/actuator/health', status: 'UNKNOWN', icon: MessageSquare },
    { name: 'Redis (via API)', url: '/actuator/health', status: 'UNKNOWN', icon: Zap },
  ]);
  const [_metrics, _setMetrics] = useState<any>(null);
  const [_loading, _setLoading] = useState(true);

  useEffect(() => {
    const check = async () => {
      try {
        const res = await api.get('/actuator/health');
        const health = res.data;
        setServices(prev => prev.map(s => {
          if (s.name === 'Java API') return { ...s, status: health.status === 'UP' ? 'HEALTHY' : 'OFFLINE' };
          if (s.name === 'MySQL (via API)') return { ...s, status: health.components?.db?.status === 'UP' ? 'HEALTHY' : 'OFFLINE' };
          if (s.name === 'RabbitMQ (via API)') return { ...s, status: health.components?.rabbit?.status === 'UP' ? 'HEALTHY' : 'OFFLINE' };
          if (s.name === 'Redis (via API)') return { ...s, status: health.components?.redis?.status === 'UP' ? 'HEALTHY' : 'OFFLINE' };
          return s;
        }));

        // Fetch some metrics
        const mRes = await api.get('/actuator/metrics/http.server.requests').catch(() => null);
        if (mRes) _setMetrics(mRes.data);
      } catch {
        setServices(prev => prev.map(s => ({ ...s, status: 'OFFLINE' })));
      } finally { _setLoading(false); }
    };
    check();
    const interval = setInterval(check, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="p-lg h-full overflow-auto space-y-lg">
      <div className="flex items-center gap-sm mb-lg">
        <Activity size={28} className="text-primary" />
        <div>
          <h1 className="font-h1 text-h1 font-bold text-on-surface">System Monitoring</h1>
          <p className="text-on-surface-variant font-body-sm">Real-time service health status</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-md">
        {services.map(s => {
          const Icon = s.icon;
          const statusColor = s.status === 'HEALTHY' ? 'border-green-500/30 bg-green-50/30' :
            s.status === 'OFFLINE' ? 'border-red-500/30 bg-red-50/30' : 'border-outline-variant';
          return (
            <div key={s.name} className={`rounded-xl border p-lg transition-all ${statusColor}`}>
              <div className="flex items-center justify-between mb-sm">
                <Icon size={22} className="text-on-surface-variant" />
                <StatusIcon s={s.status} />
              </div>
              <p className="font-label-lg font-semibold text-on-surface">{s.name}</p>
              <p className={`font-body-sm mt-xs ${
                s.status === 'HEALTHY' ? 'text-green-600' :
                s.status === 'OFFLINE' ? 'text-red-600' : 'text-yellow-600'
              }`}>{s.status}</p>
            </div>
          );
        })}
      </div>

      {/* External links */}
      <div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-lg">
        <h3 className="font-label-lg font-semibold text-on-surface mb-md">Monitoring Tools</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-sm">
          {[
            { label: 'Grafana Dashboard', url: 'http://localhost:3000', desc: 'Visual metrics and dashboards' },
            { label: 'Prometheus', url: 'http://localhost:9090', desc: 'Raw metrics and queries' },
            { label: 'RabbitMQ Management', url: 'http://localhost:15672', desc: 'Queue depth and message count' },
            { label: 'Spring Actuator', url: 'http://localhost:8080/actuator', desc: 'Application health endpoints' },
          ].map(l => (
            <a key={l.label} href={l.url} target="_blank" rel="noopener noreferrer"
              className="flex items-center gap-sm p-sm rounded-lg border border-outline-variant hover:border-primary/40 hover:bg-surface-container transition-all">
              <Activity size={16} className="text-primary shrink-0" />
              <div>
                <p className="font-label-md text-on-surface">{l.label}</p>
                <p className="font-body-sm text-on-surface-variant">{l.desc}</p>
              </div>
            </a>
          ))}
        </div>
      </div>
    </div>
  );
};
