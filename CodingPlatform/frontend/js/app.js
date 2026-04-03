// ── STATE ─────────────────────────────────────────────────────────────────────
let allProblems = [];
let currentFilter = 'ALL';
let solvedSet = new Set();

// ── INIT ──────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  updateNav();
  if (Auth.isLoggedIn()) {
    loadSolvedSet();
    showPage('pageProblems');
    loadProblems();
  } else {
    showPage('pageHome');
  }
});

async function loadSolvedSet() {
  try {
    const res = await API.mySubmissions();
    const subs = res.data || [];
    subs.filter(s => s.status === 'ACCEPTED').forEach(s => solvedSet.add(s.problemId));
  } catch (_) {}
}

// ── AUTH ──────────────────────────────────────────────────────────────────────
async function doRegister() {
  const username = document.getElementById('regUsername').value.trim();
  const email    = document.getElementById('regEmail').value.trim();
  const password = document.getElementById('regPassword').value;
  const btn = document.getElementById('btnRegister');

  if (!username || !email || !password) return toast('Please fill all fields.', 'error');
  btn.disabled = true; btn.innerHTML = '<span class="spinner"></span> Registering…';

  try {
    const res = await API.register({ username, email, password });
    const d = res.data;
    Auth.save(d.token, { id: d.userId, username: d.username, email: d.email, role: d.role });
    toast(`Welcome, ${d.username}! 🎉`, 'success');
    await loadSolvedSet();
    showPage('pageProblems');
    loadProblems();
  } catch (e) {
    toast(e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Create Account';
  }
}

async function doLogin() {
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value;
  const btn = document.getElementById('btnLogin');

  if (!username || !password) return toast('Please fill all fields.', 'error');
  btn.disabled = true; btn.innerHTML = '<span class="spinner"></span> Signing in…';

  try {
    const res = await API.login({ username, password });
    const d = res.data;
    Auth.save(d.token, { id: d.userId, username: d.username, email: d.email, role: d.role });
    toast(`Welcome back, ${d.username}!`, 'success');
    await loadSolvedSet();
    showPage('pageProblems');
    loadProblems();
    if (Auth.isAdmin()) loadAdminStats();
  } catch (e) {
    toast(e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Sign In';
  }
}

function doLogout() {
  Auth.clear();
  solvedSet.clear();
  allProblems = [];
  toast('Logged out successfully.', 'info');
  showPage('pageHome');
}

// ── PROBLEMS LIST ─────────────────────────────────────────────────────────────
async function loadProblems() {
  const tbody = document.getElementById('problemsTbody');
  tbody.innerHTML = `<tr><td colspan="5"><div class="loading-overlay"><span class="spinner"></span></div></td></tr>`;

  try {
    const res = await API.getProblems();
    allProblems = res.data || [];
    renderProblems(allProblems);
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="5"><div class="empty-state"><div class="icon">⚠️</div><p>${e.message}</p></div></td></tr>`;
  }
}

function renderProblems(problems) {
  const tbody = document.getElementById('problemsTbody');
  const search = document.getElementById('searchInput')?.value.toLowerCase() || '';
  let list = problems;

  if (currentFilter !== 'ALL') list = list.filter(p => p.difficulty === currentFilter);
  if (search) list = list.filter(p => p.title.toLowerCase().includes(search) || (p.tags||'').toLowerCase().includes(search));

  if (!list.length) {
    tbody.innerHTML = `<tr><td colspan="5"><div class="empty-state"><div class="icon">🔍</div><p>No problems found.</p></div></td></tr>`;
    return;
  }

  tbody.innerHTML = list.map((p, i) => {
    const solved = solvedSet.has(p.id);
    const dotClass = solved ? 'solved' : 'none';
    const tags = (p.tags || '').split(',').filter(Boolean).map(t => `<span class="tag">${t.trim()}</span>`).join('');
    const rate = p.acceptanceRate ? `${p.acceptanceRate}%` : '—';
    return `
      <tr onclick="openProblem(${p.id})" style="animation: fadeUp 0.4s ${i * 0.03}s both">
        <td><span class="status-dot ${dotClass}"></span></td>
        <td class="problem-title">${escapeHtml(p.title)}</td>
        <td>${diffBadge(p.difficulty)}</td>
        <td>${tags}</td>
        <td style="color:var(--text2)">${rate}</td>
      </tr>`;
  }).join('');
}

function filterProblems(diff) {
  currentFilter = diff;
  document.querySelectorAll('.filter-btn').forEach(b => {
    b.classList.toggle('active', b.dataset.diff === diff);
  });
  renderProblems(allProblems);
}

// ── PROBLEM DETAIL ────────────────────────────────────────────────────────────
async function openProblem(id) {
  currentProblemId = id;
  showPage('pageProblem');

  document.getElementById('problemDesc').innerHTML = `<div class="loading-overlay"><span class="spinner"></span></div>`;
  document.getElementById('resultArea').innerHTML = '';
  document.getElementById('prevSubmissions').innerHTML = '';

  try {
    const [probRes, mySubsRes] = await Promise.all([
      API.getProblem(id),
      Auth.isLoggedIn() ? API.mySubsForProblem(id) : Promise.resolve({ data: [] })
    ]);

    const p = probRes.data;
    const subs = mySubsRes.data || [];

    // Set starter code
    document.getElementById('codeEditor').value = p.starterCode || '// Write your solution here\n';

    // Render description
    document.getElementById('problemDesc').innerHTML = `
      <div class="problem-meta">
        ${diffBadge(p.difficulty)}
        ${(p.tags||'').split(',').filter(Boolean).map(t => `<span class="tag">${t.trim()}</span>`).join('')}
        <span style="font-size:0.72rem;color:var(--text3)">Acceptance: ${p.acceptanceRate||0}%</span>
      </div>
      <div class="problem-body">${renderMarkdown(p.description)}</div>
    `;

    document.getElementById('problemTitle').textContent = p.title;

    // Previous submissions
    if (subs.length) {
      document.getElementById('prevSubmissions').innerHTML = `
        <div class="section-title">My Submissions</div>
        <div class="submissions-list">
          ${subs.slice(0, 5).map(s => `
            <div class="sub-item">
              <div>
                <div class="sub-title">${statusBadge(s.status)}</div>
                <div class="sub-meta">${s.passedTests}/${s.totalTests} tests · ${s.executionTimeMs}ms · ${timeAgo(s.submittedAt)}</div>
              </div>
              <span style="font-size:0.78rem;color:var(--green);font-weight:700">${s.score}%</span>
            </div>
          `).join('')}
        </div>
      `;
    }
  } catch (e) {
    document.getElementById('problemDesc').innerHTML = `<div class="empty-state"><p>${e.message}</p></div>`;
  }
}

// ── SUBMIT CODE ───────────────────────────────────────────────────────────────
async function submitCode() {
  if (!Auth.isLoggedIn()) { toast('Please login to submit.', 'error'); return; }

  const code     = document.getElementById('codeEditor').value.trim();
  const language = document.getElementById('langSelect').value;
  const btn      = document.getElementById('btnSubmit');

  if (!code) return toast('Code cannot be empty.', 'error');

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> Evaluating…';
  document.getElementById('resultArea').innerHTML = `
    <div class="result-card">
      <div style="display:flex;align-items:center;gap:12px;color:var(--text2)">
        <span class="spinner"></span>
        <span style="font-size:0.85rem">Running test cases…</span>
      </div>
    </div>`;

  try {
    const res = await API.submit({ problemId: currentProblemId, code, language });
    const s = res.data;

    if (s.status === 'ACCEPTED') {
      solvedSet.add(currentProblemId);
      toast('🎉 All test cases passed!', 'success', 4000);
    } else {
      toast(`${s.passedTests}/${s.totalTests} test cases passed.`, 'info');
    }

    renderResult(s);
  } catch (e) {
    toast(e.message, 'error');
    document.getElementById('resultArea').innerHTML = `
      <div class="result-card">
        <div class="error-box">${escapeHtml(e.message)}</div>
      </div>`;
  } finally {
    btn.disabled = false;
    btn.textContent = '▶ Submit';
  }
}

function renderResult(s) {
  const testHtml = (s.passedTests + s.totalTests > 0) ? `
    <div class="test-cases">
      ${Array.from({length: s.totalTests}, (_, i) => {
        const passed = i < s.passedTests;
        return `<div class="test-case ${passed ? 'pass' : 'fail'}">
          <span class="test-icon">${passed ? '✅' : '❌'}</span>
          <span>Test Case ${i + 1}</span>
          <span style="color:${passed ? 'var(--green)' : 'var(--red)'};margin-left:auto;font-size:0.72rem">
            ${passed ? 'Passed' : 'Failed'}
          </span>
        </div>`;
      }).join('')}
    </div>` : '';

  document.getElementById('resultArea').innerHTML = `
    <div class="result-card">
      <div class="result-header">
        <span class="result-status ${s.status}">${s.status.replace(/_/g,' ')}</span>
      </div>
      <div class="result-stats">
        <span>Tests: <span>${s.passedTests}/${s.totalTests}</span></span>
        <span>Score: <span>${s.score}%</span></span>
        <span>Time: <span>${s.executionTimeMs}ms</span></span>
      </div>
      ${s.errorMessage ? `<div class="error-box" style="margin-bottom:1rem">${escapeHtml(s.errorMessage)}</div>` : ''}
      ${testHtml}
    </div>`;
}

// ── LEADERBOARD ───────────────────────────────────────────────────────────────
async function loadLeaderboard() {
  showPage('pageLeaderboard');
  const tbody = document.getElementById('lbTbody');
  tbody.innerHTML = `<tr><td colspan="7"><div class="loading-overlay"><span class="spinner"></span></div></td></tr>`;

  try {
    const res = await API.leaderboard();
    const entries = res.data || [];
    const myId = Auth.getUser()?.id;

    if (!entries.length) {
      tbody.innerHTML = `<tr><td colspan="7"><div class="empty-state"><div class="icon">🏆</div><p>No rankings yet. Be the first!</p></div></td></tr>`;
      return;
    }

    tbody.innerHTML = entries.map(e => {
      const rankClass = e.rank <= 3 ? `rank-${e.rank}` : '';
      const isMe = e.userId == myId;
      return `
        <tr class="${isMe ? 'me' : ''}">
          <td><span class="rank-num ${rankClass}">${e.rank === 1 ? '🥇' : e.rank === 2 ? '🥈' : e.rank === 3 ? '🥉' : '#'+e.rank}</span></td>
          <td>
            <span style="font-weight:600;color:${isMe ? 'var(--green)' : 'var(--text)'}">${escapeHtml(e.username)}</span>
            ${isMe ? '<span style="font-size:0.65rem;color:var(--green);margin-left:6px">(you)</span>' : ''}
          </td>
          <td><span class="score-val">${e.totalScore}</span></td>
          <td>${e.problemsSolved}</td>
          <td style="color:var(--green)">${e.easySolved}</td>
          <td style="color:var(--yellow)">${e.mediumSolved}</td>
          <td style="color:var(--red)">${e.hardSolved}</td>
        </tr>`;
    }).join('');
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="7"><div class="empty-state"><p>${e.message}</p></div></td></tr>`;
  }
}

// ── PROFILE ───────────────────────────────────────────────────────────────────
async function loadProfile() {
  if (!Auth.isLoggedIn()) { toast('Please login first.', 'error'); return; }
  showPage('pageProfile');

  try {
    const [profileRes, subsRes] = await Promise.all([API.myProfile(), API.mySubmissions()]);
    const p = profileRes.data;
    const subs = subsRes.data || [];

    document.getElementById('profileAvatar').textContent = p.username[0].toUpperCase();
    document.getElementById('profileName').textContent = p.username;
    document.getElementById('profileRole').textContent = p.role;
    document.getElementById('profileScore').textContent = p.totalScore;
    document.getElementById('profileSolved').textContent = p.problemsSolved;
    document.getElementById('profileSubmissions').textContent = p.totalSubmissions;
    document.getElementById('profileEasy').textContent = p.easySolved;
    document.getElementById('profileMedium').textContent = p.mediumSolved;
    document.getElementById('profileHard').textContent = p.hardSolved;

    // Recent submissions
    const subList = document.getElementById('profileSubList');
    if (!subs.length) {
      subList.innerHTML = `<div class="empty-state"><div class="icon">📝</div><p>No submissions yet.</p></div>`;
    } else {
      subList.innerHTML = subs.slice(0, 10).map(s => `
        <div class="sub-item" onclick="openProblem(${s.problemId})">
          <div>
            <div class="sub-title">${escapeHtml(s.problemTitle)}</div>
            <div class="sub-meta">${s.language} · ${timeAgo(s.submittedAt)}</div>
          </div>
          <div class="sub-right">
            ${statusBadge(s.status)}
            <div class="sub-meta">${s.passedTests}/${s.totalTests} tests</div>
          </div>
        </div>`).join('');
    }
  } catch (e) {
    toast(e.message, 'error');
  }
}

// ── ADMIN ─────────────────────────────────────────────────────────────────────
async function loadAdmin() {
  if (!Auth.isAdmin()) { toast('Admin access required.', 'error'); return; }
  showPage('pageAdmin');
  loadAdminStats();
  loadAdminProblems();
  loadAdminSubmissions();
}

async function loadAdminStats() {
  try {
    const [probRes, subRes, lbRes] = await Promise.all([
      API.getProblems(), API.allSubmissions(), API.leaderboard()
    ]);
    document.getElementById('statProblems').textContent = (probRes.data||[]).length;
    document.getElementById('statSubmissions').textContent = (subRes.data||[]).length;
    document.getElementById('statUsers').textContent = (lbRes.data||[]).length;
    const accepted = (subRes.data||[]).filter(s => s.status === 'ACCEPTED').length;
    const total = (subRes.data||[]).length;
    document.getElementById('statAccepted').textContent = total ? Math.round(accepted*100/total)+'%' : '0%';
  } catch (_) {}
}

async function loadAdminProblems() {
  const list = document.getElementById('adminProblemList');
  list.innerHTML = `<div class="loading-overlay"><span class="spinner"></span></div>`;
  try {
    const res = await API.getProblems();
    const problems = res.data || [];
    list.innerHTML = problems.map(p => `
      <div class="sub-item">
        <div>
          <div class="sub-title">${escapeHtml(p.title)}</div>
          <div class="sub-meta">${p.difficulty} · ${p.totalSubmissions} submissions · ${p.acceptanceRate}% acceptance</div>
        </div>
        <div style="display:flex;gap:8px;align-items:center">
          ${diffBadge(p.difficulty)}
          <button class="btn btn-sm btn-secondary" onclick="editProblem(${p.id})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="deleteProblem(${p.id}, '${escapeHtml(p.title)}')">Delete</button>
        </div>
      </div>`).join('') || `<div class="empty-state"><p>No problems yet.</p></div>`;
  } catch (e) {
    list.innerHTML = `<div class="empty-state"><p>${e.message}</p></div>`;
  }
}

async function loadAdminSubmissions() {
  const list = document.getElementById('adminSubList');
  list.innerHTML = `<div class="loading-overlay"><span class="spinner"></span></div>`;
  try {
    const res = await API.allSubmissions();
    const subs = (res.data || []).slice(0, 20);
    list.innerHTML = subs.map(s => `
      <div class="sub-item">
        <div>
          <div class="sub-title">${escapeHtml(s.username)} → ${escapeHtml(s.problemTitle)}</div>
          <div class="sub-meta">${s.language} · ${timeAgo(s.submittedAt)} · ${s.passedTests}/${s.totalTests} tests</div>
        </div>
        <div style="text-align:right">
          ${statusBadge(s.status)}
          <div class="sub-meta">${s.score}%</div>
        </div>
      </div>`).join('') || `<div class="empty-state"><p>No submissions yet.</p></div>`;
  } catch (e) {
    list.innerHTML = `<div class="empty-state"><p>${e.message}</p></div>`;
  }
}

// ── PROBLEM FORM (CREATE / EDIT) ──────────────────────────────────────────────
let editingProblemId = null;

function openCreateProblem() {
  editingProblemId = null;
  document.getElementById('problemFormTitle').textContent = 'Add New Problem';
  document.getElementById('problemForm').reset();
  document.getElementById('fStarterCode').value = 'public ReturnType methodName(ParamType param) {\n    // Write your solution here\n    return null;\n}';
  document.getElementById('fTestCases').value = JSON.stringify([
    { input: [5], expected: 55 }
  ], null, 2);
  openModal('modalProblem');
}

async function editProblem(id) {
  editingProblemId = id;
  document.getElementById('problemFormTitle').textContent = 'Edit Problem';
  try {
    const res = await API.getProblem(id);
    const p = res.data;
    document.getElementById('fTitle').value = p.title;
    document.getElementById('fDifficulty').value = p.difficulty;
    document.getElementById('fTags').value = p.tags || '';
    document.getElementById('fDescription').value = p.description;
    document.getElementById('fStarterCode').value = p.starterCode || '';
    document.getElementById('fTestCases').value = p.testCases ? JSON.stringify(JSON.parse(p.testCases), null, 2) : '';
    openModal('modalProblem');
  } catch (e) {
    toast(e.message, 'error');
  }
}

async function saveProblem() {
  const body = {
    title:       document.getElementById('fTitle').value.trim(),
    difficulty:  document.getElementById('fDifficulty').value,
    tags:        document.getElementById('fTags').value.trim(),
    description: document.getElementById('fDescription').value.trim(),
    starterCode: document.getElementById('fStarterCode').value,
    testCases:   document.getElementById('fTestCases').value.trim(),
  };

  if (!body.title || !body.description || !body.testCases) {
    return toast('Title, description and test cases are required.', 'error');
  }

  try { JSON.parse(body.testCases); } catch { return toast('Test cases must be valid JSON.', 'error'); }

  const btn = document.getElementById('btnSaveProblem');
  btn.disabled = true; btn.innerHTML = '<span class="spinner"></span> Saving…';

  try {
    if (editingProblemId) {
      await API.updateProblem(editingProblemId, body);
      toast('Problem updated!', 'success');
    } else {
      await API.createProblem(body);
      toast('Problem created!', 'success');
    }
    closeModal('modalProblem');
    loadAdminProblems();
    loadProblems();
  } catch (e) {
    toast(e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Save Problem';
  }
}

async function deleteProblem(id, title) {
  if (!confirm(`Delete "${title}"? This action cannot be undone.`)) return;
  try {
    await API.deleteProblem(id);
    toast('Problem deleted.', 'info');
    loadAdminProblems();
    loadProblems();
  } catch (e) {
    toast(e.message, 'error');
  }
}

// ── TAB KEY IN EDITOR ─────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  const editor = document.getElementById('codeEditor');
  if (editor) {
    editor.addEventListener('keydown', e => {
      if (e.key === 'Tab') {
        e.preventDefault();
        const s = editor.selectionStart;
        editor.value = editor.value.substring(0, s) + '    ' + editor.value.substring(editor.selectionEnd);
        editor.selectionStart = editor.selectionEnd = s + 4;
      }
    });
  }
});
