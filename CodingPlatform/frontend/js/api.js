// ── CONFIG ──────────────────────────────────────────────────────────────────
const API_BASE = 'http://localhost:8080/api';

// ── AUTH HELPERS ─────────────────────────────────────────────────────────────
const Auth = {
  getToken: () => localStorage.getItem('ca_token'),
  getUser:  () => JSON.parse(localStorage.getItem('ca_user') || 'null'),
  isAdmin:  () => Auth.getUser()?.role === 'ADMIN',
  isLoggedIn: () => !!Auth.getToken(),

  save(token, user) {
    localStorage.setItem('ca_token', token);
    localStorage.setItem('ca_user', JSON.stringify(user));
  },

  clear() {
    localStorage.removeItem('ca_token');
    localStorage.removeItem('ca_user');
  }
};

// ── FETCH WRAPPER ─────────────────────────────────────────────────────────────
async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = Auth.getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(API_BASE + path, { headers, ...options });
  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    throw new Error(data.message || `HTTP ${res.status}`);
  }
  return data;
}

// ── API CALLS ─────────────────────────────────────────────────────────────────
const API = {
  // Auth
  register: (body)    => api('/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  login:    (body)    => api('/auth/login',    { method: 'POST', body: JSON.stringify(body) }),

  // Problems
  getProblems: (params = '') => api('/problems' + params),
  getProblem:  (id)          => api(`/problems/${id}`),
  createProblem: (body)      => api('/admin/problems', { method: 'POST', body: JSON.stringify(body) }),
  updateProblem: (id, body)  => api(`/admin/problems/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  deleteProblem: (id)        => api(`/admin/problems/${id}`, { method: 'DELETE' }),

  // Submissions
  submit:          (body)    => api('/submissions', { method: 'POST', body: JSON.stringify(body) }),
  mySubmissions:   ()        => api('/submissions/me'),
  mySubsForProblem:(id)      => api(`/submissions/me/problem/${id}`),
  allSubmissions:  ()        => api('/admin/submissions'),
  getSubmission:   (id)      => api(`/submissions/${id}`),

  // Leaderboard & Profile
  leaderboard: ()    => api('/leaderboard'),
  topN:        (n)   => api(`/leaderboard/top?n=${n}`),
  myProfile:   ()    => api('/users/me'),
  userProfile: (id)  => api(`/users/${id}`),
};
