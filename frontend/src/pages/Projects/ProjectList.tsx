import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { projectApi } from '../../services/projectApi';
import { Plus, Loader2, Folder, Users, MoreVertical, Search } from 'lucide-react';

export const ProjectList: React.FC = () => {
  const [projects, setProjects] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [createForm, setCreateForm] = useState({ name: '', description: '' });
  const [creating, setCreating] = useState(false);

  const load = async () => {
    try {
      const res = await projectApi.getAll();
      setProjects(res.data);
    } catch { setError('Failed to load projects.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreating(true);
    try {
      await projectApi.create(createForm);
      setShowCreate(false);
      setCreateForm({ name: '', description: '' });
      load();
    } catch { setError('Failed to create project.'); }
    finally { setCreating(false); }
  };

  const filtered = projects.filter(p =>
    p.name.toLowerCase().includes(search.toLowerCase()) ||
    (p.description || '').toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="p-lg space-y-lg h-full overflow-auto">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-h1 text-h1 font-bold text-on-surface">Projects</h1>
          <p className="text-on-surface-variant font-body-sm mt-xs">{projects.length} project{projects.length !== 1 ? 's' : ''} total</p>
        </div>
        <button onClick={() => setShowCreate(true)}
          className="flex items-center gap-xs bg-primary text-on-primary px-md py-sm rounded-lg text-label-md font-label-md hover:bg-primary/90 transition-colors">
          <Plus size={16} /> New Project
        </button>
      </div>

      {/* Search */}
      <div className="relative">
        <Search size={16} className="absolute left-sm top-1/2 -translate-y-1/2 text-on-surface-variant" />
        <input value={search} onChange={e => setSearch(e.target.value)}
          placeholder="Search projects..."
          className="w-full pl-[36px] pr-sm py-sm bg-surface-container-lowest border border-outline-variant rounded-lg text-body-sm focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none" />
      </div>

      {loading && <div className="flex justify-center py-xl"><Loader2 size={32} className="animate-spin text-primary" /></div>}
      {error && <div className="p-md bg-error-container text-on-error-container rounded-lg">{error}</div>}

      {!loading && filtered.length === 0 && (
        <div className="text-center py-2xl border-2 border-dashed border-outline-variant rounded-xl">
          <Folder size={48} className="mx-auto text-on-surface-variant opacity-30 mb-sm" />
          <p className="font-label-lg text-on-surface-variant">No projects found</p>
          <p className="font-body-sm text-on-surface-variant mt-xs">Create your first project to get started.</p>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
        {filtered.map(p => (
          <div key={p.id} className="bg-surface-container-lowest rounded-xl border border-outline-variant hover:border-primary/40 hover:shadow-md transition-all group">
            <div className="p-lg">
              <div className="flex items-start justify-between mb-md">
                <div className="w-10 h-10 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center font-bold text-label-lg">
                  {p.name.charAt(0).toUpperCase()}
                </div>
                <button className="opacity-0 group-hover:opacity-100 transition-opacity text-on-surface-variant hover:text-on-surface">
                  <MoreVertical size={18} />
                </button>
              </div>
              <h3 className="font-label-lg text-label-lg font-semibold text-on-surface mb-xs">{p.name}</h3>
              <p className="font-body-sm text-on-surface-variant line-clamp-2">{p.description || 'No description'}</p>
            </div>
            <div className="px-lg pb-lg flex items-center justify-between">
              <div className="flex items-center gap-xs text-on-surface-variant">
                <Users size={14} />
                <span className="font-body-sm">Team</span>
              </div>
              <Link to={`/projects/${p.id}/board`}
                className="text-primary font-label-md text-label-md hover:underline">
                Open Board →
              </Link>
            </div>
          </div>
        ))}
      </div>

      {/* Create Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="bg-surface-container-lowest rounded-xl border border-outline-variant shadow-xl w-[480px] p-xl">
            <h2 className="font-h2 text-h2 font-bold text-on-surface mb-lg">New Project</h2>
            <form onSubmit={handleCreate} className="space-y-md">
              <div>
                <label className="block font-label-md text-label-md text-on-surface mb-xs">Project Name</label>
                <input value={createForm.name} onChange={e => setCreateForm(f => ({ ...f, name: e.target.value }))}
                  required placeholder="My awesome project"
                  className="w-full px-sm py-[8px] border border-outline-variant rounded-md text-body-sm focus:border-primary focus:outline-none" />
              </div>
              <div>
                <label className="block font-label-md text-label-md text-on-surface mb-xs">Description</label>
                <textarea value={createForm.description} onChange={e => setCreateForm(f => ({ ...f, description: e.target.value }))}
                  rows={3} placeholder="What is this project about?"
                  className="w-full px-sm py-[8px] border border-outline-variant rounded-md text-body-sm focus:border-primary focus:outline-none resize-none" />
              </div>
              <div className="flex gap-sm pt-sm">
                <button type="button" onClick={() => setShowCreate(false)}
                  className="flex-1 py-sm border border-outline-variant rounded-lg text-on-surface hover:bg-surface-container transition-colors">
                  Cancel
                </button>
                <button type="submit" disabled={creating}
                  className="flex-1 py-sm bg-primary text-on-primary rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-70 flex items-center justify-center gap-xs">
                  {creating && <Loader2 size={14} className="animate-spin" />}
                  Create Project
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
