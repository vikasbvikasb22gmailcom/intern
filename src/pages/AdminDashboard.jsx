import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout.jsx';
import { getDashboard, getAllPatients, getAllAppointments } from '../services/api.js';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';

const COLORS = ['#00d4ff', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

const statusColor = {
  CONFIRMED: 'badge-confirmed', COMPLETED: 'badge-completed',
  CANCELLED: 'badge-cancelled', PENDING: 'badge-pending', IN_PROGRESS: 'badge-in-progress',
};

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const [dashRes, apptRes, patRes] = await Promise.all([
        getDashboard(),
        getAllAppointments({ page: 0, size: 10 }),
        getAllPatients({ page: 0, size: 10 }),
      ]);
      setStats(dashRes.data.data);
      setAppointments(apptRes.data.data.content || []);
      setPatients(patRes.data.data.content || []);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  if (loading) return (
    <Layout>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh' }}>
        <div className="spinner" style={{ width: 40, height: 40 }} />
      </div>
    </Layout>
  );

  const specData = stats?.appointmentsBySpecialization
    ? Object.entries(stats.appointmentsBySpecialization).map(([name, value]) => ({ name, value }))
    : [];

  const statusData = stats?.appointmentsByStatus
    ? Object.entries(stats.appointmentsByStatus).map(([name, value]) => ({ name, value }))
    : [];

  return (
    <Layout>
      <div className="page">
        <div className="page-header">
          <h1>Admin Dashboard 🛡️</h1>
          <p>System overview and management</p>
        </div>

        {/* Stat Cards */}
        <div className="grid-4" style={{ marginBottom: '28px' }}>
          {[
            { label: 'Total Patients', value: stats?.totalPatients, color: 'stat-cyan', icon: '👥' },
            { label: 'Total Doctors', value: stats?.totalDoctors, color: 'stat-blue', icon: '👨‍⚕️' },
            { label: "Today's Appointments", value: stats?.totalAppointmentsToday, color: 'stat-yellow', icon: '📅' },
            { label: 'Completed Today', value: stats?.completedAppointmentsToday, color: 'stat-green', icon: '✅' },
          ].map(s => (
            <div key={s.label} className={`card stat-card ${s.color}`}>
              <div style={{ fontSize: '28px', marginBottom: '8px' }}>{s.icon}</div>
              <div className="stat-value">{s.value ?? 0}</div>
              <div className="stat-label">{s.label}</div>
            </div>
          ))}
        </div>

        {/* Tabs */}
        <div style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
          {['overview', 'appointments', 'patients'].map(tab => (
            <button key={tab} onClick={() => setActiveTab(tab)} className={`btn ${activeTab === tab ? 'btn-primary' : 'btn-secondary'}`}
              style={{ textTransform: 'capitalize' }}>
              {tab}
            </button>
          ))}
        </div>

        {/* Overview Tab */}
        {activeTab === 'overview' && (
          <div className="fade-in">
            <div className="grid-2">
              {/* Bar Chart - Specializations */}
              <div className="card">
                <h3 style={{ fontWeight: 700, marginBottom: '20px', fontSize: '15px' }}>Appointments by Specialization</h3>
                {specData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={200}>
                    <BarChart data={specData}>
                      <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                      <YAxis tick={{ fill: '#94a3b8', fontSize: 11 }} />
                      <Tooltip contentStyle={{ background: '#111827', border: '1px solid #1e2d45', borderRadius: 8, color: '#e2e8f0' }} />
                      <Bar dataKey="value" fill="#00d4ff" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                ) : <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No data yet</div>}
              </div>

              {/* Pie Chart - Status */}
              <div className="card">
                <h3 style={{ fontWeight: 700, marginBottom: '20px', fontSize: '15px' }}>Appointments by Status</h3>
                {statusData.length > 0 ? (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                    <ResponsiveContainer width="60%" height={180}>
                      <PieChart>
                        <Pie data={statusData} cx="50%" cy="50%" innerRadius={50} outerRadius={80} dataKey="value">
                          {statusData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                        </Pie>
                        <Tooltip contentStyle={{ background: '#111827', border: '1px solid #1e2d45', borderRadius: 8, color: '#e2e8f0' }} />
                      </PieChart>
                    </ResponsiveContainer>
                    <div style={{ flex: 1 }}>
                      {statusData.map((s, i) => (
                        <div key={s.name} style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                          <div style={{ width: 10, height: 10, borderRadius: '50%', background: COLORS[i % COLORS.length], flexShrink: 0 }} />
                          <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{s.name}</span>
                          <span style={{ fontSize: '12px', color: 'var(--text-primary)', fontWeight: 700, marginLeft: 'auto' }}>{s.value}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                ) : <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No data yet</div>}
              </div>
            </div>

            {/* Today Summary */}
            <div className="card" style={{ marginTop: '20px' }}>
              <h3 style={{ fontWeight: 700, marginBottom: '16px', fontSize: '15px' }}>Today's Summary</h3>
              <div className="grid-4">
                {[
                  { label: 'Pending', value: stats?.pendingAppointmentsToday, color: 'var(--accent-yellow)' },
                  { label: 'Completed', value: stats?.completedAppointmentsToday, color: 'var(--accent-green)' },
                  { label: 'Cancelled', value: stats?.cancelledAppointmentsToday, color: 'var(--accent-red)' },
                  { label: 'Total Ever', value: stats?.totalAppointmentsAllTime, color: 'var(--accent-cyan)' },
                ].map(s => (
                  <div key={s.label} style={{ textAlign: 'center', padding: '16px', background: 'var(--bg-secondary)', borderRadius: 'var(--radius-sm)' }}>
                    <div style={{ fontSize: '28px', fontWeight: 800, color: s.color, fontFamily: 'var(--font-mono)' }}>{s.value ?? 0}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)', letterSpacing: '1px', textTransform: 'uppercase', marginTop: 4 }}>{s.label}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Appointments Tab */}
        {activeTab === 'appointments' && (
          <div className="fade-in card">
            <h3 style={{ fontWeight: 700, marginBottom: '20px', fontSize: '15px' }}>All Appointments</h3>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Patient</th><th>Doctor</th><th>Date</th><th>Time</th><th>Queue</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {appointments.map(a => (
                    <tr key={a.id}>
                      <td style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{a.patientName}</td>
                      <td>Dr. {a.doctorName}</td>
                      <td>{a.appointmentDate}</td>
                      <td style={{ fontFamily: 'var(--font-mono)' }}>{a.appointmentTime}</td>
                      <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--accent-cyan)' }}>#{a.queueNumber}</td>
                      <td><span className={`badge ${statusColor[a.status] || 'badge-pending'}`}>{a.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Patients Tab */}
        {activeTab === 'patients' && (
          <div className="fade-in card">
            <h3 style={{ fontWeight: 700, marginBottom: '20px', fontSize: '15px' }}>All Patients</h3>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Name</th><th>Email</th><th>Phone</th><th>Joined</th></tr>
                </thead>
                <tbody>
                  {patients.map(p => (
                    <tr key={p.id}>
                      <td style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{p.firstName} {p.lastName}</td>
                      <td>{p.email}</td>
                      <td style={{ fontFamily: 'var(--font-mono)' }}>{p.phone || '-'}</td>
                      <td style={{ color: 'var(--text-muted)' }}>{p.createdAt?.split('T')[0]}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}
