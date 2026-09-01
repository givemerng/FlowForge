import React, { useState } from 'react';
import { Settings as SettingsIcon, User, Lock, Bell, Save, Loader2 } from 'lucide-react';

export const Settings: React.FC = () => {
  const [activeTab, setActiveTab] = useState('profile');
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [profile, setProfile] = useState({
    displayName: localStorage.getItem('username') || '',
    email: '',
  });
  const [notifications, setNotifications] = useState({
    taskAssigned: true,
    taskCompleted: true,
    jobFailed: true,
    reportReady: true,
  });

  const handleSave = async () => {
    setSaving(true);
    await new Promise(r => setTimeout(r, 800));
    setSaving(false);
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  const tabs = [
    { id: 'profile', label: 'Profile', icon: User },
    { id: 'notifications', label: 'Notifications', icon: Bell },
    { id: 'security', label: 'Security', icon: Lock },
  ];

  return (
    <div className="p-lg h-full overflow-auto space-y-lg">
      <div className="flex items-center gap-sm">
        <SettingsIcon size={28} className="text-primary" />
        <div>
          <h1 className="font-h1 text-h1 font-bold text-on-surface">Settings</h1>
          <p className="text-on-surface-variant font-body-sm">Manage your account and preferences</p>
        </div>
      </div>

      <div className="flex gap-lg">
        {/* Tabs */}
        <div className="w-48 shrink-0 space-y-xs">
          {tabs.map(t => {
            const Icon = t.icon;
            return (
              <button key={t.id} onClick={() => setActiveTab(t.id)}
                className={`w-full flex items-center gap-sm px-md py-sm rounded-lg text-left transition-colors font-label-md ${
                  activeTab === t.id ? 'bg-primary text-on-primary' : 'hover:bg-surface-container text-on-surface-variant'
                }`}>
                <Icon size={16} /> {t.label}
              </button>
            );
          })}
        </div>

        {/* Content */}
        <div className="flex-1 bg-surface-container-lowest border border-outline-variant rounded-xl p-lg">
          {activeTab === 'profile' && (
            <div className="space-y-md max-w-md">
              <h3 className="font-label-lg font-semibold text-on-surface">Profile Settings</h3>
              <div>
                <label className="block font-label-md text-on-surface mb-xs">Display Name</label>
                <input value={profile.displayName}
                  onChange={e => setProfile(p => ({ ...p, displayName: e.target.value }))}
                  className="w-full px-sm py-[8px] border border-outline-variant rounded-md text-body-sm focus:border-primary focus:outline-none" />
              </div>
              <div>
                <label className="block font-label-md text-on-surface mb-xs">Email</label>
                <input value={profile.email} type="email"
                  onChange={e => setProfile(p => ({ ...p, email: e.target.value }))}
                  className="w-full px-sm py-[8px] border border-outline-variant rounded-md text-body-sm focus:border-primary focus:outline-none" />
              </div>
            </div>
          )}

          {activeTab === 'notifications' && (
            <div className="space-y-md max-w-md">
              <h3 className="font-label-lg font-semibold text-on-surface">Notification Preferences</h3>
              {Object.entries(notifications).map(([key, val]) => (
                <label key={key} className="flex items-center justify-between py-sm border-b border-outline-variant/50 cursor-pointer">
                  <span className="font-label-md text-on-surface capitalize">{key.replace(/([A-Z])/g, ' $1')}</span>
                  <input type="checkbox" checked={val}
                    onChange={e => setNotifications(n => ({ ...n, [key]: e.target.checked }))}
                    className="w-4 h-4 rounded text-primary" />
                </label>
              ))}
            </div>
          )}

          {activeTab === 'security' && (
            <div className="space-y-md max-w-md">
              <h3 className="font-label-lg font-semibold text-on-surface">Security Settings</h3>
              <p className="text-on-surface-variant font-body-sm">Password changes are managed through the backend API. Use your admin credentials to update security settings.</p>
              <div className="p-md bg-primary-container/20 border border-primary/20 rounded-lg">
                <p className="font-label-md text-on-surface">🔒 JWT tokens expire after 24 hours.</p>
                <p className="font-body-sm text-on-surface-variant mt-xs">You will be automatically logged out when your session expires.</p>
              </div>
              <button onClick={() => { localStorage.removeItem('token'); window.location.href = '/login'; }}
                className="text-error hover:underline font-label-md text-body-sm">
                Logout from all sessions
              </button>
            </div>
          )}

          <div className="mt-lg">
            <button onClick={handleSave} disabled={saving}
              className="flex items-center gap-xs bg-primary text-on-primary px-lg py-sm rounded-lg hover:bg-primary/90 disabled:opacity-70 transition-colors font-label-md">
              {saving ? <Loader2 size={14} className="animate-spin" /> : <Save size={14} />}
              {saved ? 'Saved!' : 'Save Changes'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
