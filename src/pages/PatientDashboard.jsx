import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout.jsx';
import { getMyAppointments, getQueueStatus } from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useNavigate } from 'react-router-dom';

const statusColor = {
  CONFIRMED: 'badge-confirmed', COMPLETED: 'badge-completed',
  CANCELLED: 'badge-cancelled', PENDING: 'badge-pending', IN_PROGRESS: 'badge-in-progress',
};

export default function PatientDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [queueStatus, setQueueStatus] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const res = await getMyAppointments({ page: 0, size: 5 });
      const appts = res.data.data.content || [];
      setAppointments(appts);
      // Get queue status for latest confirmed appointment
      const confirmed = appts.find(a => a.status === 'CONFIRMED' || a.status === 'IN_PROGRESS');
      if (confirmed) {
        const qRes = await getQueueStatus(confirmed.id);
        setQueueStatus(qRes.data.data);
      }
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const upcoming = appointments.filter(a => ['CONFIRMED', 'PENDING', 'IN_PROGRESS'].includes(a.status));
  const completed = appointments.filter(a => a.status === 'COMPLETED').length;

  return (
    <Layout>
      <div className="page">
        <div className="page-header">
          <h1>Welcome back, {user?.firstName}! 👋</h1>
          <p>Here's your health overview</p>
        </div>

        {/* Stats */}
        <div className="grid-3" style={{ marginBottom: '28px' }}>
          <div className="card stat-card stat-cyan">
            <div className="stat-value">{upcoming.length}</div>
            <div className="stat-label">Upcoming Appointments</div>
          </div>
          <div className="card stat-card stat-green">
            <div className="stat-value">{completed}</div>
            <div className="stat-label">Completed Visits</div>
          </div>
          <div className="card stat-card stat-blue">
            <div className="stat-value">{appointments.length}</div>
            <div className="stat-label">Total Appointments</div>
          </div>
        </div>

        {/* Live Queue Status */}
        {queueStatus && (
          <div className="card card-glow" style={{ marginBottom: '28px', borderColor: 'rgba(0,212,255,0.3)' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
              <h2 style={{ fontSize: '16px', fontWeight: 700 }}>🔴 Live Queue Status</h2>
              <span className={`badge ${statusColor[queueStatus.status]}`}>{queueStatus.status}</span>
            </div>
            <div className="grid-4">
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '32px', fontWeight: 800, color: 'var(--accent-cyan)', fontFamily: 'var(--font-mono)' }}>
                  #{queueStatus.queueNumber}
                </div>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', letterSpacing: '1px' }}>YOUR NUMBER</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '32px', fontWeight: 800, color: 'var(--accent-yellow)', fontFamily: 'var(--font-mono)' }}>
                  {queueStatus.patientsAhead}
                </div>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', letterSpacing: '1px' }}>AHEAD OF YOU</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '32px', fontWeight: 800, color: 'var(--accent-green)', fontFamily: 'var(--font-mono)' }}>
                  {queueStatus.estimatedWaitMinutes}m
                </div>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', letterSpacing: '1px' }}>EST. WAIT</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text-primary)' }}>
                  {queueStatus.doctorName}
                </div>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', letterSpacing: '1px' }}>YOUR DOCTOR</div>
              </div>
            </div>
            <div style={{ marginTop: '16px', padding: '10px 14px', background: 'rgba(0,212,255,0.05)', borderRadius: 'var(--radius-sm)', fontSize: '13px', color: 'var(--text-secondary)' }}>
              💬 {queueStatus.message}
            </div>
          </div>
        )}

        {/* Quick Actions */}
        <div className="grid-2" style={{ marginBottom: '28px' }}>
          <button className="btn btn-primary" style={{ padding: '16px', fontSize: '15px', justifyContent: 'center' }}
            onClick={() => navigate('/patient/book')}>
            📅 Book New Appointment
          </button>
          <button className="btn btn-secondary" style={{ padding: '16px', fontSize: '15px', justifyContent: 'center' }}
            onClick={() => navigate('/patient/appointments')}>
            📋 View All Appointments
          </button>
        </div>

        {/* Recent Appointments */}
        <div className="card">
          <h2 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '20px' }}>Recent Appointments</h2>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px' }}><div className="spinner" /></div>
          ) : appointments.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
              No appointments yet. Book your first appointment!
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Doctor</th><th>Date</th><th>Time</th><th>Queue</th><th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map(a => (
                    <tr key={a.id}>
                      <td style={{ color: 'var(--text-primary)', fontWeight: 600 }}>Dr. {a.doctorName}</td>
                      <td>{a.appointmentDate}</td>
                      <td style={{ fontFamily: 'var(--font-mono)' }}>{a.appointmentTime}</td>
                      <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--accent-cyan)' }}>#{a.queueNumber}</td>
                      <td><span className={`badge ${statusColor[a.status] || 'badge-pending'}`}>{a.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}
