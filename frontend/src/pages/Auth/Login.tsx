import React, { useState } from 'react';
import api from '../../services/api';
import { AxiosError } from 'axios';
import { useNavigate } from 'react-router-dom';

export const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/auth/login', { username, password });
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        // Set user info to some global state if necessary, or just redirect
        navigate('/dashboard');
      }
    } catch (err) {
      const error = err as AxiosError<{ message?: string } | string>;
      const responseMessage = typeof error.response?.data === 'string'
        ? error.response.data
        : error.response?.data?.message;
      setError(responseMessage || 'Failed to login. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-surface w-full">
      <div className="bg-surface-container-lowest p-xl rounded-xl border border-outline-variant shadow-md w-[400px]">
        <div className="flex flex-col items-center mb-lg">
          <div className="w-12 h-12 rounded bg-primary-container flex items-center justify-center text-on-primary-container mb-sm">
            <span className="material-symbols-outlined text-h1">architecture</span>
          </div>
          <h2 className="font-h2 text-h2 font-bold text-on-surface">Welcome Back</h2>
          <p className="font-body-sm text-body-sm text-on-surface-variant text-center mt-xs">
            Sign in to access FlowForge
          </p>
        </div>

        {error && (
          <div className="mb-md p-sm bg-error-container text-on-error-container text-body-sm font-body-sm rounded">
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-md">
          <div>
            <label className="block font-label-md text-label-md text-on-surface mb-xs">Username</label>
            <input 
              type="text" 
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-sm py-[8px] bg-surface-container-lowest border border-outline-variant rounded-md text-body-sm focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none"
              placeholder="Enter your username"
              required
            />
          </div>
          <div>
            <label className="block font-label-md text-label-md text-on-surface mb-xs">Password</label>
            <input 
              type="password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-sm py-[8px] bg-surface-container-lowest border border-outline-variant rounded-md text-body-sm focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none"
              placeholder="••••••••"
              required
            />
          </div>
          <div className="flex items-center justify-between mt-sm">
            <label className="flex items-center gap-xs cursor-pointer">
              <input type="checkbox" className="rounded border-outline-variant text-primary focus:ring-primary" />
              <span className="font-body-sm text-body-sm text-on-surface-variant">Remember me</span>
            </label>
            <a href="#" className="font-label-md text-label-md text-primary hover:underline">Forgot password?</a>
          </div>
          <button 
            type="submit" 
            disabled={loading}
            className="w-full bg-primary text-on-primary font-label-md text-label-md py-sm rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-70 mt-md flex justify-center"
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        
        <p className="font-body-sm text-body-sm text-center text-on-surface-variant mt-lg">
          Don't have an account? <a href="#" className="text-primary hover:underline font-semibold">Register here</a>
        </p>
      </div>
    </div>
  );
};
