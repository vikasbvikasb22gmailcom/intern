import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout.jsx';
import { getDoctorQueue, updateAppointmentStatus, getDoctorById } from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';

const statusColor = {
  CONFIRMED: 'badge-confirmed', COMPLETED: 'badge-completed',
  CANCELLED: 'badge-cancelled', PENDING: 'badge-pending', IN_PROGRESS: 'badge-in-progress',
};

export default function DoctorDashboard() {
  const { user } = useAuth();
  const [queue, setQueue] = useState([]);
  const [doctor, setDoctor] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(null);
  const [notes, setNotes] = useState('');
  const [prescription, setPrescription] = useState('');
  const [activeAppt, setActiveAppt] = useState(null);

  useEffect(() => { loadQueue(); }, []);

  const loadQueue = async () => {
    setLoading(true);
    try {
      // Get doctor info first
      const { data: profileData } = await import('../services/api').then(m => m.getProfile());
      // Load queue for doctor id 1 (simplified - in production would use doctor's actual ID)
      const res = await getDoctorQueue(1);
      setQueue(res.data.data || []);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const updateStatus = async (appointmentId, status) => {
    setUpdating(appointmentId);
    try {
      await updateAppointmentStatus(appointmentId, {
        status,
        doctorNotes: notes || undefined,
        prescription: prescription || undefined,
      });
      setNotes(''); setPrescription(''); setActiveAppt(null);
      await loadQueue();
    } catch (e) { console.error(e); }
    finally { setUpdating(null); }
  };

  const inProgress = queue.filter(q => q.status === 'IN_PROGRESS');
  const waiting = queue.filter(q => q.status === 'CONFIRMED');

  return (
    <Layout>
      <div className="page">
        <div className="page-header">
          <h1>Doctor Dashboard 👨‍⚕️</h1>
          <p>Welcome Dr. {user?.firstName}! Manage today's queue</p>
        </div>

        {/* Stats */}
        <div className="grid-3" style={{ marginBottom: '28px' }}>
          <div className="card stat-card stat-yellow">
            <div className="stat-value">{waiting.length}</div>
            <div className="stat-label">Waiting Patients</div>
          </div>
          <div className="card stat-card stat-blue">
            <div className="stat-value" style={{ color: 'var(--accent-purple)' }}>{inProgress.length}</div>
            <div className="stat-label">In Progress</div>
          </div>
          <div className="card stat-card stat-green">
            <div className="stat-value">{queue.length}</div>
            <div className="stat-label">Total Today</div>
          </div>
        </div>

        {/* Current Patient */}
        {inProgress.length > 0 && (
          <div className="card" style={{ marginBottom: '24px', borderColor: 'rgba(139,92,246,0.4)' }}>
            <h3 style={{ fontWeight: 700, marginBottom: '16px', color: 'var(--accent-purple)' }}>
              🔵 Currently Seeing
            </h3>
            {inProgress.map(appt => (
              <div key={appt.appointmentId}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                  <div>
                    <div style={{ fontSize: '20px', fontWeight: 800 }}>{appt.doctorName}</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
                      Queue #{appt.queueNumber} • {appt.appointmentTime}
                    </div>
                  </div>
                  <span className="badge badge-in-progress">IN PROGRESS</span>
                </div>

                {activeAppt === appt.appointmentId ? (
                  <div>
                    <div className="input-group">
                      <label>DOCTOR NOTES</label>
                      <textarea rows={2} value={notes} onChange={e => setNotes(e.target.value)}
                        placeholder="Enter diagnosis or notes..." />
                    </div>
                    <div className="input-group">
                      <label>PRESCRIPTION</label>
                      <textarea rows={2} value={prescription} onChange={e => setPrescription(e.target.value)}
                        placeholder="Enter prescription..." />
                    </div>
                    <div style={{ display: 'flex', gap: '12px' }}>
                      <button className="btn btn-success" onClick={() => updateStatus(appt.appointmentId, 'COMPLETED')}
                        disabled={updating === appt.appointmentId}>
                        {updating === appt.appointmentId ? <span className="spinner" style={{ width: 16, height: 16 }} /> : '✓ Complete'}
                      </button>
                      <button className="btn btn-secondary" onClick={() => setActiveAppt(null)}>Cancel</button>
                    </div>
                  </div>
                ) : (
                  <button className="btn btn-success" onClick={() => setActiveAppt(appt.appointmentId)}>
                    ✓ Complete Appointment
                  </button>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Queue */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ fontWeight: 700, fontSize: '15px' }}>Today's Waiting Queue</h3>
            <button className="btn btn-secondary" onClick={loadQueue} style={{ fontSize: '12px', padding: '6px 12px' }}>
              🔄 Refresh
            </button>
          </div>

          {loading ? (
            <div style={{ textAlign: 'center', padding: 40 }}><div className="spinner" /></div>
          ) : waiting.length === 0 ? (
            <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
              No patients waiting in queue
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {waiting.map((appt, idx) => (
                <div key={appt.appointmentId} style={{
                  padding: '16px',
                  background: idx === 0 ? 'rgba(0,212,255,0.05)' : 'var(--bg-secondary)',
                  border: `1px solid ${idx === 0 ? 'rgba(0,212,255,0.2)' : 'var(--border)'}`,
                  borderRadius: 'var(--radius-sm)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: '16px',
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <div style={{
                      width: 40, height: 40, borderRadius: '50%',
                      background: idx === 0 ? 'linear-gradient(135deg, var(--accent-cyan), var(--accent-blue))' : 'var(--bg-card)',
                      border: `1px solid ${idx === 0 ? 'transparent' : 'var(--border)'}`,
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: '16px', fontWeight: 800,
                      color: idx === 0 ? '#000' : 'var(--text-muted)',
                      fontFamily: 'var(--font-mono)',
                    }}>
                      {appt.queueNumber}
                    </div>
                    <div>
                      <div style={{ fontWeight: 700, color: 'var(--text-primary)' }}>
                        Patient #{appt.queueNumber}
                      </div>
                      <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                        🕐 {appt.appointmentTime} • Wait: {appt.estimatedWaitMinutes}min
                      </div>
                    </div>
                  </div>

                  {idx === 0 && inProgress.length === 0 && (
                    <button className="btn btn-primary" onClick={() => updateStatus(appt.appointmentId, 'IN_PROGRESS')}
                      disabled={updating === appt.appointmentId} style={{ fontSize: '13px' }}>
                      {updating === appt.appointmentId ? <span className="spinner" style={{ width: 16, height: 16 }} /> : '→ Call Next'}
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}
