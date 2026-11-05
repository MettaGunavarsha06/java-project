/**
 * Admin Dashboard Management JavaScript
 * Handles Quiz authoring, Question bank CRUD, Student monitoring,
 * and Platform Analytics with Chart.js.
 */

let activeQuizIdForQuestions = null;
let performanceChart = null;

document.addEventListener('DOMContentLoaded', async () => {
  if (!Auth.isLoggedIn() || !Auth.isAdmin()) {
    alert('Access Denied. Administrator privileges required.');
    window.location.href = 'login.html';
    return;
  }

  await loadDashboardStats();
  await loadQuizzesTable();
  await loadStudentsTable();
  await loadAttemptsTable();
});

async function loadDashboardStats() {
  const res = await apiRequest('/admin/stats');
  if (!res.success || !res.data) return;

  const stats = res.data;
  const localList = QuizStore.getLocalQuizzes();
  const quizCount = Math.max(stats.totalQuizzes || 0, localList.length);
  document.getElementById('stat-total-quizzes').innerText = quizCount;
  
  let localStudentsCount = 0;
  try {
    const localCandidates = JSON.parse(localStorage.getItem('registered_candidates') || '[]');
    if (Array.isArray(localCandidates)) localStudentsCount = localCandidates.length;
  } catch (e) {}

  document.getElementById('stat-total-students').innerText = Math.max(stats.totalStudents || 0, localStudentsCount, cachedCandidatesList.length);
  document.getElementById('stat-total-attempts').innerText = stats.totalAttempts || 0;
  document.getElementById('stat-avg-score').innerText = (stats.averageScore || 0) + '%';

  // Render Charts
  renderPerformanceChart(stats);
}

function renderPerformanceChart(stats) {
  const ctx = document.getElementById('passRateChart');
  if (!ctx) return;

  if (performanceChart) {
    performanceChart.destroy();
  }

  const passed = stats.passedCount || 0;
  const failed = stats.failedCount || 0;

  performanceChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Passed (>= 50%)', 'Failed (< 50%)'],
      datasets: [{
        data: [passed, failed],
        backgroundColor: ['#10b981', '#ef4444'],
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'bottom' }
      },
      cutout: '70%'
    }
  });
}

// ---------------- QUIZ MANAGEMENT ----------------

async function loadQuizzesTable() {
  const quizzes = await QuizStore.getMergedQuizzes();
  const tbody = document.getElementById('admin-quizzes-tbody');
  if (!tbody) return;

  if (!quizzes || quizzes.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No quizzes available. Click "Create Quiz" above.</td></tr>';
    return;
  }

  tbody.innerHTML = quizzes.map(q => `
    <tr>
      <td class="fw-bold">#${q.id}</td>
      <td>
        <div class="fw-bold text-dark">${escapeHtml(q.title)}</div>
        <div class="small text-muted text-truncate" style="max-width: 320px;">${escapeHtml(q.description)}</div>
      </td>
      <td><span class="badge bg-light text-dark border">${q.duration} Mins</span></td>
      <td><span class="badge bg-primary">${q.totalMarks} Marks</span></td>
      <td><span class="badge bg-info text-dark">${q.questionCount || (q.questions ? q.questions.length : 0)} Questions</span></td>
      <td class="text-end">
        <button class="btn btn-sm btn-outline-info me-1" title="Share Quiz Link & QR Code" onclick="openShareQuizModal(${q.id}, '${escapeHtml(q.title)}', false)">
          <i class="bi bi-qr-code me-1"></i> Share / QR
        </button>
        <button class="btn btn-sm btn-outline-primary me-1" onclick="manageQuestions(${q.id}, '${escapeHtml(q.title)}')">
          <i class="bi bi-list-check me-1"></i> Questions
        </button>
        <button class="btn btn-sm btn-outline-secondary me-1" onclick="openEditQuizModal(${q.id})">
          <i class="bi bi-pencil"></i>
        </button>
        <button class="btn btn-sm btn-outline-danger me-1" onclick="deleteQuiz(${q.id})">
          <i class="bi bi-trash"></i>
        </button>
        <button class="btn btn-sm btn-outline-success" title="Export Report" onclick="exportQuizReport(${q.id})">
          <i class="bi bi-file-earmark-arrow-down"></i>
        </button>
      </td>
    </tr>
  `).join('');
}

