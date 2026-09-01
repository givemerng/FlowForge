import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { KanbanColumn } from '../../components/ui/KanbanColumn';
import { Task } from '../../components/ui/TaskCard';
import api from '../../services/api';

export const ProjectBoard: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

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
      } catch (error) {
        setError('Failed to load project tasks.');
      } finally {
        setLoading(false);
      }
    };
    fetchTasks();
  }, [projectId]);

  const todoTasks = tasks.filter(t => t.status === 'TODO');
  const inProgressTasks = tasks.filter(t => t.status === 'IN_PROGRESS');
  const reviewTasks = tasks.filter(t => t.status === 'REVIEW');
  const doneTasks = tasks.filter(t => t.status === 'DONE');

  return (
    <div className="flex-1 overflow-x-auto overflow-y-hidden p-lg flex gap-lg bg-surface-container-low items-start h-full">
      {loading ? (
         <div className="flex items-center justify-center w-full h-full text-on-surface-variant">Loading Board...</div>
      ) : error ? (
         <div className="flex items-center justify-center w-full h-full text-error">{error}</div>
      ) : (
         <>
            <KanbanColumn title="TODO" tasks={todoTasks} indicatorColor="bg-outline" />
            <KanbanColumn title="IN PROGRESS" tasks={inProgressTasks} indicatorColor="bg-primary-container" />
            <KanbanColumn title="REVIEW" tasks={reviewTasks} indicatorColor="bg-tertiary-container" />
            <KanbanColumn title="DONE" tasks={doneTasks} indicatorColor="bg-green-500" />
         </>
      )}
    </div>
  );
};
