import { TaskCard, Task } from './TaskCard';
import { MoreHorizontal, Plus } from 'lucide-react';

interface KanbanColumnProps {
  title: string;
  tasks: Task[];
  indicatorColor: string;
}

export const KanbanColumn: React.FC<KanbanColumnProps> = ({ title, tasks, indicatorColor }) => {
  return (
    <div className="w-[320px] shrink-0 flex flex-col max-h-full">
      <div className="flex justify-between items-center mb-sm px-xs">
        <div className="flex items-center gap-sm">
          <span className={`w-2 h-2 rounded-full ${indicatorColor}`}></span>
          <h3 className="font-label-md text-label-md text-on-surface font-semibold">{title}</h3>
          <span className="bg-surface-container-high text-on-surface-variant font-metadata text-metadata px-[6px] py-[2px] rounded-full">
            {tasks.length}
          </span>
        </div>
        <button className="text-on-surface-variant hover:text-primary">
          <MoreHorizontal size={20} />
        </button>
      </div>
      
      <div className="flex-1 overflow-y-auto space-y-sm pr-xs pb-lg custom-scrollbar">
        {tasks.length === 0 ? (
           <div className="flex flex-col items-center justify-center py-xl border-2 border-dashed border-outline-variant/50 rounded-lg">
               <p className="font-metadata text-metadata text-on-surface-variant">No tasks</p>
           </div>
        ) : (
           tasks.map(task => <TaskCard key={task.id} task={task} />)
        )}
      </div>
      
      <button className="mt-sm flex items-center gap-xs text-on-surface-variant hover:text-on-surface hover:bg-surface-container px-sm py-xs rounded transition-colors font-label-md text-label-md">
        <Plus size={16} /> Add Task
      </button>
    </div>
  );
};