function openCreateQuizModal() {
  document.getElementById('quizModalTitle').innerText = 'Create New Quiz';
  document.getElementById('quizForm').reset();
  document.getElementById('quiz-id-field').value = '';
  const qCountInput = document.getElementById('quiz-questions-count-input');
  if (qCountInput) qCountInput.value = '10';
  const modal = new bootstrap.Modal(document.getElementById('quizModal'));
  modal.show();
}

async function openEditQuizModal(id) {
  let q = null;
  const res = await apiRequest(`/quizzes/${id}`);
  if (res && res.success && res.data) {
    q = res.data;
  } else {
    q = QuizStore.getLocalQuizzes().find(x => x.id === parseInt(id));
  }

  if (!q) {
    showToast('Failed to fetch quiz details', 'error');
    return;
  }
  document.getElementById('quizModalTitle').innerText = 'Edit Quiz #' + q.id;
  document.getElementById('quiz-id-field').value = q.id;
  document.getElementById('quiz-title-input').value = q.title;
  document.getElementById('quiz-desc-input').value = q.description;
  document.getElementById('quiz-duration-input').value = q.duration;
  document.getElementById('quiz-marks-input').value = q.totalMarks;

  const qCountInput = document.getElementById('quiz-questions-count-input');
  if (qCountInput) {
    const totalQ = q.questions ? q.questions.length : (q.questionCount || 10);
    qCountInput.value = totalQ;
    if (!qCountInput.value) {
      const opt = document.createElement('option');
      opt.value = totalQ;
      opt.innerText = `${totalQ} Questions`;
      qCountInput.appendChild(opt);
      qCountInput.value = totalQ;
    }
  }

  const modal = new bootstrap.Modal(document.getElementById('quizModal'));
  modal.show();
}

