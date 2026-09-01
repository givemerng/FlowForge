import React, { useEffect, useState } from 'react';
import { notificationApi } from '../../services/notificationApi';
import { BellOff, Loader2, CheckCheck } from 'lucide-react';

export const Notifications: React.FC = () => {
  const [notifs, setNotifs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      const res = await notificationApi.getAll();
      setNotifs(res.data);
    } catch {} finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const markRead = async (id: number) => {
    await notificationApi.markRead(id);
    setNotifs(n => n.map(x => x.id === id ? { ...x, read: true } : x));
  };

  const markAll = async () => {
    await notificationApi.markAllRead();
    setNotifs(n => n.map(x => ({ ...x, read: true })));
  };

  const unreadCount = notifs.filter(n => !n.read).length;

  return (
    <div className="p-lg h-full overflow-auto">
      <div className="flex items-center justify-between mb-lg">
        <div>
          <h1 className="font-h1 text-h1 font-bold text-on-surface">Notifications</h1>
          <p className="text-on-surface-variant font-body-sm mt-xs">
            {unreadCount > 0 ? `${unreadCount} unread notification${unreadCount > 1 ? 's' : ''}` : 'All caught up!'}
          </p>
        </div>
        {unreadCount > 0 && (
          <button onClick={markAll} className="flex items-center gap-xs text-primary hover:underline font-label-md">
            <CheckCheck size={16} /> Mark all read
          </button>
        )}
      </div>

      {loading ? (
        <div className="flex justify-center py-xl"><Loader2 size={32} className="animate-spin text-primary" /></div>
      ) : notifs.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-2xl border-2 border-dashed border-outline-variant rounded-xl">
          <BellOff size={48} className="text-on-surface-variant opacity-30 mb-md" />
          <p className="font-label-lg text-on-surface-variant">No notifications</p>
        </div>
      ) : (
        <div className="space-y-sm max-w-2xl">
          {notifs.map(n => (
            <div key={n.id}
              className={`p-md rounded-xl border cursor-pointer transition-all ${
                n.read ? 'bg-surface-container-lowest border-outline-variant' : 'border-primary/30 bg-primary-container/10 hover:bg-primary-container/15'
              }`}
              onClick={() => !n.read && markRead(n.id)}>
              <div className="flex items-start gap-sm">
                <div className={`w-2 h-2 rounded-full mt-1.5 shrink-0 ${n.read ? 'bg-outline-variant' : 'bg-primary'}`} />
                <div className="flex-1">
                  <p className={`font-label-md ${n.read ? 'text-on-surface-variant' : 'text-on-surface font-semibold'}`}>{n.title}</p>
                  {n.body && <p className="font-body-sm text-on-surface-variant mt-xs">{n.body}</p>}
                  <p className="font-body-sm text-on-surface-variant/60 mt-xs text-xs">
                    {new Date(n.createdAt).toLocaleString()}
                  </p>
                </div>
                {!n.read && (
                  <button onClick={e => { e.stopPropagation(); markRead(n.id); }}
                    className="text-xs text-primary hover:underline shrink-0">Mark read</button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
