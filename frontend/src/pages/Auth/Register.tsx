import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../../services/authApi';
import { User, Mail, Lock, ArrowRight, Loader2, CheckCircle } from 'lucide-react';

export const Register: React.FC = () => {
  const [form, setForm] = useState({ username: '', email: '', password: '', confirm: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.confirm) { setError('Passwords do not match.'); return; }
    if (form.password.length < 6) { setError('Password must be at least 6 characters.'); return; }
    setLoading(true);
    try {
      await authApi.register({ username: form.username, email: form.email, password: form.password });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      setError(err.response?.data || 'Registration failed. Please try again.');
    } finally { setLoading(false); }
  };

  if (success) return (
    <div className="min-h-screen flex items-center justify-center bg-surface">
      <div className="text-center space-y-md">
        <CheckCircle size={64} className="text-green-500 mx-auto" />
        <h2 className="font-h2 text-h2 text-on-surface">Account Created!</h2>
        <p className="text-on-surface-variant">Redirecting you to login...</p>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen flex items-center justify-center bg-surface">
      <div className="bg-surface-container-lowest p-xl rounded-xl border border-outline-variant shadow-md w-[420px]">
        <div className="flex flex-col items-center mb-lg">
          <div className="w-12 h-12 rounded-lg bg-primary flex items-center justify-center text-on-primary mb-sm">
            <span className="font-mono font-bold text-lg">FF</span>
          </div>
          <h2 className="font-h2 text-h2 font-bold text-on-surface">Create Account</h2>
          <p className="font-body-sm text-body-sm text-on-surface-variant mt-xs">Join FlowForge today</p>
        </div>

        {error && (
          <div className="mb-md p-sm bg-error-container text-on-error-container text-body-sm rounded flex items-center gap-xs">
            <span>⚠</span> {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-md">
          {[
            { icon: User, label: 'Username', name: 'username', type: 'text', placeholder: 'johndoe' },
            { icon: Mail, label: 'Email', name: 'email', type: 'email', placeholder: 'john@example.com' },
            { icon: Lock, label: 'Password', name: 'password', type: 'password', placeholder: '••••••••' },
            { icon: Lock, label: 'Confirm Password', name: 'confirm', type: 'password', placeholder: '••••••••' },
          ].map(({ icon: Icon, label, name, type, placeholder }) => (
            <div key={name}>
              <label className="block font-label-md text-label-md text-on-surface mb-xs">{label}</label>
              <div className="relative">
                <Icon size={16} className="absolute left-sm top-1/2 -translate-y-1/2 text-on-surface-variant" />
                <input
                  type={type} name={name} value={(form as any)[name]}
                  onChange={handleChange} placeholder={placeholder} required
                  className="w-full pl-[36px] pr-sm py-[8px] bg-surface-container-lowest border border-outline-variant rounded-md text-body-sm focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none"
                />
              </div>
            </div>
          ))}

          <button type="submit" disabled={loading}
            className="w-full bg-primary text-on-primary font-label-md py-sm rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-70 flex items-center justify-center gap-xs mt-md">
            {loading ? <Loader2 size={16} className="animate-spin" /> : <ArrowRight size={16} />}
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <p className="text-center text-on-surface-variant font-body-sm mt-lg">
          Already have an account?{' '}
          <Link to="/login" className="text-primary hover:underline font-semibold">Sign in</Link>
        </p>
      </div>
    </div>
  );
};
