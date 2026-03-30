import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout.jsx';
import { getDoctors, createDoctor, addSchedule, getSpecializations } from '../services/api.js';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

export default function AdminDoctors() {
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showScheduleModal, setShowScheduleModal] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [search, setSearch] = useState('');

  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', phone: '',
    password: '', specialization: '', qualification: '',
    consultationFee: 500, experienceYears: 1, maxPatientsPerDay: 20,
  });
  const [schedule, setSchedule] = useState({
    dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00', isActive: true,
  });

  useEffect(() => { loadDoctors(); }, []);

  const loadDoctors = async () => {
    setLoading(true);
    try {
      const res = await getDoctors({ page: 0, size: 50, search });
      setDoctors(res.data.data.content || []);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const handleCreateDoctor = async () => {
    setSaving(true); setError('');
    try {
      await createDoctor(form);
      setSuccess('Doctor created successfully!');
      setShowModal(false);
      setForm({ firstName: '', lastName: '', email: '', phone: '', password: '', specialization: '', qualification: '', consultationFee: 500, experienceYears: 1, maxPatientsPerDay: 20 });
      await loadDoctors();
      setTimeout(() => setSuccess(''), 3000);
    } catch (e) {
      setError(e.response?.data?.error || 'Failed to create doctor');
    } finally { setSaving(false); }
  };

  const handleAddSchedule = async () => {
    setSaving(true); setError('');
    try {
      await addSchedule(showScheduleModal, schedule);
      setSuccess('Schedule added!');
      setShowScheduleModal(null);
      setTimeout(() => setSuccess(''), 3000);
    } catch (e) {
      setError(e.response?.data?.error || 'Failed to add schedule');
    } finally { setSaving(false); }
  };

  return (
    <Layout>
      <div className="page">
        <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1>Manage Doctors 👨‍⚕️</h1>
            <p>Add, edit and manage doctor profiles</p>
          </div>
          <button className="btn btn-primary" onClick={() => { setShowModal(true); setError(''); }}>
            + Add Doctor
          </button>
        </div>

        {success && <div className="alert alert-success">{success}</div>}

        {/* Search */}
        <div className="input-group" style={{ maxWidth: '400px', marginBottom: '20px' }}>
          <input placeholder="🔍 Search by name or specialization..."
            value={search} onChange={e => setSearch(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && loadDoctors()} />
        </div>

        {/* Doctors Grid */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: 60 }}><div className="spinner" style={{ width: 40, height: 40 }} /></div>
        ) : doctors.length === 0 ? (
          <div className="card" style={{ textAlign: 'center', padding: '60px' }}>
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>👨‍⚕️</div>
            <h3 style={{ fontWeight: 700, marginBottom: '8px' }}>No Doctors Yet</h3>
            <p style={{ color: 'var(--text-muted)', marginBottom: '20px' }}>Click "Add Doctor" to get started!</p>
            <button className="btn btn-primary" onClick={() => setShowModal(true)}>+ Add First Doctor</button>
          </div>
        ) : (
          <div className="grid-2">
            {doctors.map(doc => (
              <div key={doc.id} className="card">
                <div style={{ display: 'flex', gap: '16px', alignItems: 'flex-start' }}>
                  <div style={{
                    width: 52, height: 52, borderRadius: '50%',
                    background: 'linear-gradient(135deg, var(--accent-cyan), var(--accent-blue))',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: '18px', fontWeight: 800, color: '#000', flexShrink: 0,
                  }}>
                    {doc.firstName?.[0]}{doc.lastName?.[0]}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 700, fontSize: '16px' }}>Dr. {doc.firstName} {doc.lastName}</div>
                    <div style={{ color: 'var(--accent-cyan)', fontSize: '13px', marginBottom: '8px' }}>{doc.specialization}</div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '4px', fontSize: '12px', color: 'var(--text-muted)' }}>
                      <span>📧 {doc.email}</span>
                      <span>💰 ₹{doc.consultationFee}</span>
                      <span>⭐ {doc.experienceYears} yrs exp</span>
                      <span>👥 Max {doc.maxPatientsPerDay}/day</span>
                    </div>

                    {/* Schedules */}
                    {doc.schedules?.length > 0 && (
                      <div style={{ marginTop: '10px', display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                        {doc.schedules.map(s => (
                          <span key={s.id} style={{
                            fontSize: '10px', padding: '3px 8px',
                            background: 'rgba(0,212,255,0.1)',
                            border: '1px solid rgba(0,212,255,0.2)',
                            borderRadius: '20px', color: 'var(--accent-cyan)',
                          }}>
                            {s.dayOfWeek?.substring(0, 3)} {s.startTime?.substring(0, 5)}-{s.endTime?.substring(0, 5)}
                          </span>
                        ))}
                      </div>
                    )}

                    <div style={{ display: 'flex', gap: '8px', marginTop: '12px' }}>
                      <button className="btn btn-secondary" onClick={() => { setShowScheduleModal(doc.id); setError(''); }}
                        style={{ fontSize: '12px', padding: '6px 12px' }}>
                        📅 Add Schedule
                      </button>
                      <span className={`badge ${doc.available ? 'badge-confirmed' : 'badge-cancelled'}`}>
                        {doc.available ? 'Available' : 'Unavailable'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Create Doctor Modal */}
        {showModal && (
          <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '20px' }}
            onClick={() => setShowModal(false)}>
            <div className="card fade-in" style={{ maxWidth: '560px', width: '100%', maxHeight: '90vh', overflowY: 'auto' }}
              onClick={e => e.stopPropagation()}>
              <h3 style={{ fontWeight: 700, marginBottom: '20px', fontSize: '18px' }}>Add New Doctor</h3>
              {error && <div className="alert alert-error">{error}</div>}

              <div className="grid-2" style={{ gap: '12px' }}>
                <div className="input-group"><label>FIRST NAME</label>
                  <input value={form.firstName} onChange={e => setForm({ ...form, firstName: e.target.value })} placeholder="Priya" /></div>
                <div className="input-group"><label>LAST NAME</label>
                  <input value={form.lastName} onChange={e => setForm({ ...form, lastName: e.target.value })} placeholder="Sharma" /></div>
              </div>
              <div className="input-group"><label>EMAIL</label>
                <input type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} placeholder="dr.priya@hospital.com" /></div>
              <div className="grid-2" style={{ gap: '12px' }}>
                <div className="input-group"><label>PHONE</label>
                  <input value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} placeholder="9123456789" /></div>
                <div className="input-group"><label>PASSWORD</label>
                  <input type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} placeholder="••••••" /></div>
              </div>
              <div className="grid-2" style={{ gap: '12px' }}>
                <div className="input-group"><label>SPECIALIZATION</label>
                  <input value={form.specialization} onChange={e => setForm({ ...form, specialization: e.target.value })} placeholder="Cardiology" /></div>
                <div className="input-group"><label>QUALIFICATION</label>
                  <input value={form.qualification} onChange={e => setForm({ ...form, qualification: e.target.value })} placeholder="MBBS, MD" /></div>
              </div>
              <div className="grid-3" style={{ gap: '12px' }}>
                <div className="input-group"><label>FEE (₹)</label>
                  <input type="number" value={form.consultationFee} onChange={e => setForm({ ...form, consultationFee: +e.target.value })} /></div>
                <div className="input-group"><label>EXPERIENCE (YRS)</label>
                  <input type="number" value={form.experienceYears} onChange={e => setForm({ ...form, experienceYears: +e.target.value })} /></div>
                <div className="input-group"><label>MAX PATIENTS/DAY</label>
                  <input type="number" value={form.maxPatientsPerDay} onChange={e => setForm({ ...form, maxPatientsPerDay: +e.target.value })} /></div>
              </div>

              <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
                <button className="btn btn-secondary" onClick={() => setShowModal(false)} style={{ flex: 1, justifyContent: 'center' }}>Cancel</button>
                <button className="btn btn-primary" onClick={handleCreateDoctor} disabled={saving} style={{ flex: 2, justifyContent: 'center' }}>
                  {saving ? <span className="spinner" style={{ width: 18, height: 18, borderWidth: 2 }} /> : '+ Create Doctor'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Add Schedule Modal */}
        {showScheduleModal && (
          <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '20px' }}
            onClick={() => setShowScheduleModal(null)}>
            <div className="card fade-in" style={{ maxWidth: '400px', width: '100%' }} onClick={e => e.stopPropagation()}>
              <h3 style={{ fontWeight: 700, marginBottom: '20px' }}>Add Schedule</h3>
              {error && <div className="alert alert-error">{error}</div>}

              <div className="input-group"><label>DAY OF WEEK</label>
                <select value={schedule.dayOfWeek} onChange={e => setSchedule({ ...schedule, dayOfWeek: e.target.value })}>
                  {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
                </select>
              </div>
              <div className="grid-2" style={{ gap: '12px' }}>
                <div className="input-group"><label>START TIME</label>
                  <input type="time" value={schedule.startTime} onChange={e => setSchedule({ ...schedule, startTime: e.target.value })} /></div>
                <div className="input-group"><label>END TIME</label>
                  <input type="time" value={schedule.endTime} onChange={e => setSchedule({ ...schedule, endTime: e.target.value })} /></div>
              </div>

              <div style={{ display: 'flex', gap: '12px' }}>
                <button className="btn btn-secondary" onClick={() => setShowScheduleModal(null)} style={{ flex: 1, justifyContent: 'center' }}>Cancel</button>
                <button className="btn btn-primary" onClick={handleAddSchedule} disabled={saving} style={{ flex: 2, justifyContent: 'center' }}>
                  {saving ? <span className="spinner" style={{ width: 18, height: 18, borderWidth: 2 }} /> : '✓ Save Schedule'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}
