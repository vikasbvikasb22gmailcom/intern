import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout.jsx';
import { getNotifications, getUnreadCount, markNotificationRead } from '../services/api.js';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [notifRes, countRes] = await Promise.all([
        getNotifications(),
        getUnreadCount(),
      ]);
      setNotifications(notifRes.data.data.content || []);
      setUnreadCount(countRes.data.data || 0);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const handleMarkRead = async (id) => {
    try {
      await markNotificationRead(id);
      setNotifications(prev =>
        prev.map(n => n.id === id ? { ...n, read: true } : n)
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (e) { console.error(e); }
  };

  const typeIcon = { EMAIL: '📧', SMS: '📱', PUSH: '🔔' };

  return (
    <Layout>
      <div className="page">
        <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1>Notifications 🔔</h1>
            <p>All your appointment alerts and updates</p>
          </div>
          {unreadCount > 0 && (
            <div style={{
              background: 'rgba(0,212,255,0.1)',
              border: '1px solid rgba(0,212,255,0.3)',
              borderRadius: '20px',
              padding: '6px 16px',
              color: 'var(--accent-cyan)',
              fontSize: '13px',
              fontWeight: 700,
            }}>
              {unreadCount} unread
            </div>
          )}
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: 60 }}>
            <div className="spinner" style={{ width: 40, height: 40 }} />
          </div>
        ) : notifications.length === 0 ? (
          <div className="card" style={{ textAlign: 'center', padding: '60px' }}>
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>🔔</div>
            <h3 style={{ fontWeight: 700, marginBottom: '8px' }}>No Notifications Yet</h3>
            <p style={{ color: 'var(--text-muted)' }}>You'll receive alerts when appointments are confirmed.</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {notifications.map(n => (
              <div key={n.id} className="card slide-in" style={{
                borderLeft: `3px solid ${n.read ? 'var(--border)' : 'var(--accent-cyan)'}`,
                opacity: n.read ? 0.7 : 1,
                padding: '16px 20px',
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '12px' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
                      <span style={{ fontSize: '18px' }}>{typeIcon[n.type] || '🔔'}</span>
                      <span style={{ fontWeight: 700, color: 'var(--text-primary)', fontSize: '14px' }}>{n.title}</span>
                      {!n.read && (
                        <span style={{
                          width: 8, height: 8, borderRadius: '50%',
                          background: 'var(--accent-cyan)',
                          display: 'inline-block', flexShrink: 0,
                        }} />
                      )}
                    </div>
                    <pre style={{
                      fontSize: '13px',
                      color: 'var(--text-secondary)',
                      whiteSpace: 'pre-wrap',
                      fontFamily: 'var(--font-display)',
                      lineHeight: 1.6,
                      margin: 0,
                    }}>{n.message}</pre>
                    <div style={{
                      marginTop: '10px',
                      fontSize: '11px',
                      color: 'var(--text-muted)',
                      display: 'flex', gap: '16px',
                    }}>
                      <span>🕐 {n.createdAt?.replace('T', ' ').substring(0, 16)}</span>
                      <span style={{ color: n.sent ? 'var(--accent-green)' : 'var(--accent-red)' }}>
                        {n.sent ? '✓ Sent' : '✗ Failed'}
                      </span>
                      <span style={{ color: 'var(--accent-cyan)' }}>{n.type}</span>
                    </div>
                  </div>
                  {!n.read && (
                    <button className="btn btn-secondary" onClick={() => handleMarkRead(n.id)}
                      style={{ fontSize: '11px', padding: '5px 10px', flexShrink: 0 }}>
                      Mark Read
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}
