import React, { useState } from 'react';
import { Brain, Loader2, AlertTriangle, TrendingUp, TrendingDown, Minus } from 'lucide-react';

const RiskBadge = ({ risk }: { risk: string }) => {
  const map: Record<string, string> = {
    HIGH: 'bg-red-100 text-red-700 border-red-200',
    MEDIUM: 'bg-yellow-100 text-yellow-700 border-yellow-200',
    LOW: 'bg-green-100 text-green-700 border-green-200',
  };
  const icons: Record<string, React.ElementType> = { HIGH: TrendingUp, MEDIUM: Minus, LOW: TrendingDown };
  const Icon = icons[risk] || Minus;
  return (
    <span className={`flex items-center gap-xs px-sm py-xs rounded-full border font-label-md ${map[risk] || ''}`}>
      <Icon size={12} /> {risk}
    </span>
  );
};

export const Insights: React.FC = () => {
  const [insights, setInsights] = useState<any[]>([]);
  const [taskId, setTaskId] = useState('');
  const [fetching, setFetching] = useState(false);
  const [result, setResult] = useState<any>(null);

  const analyzeTask = async (e: React.FormEvent) => {
    e.preventDefault();
    setFetching(true);
    setResult(null);
    try {
      const baseUrl = import.meta.env.VITE_PYTHON_URL || 'http://localhost:8000';
      const res = await fetch(`${baseUrl}/api/risk/${taskId}`);
      const data = await res.json();
      setResult(data);
      setInsights(prev => [data, ...prev.filter((i: any) => i.taskId !== data.taskId)].slice(0, 10));
    } catch {
      setResult({ error: 'Failed to reach Python intelligence service.' });
    } finally {
      setFetching(false);
    }
  };

  return (
    <div className="p-lg h-full overflow-auto space-y-lg">
      <div className="flex items-center gap-sm">
        <Brain size={28} className="text-primary" />
        <div>
          <h1 className="font-h1 text-h1 font-bold text-on-surface">Intelligent Insights</h1>
          <p className="text-on-surface-variant font-body-sm">Rule-based risk and workload analysis</p>
        </div>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-lg max-w-xl">
        <h3 className="font-label-lg font-semibold text-on-surface mb-md">Analyze Task Risk</h3>
        <form onSubmit={analyzeTask} className="flex gap-sm">
          <input value={taskId} onChange={e => setTaskId(e.target.value)}
            placeholder="Enter Task ID" required type="number"
            className="flex-1 px-sm py-[8px] border border-outline-variant rounded-lg text-body-sm focus:border-primary focus:outline-none" />
          <button type="submit" disabled={fetching}
            className="bg-primary text-on-primary px-lg py-sm rounded-lg hover:bg-primary/90 disabled:opacity-70 flex items-center gap-xs font-label-md">
            {fetching ? <Loader2 size={14} className="animate-spin" /> : <Brain size={14} />}
            Analyze
          </button>
        </form>

        {result && (
          <div className="mt-lg border border-outline-variant rounded-lg p-md">
            {result.error ? (
              <div className="flex items-center gap-xs text-error">
                <AlertTriangle size={16} /> {result.error}
              </div>
            ) : (
              <>
                <div className="flex items-center justify-between mb-sm">
                  <span className="font-label-md font-semibold text-on-surface">Task #{result.taskId}</span>
                  <RiskBadge risk={result.risk} />
                </div>
                <div className="mb-sm">
                  <div className="flex items-center justify-between text-body-sm mb-xs">
                    <span className="text-on-surface-variant">Risk Score</span>
                    <span className="font-semibold text-on-surface">{result.riskScore}/100</span>
                  </div>
                  <div className="bg-surface-container rounded-full h-2">
                    <div className={`h-2 rounded-full transition-all ${
                      result.riskScore >= 70 ? 'bg-red-500' : result.riskScore >= 40 ? 'bg-yellow-500' : 'bg-green-500'
                    }`} style={{ width: `${result.riskScore}%` }} />
                  </div>
                </div>
                <div className="space-y-xs">
                  {result.reasons?.map((r: string, i: number) => (
                    <div key={i} className="flex items-start gap-xs text-body-sm text-on-surface-variant">
                      <span className="text-primary mt-0.5">•</span> {r}
                    </div>
                  ))}
                </div>
              </>
            )}
          </div>
        )}
      </div>

      {insights.length > 0 && (
        <div>
          <h3 className="font-label-lg font-semibold text-on-surface mb-md">Recent Analyses</h3>
          <div className="space-y-sm">
            {insights.map((i: any) => (
              <div key={i.taskId} className="bg-surface-container-lowest border border-outline-variant rounded-xl p-md flex items-center gap-md">
                <div className="flex-1">
                  <p className="font-label-md text-on-surface">Task #{i.taskId}</p>
                  <p className="font-body-sm text-on-surface-variant">{i.reasons?.[0]}</p>
                </div>
                <div className="flex items-center gap-sm">
                  <span className="font-label-md text-on-surface-variant">{i.riskScore}/100</span>
                  <RiskBadge risk={i.risk} />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
