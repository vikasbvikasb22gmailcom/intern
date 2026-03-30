import axios from 'axios';

const API_BASE = 'http://localhost:8083';

const api = axios.create({ baseURL: API_BASE });

// Auto-attach token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auto-logout on 401
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

// Auth
export const login = (data) => api.post('/api/auth/login', data);
export const register = (data) => api.post('/api/auth/register', data);

// Doctors
export const getDoctors = (params) => api.get('/api/doctors', { params });
export const getDoctorById = (id) => api.get(`/api/doctors/${id}`);
export const createDoctor = (data) => api.post('/api/doctors', data);
export const updateDoctor = (id, data) => api.put(`/api/doctors/${id}`, data);
export const getDoctorSchedule = (id) => api.get(`/api/doctors/${id}/schedule`);
export const addSchedule = (id, data) => api.post(`/api/doctors/${id}/schedule`, data);
export const getSpecializations = () => api.get('/api/doctors/specializations');

// Appointments
export const getAvailableSlots = (doctorId, date) =>
  api.get('/api/appointments/available-slots', { params: { doctorId, date } });
export const bookAppointment = (data) => api.post('/api/appointments', data);
export const getMyAppointments = (params) => api.get('/api/appointments/my', { params });
export const getAppointmentById = (id) => api.get(`/api/appointments/${id}`);
export const getQueueStatus = (id) => api.get(`/api/appointments/${id}/queue-status`);
export const getDoctorQueue = (doctorId) => api.get(`/api/appointments/queue/doctor/${doctorId}`);
export const cancelAppointment = (id, reason) =>
  api.patch(`/api/appointments/${id}/cancel`, null, { params: { reason } });
export const updateAppointmentStatus = (id, data) => api.patch(`/api/appointments/${id}/status`, data);
export const getAllAppointments = (params) => api.get('/api/appointments', { params });
export const getDoctorAppointments = (doctorId, params) =>
  api.get(`/api/appointments/doctor/${doctorId}`, { params });

// Notifications
export const getNotifications = () => api.get('/api/notifications');
export const getUnreadCount = () => api.get('/api/notifications/unread-count');
export const markNotificationRead = (id) => api.patch(`/api/notifications/${id}/read`);

// Admin
export const getDashboard = () => api.get('/api/admin/dashboard');
export const getAllPatients = (params) => api.get('/api/admin/patients', { params });

// Profile
export const getProfile = () => api.get('/api/profile');
export const updateProfile = (data) => api.put('/api/profile', data);

export default api;
