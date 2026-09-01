import { render, screen } from '@testing-library/react';
import { vi } from 'vitest';
import { TaskCard, Task } from './TaskCard';
import { describe, it, expect } from 'vitest';

// Mock DndContext hooks as TaskCard relies on useSortable
vi.mock('@dnd-kit/sortable', () => ({
  useSortable: () => ({
    attributes: {},
    listeners: {},
    setNodeRef: vi.fn(),
    transform: null,
    transition: undefined,
    isDragging: false,
  }),
}));

describe('TaskCard', () => {
  const mockTask: Task = {
    id: 123,
    title: 'Test Task Creation',
    status: 'TODO',
    priority: 'HIGH',
    createdAt: new Date().toISOString(),
    assignedTo: { id: 1, username: 'testuser' }
  };

  it('renders task details correctly', () => {
    render(<TaskCard task={mockTask} />);
    
    // Check Title
    expect(screen.getByText('Test Task Creation')).toBeInTheDocument();
    
    // Check ID
    expect(screen.getByText('TSK-123')).toBeInTheDocument();
    
    // Check Priority
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    
    // Check Assigned To initial
    expect(screen.getByText('T')).toBeInTheDocument();
  });

  it('applies correct priority styling', () => {
    render(<TaskCard task={mockTask} />);
    const priorityBadge = screen.getByText('HIGH');
    expect(priorityBadge).toHaveClass('text-error');
  });
});
