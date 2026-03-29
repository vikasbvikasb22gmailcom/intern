import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout.jsx';
import { getProfile, updateProfile } from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';

export default function ProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { loadProfile(); }, []);

  const loadProfile = async () => {
    setLoading(true);
    try {
      const res = await getProfile();
      const data = res.data.data;
      setProfile(data);
      setForm({
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        phone: data.phone || '',
        address: data.address || '',
        age: data.age || '',
        bloodGroup: data.bloodGroup || '',
      });
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess(false);
    try {
      await updateProfile(form);
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (e) {
      setError(e.response?.data?.error || 'Failed to update profile');
    } finally { setSaving(false); }
  };

  const roleColors = {
    ROLE_ADMIN: 'var(--accent-red)',
    ROLE_DOCTOR: 'var(--accent-cyan)',
    ROLE_PATIENT: 'var(--accent-green)',
  };

  if (loading) return (
    <Layout>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh' }}>
        <div className="spinner" style={{ width: 40, height: 40 }} />
      </div>
    </Layout>
  );

  return (
    <Layout>
      <div className="page">
        <div className="page-header">
          <h1>My Profile 👤</h1>
          <p>Manage your personal information</p>
        </div>

        <div className="grid-2" style={{ alignItems: 'start' }}>
          {/* Profile Card */}
          <div>
            <div className="card" style={{ textAlign: 'center', padding: '40px 24px', marginBottom: '20px' }}>
              <div style={{
                width: 80, height: 80, borderRadius: '50%',
                background: 'linear-gradient(135deg, var(--accent-cyan), var(--accent-blue))',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: '28px', fontWeight: 800, color: '#000',
                margin: '0 auto 16px',
              }}>
                {profile?.firstName?.[0]}{profile?.lastName?.[0]}
              </div>
              <h2 style={{ fontWeight: 800, fontSize: '20px' }}>
                {profile?.firstName} {profile?.lastName}
              </h2>
              <div style={{
                display: 'inline-block',
                marginTop: '8px',
                padding: '4px 12px',
                borderRadius: '20px',
                background: `rgba(${profile?.role === 'ROLE_ADMIN' ? '239,68,68' : profile?.role === 'ROLE_DOCTOR' ? '0,212,255' : '16,185,129'}, 0.1)`,
                border: `1px solid ${roleColors[profile?.role]}30`,
                color: roleColors[profile?.role],
                fontSize: '12px',
                fontWeight: 700,
                letterSpacing: '1px',
              }}>
                {profile?.role?.replace('ROLE_', '')}
              </div>
              <div style={{ marginTop: '20px', color: 'var(--text-secondary)', fontSize: '14px' }}>
                {profile?.email}
              </div>
            </div>

            {/* Info Summary */}
            <div className="card">
              <h3 style={{ fontWeight: 700, marginBottom: '16px', fontSize: '14px', color: 'var(--text-muted)', letterSpacing: '1px' }}>
                ACCOUNT INFO
              </h3>
              {[
                ['User ID', `#${profile?.id}`],
                ['Email', profile?.email],
                ['Phone', profile?.phone || 'Not set'],
                ['Blood Group', profile?.bloodGroup || 'Not set'],
                ['Age', profile?.age ? `${profile.age} years` : 'Not set'],
                ['Status', profile?.enabled ? '✅ Active' : '❌ Disabled'],
              ].map(([label, value]) => (
                <div key={label} style={{
                  display: 'flex', justifyContent: 'space-between',
                  padding: '10px 0',
                  borderBottom: '1px solid var(--border)',
                  fontSize: '13px',
                }}>
                  <span style={{ color: 'var(--text-muted)' }}>{label}</span>
                  <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{value}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Edit Form */}
          <div className="card">
            <h3 style={{ fontWeight: 700, marginBottom: '20px', fontSize: '15px' }}>Edit Profile</h3>

            {success && <div className="alert alert-success">✅ Profile updated successfully!</div>}
            {error && <div className="alert alert-error">{error}</div>}

            <div className="grid-2" style={{ gap: '12px' }}>
              <div className="input-group">
                <label>FIRST NAME</label>
                <input value={form.firstName} onChange={e => setForm({ ...form, firstName: e.target.value })} />
              </div>
              <div className="input-group">
                <label>LAST NAME</label>
                <input value={form.lastName} onChange={e => setForm({ ...form, lastName: e.target.value })} />
              </div>
            </div>

            <div className="input-group">
              <label>PHONE NUMBER</label>
              <input value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })}
                placeholder="9876543210" />
            </div>

            <div className="input-group">
              <label>ADDRESS</label>
              <textarea rows={2} value={form.address} onChange={e => setForm({ ...form, address: e.target.value })}
                placeholder="Your address..." style={{ resize: 'vertical' }} />
            </div>

            <div className="grid-2" style={{ gap: '12px' }}>
              <div className="input-group">
                <label>AGE</label>
                <input type="number" value={form.age} onChange={e => setForm({ ...form, age: e.target.value })}
                  placeholder="25" min="1" max="120" />
              </div>
              <div className="input-group">
                <label>BLOOD GROUP</label>
                <select value={form.bloodGroup} onChange={e => setForm({ ...form, bloodGroup: e.target.value })}>
                  <option value="">Select</option>
                  {['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'].map(bg => (
                    <option key={bg} value={bg}>{bg}</option>
                  ))}
                </select>
              </div>
            </div>

            <button className="btn btn-primary btn-full" onClick={handleSave} disabled={saving}
              style={{ marginTop: '8px' }}>
              {saving
                ? <span className="spinner" style={{ width: 18, height: 18, borderWidth: 2 }} />
                : '💾 Save Changes'}
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
}
