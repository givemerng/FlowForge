import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it } from 'vitest';
import Sidebar from './Sidebar';

const renderSidebar = (role: string) => {
  localStorage.setItem('role', role);
  render(
    <MemoryRouter>
      <Sidebar />
    </MemoryRouter>
  );
};

describe('Sidebar permissions', () => {
  beforeEach(() => localStorage.clear());

  it('hides management navigation from members', () => {
    renderSidebar('MEMBER');
    expect(screen.queryByText('New Project')).not.toBeInTheDocument();
    expect(screen.queryByText('Jobs')).not.toBeInTheDocument();
    expect(screen.queryByText('Monitoring')).not.toBeInTheDocument();
    expect(screen.queryByText('Users')).not.toBeInTheDocument();
  });

  it('shows operational navigation to managers', () => {
    renderSidebar('MANAGER');
    expect(screen.getByText('New Project')).toBeInTheDocument();
    expect(screen.getByText('Jobs')).toBeInTheDocument();
    expect(screen.getByText('Monitoring')).toBeInTheDocument();
    expect(screen.queryByText('Users')).not.toBeInTheDocument();
  });

  it('shows all management navigation to administrators', () => {
    renderSidebar('ADMIN');
    expect(screen.getByText('New Project')).toBeInTheDocument();
    expect(screen.getByText('Jobs')).toBeInTheDocument();
    expect(screen.getByText('Monitoring')).toBeInTheDocument();
    expect(screen.getByText('Users')).toBeInTheDocument();
  });
});
