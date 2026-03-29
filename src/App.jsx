import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext.jsx';
import './index.css';

import LoginPage from './pages/LoginPage.jsx';
import PatientDashboard from './pages/PatientDashboard.jsx';
import BookAppointment from './pages/BookAppointment.jsx';
import MyAppointments from './pages/MyAppointments.jsx';
import NotificationsPage from './pages/NotificationsPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import AdminDashboard from './pages/AdminDashboard.jsx';
import AdminDoctors from './pages/AdminDoctors.jsx';
import AdminPatients from './pages/AdminPatients.jsx';
import DoctorDashboard from './pages/DoctorDashboard.jsx';

// Protected route wrapper
const Protected = ({ children, roles }) => {
  const { user, loading } = useAuth();
  if (loading) return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh', background: 'var(--bg-primary)' }}>
      <div style={{ textAlign: 'center' }}>
        <div className="spinner" style={{ width: 40, height: 40, margin: '0 auto 16px' }} />
        <div style={{ color: 'var(--text-muted)', fontSize: '14px' }}>Loading...</div>
      </div>
    </div>
  );
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/login" replace />;
  return children;
};

function AppRoutes() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={!user ? <LoginPage /> : <Navigate to={
        user.role === 'ROLE_ADMIN' ? '/admin' :
        user.role === 'ROLE_DOCTOR' ? '/doctor' : '/patient'
      } replace />} />

      {/* Patient Routes */}
      <Route path="/patient" element={<Protected roles={['ROLE_PATIENT']}><PatientDashboard /></Protected>} />
      <Route path="/patient/book" element={<Protected roles={['ROLE_PATIENT']}><BookAppointment /></Protected>} />
      <Route path="/patient/appointments" element={<Protected roles={['ROLE_PATIENT']}><MyAppointments /></Protected>} />
      <Route path="/patient/notifications" element={<Protected roles={['ROLE_PATIENT']}><NotificationsPage /></Protected>} />
      <Route path="/patient/profile" element={<Protected roles={['ROLE_PATIENT']}><ProfilePage /></Protected>} />

      {/* Doctor Routes */}
      <Route path="/doctor" element={<Protected roles={['ROLE_DOCTOR']}><DoctorDashboard /></Protected>} />
      <Route path="/doctor/queue" element={<Protected roles={['ROLE_DOCTOR']}><DoctorDashboard /></Protected>} />
      <Route path="/doctor/appointments" element={<Protected roles={['ROLE_DOCTOR']}><DoctorDashboard /></Protected>} />
      <Route path="/doctor/profile" element={<Protected roles={['ROLE_DOCTOR']}><ProfilePage /></Protected>} />

      {/* Admin Routes */}
      <Route path="/admin" element={<Protected roles={['ROLE_ADMIN']}><AdminDashboard /></Protected>} />
      <Route path="/admin/doctors" element={<Protected roles={['ROLE_ADMIN']}><AdminDoctors /></Protected>} />
      <Route path="/admin/patients" element={<Protected roles={['ROLE_ADMIN']}><AdminPatients /></Protected>} />
      <Route path="/admin/appointments" element={<Protected roles={['ROLE_ADMIN']}><AdminDashboard /></Protected>} />

      {/* Default redirect */}
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}
