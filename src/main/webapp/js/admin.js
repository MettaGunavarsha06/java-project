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
  document.getElementById('stat-total-quizzes').innerText = stats.totalQuizzes || 0;
  document.getElementById('stat-total-students').innerText = stats.totalStudents || 0;
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
  const res = await apiRequest('/quizzes');
  const tbody = document.getElementById('admin-quizzes-tbody');
  if (!tbody) return;

  if (!res.success || !res.data || res.data.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No quizzes available. Click "Create Quiz" above.</td></tr>';
    return;
  }

  tbody.innerHTML = res.data.map(q => `
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
  const modal = new bootstrap.Modal(document.getElementById('quizModal'));
  modal.show();
}

async function openEditQuizModal(id) {
  const res = await apiRequest(`/quizzes/${id}`);
  if (!res.success || !res.data) {
    showToast('Failed to fetch quiz details', 'error');
    return;
  }

  const q = res.data;
  document.getElementById('quizModalTitle').innerText = 'Edit Quiz #' + q.id;
  document.getElementById('quiz-id-field').value = q.id;
  document.getElementById('quiz-title-input').value = q.title;
  document.getElementById('quiz-desc-input').value = q.description;
  document.getElementById('quiz-duration-input').value = q.duration;
  document.getElementById('quiz-marks-input').value = q.totalMarks;

  const modal = new bootstrap.Modal(document.getElementById('quizModal'));
  modal.show();
}

async function saveQuiz() {
  const id = document.getElementById('quiz-id-field').value;
  const title = document.getElementById('quiz-title-input').value.trim();
  const description = document.getElementById('quiz-desc-input').value.trim();
  const duration = parseInt(document.getElementById('quiz-duration-input').value);
  const totalMarks = parseInt(document.getElementById('quiz-marks-input').value);

  if (!title) {
    alert('Please enter a quiz title');
    return;
  }

  let res;
  if (id) {
    // Update
    res = await apiRequest(`/admin/quizzes/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ title, description, duration, totalMarks })
    });
  } else {
    // Create
    res = await apiRequest('/admin/quizzes', {
      method: 'POST',
      body: JSON.stringify({ title, description, duration, totalMarks })
    });
  }

  if (res.success) {
    showToast(id ? 'Quiz updated' : 'Quiz created', 'success');
    bootstrap.Modal.getInstance(document.getElementById('quizModal')).hide();
    loadQuizzesTable();
    loadDashboardStats();
  } else {
    alert(res.message || 'Error saving quiz');
  }
}

async function deleteQuiz(id) {
  if (!confirm(`Are you sure you want to delete Quiz #${id}? All associated questions and attempts will be removed.`)) return;

  const res = await apiRequest(`/admin/quizzes/${id}`, { method: 'DELETE' });
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
  if (!container) return;

  if (!res.success || !res.data) {
    container.innerHTML = '<div class="alert alert-danger">Error loading questions.</div>';
    return;
  }

  const questions = res.data.questions || [];
  if (questions.length === 0) {
    container.innerHTML = '<div class="text-center py-4 text-muted">No questions yet for this quiz. Click "+ Add Question" below.</div>';
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
    loadQuestionsForActiveQuiz();
    loadQuizzesTable();
  } else {
    alert(res.message || 'Error deleting question');
  }
}

// ---------------- STUDENTS LIST & SEARCH ----------------

async function loadStudentsTable(keyword = '') {
  const url = keyword ? `/admin/students?q=${encodeURIComponent(keyword)}` : '/admin/students';
  const res = await apiRequest(url);
  const tbody = document.getElementById('admin-students-tbody');
  if (!tbody) return;

  if (!res.success || !res.data || res.data.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4 text-muted">No students found.</td></tr>';
    return;
  }

  tbody.innerHTML = res.data.map(s => `
    <tr>
      <td class="fw-bold">#${s.id}</td>
      <td>
        <div class="fw-bold text-dark">${escapeHtml(s.name)}</div>
      </td>
      <td>${escapeHtml(s.email)}</td>
      <td><span class="badge bg-light text-dark border">${s.totalQuizzesAttempted || 0} Quizzes</span></td>
      <td><span class="badge bg-primary">${s.averageScore || 0}% Avg</span></td>
    </tr>
  `).join('');
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

