import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const navItems = {
  ROLE_PATIENT: [
    { icon: '⊞', label: 'Dashboard', path: '/patient' },
    { icon: '📅', label: 'Book Appointment', path: '/patient/book' },
    { icon: '📋', label: 'My Appointments', path: '/patient/appointments' },
    { icon: '🔔', label: 'Notifications', path: '/patient/notifications' },
    { icon: '👤', label: 'Profile', path: '/patient/profile' },
  ],
  ROLE_DOCTOR: [
    { icon: '⊞', label: 'Dashboard', path: '/doctor' },
    { icon: '🏥', label: "Today's Queue", path: '/doctor/queue' },
    { icon: '📋', label: 'Appointments', path: '/doctor/appointments' },
    { icon: '👤', label: 'Profile', path: '/doctor/profile' },
  ],
  ROLE_ADMIN: [
    { icon: '⊞', label: 'Dashboard', path: '/admin' },
    { icon: '👨‍⚕️', label: 'Doctors', path: '/admin/doctors' },
    { icon: '👥', label: 'Patients', path: '/admin/patients' },
    { icon: '📅', label: 'Appointments', path: '/admin/appointments' },
  ],
};

export default function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const items = navItems[user?.role] || [];

  return (
    <aside style={{
      width: collapsed ? '64px' : '240px',
      minHeight: '100vh',
      background: 'var(--bg-secondary)',
      borderRight: '1px solid var(--border)',
      display: 'flex',
      flexDirection: 'column',
      transition: 'width 0.3s ease',
      position: 'fixed',
      top: 0, left: 0, bottom: 0,
      zIndex: 100,
    }}>
      {/* Logo */}
      <div style={{
        padding: '20px 16px',
        borderBottom: '1px solid var(--border)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
      }}>
        {!collapsed && (
          <div>
            <div style={{ fontSize: '16px', fontWeight: 800, color: 'var(--accent-cyan)' }}>
              🏥 MediQueue
            </div>
            <div style={{ fontSize: '10px', color: 'var(--text-muted)', letterSpacing: '2px', marginTop: 2 }}>
              HOSPITAL SYSTEM
            </div>
          </div>
        )}
        <button onClick={() => setCollapsed(!collapsed)} style={{
          background: 'none', border: 'none', color: 'var(--text-muted)',
          cursor: 'pointer', fontSize: '18px', padding: '4px',
        }}>
          {collapsed ? '→' : '←'}
        </button>
      </div>

      {/* User Info */}
      {!collapsed && (
        <div style={{
          padding: '16px',
          borderBottom: '1px solid var(--border)',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
        }}>
          <div style={{
            width: 36, height: 36,
            borderRadius: '50%',
            background: 'linear-gradient(135deg, var(--accent-cyan), var(--accent-blue))',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '14px', fontWeight: 700, color: '#000',
            flexShrink: 0,
          }}>
            {user?.firstName?.[0]}{user?.lastName?.[0]}
          </div>
          <div style={{ overflow: 'hidden' }}>
            <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {user?.firstName} {user?.lastName}
            </div>
            <div style={{ fontSize: '10px', color: 'var(--accent-cyan)', letterSpacing: '1px' }}>
              {user?.role?.replace('ROLE_', '')}
            </div>
          </div>
        </div>
      )}

      {/* Nav Items */}
      <nav style={{ flex: 1, padding: '12px 8px', overflowY: 'auto' }}>
        {items.map(item => {
          const active = location.pathname === item.path;
          return (
            <button key={item.path} onClick={() => navigate(item.path)}
              style={{
                width: '100%',
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                padding: collapsed ? '12px' : '10px 12px',
                justifyContent: collapsed ? 'center' : 'flex-start',
                background: active ? 'rgba(0,212,255,0.1)' : 'transparent',
                border: active ? '1px solid rgba(0,212,255,0.2)' : '1px solid transparent',
                borderRadius: 'var(--radius-sm)',
                color: active ? 'var(--accent-cyan)' : 'var(--text-secondary)',
                cursor: 'pointer',
                marginBottom: '4px',
                transition: 'var(--transition)',
                fontSize: '14px',
                fontFamily: 'var(--font-display)',
                fontWeight: active ? 700 : 400,
              }}
              title={collapsed ? item.label : ''}
            >
              <span style={{ fontSize: '16px' }}>{item.icon}</span>
              {!collapsed && <span>{item.label}</span>}
            </button>
          );
        })}
      </nav>

      {/* Logout */}
      <div style={{ padding: '12px 8px', borderTop: '1px solid var(--border)' }}>
        <button onClick={logout} style={{
          width: '100%',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          padding: collapsed ? '12px' : '10px 12px',
          justifyContent: collapsed ? 'center' : 'flex-start',
          background: 'transparent',
          border: '1px solid transparent',
          borderRadius: 'var(--radius-sm)',
          color: 'var(--accent-red)',
          cursor: 'pointer',
          fontSize: '14px',
          fontFamily: 'var(--font-display)',
          transition: 'var(--transition)',
        }}>
          <span>🚪</span>
          {!collapsed && <span>Logout</span>}
        </button>
      </div>
    </aside>
  );
}
