import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout.jsx';
import { getDoctors, getAvailableSlots, bookAppointment } from '../services/api.js';
import { useNavigate } from 'react-router-dom';

export default function BookAppointment() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [selectedDate, setSelectedDate] = useState('');
  const [slots, setSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState('');
  const [symptoms, setSymptoms] = useState('');
  const [loading, setLoading] = useState(false);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => { loadDoctors(); }, []);

  const loadDoctors = async () => {
    setLoading(true);
    try {
      const res = await getDoctors({ page: 0, size: 20 });
      setDoctors(res.data.data.content || []);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const loadSlots = async () => {
    if (!selectedDoctor || !selectedDate) return;
    setSlotsLoading(true);
    setSlots([]);
    setSelectedSlot('');
    try {
      const res = await getAvailableSlots(selectedDoctor.id, selectedDate);
      setSlots(res.data.data || []);
    } catch (e) {
      setError(e.response?.data?.error || 'No slots available for this date');
    } finally { setSlotsLoading(false); }
  };

  useEffect(() => { if (selectedDate && selectedDoctor) loadSlots(); }, [selectedDate]);

  const handleBook = async () => {
    if (!selectedSlot) { setError('Please select a time slot'); return; }
    setLoading(true);
    setError('');
    try {
      await bookAppointment({
        doctorId: selectedDoctor.id,
        appointmentDate: selectedDate,
        appointmentTime: selectedSlot.substring(0, 5),
        symptoms,
      });
      setSuccess(true);
      setTimeout(() => navigate('/patient/appointments'), 2000);
    } catch (e) {
      setError(e.response?.data?.error || 'Booking failed. Please try again.');
    } finally { setLoading(false); }
  };

  const today = new Date().toISOString().split('T')[0];

  if (success) return (
    <Layout>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '60vh' }}>
        <div className="card fade-in" style={{ textAlign: 'center', padding: '60px', maxWidth: '400px' }}>
          <div style={{ fontSize: '64px', marginBottom: '16px' }}>✅</div>
          <h2 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--accent-green)' }}>Appointment Booked!</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>Redirecting to your appointments...</p>
        </div>
      </div>
    </Layout>
  );

  return (
    <Layout>
      <div className="page">
        <div className="page-header">
          <h1>Book Appointment</h1>
          <p>Find a doctor and schedule your visit</p>
        </div>

        {/* Steps */}
        <div style={{ display: 'flex', gap: '8px', marginBottom: '28px' }}>
          {['Select Doctor', 'Choose Date & Time', 'Confirm'].map((s, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <div style={{
                width: 28, height: 28, borderRadius: '50%',
                background: step > i + 1 ? 'var(--accent-green)' : step === i + 1 ? 'var(--accent-cyan)' : 'var(--bg-card)',
                border: `1px solid ${step >= i + 1 ? 'transparent' : 'var(--border)'}`,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: '12px', fontWeight: 700, color: step >= i + 1 ? '#000' : 'var(--text-muted)',
              }}>{step > i + 1 ? '✓' : i + 1}</div>
              <span style={{ fontSize: '13px', color: step === i + 1 ? 'var(--text-primary)' : 'var(--text-muted)', fontWeight: step === i + 1 ? 700 : 400 }}>{s}</span>
              {i < 2 && <span style={{ color: 'var(--border)', margin: '0 4px' }}>→</span>}
            </div>
          ))}
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {/* Step 1: Select Doctor */}
        {step === 1 && (
          <div className="fade-in">
            <div className="grid-2">
              {loading ? <div style={{ gridColumn: '1/-1', textAlign: 'center', padding: 40 }}><div className="spinner" /></div>
                : doctors.map(doc => (
                  <div key={doc.id} className="card" onClick={() => { setSelectedDoctor(doc); setStep(2); setError(''); }}
                    style={{ cursor: 'pointer', borderColor: selectedDoctor?.id === doc.id ? 'var(--accent-cyan)' : 'var(--border)' }}>
                    <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                      <div style={{
                        width: 48, height: 48, borderRadius: '50%',
                        background: 'linear-gradient(135deg, var(--accent-cyan), var(--accent-blue))',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        fontSize: '18px', fontWeight: 700, color: '#000', flexShrink: 0,
                      }}>
                        {doc.firstName?.[0]}{doc.lastName?.[0]}
                      </div>
                      <div>
                        <div style={{ fontWeight: 700, color: 'var(--text-primary)' }}>Dr. {doc.firstName} {doc.lastName}</div>
                        <div style={{ fontSize: '13px', color: 'var(--accent-cyan)' }}>{doc.specialization}</div>
                        <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: 4 }}>
                          ⭐ {doc.experienceYears} yrs exp • ₹{doc.consultationFee}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
            </div>
            {doctors.length === 0 && !loading && (
              <div style={{ textAlign: 'center', padding: 60, color: 'var(--text-muted)' }}>
                No doctors available. Contact admin to add doctors.
              </div>
            )}
          </div>
        )}

        {/* Step 2: Date & Time */}
        {step === 2 && (
          <div className="fade-in">
            <div className="card" style={{ maxWidth: '600px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24 }}>
                <div style={{
                  width: 40, height: 40, borderRadius: '50%',
                  background: 'linear-gradient(135deg, var(--accent-cyan), var(--accent-blue))',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '14px', fontWeight: 700, color: '#000',
                }}>{selectedDoctor?.firstName?.[0]}{selectedDoctor?.lastName?.[0]}</div>
                <div>
                  <div style={{ fontWeight: 700 }}>Dr. {selectedDoctor?.firstName} {selectedDoctor?.lastName}</div>
                  <div style={{ fontSize: '13px', color: 'var(--accent-cyan)' }}>{selectedDoctor?.specialization}</div>
                </div>
              </div>

              <div className="input-group">
                <label>SELECT DATE</label>
                <input type="date" min={today} value={selectedDate}
                  onChange={e => { setSelectedDate(e.target.value); setError(''); }} />
              </div>

              {slotsLoading && <div style={{ textAlign: 'center', padding: 20 }}><div className="spinner" /></div>}

              {slots.length > 0 && (
                <div>
                  <label style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 600, letterSpacing: '0.5px', display: 'block', marginBottom: '12px' }}>
                    SELECT TIME SLOT
                  </label>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '8px', marginBottom: '20px' }}>
                    {slots.map(slot => (
                      <button key={slot} onClick={() => { setSelectedSlot(slot); setError(''); }}
                        style={{
                          padding: '8px',
                          background: selectedSlot === slot ? 'rgba(0,212,255,0.15)' : 'var(--bg-secondary)',
                          border: `1px solid ${selectedSlot === slot ? 'var(--accent-cyan)' : 'var(--border)'}`,
                          borderRadius: 'var(--radius-sm)',
                          color: selectedSlot === slot ? 'var(--accent-cyan)' : 'var(--text-secondary)',
                          cursor: 'pointer',
                          fontFamily: 'var(--font-mono)',
                          fontSize: '13px',
                          transition: 'var(--transition)',
                        }}>
                        {slot.substring(0, 5)}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {slots.length === 0 && selectedDate && !slotsLoading && (
                <div style={{ textAlign: 'center', padding: '20px', color: 'var(--text-muted)', fontSize: '14px' }}>
                  No available slots for this date.
                </div>
              )}

              <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
                <button className="btn btn-secondary" onClick={() => { setStep(1); setSelectedDate(''); setSlots([]); }}>← Back</button>
                {selectedSlot && <button className="btn btn-primary" onClick={() => setStep(3)}>Next →</button>}
              </div>
            </div>
          </div>
        )}

        {/* Step 3: Confirm */}
        {step === 3 && (
          <div className="fade-in">
            <div className="card" style={{ maxWidth: '500px' }}>
              <h3 style={{ fontWeight: 700, marginBottom: '20px' }}>Confirm Appointment</h3>

              <div style={{ background: 'var(--bg-secondary)', borderRadius: 'var(--radius-sm)', padding: '16px', marginBottom: '20px' }}>
                {[
                  ['Doctor', `Dr. ${selectedDoctor?.firstName} ${selectedDoctor?.lastName}`],
                  ['Specialization', selectedDoctor?.specialization],
                  ['Date', selectedDate],
                  ['Time', selectedSlot?.substring(0, 5)],
                  ['Fee', `₹${selectedDoctor?.consultationFee}`],
                ].map(([label, value]) => (
                  <div key={label} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid var(--border)' }}>
                    <span style={{ color: 'var(--text-muted)', fontSize: '13px' }}>{label}</span>
                    <span style={{ color: 'var(--text-primary)', fontWeight: 600, fontSize: '13px' }}>{value}</span>
                  </div>
                ))}
              </div>

              <div className="input-group">
                <label>SYMPTOMS (Optional)</label>
                <textarea rows={3} placeholder="Describe your symptoms..." value={symptoms}
                  onChange={e => setSymptoms(e.target.value)}
                  style={{ resize: 'vertical' }} />
              </div>

              <div style={{ display: 'flex', gap: '12px' }}>
                <button className="btn btn-secondary" onClick={() => setStep(2)}>← Back</button>
                <button className="btn btn-primary" onClick={handleBook} disabled={loading} style={{ flex: 1, justifyContent: 'center' }}>
                  {loading ? <span className="spinner" style={{ width: 18, height: 18, borderWidth: 2 }} /> : '✓ Confirm Booking'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}
