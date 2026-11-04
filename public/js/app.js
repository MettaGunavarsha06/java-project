/**
 * Core Application JavaScript
 * Manages Authentication, Sessions, API Requests, and Global UI Elements.
 */

const API_BASE = '/api';

// Current session state
const Auth = {
  getToken() {
    return localStorage.getItem('quiz_token');
  },
  getUser() {
    const u = localStorage.getItem('quiz_user');
    return u ? JSON.parse(u) : null;
  },
  setSession(token, user) {
    localStorage.setItem('quiz_token', token);
    localStorage.setItem('quiz_user', JSON.stringify(user));
  },
  clearSession() {
    localStorage.removeItem('quiz_token');
    localStorage.removeItem('quiz_user');
  },
  isLoggedIn() {
    return !!this.getToken();
  },
  isAdmin() {
    const user = this.getUser();
    return user && user.role === 'ADMIN';
  },
  isStudent() {
    const user = this.getUser();
    return user && user.role === 'STUDENT';
  }
};

/**
 * Route guard requiring active login session and expected role
 */
function requireAuth(expectedRole = null) {
  const token = Auth.getToken();
  const user = Auth.getUser();

  if (!token || !user) {
    const currentPath = window.location.pathname.split('/').pop() || 'index.html';
    window.location.href = `login.html?redirect=${encodeURIComponent(currentPath)}`;
    return null;
  }

  if (expectedRole && user.role !== expectedRole) {
    if (user.role === 'ADMIN') {
      window.location.href = 'admin-dashboard.html';
    } else {
      window.location.href = 'student-dashboard.html';
    }
    return null;
  }

  return user;
}

/**
 * Standardized fetch wrapper with Bearer token authentication
 */
async function apiRequest(endpoint, options = {}) {
  const headers = options.headers || {};
  headers['Content-Type'] = 'application/json';

  const token = Auth.getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers
    });
    const data = await response.json();
    return data;
  } catch (err) {
    console.error('API Request failed:', err);
    return { success: false, message: 'Network error or server unreachable.' };
  }
}

/**
 * Show animated toast notification
 */
function showToast(message, type = 'info') {
  let toastContainer = document.getElementById('toast-container');
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toast-container';
    toastContainer.style.cssText = 'position:fixed;bottom:24px;right:24px;z-index:9999;display:flex;flex-direction:column;gap:8px;';
    document.body.appendChild(toastContainer);
  }

  const toast = document.createElement('div');
  const bgClass = type === 'success' ? '#10b981' : (type === 'error' ? '#ef4444' : '#2563eb');
  toast.style.cssText = `background:${bgClass};color:#fff;padding:12px 20px;border-radius:8px;box-shadow:0 4px 12px rgba(0,0,0,0.15);font-weight:500;font-size:0.9rem;transition:all 0.3s ease;transform:translateY(10px);opacity:0;`;
  toast.innerText = message;

  toastContainer.appendChild(toast);
  setTimeout(() => {
    toast.style.transform = 'translateY(0)';
    toast.style.opacity = '1';
  }, 10);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

/**
 * Initialize dynamic navbar based on user session
 */
function renderNavbar() {
  const navContainer = document.getElementById('navbar-auth-links');
  if (!navContainer) return;

  const user = Auth.getUser();

  if (user) {
    const dashboardUrl = user.role === 'ADMIN' ? 'admin-dashboard.html' : 'student-dashboard.html';
    const roleBadge = user.role === 'ADMIN' ? '<span class="badge bg-primary">Admin</span>' : '<span class="badge bg-info text-dark">Student</span>';

    navContainer.innerHTML = `
      <div class="d-flex align-items-center gap-3">
        <a href="${dashboardUrl}" class="nav-link-custom">
          <i class="bi bi-speedometer2 me-1"></i> Dashboard
        </a>
        <a href="leaderboard.html" class="nav-link-custom">
          <i class="bi bi-trophy me-1"></i> Leaderboard
        </a>
        <div class="dropdown">
          <button class="btn btn-outline-custom dropdown-toggle d-flex align-items-center gap-2 py-1 px-3" type="button" data-bs-toggle="dropdown">
            <i class="bi bi-person-circle"></i>
            <span>${escapeHtml(user.name)}</span>
            ${roleBadge}
          </button>
          <ul class="dropdown-menu dropdown-menu-end shadow-sm">
            <li><span class="dropdown-item-text text-muted small">${escapeHtml(user.email)}</span></li>
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item" href="${dashboardUrl}"><i class="bi bi-grid me-2"></i>My Dashboard</a></li>
            <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="handleLogout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
          </ul>
        </div>
      </div>
    `;
  } else {
    navContainer.innerHTML = `
      <div class="d-flex align-items-center gap-2">
        <a href="leaderboard.html" class="nav-link-custom me-2">
          <i class="bi bi-trophy me-1"></i> Leaderboard
        </a>
        <a href="login.html" class="btn btn-outline-custom">Log In</a>
        <a href="register.html" class="btn btn-primary-custom">Get Started</a>
      </div>
    `;
  }
}

async function handleLogout() {
  await apiRequest('/auth/logout', { method: 'POST' });
  Auth.clearSession();
  showToast('Logged out successfully', 'info');
  setTimeout(() => {
    window.location.href = 'index.html';
  }, 500);
}

function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.innerText = text;
  return div.innerHTML;
}

/**
 * Live Database Connectivity Status Bar & Auto-Reconnect
 */
async function checkDatabaseStatus() {
  try {
    const res = await apiRequest('/system/db-status');
    if (!res.success || !res.data) return;

    let banner = document.getElementById('global-db-status-banner');
    if (!banner) {
      banner = document.createElement('div');
      banner.id = 'global-db-status-banner';
      banner.style.cssText = 'position:fixed;bottom:12px;left:16px;z-index:9990;font-size:0.8rem;padding:6px 14px;border-radius:20px;box-shadow:0 2px 8px rgba(0,0,0,0.1);display:flex;align-items:center;gap:8px;backdrop-filter:blur(6px);';
      document.body.appendChild(banner);
    }

    if (res.data.connected) {
      banner.style.background = '#dcfce7';
      banner.style.color = '#15803d';
      banner.style.border = '1px solid #86efac';
      banner.innerHTML = `<i class="bi bi-database-check"></i> <strong>MySQL Active:</strong> ${escapeHtml(res.data.database)}`;
    } else {
      banner.style.background = '#fef9c3';
      banner.style.color = '#854d0e';
      banner.style.border = '1px solid #fde047';
      banner.innerHTML = `<i class="bi bi-database-exclamation"></i> <span>Persistence: Embedded In-Memory</span> 
        <button onclick="reconnectDatabase(this)" class="btn btn-xs btn-outline-warning py-0 px-2 ms-1 rounded-pill" style="font-size:0.75rem;">
          <i class="bi bi-arrow-repeat"></i> Connect MySQL
        </button>`;
    }
  } catch (err) {
    // Silent ignore
  }
}

async function reconnectDatabase(btn) {
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm" style="width:0.7rem;height:0.7rem;"></span> Connecting...';
  }
  const res = await apiRequest('/system/reconnect-db', { method: 'POST' });
  if (res.success && res.data && res.data.connected) {
    showToast('Connected to MySQL database online_quiz_db!', 'success');
  } else {
    showToast('MySQL server not responding on localhost:3306. Check MySQL service.', 'info');
  }
  checkDatabaseStatus();
}

document.addEventListener('DOMContentLoaded', () => {
  renderNavbar();
  checkDatabaseStatus();
});

