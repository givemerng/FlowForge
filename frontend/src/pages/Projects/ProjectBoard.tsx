import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { KanbanColumn } from '../../components/ui/KanbanColumn';
import { Task } from '../../components/ui/TaskCard';
import api from '../../services/api';
import { taskApi } from '../../services/taskApi';
import { DndContext, DragEndEvent, PointerSensor, useSensor, useSensors, closestCorners } from '@dnd-kit/core';

export const ProjectBoard: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [showLabelsModal, setShowLabelsModal] = useState(false);
  const [labels, setLabels] = useState<{id: number, name: string, color: string}[]>([]);
  const [newLabelName, setNewLabelName] = useState('');
  const [newLabelColor, setNewLabelColor] = useState('#3B82F6');
  
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 5 },
    })
  );

  useEffect(() => {
    const fetchTasks = async () => {
      if (!projectId) {
        setError('Project not found.');
        setLoading(false);
        return;
      }

      try {
        setError('');
        const response = await api.get(`/projects/${projectId}/tasks`);
        setTasks(response.data);
        const labelsResponse = await api.get(`/projects/${projectId}/labels`);
        setLabels(labelsResponse.data);
      } catch (error) {
        setError('Failed to load project tasks or labels.');
      } finally {
        setLoading(false);
      }
    };
    fetchTasks();
  }, [projectId]);

  const handleCreateLabel = async () => {
    if (!newLabelName) return;
    try {
      const response = await api.post(`/projects/${projectId}/labels`, { name: newLabelName, color: newLabelColor });
      setLabels([...labels, response.data]);
      setNewLabelName('');
    } catch (err) {
      alert('Failed to create label');
    }
  };

  const handleDeleteLabel = async (labelId: number) => {
    try {
      await api.delete(`/projects/${projectId}/labels/${labelId}`);
      setLabels(labels.filter(l => l.id !== labelId));
      // update tasks as well optimistically
      setTasks(tasks.map(t => ({
        ...t,
        labels: t.labels?.filter(l => l.id !== labelId)
      })));
    } catch (err) {
      alert('Failed to delete label');
    }
  };

  const todoTasks = tasks.filter(t => t.status === 'TODO');
  const inProgressTasks = tasks.filter(t => t.status === 'IN_PROGRESS');
  const reviewTasks = tasks.filter(t => t.status === 'REVIEW');
  const doneTasks = tasks.filter(t => t.status === 'DONE');

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over) return;

    const taskId = active.id as number;
    let newStatus = over.id as string; // if dropped directly on column
    
    // if dropped on another task, find that task's status
    if (newStatus !== 'TODO' && newStatus !== 'IN PROGRESS' && newStatus !== 'REVIEW' && newStatus !== 'DONE') {
       const overTask = tasks.find(t => t.id === over.id);
       if (overTask) newStatus = overTask.status;
    }
    
    // Map "IN PROGRESS" back to "IN_PROGRESS" enum value
    if (newStatus === 'IN PROGRESS') newStatus = 'IN_PROGRESS';

    const activeTask = tasks.find(t => t.id === taskId);
    if (!activeTask || activeTask.status === newStatus) return;

    const previousStatus = activeTask.status;

    // Optimistic update
    setTasks(prev => prev.map(t => t.id === taskId ? { ...t, status: newStatus } : t));

    try {
      await taskApi.updateStatus(taskId, newStatus);
    } catch (e) {
      // Revert on failure
      setTasks(prev => prev.map(t => t.id === taskId ? { ...t, status: previousStatus } : t));
    }
  };

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <div className="flex items-center justify-between p-lg bg-surface-container-low border-b border-surface-container">
        <h1 className="text-2xl font-bold text-on-surface">Project Board</h1>
        <button 
          onClick={() => setShowLabelsModal(true)}
          className="bg-primary text-on-primary px-4 py-2 rounded font-medium hover:bg-primary/90 transition"
        >
          Manage Labels
        </button>
      </div>
      
      <div className="flex-1 overflow-x-auto p-lg flex gap-lg bg-surface-container-low items-start">
        {loading ? (
           <div className="flex items-center justify-center w-full h-full text-on-surface-variant">Loading Board...</div>
        ) : error ? (
           <div className="flex items-center justify-center w-full h-full text-error">{error}</div>
        ) : (
           <DndContext sensors={sensors} collisionDetection={closestCorners} onDragEnd={handleDragEnd}>
              <KanbanColumn title="TODO" tasks={todoTasks} indicatorColor="bg-outline" />
              <KanbanColumn title="IN PROGRESS" tasks={inProgressTasks} indicatorColor="bg-primary-container" />
              <KanbanColumn title="REVIEW" tasks={reviewTasks} indicatorColor="bg-tertiary-container" />
              <KanbanColumn title="DONE" tasks={doneTasks} indicatorColor="bg-green-500" />
           </DndContext>
        )}
      </div>

      {showLabelsModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-surface-container-lowest p-6 rounded-lg w-full max-w-md border border-outline-variant shadow-xl">
            <h2 className="text-xl font-bold text-on-surface mb-4">Manage Project Labels</h2>
            
            <div className="flex gap-2 mb-6">
              <input 
                type="text" 
                placeholder="Label name"
                className="flex-1 bg-surface-container border border-outline-variant rounded px-3 py-2 text-on-surface focus:outline-none focus:border-primary"
                value={newLabelName}
                onChange={e => setNewLabelName(e.target.value)}
              />
              <input 
                type="color" 
                value={newLabelColor}
                onChange={e => setNewLabelColor(e.target.value)}
                className="w-10 h-10 rounded cursor-pointer border border-outline-variant"
              />
              <button 
                onClick={handleCreateLabel}
                className="bg-primary text-on-primary px-4 py-2 rounded hover:bg-primary/90"
              >
                Add
              </button>
            </div>

            <div className="space-y-2 mb-6 max-h-60 overflow-y-auto">
              {labels.map(label => (
                <div key={label.id} className="flex items-center justify-between p-2 bg-surface-container rounded border border-outline-variant">
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 rounded-full" style={{ backgroundColor: label.color }}></div>
                    <span className="text-on-surface">{label.name}</span>
                  </div>
                  <button 
                    onClick={() => handleDeleteLabel(label.id)}
                    className="text-error hover:text-error/80 text-sm"
                  >
                    Delete
                  </button>
                </div>
              ))}
              {labels.length === 0 && <div className="text-on-surface-variant text-sm">No labels created yet.</div>}
            </div>

            <div className="flex justify-end">
              <button 
                onClick={() => setShowLabelsModal(false)}
                className="bg-surface-container text-on-surface-variant px-4 py-2 rounded hover:bg-surface-container-high transition"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
