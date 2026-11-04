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

document.addEventListener('DOMContentLoaded', () => {
  renderNavbar();
});

