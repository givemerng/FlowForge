import { Calendar, Paperclip, AlignLeft } from 'lucide-react';

import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

export interface Task {
  id: number;
  title: string;
  status: string;
  priority: string;
  createdAt: string;
  assignedTo?: { id: number, username: string };
  labels?: { id: number, name: string, color: string }[];
}

interface TaskCardProps {
  task: Task;
}

export const TaskCard: React.FC<TaskCardProps> = ({ task }) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ 
    id: task.id,
    data: { status: task.status }
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.4 : 1,
  };

  return (
    <div 
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      className="bg-surface-container-lowest border border-outline-variant rounded-lg p-md hover:border-primary/50 hover:shadow-[0_4px_12px_rgba(0,0,0,0.08)] transition-all cursor-grab group mb-sm touch-none"
    >
      <div className="flex justify-between items-start mb-sm">
        <span className="font-mono-id text-mono-id text-on-surface-variant">TSK-{task.id}</span>
        {task.assignedTo && (
           <div className="w-5 h-5 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center text-[10px] font-bold">
               {task.assignedTo.username.charAt(0).toUpperCase()}
           </div>
        )}
      </div>
      <h4 className="font-body-sm text-body-sm text-on-surface font-medium leading-tight mb-md">{task.title}</h4>
      <div className="flex flex-wrap gap-xs mb-md">
        <span className={`px-xs py-[2px] rounded border font-metadata text-metadata ${
            task.priority === 'HIGH' || task.priority === 'CRITICAL' 
              ? 'bg-error-container/30 text-error border-error-container' 
              : 'bg-surface-container text-on-surface-variant border-outline-variant'
          }`}>
          {task.priority}
        </span>
        {task.labels?.map(label => (
          <span 
            key={label.id} 
            className="px-xs py-[2px] rounded border font-metadata text-metadata text-white" 
            style={{ backgroundColor: label.color, borderColor: label.color }}
          >
            {label.name}
          </span>
        ))}
      </div>
      <div className="flex justify-between items-center border-t border-surface-container pt-sm mt-xs">
        <div className="flex items-center gap-xs text-on-surface-variant font-metadata text-metadata">
          <Calendar size={14} />
          {new Date(task.createdAt).toLocaleDateString()}
        </div>
        <div className="flex items-center gap-xs text-on-surface-variant font-metadata text-metadata opacity-0 group-hover:opacity-100 transition-opacity">
          <AlignLeft size={14} />
          <Paperclip size={14} />
        </div>
      </div>
    </div>
  );
};
