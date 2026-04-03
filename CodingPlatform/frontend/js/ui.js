// ── TOAST ─────────────────────────────────────────────────────────────────────
function toast(msg, type = 'info', duration = 3500) {
  const container = document.getElementById('toastContainer');
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  const icons = { success: '✅', error: '❌', info: 'ℹ️' };
  el.innerHTML = `<span>${icons[type]}</span><span>${msg}</span>`;
  container.appendChild(el);
  setTimeout(() => { el.style.opacity = '0'; el.style.transform = 'translateX(30px)'; el.style.transition = '0.3s'; setTimeout(() => el.remove(), 300); }, duration);
}

// ── MODAL ─────────────────────────────────────────────────────────────────────
function openModal(id) { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

// ── ROUTER ────────────────────────────────────────────────────────────────────
let currentProblemId = null;

function showPage(id) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  const page = document.getElementById(id);
  if (page) {
    page.classList.add('active');
    window.scrollTo(0, 0);
  }
  updateNav();
}

function updateNav() {
  const loggedIn = Auth.isLoggedIn();
  const user = Auth.getUser();

  document.getElementById('navGuest').style.display    = loggedIn ? 'none' : 'flex';
  document.getElementById('navAuth').style.display     = loggedIn ? 'flex' : 'none';
  document.getElementById('navAdmin').style.display    = (loggedIn && Auth.isAdmin()) ? 'flex' : 'none';
  document.getElementById('navUsername').textContent   = user?.username || '';
}

// ── HELPERS ───────────────────────────────────────────────────────────────────
function diffBadge(d) {
  return `<span class="diff-badge diff-${d}">${d}</span>`;
}

function statusBadge(s) {
  const colors = {
    ACCEPTED: 'var(--green)', WRONG_ANSWER: 'var(--red)',
    COMPILE_ERROR: 'var(--orange)', RUNTIME_ERROR: 'var(--orange)',
    PARTIAL: 'var(--yellow)', TIME_LIMIT_EXCEEDED: 'var(--blue)', PENDING: 'var(--text2)'
  };
  return `<span style="color:${colors[s]||'var(--text2)'}; font-weight:600">${s.replace(/_/g,' ')}</span>`;
}

function timeAgo(dateStr) {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs/24)}d ago`;
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

function renderMarkdown(text) {
  if (!text) return '';
  return text
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>');
}
