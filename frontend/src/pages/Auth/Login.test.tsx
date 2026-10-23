import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import api from '../../services/api';
import { Login } from './Login';

vi.mock('../../services/api', () => ({
  default: { post: vi.fn() },
}));

const renderLogin = () => render(
  <MemoryRouter initialEntries={['/login']}>
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/dashboard" element={<div>Dashboard loaded</div>} />
      <Route path="/register" element={<div>Register</div>} />
    </Routes>
  </MemoryRouter>
);

describe('Login', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('stores the session and navigates after successful authentication', async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: { token: 'jwt-token', username: 'manager', role: 'MANAGER', id: 7 },
    });
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText('Enter your username'), 'manager');
    await user.type(screen.getByLabelText('Password'), 'manager123!');
    await user.click(screen.getByRole('button', { name: 'Sign In' }));

    expect(await screen.findByText('Dashboard loaded')).toBeInTheDocument();
    expect(localStorage.getItem('token')).toBe('jwt-token');
    expect(localStorage.getItem('role')).toBe('MANAGER');
  });

  it('shows the API message after rejected authentication', async () => {
    vi.mocked(api.post).mockRejectedValue({ response: { data: { message: 'Invalid username or password' } } });
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText('Enter your username'), 'manager');
    await user.type(screen.getByLabelText('Password'), 'wrong-password');
    await user.click(screen.getByRole('button', { name: 'Sign In' }));

    expect(await screen.findByText('Invalid username or password')).toBeInTheDocument();
    expect(localStorage.getItem('token')).toBeNull();
  });
});
