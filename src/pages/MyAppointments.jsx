import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout.jsx';
import { getMyAppointments, cancelAppointment, getQueueStatus } from '../services/api.js';

const statusColor = {
  CONFIRMED: 'badge-confirmed', COMPLETED: 'badge-completed',
  CANCELLED: 'badge-cancelled', PENDING: 'badge-pending', IN_PROGRESS: 'badge-in-progress',
};

export default function MyAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(null);
  const [queueModal, setQueueModal] = useState(null);
  const [queueData, setQueueData] = useState(null);

  useEffect(() => { load(); }, []);

  const load = async () => {
    setLoading(true);
    try {
      const res = await getMyAppointments({ page: 0, size: 20 });
      setAppointments(res.data.data.content || []);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const handleCancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
    setCancelling(id);
    try {
      await cancelAppointment(id, 'Cancelled by patient');
      await load();
    } catch (e) { console.error(e); }
    finally { setCancelling(null); }
  };

  const checkQueue = async (id) => {
    setQueueModal(id);
    try {
      const res = await getQueueStatus(id);
      setQueueData(res.data.data);
    } catch (e) { console.error(e); }
  };

  return (
    <Layout>
      <div className="page">
        <div className="page-header">
          <h1>My Appointments</h1>
          <p>View and manage all your appointments</p>
        </div>

        {/* Queue Modal */}
        {queueModal && (
          <div style={{
            position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            zIndex: 1000,
          }} onClick={() => { setQueueModal(null); setQueueData(null); }}>
            <div className="card fade-in" style={{ maxWidth: '400px', width: '90%' }}
              onClick={e => e.stopPropagation()}>
              <h3 style={{ fontWeight: 700, marginBottom: '20px' }}>🔴 Live Queue Status</h3>
              {queueData ? (
                <>
                  <div className="grid-2" style={{ gap: '12px', marginBottom: '16px' }}>
                    {[
                      { label: 'Your Queue #', value: `#${queueData.queueNumber}`, color: 'var(--accent-cyan)' },
                      { label: 'Patients Ahead', value: queueData.patientsAhead, color: 'var(--accent-yellow)' },
                      { label: 'Est. Wait', value: `${queueData.estimatedWaitMinutes} min`, color: 'var(--accent-green)' },
                      { label: 'Status', value: queueData.status, color: 'var(--accent-blue)' },
                    ].map(s => (
                      <div key={s.label} style={{ padding: '12px', background: 'var(--bg-secondary)', borderRadius: 'var(--radius-sm)', textAlign: 'center' }}>
                        <div style={{ fontSize: '20px', fontWeight: 800, color: s.color, fontFamily: 'var(--font-mono)' }}>{s.value}</div>
                        <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: 4 }}>{s.label}</div>
                      </div>
                    ))}
                  </div>
                  <div style={{ padding: '12px', background: 'rgba(0,212,255,0.05)', borderRadius: 'var(--radius-sm)', fontSize: '13px', color: 'var(--text-secondary)' }}>
                    💬 {queueData.message}
                  </div>
                </>
              ) : <div style={{ textAlign: 'center', padding: 20 }}><div className="spinner" /></div>}
              <button className="btn btn-secondary" onClick={() => { setQueueModal(null); setQueueData(null); }}
                style={{ width: '100%', marginTop: '16px', justifyContent: 'center' }}>
                Close
              </button>
            </div>
          </div>
        )}

        {loading ? (
          <div style={{ textAlign: 'center', padding: 60 }}><div className="spinner" style={{ width: 40, height: 40 }} /></div>
        ) : appointments.length === 0 ? (
          <div className="card" style={{ textAlign: 'center', padding: '60px' }}>
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>📅</div>
            <h3 style={{ fontWeight: 700, marginBottom: '8px' }}>No Appointments Yet</h3>
            <p style={{ color: 'var(--text-muted)' }}>Book your first appointment to get started!</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {appointments.map(a => (
              <div key={a.id} className="card slide-in" style={{
                borderLeft: `3px solid ${
                  a.status === 'CONFIRMED' ? 'var(--accent-blue)' :
                  a.status === 'COMPLETED' ? 'var(--accent-green)' :
                  a.status === 'CANCELLED' ? 'var(--accent-red)' :
                  a.status === 'IN_PROGRESS' ? 'var(--accent-purple)' : 'var(--border)'
                }`,
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                      <h3 style={{ fontWeight: 700, fontSize: '16px' }}>Dr. {a.doctorName}</h3>
                      <span className={`badge ${statusColor[a.status] || 'badge-pending'}`}>{a.status}</span>
                    </div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '13px' }}>{a.doctorSpecialization}</div>
                    <div style={{ marginTop: '12px', display: 'flex', gap: '20px', flexWrap: 'wrap' }}>
                      <div>
                        <div style={{ fontSize: '11px', color: 'var(--text-muted)', letterSpacing: '1px' }}>DATE</div>
                        <div style={{ fontWeight: 600 }}>{a.appointmentDate}</div>
                      </div>
                      <div>
                        <div style={{ fontSize: '11px', color: 'var(--text-muted)', letterSpacing: '1px' }}>TIME</div>
                        <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>{a.appointmentTime}</div>
                      </div>
                      <div>
                        <div style={{ fontSize: '11px', color: 'var(--text-muted)', letterSpacing: '1px' }}>QUEUE #</div>
                        <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'var(--accent-cyan)' }}>#{a.queueNumber}</div>
                      </div>
                      {a.estimatedWaitMinutes > 0 && (
                        <div>
                          <div style={{ fontSize: '11px', color: 'var(--text-muted)', letterSpacing: '1px' }}>EST. WAIT</div>
                          <div style={{ fontWeight: 600 }}>{a.estimatedWaitMinutes} min</div>
                        </div>
                      )}
                    </div>
                    {a.symptoms && (
                      <div style={{ marginTop: '8px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                        🤒 {a.symptoms}
                      </div>
                    )}
                    {a.prescription && (
                      <div style={{ marginTop: '8px', fontSize: '13px', color: 'var(--accent-green)' }}>
                        💊 {a.prescription}
                      </div>
                    )}
                  </div>

                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                    {['CONFIRMED', 'IN_PROGRESS'].includes(a.status) && (
                      <button className="btn btn-secondary" onClick={() => checkQueue(a.id)}
                        style={{ fontSize: '12px', padding: '6px 12px' }}>
                        🔴 Queue Status
                      </button>
                    )}
                    {a.status === 'CONFIRMED' && (
                      <button className="btn btn-danger" onClick={() => handleCancel(a.id)}
                        disabled={cancelling === a.id}
                        style={{ fontSize: '12px', padding: '6px 12px' }}>
                        {cancelling === a.id ? <span className="spinner" style={{ width: 14, height: 14 }} /> : '✕ Cancel'}
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}
