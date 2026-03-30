import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login, register } from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';

export default function LoginPage() {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', phone: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = mode === 'login'
        ? await login({ email: form.email, password: form.password })
        : await register(form);
      const data = res.data.data;
      loginUser(data, data.accessToken);
      if (data.role === 'ROLE_ADMIN') navigate('/admin');
      else if (data.role === 'ROLE_DOCTOR') navigate('/doctor');
      else navigate('/patient');
    } catch (err) {
      setError(err.response?.data?.error || 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      background: 'var(--bg-primary)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '20px',
      position: 'relative',
      overflow: 'hidden',
    }}>
      {/* Background decoration */}
      <div style={{
        position: 'absolute', top: '-20%', right: '-10%',
        width: '600px', height: '600px',
        background: 'radial-gradient(circle, rgba(0,212,255,0.05) 0%, transparent 70%)',
        pointerEvents: 'none',
      }} />
      <div style={{
        position: 'absolute', bottom: '-20%', left: '-10%',
        width: '500px', height: '500px',
        background: 'radial-gradient(circle, rgba(59,130,246,0.05) 0%, transparent 70%)',
        pointerEvents: 'none',
      }} />

      <div className="fade-in" style={{ width: '100%', maxWidth: '420px' }}>
        {/* Logo */}
        <div style={{ textAlign: 'center', marginBottom: '40px' }}>
          <div style={{ fontSize: '48px', marginBottom: '8px' }}>🏥</div>
          <h1 style={{ fontSize: '28px', fontWeight: 800, color: 'var(--accent-cyan)' }}>MediQueue</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px', letterSpacing: '2px', marginTop: 4 }}>
            HOSPITAL QUEUE SYSTEM
          </p>
        </div>

        {/* Card */}
        <div className="card" style={{ padding: '32px' }}>
          {/* Tabs */}
          <div style={{
            display: 'flex', background: 'var(--bg-secondary)',
            borderRadius: 'var(--radius-sm)', padding: '4px', marginBottom: '28px',
          }}>
            {['login', 'register'].map(tab => (
              <button key={tab} onClick={() => { setMode(tab); setError(''); }}
                style={{
                  flex: 1, padding: '8px',
                  background: mode === tab ? 'var(--bg-card-hover)' : 'transparent',
                  border: mode === tab ? '1px solid var(--border-bright)' : '1px solid transparent',
                  borderRadius: 'var(--radius-sm)',
                  color: mode === tab ? 'var(--accent-cyan)' : 'var(--text-muted)',
                  cursor: 'pointer',
                  fontFamily: 'var(--font-display)',
                  fontWeight: 600,
                  fontSize: '13px',
                  transition: 'var(--transition)',
                  textTransform: 'capitalize',
                }}>
                {tab}
              </button>
            ))}
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            {mode === 'register' && (
              <div className="grid-2" style={{ gap: '12px' }}>
                <div className="input-group">
                  <label>FIRST NAME</label>
                  <input placeholder="John" value={form.firstName}
                    onChange={e => setForm({ ...form, firstName: e.target.value })} required />
                </div>
                <div className="input-group">
                  <label>LAST NAME</label>
                  <input placeholder="Doe" value={form.lastName}
                    onChange={e => setForm({ ...form, lastName: e.target.value })} required />
                </div>
              </div>
            )}

            <div className="input-group">
              <label>EMAIL</label>
              <input type="email" placeholder="you@example.com" value={form.email}
                onChange={e => setForm({ ...form, email: e.target.value })} required />
            </div>

            {mode === 'register' && (
              <div className="input-group">
                <label>PHONE</label>
                <input placeholder="9876543210" value={form.phone}
                  onChange={e => setForm({ ...form, phone: e.target.value })} required />
              </div>
            )}

            <div className="input-group">
              <label>PASSWORD</label>
              <input type="password" placeholder="••••••••" value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })} required />
            </div>

            <button type="submit" className="btn btn-primary btn-full" disabled={loading}
              style={{ marginTop: '8px' }}>
              {loading ? <span className="spinner" style={{ width: 18, height: 18, borderWidth: 2 }} />
                : mode === 'login' ? '→ Sign In' : '→ Create Account'}
            </button>
          </form>

          {/* Quick login hint */}
          <div style={{
            marginTop: '20px', padding: '12px',
            background: 'var(--bg-secondary)', borderRadius: 'var(--radius-sm)',
            fontSize: '12px', color: 'var(--text-muted)',
          }}>
            <div style={{ fontWeight: 700, color: 'var(--text-secondary)', marginBottom: 6 }}>Quick Login:</div>
            <div>Admin: admin@hospital.com / Admin@123</div>
          </div>
        </div>
      </div>
    </div>
  );
}