async function saveQuiz() {
  const id = document.getElementById('quiz-id-field').value;
  const title = document.getElementById('quiz-title-input').value.trim();
  const description = document.getElementById('quiz-desc-input').value.trim();
  const duration = parseInt(document.getElementById('quiz-duration-input').value);
  const totalMarks = parseInt(document.getElementById('quiz-marks-input').value);
  const questionCount = parseInt(document.getElementById('quiz-questions-count-input')?.value) || 10;

  if (!title) {
    alert('Please enter a quiz title');
    return;
  }

  let res;
  if (id) {
    // Update
    res = await apiRequest(`/admin/quizzes/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ title, description, duration, totalMarks, questionCount })
    });
  } else {
    // Create
    res = await apiRequest('/admin/quizzes', {
      method: 'POST',
      body: JSON.stringify({ title, description, duration, totalMarks, questionCount })
    });
  }

  if (res.success) {
    const isNew = !id;
    // Persist via QuizStore
    if (res.data) {
      QuizStore.saveQuiz(res.data);
    }

    bootstrap.Modal.getInstance(document.getElementById('quizModal')).hide();
    loadQuizzesTable();
    loadDashboardStats();

    if (isNew && res.data && res.data.id) {
      showToast('Quiz created! Showing shareable link & QR code', 'success');
      setTimeout(() => {
        openShareQuizModal(res.data.id, title, true);
      }, 300);
    } else {
      showToast('Quiz updated successfully', 'success');
    }
  } else {
    alert(res.message || 'Error saving quiz');
  }
}

async function deleteQuiz(id) {
  if (!confirm(`Are you sure you want to delete Quiz #${id}? All associated questions and attempts will be removed.`)) return;

  const res = await apiRequest(`/admin/quizzes/${id}`, { method: 'DELETE' });
  QuizStore.removeQuiz(id);
  if (res.success) {
    showToast('Quiz deleted successfully', 'success');
    loadQuizzesTable();
    loadDashboardStats();
  } else {
    alert(res.message || 'Error deleting quiz');
  }
}

async function exportQuizReport(id) {
  showToast('Generating report file...', 'info');
  const res = await apiRequest(`/admin/export/quiz/${id}`, { method: 'POST' });
  if (res.success) {
    showToast(`Report saved: ${res.data.filename}`, 'success');
    alert(`Report generated on server: \n${res.data.filepath}`);
  } else {
    alert(res.message || 'Failed to export report');
  }
}

// ---------------- QUESTION MANAGEMENT ----------------

async function manageQuestions(quizId, quizTitle) {
  activeQuizIdForQuestions = quizId;
  document.getElementById('questionModalQuizTitle').innerText = `${quizTitle} (ID: ${quizId})`;
  await loadQuestionsForActiveQuiz();
  const modal = new bootstrap.Modal(document.getElementById('questionsManagerModal'));
  modal.show();
}

async function loadQuestionsForActiveQuiz() {
  const res = await apiRequest(`/quizzes/${activeQuizIdForQuestions}`);
  const container = document.getElementById('questions-list-container');
  const countBadge = document.getElementById('questions-count-badge');
  const addBtn = document.getElementById('btn-add-question-modal');
  if (!container) return;

  if (!res.success || !res.data) {
    container.innerHTML = '<div class="alert alert-danger">Error loading questions.</div>';
    return;
  }

  // Sync to local storage for instant student session consistency
  try {
    const saved = JSON.parse(localStorage.getItem('saved_quizzes') || '[]');
    const idx = saved.findIndex(s => s.id === res.data.id);
    if (idx !== -1) saved[idx] = res.data;
    else saved.push(res.data);
    localStorage.setItem('saved_quizzes', JSON.stringify(saved));
  } catch (e) {}

  const questions = res.data.questions || [];
  if (countBadge) {
    countBadge.innerText = `${questions.length} / 100 Questions`;
    if (questions.length >= 100) {
      countBadge.className = 'badge bg-danger px-2 py-1';
      if (addBtn) addBtn.disabled = true;
    } else {
      countBadge.className = 'badge bg-primary px-2 py-1';
      if (addBtn) addBtn.disabled = false;
    }
  }

  if (questions.length === 0) {
    container.innerHTML = '<div class="text-center py-4 text-muted">No questions yet for this quiz. Use "+ Add Question" or "Quick Scale Questions" above.</div>';
    return;
  }

  container.innerHTML = questions.map((q, idx) => `
    <div class="card mb-3 border">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-start mb-2">
          <h6 class="fw-bold mb-0">Q${idx + 1}. ${escapeHtml(q.questionText)}</h6>
          <div class="btn-group btn-group-sm">
            <button class="btn btn-outline-secondary btn-sm" onclick="openEditQuestionModal(${JSON.stringify(q).replace(/"/g, '&quot;')})">
              <i class="bi bi-pencil"></i>
            </button>
            <button class="btn btn-outline-danger btn-sm" onclick="deleteQuestion(${q.id})">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
        <div class="row g-2 small mt-2">
          <div class="col-6 p-2 border rounded ${q.correctAnswer === 'A' ? 'bg-success-subtle border-success fw-bold' : ''}">A) ${escapeHtml(q.optionA)}</div>
          <div class="col-6 p-2 border rounded ${q.correctAnswer === 'B' ? 'bg-success-subtle border-success fw-bold' : ''}">B) ${escapeHtml(q.optionB)}</div>
          <div class="col-6 p-2 border rounded ${q.correctAnswer === 'C' ? 'bg-success-subtle border-success fw-bold' : ''}">C) ${escapeHtml(q.optionC)}</div>
          <div class="col-6 p-2 border rounded ${q.correctAnswer === 'D' ? 'bg-success-subtle border-success fw-bold' : ''}">D) ${escapeHtml(q.optionD)}</div>
        </div>
        <div class="mt-2 text-muted small">
          <span class="badge bg-success">Correct: Option ${q.correctAnswer}</span>
        </div>
      </div>
    </div>
  `).join('');
}

async function quickScaleQuestions(targetCount) {
  if (!activeQuizIdForQuestions) return;
  targetCount = parseInt(targetCount);
  if (isNaN(targetCount) || targetCount < 1) targetCount = 10;
  if (targetCount > 100) targetCount = 100;

  showToast(`Scaling question bank to ${targetCount} questions...`, 'info');

  const res = await apiRequest(`/admin/quizzes/${activeQuizIdForQuestions}/generate-questions`, {
    method: 'POST',
    body: JSON.stringify({ count: targetCount })
  });

  if (res.success) {
    showToast(res.message || `Scaled to ${targetCount} questions!`, 'success');
    if (res.data && res.data.quiz) {
      try {
        const saved = JSON.parse(localStorage.getItem('saved_quizzes') || '[]');
        const idx = saved.findIndex(s => s.id === res.data.quiz.id);
        if (idx !== -1) saved[idx] = res.data.quiz;
        else saved.push(res.data.quiz);
        localStorage.setItem('saved_quizzes', JSON.stringify(saved));
      } catch (e) {}
    }
    await loadQuestionsForActiveQuiz();
    await loadQuizzesTable();
  } else {
    alert(res.message || 'Error scaling questions');
  }
}

function applyCustomScale() {
  const input = document.getElementById('quick-custom-scale-input');
  if (!input) return;
  const count = parseInt(input.value);
  if (!isNaN(count) && count >= 1 && count <= 100) {
    quickScaleQuestions(count);
    input.value = '';
  } else {
    alert('Please enter a valid question count between 1 and 100');
  }
}

function openAddQuestionModal() {
  document.getElementById('singleQuestionForm').reset();
  document.getElementById('question-id-field').value = '';
  document.getElementById('questionEditorModalTitle').innerText = 'Add New Question';
  const modal = new bootstrap.Modal(document.getElementById('questionEditorModal'));
  modal.show();
}

function openEditQuestionModal(q) {
  document.getElementById('question-id-field').value = q.id;
  document.getElementById('q-text-input').value = q.questionText;
  document.getElementById('q-optA-input').value = q.optionA;
  document.getElementById('q-optB-input').value = q.optionB;
  document.getElementById('q-optC-input').value = q.optionC;
  document.getElementById('q-optD-input').value = q.optionD;
  document.getElementById('q-correct-select').value = q.correctAnswer;
  document.getElementById('questionEditorModalTitle').innerText = 'Edit Question #' + q.id;

  const modal = new bootstrap.Modal(document.getElementById('questionEditorModal'));
  modal.show();
}

async function saveQuestion() {
  const id = document.getElementById('question-id-field').value;
  const questionText = document.getElementById('q-text-input').value.trim();
  const optionA = document.getElementById('q-optA-input').value.trim();
  const optionB = document.getElementById('q-optB-input').value.trim();
  const optionC = document.getElementById('q-optC-input').value.trim();
  const optionD = document.getElementById('q-optD-input').value.trim();
  const correctAnswer = document.getElementById('q-correct-select').value;

  if (!questionText || !optionA || !optionB || !optionC || !optionD) {
    alert('Please fill out question text and all four options');
    return;
  }

  let res;
  if (id) {
    // Update
    res = await apiRequest(`/admin/questions/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ questionText, optionA, optionB, optionC, optionD, correctAnswer })
    });
  } else {
    // Create
    res = await apiRequest('/admin/questions', {
      method: 'POST',
      body: JSON.stringify({ quizId: activeQuizIdForQuestions, questionText, optionA, optionB, optionC, optionD, correctAnswer })
    });
  }

  if (res.success) {
    showToast(id ? 'Question updated' : 'Question added', 'success');
    bootstrap.Modal.getInstance(document.getElementById('questionEditorModal')).hide();
    if (res.quiz || (res.data && res.data.quiz)) {
      const qz = res.quiz || res.data.quiz;
      try {
        const saved = JSON.parse(localStorage.getItem('saved_quizzes') || '[]');
        const idx = saved.findIndex(s => s.id === qz.id);
        if (idx !== -1) saved[idx] = qz;
        else saved.push(qz);
        localStorage.setItem('saved_quizzes', JSON.stringify(saved));
      } catch (e) {}
    }
    loadQuestionsForActiveQuiz();
    loadQuizzesTable();
  } else {
    alert(res.message || 'Error saving question');
  }
}

async function deleteQuestion(id) {
  if (!confirm('Are you sure you want to delete this question?')) return;
  const res = await apiRequest(`/admin/questions/${id}`, { method: 'DELETE' });
  if (res.success) {
    showToast('Question deleted', 'success');
    if (res.quiz || (res.data && res.data.quiz)) {
      const qz = res.quiz || res.data.quiz;
      try {
        const saved = JSON.parse(localStorage.getItem('saved_quizzes') || '[]');
        const idx = saved.findIndex(s => s.id === qz.id);
        if (idx !== -1) saved[idx] = qz;
        else saved.push(qz);
        localStorage.setItem('saved_quizzes', JSON.stringify(saved));
      } catch (e) {}
    }
    loadQuestionsForActiveQuiz();
    loadQuizzesTable();
  } else {
    alert(res.message || 'Error deleting question');
  }
}

// ---------------- CANDIDATES LIST & SEARCH ----------------
let cachedCandidatesList = [];

async function loadStudentsTable(keyword = '') {
  const url = keyword ? `/admin/students?q=${encodeURIComponent(keyword)}` : '/admin/students';
  let students = [];
  try {
    const res = await apiRequest(url);
    if (res && res.success && Array.isArray(res.data)) {
      students = res.data;
    }
  } catch (err) {
    console.warn('Failed to fetch students from API', err);
  }

  // Merge any locally registered candidates
  try {
    const localCandidates = JSON.parse(localStorage.getItem('registered_candidates') || '[]');
    if (Array.isArray(localCandidates) && localCandidates.length > 0) {
      const missingOnServer = [];
      localCandidates.forEach(lc => {
        if (!lc || !lc.email) return;
        const exists = students.some(s => s.email && s.email.toLowerCase() === lc.email.toLowerCase());
        if (!exists) {
          const formatted = {
            id: lc.id || (students.length + 1),
            name: lc.name,
            email: lc.email,
            phone: lc.phone || 'N/A',
            gender: lc.gender || 'N/A',
            dob: lc.dob || 'N/A',
            address: lc.address || 'N/A',
            city: lc.city || '',
            state: lc.state || '',
            institution: lc.institution || 'N/A',
            department: lc.department || lc.degree || 'N/A',
            rollNumber: lc.rollNumber || 'No Roll #',
            degree: lc.degree || 'N/A',
            yearOfStudy: lc.yearOfStudy || 'N/A',
            cgpa: lc.cgpa || 'N/A',
            bio: lc.bio || 'Registered candidate',
            loginCount: lc.loginCount || 1,
            lastLoginAt: lc.lastLoginAt || new Date().toISOString().replace('T', ' ').substring(0, 19),
            lastLoginIp: lc.lastLoginIp || '127.0.0.1',
            status: lc.status || 'ACTIVE',
            totalQuizzesAttempted: 0,
            averageScore: 0
          };
          students.push(formatted);
          missingOnServer.push(lc);
        }
      });
      if (missingOnServer.length > 0) {
        apiRequest('/admin/students/sync', {
          method: 'POST',
          body: JSON.stringify({ students: missingOnServer })
        }).catch(() => {});
      }
    }
  } catch (e) {}

  const tbody = document.getElementById('admin-students-tbody');
  if (!tbody) return;

  if (students.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">No candidates found matching criteria.</td></tr>';
    return;
  }

  cachedCandidatesList = students;

  tbody.innerHTML = res.data.map(s => `
    <tr>
      <td class="fw-bold">#${s.id}</td>
      <td>
        <div class="d-flex align-items-center gap-2">
          <div class="stat-icon bg-light text-primary" style="width: 32px; height: 32px; font-size: 0.9rem;">
            <i class="bi bi-person"></i>
          </div>
          <div>
            <div class="fw-bold text-dark">${escapeHtml(s.name)}</div>
            <span class="badge bg-light text-muted border small">${escapeHtml(s.rollNumber || 'No Roll #')}</span>
          </div>
        </div>
      </td>
      <td>
        <div class="small fw-semibold text-dark">${escapeHtml(s.email)}</div>
        <div class="small text-muted"><i class="bi bi-telephone me-1"></i> ${escapeHtml(s.phone || 'N/A')}</div>
      </td>
      <td>
        <div class="small fw-semibold text-truncate" style="max-width: 180px;" title="${escapeHtml(s.institution)}">${escapeHtml(s.institution || 'N/A')}</div>
        <div class="small text-muted text-truncate" style="max-width: 180px;">${escapeHtml(s.department || s.degree || '')}</div>
      </td>
      <td>
        <div class="small fw-semibold text-dark">${escapeHtml(s.yearOfStudy || 'N/A')}</div>
        <span class="badge bg-success-subtle text-success border border-success-subtle">${escapeHtml(s.cgpa ? 'CGPA ' + s.cgpa : 'N/A')}</span>
      </td>
      <td>
        <span class="badge bg-primary-subtle text-primary border border-primary-subtle mb-1 d-inline-block">
          <i class="bi bi-box-arrow-in-right me-1"></i> ${s.loginCount || 1} Logins
        </span>
        <div class="small text-muted" style="font-size: 0.75rem;">
          Last: ${s.lastLoginAt ? s.lastLoginAt.substring(0, 16) : 'Never'}
        </div>
      </td>
      <td>
        <span class="badge bg-light text-dark border me-1">${s.totalQuizzesAttempted || 0} Attempts</span>
        <span class="badge bg-primary">${s.averageScore || 0}% Avg</span>
      </td>
      <td class="text-end">
        <button class="btn btn-sm btn-outline-primary" onclick="viewCandidateProfile(${s.id})">
          <i class="bi bi-person-lines-fill me-1"></i> Full Profile
        </button>
      </td>
    </tr>
  `).join('');
}

async function viewCandidateProfile(candidateId) {
  const candidate = cachedCandidatesList.find(c => c.id === candidateId);
  if (!candidate) return;

  document.getElementById('cand-modal-name').innerText = candidate.name;
  document.getElementById('cand-modal-id').innerText = `Candidate ID: #${candidate.id}`;

  document.getElementById('cand-detail-name').innerText = candidate.name || '--';
  document.getElementById('cand-detail-email').innerText = candidate.email || '--';
  document.getElementById('cand-detail-phone').innerText = candidate.phone || 'Not Provided';
  document.getElementById('cand-detail-dob').innerText = candidate.dob || 'Not Provided';
  document.getElementById('cand-detail-gender').innerText = candidate.gender || 'Not Provided';
  document.getElementById('cand-detail-citystate').innerText = (candidate.city || '') + (candidate.state ? ', ' + candidate.state : '') || 'Not Provided';
  document.getElementById('cand-detail-address').innerText = candidate.address || 'Not Provided';

  document.getElementById('cand-detail-institution').innerText = candidate.institution || 'Not Provided';
  document.getElementById('cand-detail-department').innerText = candidate.department || 'Not Provided';
  document.getElementById('cand-detail-roll').innerText = candidate.rollNumber || 'Not Provided';
  document.getElementById('cand-detail-degree').innerText = candidate.degree || 'Not Provided';
  document.getElementById('cand-detail-year').innerText = candidate.yearOfStudy || 'Not Provided';
  document.getElementById('cand-detail-cgpa').innerText = candidate.cgpa ? candidate.cgpa : 'N/A';
  document.getElementById('cand-detail-bio').innerText = candidate.bio || 'No bio provided.';

  document.getElementById('cand-detail-logincount').innerText = candidate.loginCount || 1;
  document.getElementById('cand-detail-lastlogin').innerText = candidate.lastLoginAt || 'Active Now';
  document.getElementById('cand-detail-lastip').innerText = candidate.lastLoginIp || '127.0.0.1';
  document.getElementById('cand-detail-status').innerText = candidate.status || 'ACTIVE';

  // Fetch candidate login session trail
  const loginsTbody = document.getElementById('cand-logins-tbody');
  loginsTbody.innerHTML = '<tr><td colspan="5" class="text-center py-3 text-muted"><span class="spinner-border spinner-border-sm me-1"></span> Loading login audit trail from MySQL...</td></tr>';

  const modal = new bootstrap.Modal(document.getElementById('candidateDetailModal'));
  modal.show();

  try {
    const res = await apiRequest(`/admin/candidates/${candidateId}/logins`);
    if (res.success && res.data && res.data.length > 0) {
      document.getElementById('cand-logins-badge').innerText = `${res.data.length} Logins Recorded`;
      loginsTbody.innerHTML = res.data.map(h => `
        <tr>
          <td class="fw-bold">#${h.id}</td>
          <td><i class="bi bi-clock me-1 text-primary"></i> ${h.loginTime}</td>
          <td><span class="badge bg-light text-dark border font-monospace">${h.ipAddress || '127.0.0.1'}</span></td>
          <td class="small text-truncate" style="max-width: 280px;" title="${escapeHtml(h.userAgent)}">${escapeHtml(h.userAgent || 'Web Browser')}</td>
          <td><span class="badge bg-success-subtle text-success border border-success-subtle">${h.loginStatus}</span></td>
        </tr>
      `).join('');
    } else {
      document.getElementById('cand-logins-badge').innerText = '1 Login Recorded';
      loginsTbody.innerHTML = `
        <tr>
          <td class="fw-bold">#1</td>
          <td><i class="bi bi-clock me-1 text-primary"></i> ${candidate.lastLoginAt || 'Recent'}</td>
          <td><span class="badge bg-light text-dark border font-monospace">${candidate.lastLoginIp || '127.0.0.1'}</span></td>
          <td class="small">Active Web Session</td>
          <td><span class="badge bg-success-subtle text-success border border-success-subtle">SUCCESS</span></td>
        </tr>
      `;
    }
  } catch (err) {
    loginsTbody.innerHTML = '<tr><td colspan="5" class="text-center py-3 text-muted">Active session recorded.</td></tr>';
  }
}

function handleStudentSearch(input) {
  const val = input.value.trim();
  loadStudentsTable(val);
}

// ---------------- ATTEMPTS AUDIT ----------------

async function loadAttemptsTable() {
  const res = await apiRequest('/admin/attempts');
  const tbody = document.getElementById('admin-attempts-tbody');
  if (!tbody) return;

  if (!res.success || !res.data || res.data.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No quiz attempts recorded yet.</td></tr>';
    return;
  }

  tbody.innerHTML = res.data.map(a => {
    const isPass = a.percentage >= 50;
    const badgeClass = isPass ? 'bg-success' : 'bg-danger';
    return `
      <tr>
        <td class="fw-bold">#${a.id}</td>
        <td>${escapeHtml(a.studentName)} <span class="text-muted small">(${escapeHtml(a.studentEmail)})</span></td>
        <td>${escapeHtml(a.quizTitle)}</td>
        <td class="fw-bold">${a.score} / ${a.totalMarks || 50}</td>
        <td><span class="badge ${badgeClass}">${a.percentage.toFixed(1)}% (${isPass ? 'PASS' : 'FAIL'})</span></td>
        <td class="small text-muted">${a.attemptDate || 'N/A'}</td>
        <td>
          <a href="result-view.html?attemptId=${a.id}" class="btn btn-sm btn-outline-primary">
            <i class="bi bi-eye"></i> View
          </a>
        </td>
      </tr>
    `;
  }).join('');
}

// ---------------- SHARE & QR CODE FUNCTIONS ----------------
let currentShareQuizId = null;
let currentShareQuizTitle = '';

function openShareQuizModal(quizId, quizTitle, isJustCreated = false) {
  currentShareQuizId = quizId;
  currentShareQuizTitle = quizTitle;

  document.getElementById('shareQuizTitle').innerText = `${quizTitle} (Quiz #${quizId})`;

  const alertBadge = document.getElementById('quiz-created-badge');
  if (alertBadge) {
    if (isJustCreated) {
      alertBadge.classList.remove('d-none');
    } else {
      alertBadge.classList.add('d-none');
    }
  }

  // Generate URL with metadata so any external device scanning QR gets full details
  const origin = window.location.origin;
  const qObj = QuizStore.getLocalQuizzes().find(q => q.id === quizId) || {};
  const dur = qObj.duration || 10;
  const marks = qObj.totalMarks || 50;
  const qc = qObj.questionCount || (qObj.questions ? qObj.questions.length : 10);
  const quizUrl = `${origin}/quiz-view.html?id=${quizId}&title=${encodeURIComponent(quizTitle)}&dur=${dur}&marks=${marks}&qc=${qc}`;
  document.getElementById('share-quiz-url-input').value = quizUrl;
  document.getElementById('share-direct-open-btn').href = quizUrl;

  // Reset copy button text
  const copyBtnText = document.getElementById('copy-btn-text');
  const copyBtnIcon = document.getElementById('copy-btn-icon');
  if (copyBtnText) copyBtnText.innerText = 'Copy Link';
  if (copyBtnIcon) copyBtnIcon.className = 'bi bi-clipboard me-1';

  // Render QR Code
  const qrContainer = document.getElementById('qrcode-container');
  qrContainer.innerHTML = '';

  try {
    if (typeof QRCode !== 'undefined') {
      new QRCode(qrContainer, {
        text: quizUrl,
        width: 190,
        height: 190,
        colorDark: "#0f172a",
        colorLight: "#ffffff",
        correctLevel: QRCode.CorrectLevel.M
      });
    } else {
      // Fallback to QR API image
      const img = document.createElement('img');
      img.src = `https://api.qrserver.com/v1/create-qr-code/?size=190x190&data=${encodeURIComponent(quizUrl)}`;
      img.alt = `QR Code for ${quizTitle}`;
      img.className = 'img-fluid rounded';
      img.style.maxWidth = '190px';
      qrContainer.appendChild(img);
    }
  } catch (err) {
    console.warn('QR Code generation error, using fallback image:', err);
    qrContainer.innerHTML = `<img src="https://api.qrserver.com/v1/create-qr-code/?size=190x190&data=${encodeURIComponent(quizUrl)}" alt="QR Code" class="img-fluid rounded" style="max-width:190px;">`;
  }

  const modal = new bootstrap.Modal(document.getElementById('shareQuizModal'));
  modal.show();
}

function copyQuizLink() {
  const input = document.getElementById('share-quiz-url-input');
  if (!input) return;

  input.select();
  input.setSelectionRange(0, 99999);

  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(input.value).then(() => {
      onCopySuccess();
    }).catch(() => {
      document.execCommand('copy');
      onCopySuccess();
    });
  } else {
    document.execCommand('copy');
    onCopySuccess();
  }
}

function onCopySuccess() {
  const copyBtnText = document.getElementById('copy-btn-text');
  const copyBtnIcon = document.getElementById('copy-btn-icon');
  if (copyBtnText) copyBtnText.innerText = 'Copied! ✓';
  if (copyBtnIcon) copyBtnIcon.className = 'bi bi-check2 me-1 text-success';
  showToast('Quiz link copied to clipboard!', 'success');

  setTimeout(() => {
    if (copyBtnText) copyBtnText.innerText = 'Copy Link';
    if (copyBtnIcon) copyBtnIcon.className = 'bi bi-clipboard me-1';
  }, 3000);
}

function downloadQRCode() {
  const qrContainer = document.getElementById('qrcode-container');
  if (!qrContainer) return;

  const canvas = qrContainer.querySelector('canvas');
  if (canvas) {
    const link = document.createElement('a');
    link.download = `quiz_${currentShareQuizId}_qrcode.png`;
    link.href = canvas.toDataURL('image/png');
    link.click();
    showToast('QR Code downloaded', 'success');
    return;
  }

  const img = qrContainer.querySelector('img');
  if (img && img.src) {
    const link = document.createElement('a');
    link.download = `quiz_${currentShareQuizId}_qrcode.png`;
    link.href = img.src;
    link.target = '_blank';
    link.click();
    showToast('Opening QR Code image', 'info');
  }
}

function openQuestionsFromShare() {
  const shareModal = bootstrap.Modal.getInstance(document.getElementById('shareQuizModal'));
  if (shareModal) shareModal.hide();

  if (currentShareQuizId) {
    manageQuestions(currentShareQuizId, currentShareQuizTitle);
  }
}

