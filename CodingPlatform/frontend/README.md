# ⚡ CodeArena — Frontend

A clean, dark-themed SPA frontend for the CodeArena Spring Boot backend.

## 🗂️ Files

```
CodeArena-Frontend/
├── index.html        ← Single page app (all pages)
├── css/
│   └── style.css     ← All styles (dark terminal theme)
└── js/
    ├── api.js        ← All API calls to backend
    ├── ui.js         ← Toast, modal, router helpers
    └── app.js        ← Page logic (auth, problems, submit, leaderboard)
```

## 🚀 How to Run

### Step 1 — Start the Backend first
```bash
cd CodeArena
mvn spring-boot:run
```
Backend must be running at `http://localhost:8080`

### Step 2 — Open the Frontend

**Option A (Recommended): Live Server in VS Code**
1. Install the **Live Server** extension in VS Code
2. Right-click `index.html` → **Open with Live Server**
3. Opens at `http://127.0.0.1:5500`

**Option B: Direct file open**
- Double-click `index.html` to open in browser
- ⚠️ Some browsers block fetch from `file://` — use Live Server if issues

## 🖥️ Pages

| Page | Description |
|------|-------------|
| **Home** | Landing page with CTA |
| **Register** | Create new account |
| **Login** | Sign in (admin: admin/admin123) |
| **Problems** | Browse & filter problems |
| **Problem Detail** | Read problem + code editor + submit |
| **Leaderboard** | Ranked table of all users |
| **Profile** | Your stats and submission history |
| **Admin Dashboard** | Manage problems, view all submissions |

## 🎨 Design

- **Dark terminal aesthetic** — JetBrains Mono + Syne fonts
- **Green accent** (#00ff88) on black background
- **Color-coded**: Easy=green, Medium=yellow, Hard=red
- **Responsive** — works on mobile too

## ⚙️ Config

To change the backend URL, edit line 2 of `js/api.js`:
```js
const API_BASE = 'http://localhost:8080/api';
```
