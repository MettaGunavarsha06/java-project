/**
 * Interactive Quiz Session Engine
 * Handles countdown timer, MCQ question rendering, answer state tracking,
 * palette navigation, and auto-submission upon timeout.
 */

let currentQuiz = null;
let currentQuestions = [];
let currentIndex = 0;
let answersMap = {}; // questionId -> "A" | "B" | "C" | "D"
let timerInterval = null;
let remainingSeconds = 0;
let isSubmitting = false;

document.addEventListener('DOMContentLoaded', async () => {
  // Check student authentication
  if (!Auth.isLoggedIn()) {
    const currentFull = window.location.pathname.split('/').pop() + window.location.search;
    window.location.href = `login.html?redirect=${encodeURIComponent(currentFull)}`;
    return;
  }

  const urlParams = new URLSearchParams(window.location.search);
  const quizId = urlParams.get('id');

  if (!quizId) {
    alert('No quiz selected');
    window.location.href = 'student-dashboard.html';
    return;
  }

  await initializeQuiz(quizId);
});

async function initializeQuiz(quizId) {
  // 1. Try to start quiz on backend to trigger server-side timer thread
  let startRes = await apiRequest(`/student/quiz/${quizId}/start`, { method: 'POST' });

  // Fallback: Check local storage for quiz if backend doesn't have it (e.g., custom admin quiz on serverless cold restart)
  if (!startRes || !startRes.success || !startRes.data) {
    const cachedList = JSON.parse(localStorage.getItem('saved_quizzes') || '[]');
    const localQuiz = cachedList.find(q => q.id === parseInt(quizId));
    if (localQuiz) {
      startRes = {
        success: true,
        data: {
          quiz: localQuiz,
          durationSeconds: (localQuiz.duration || 10) * 60,
          sessionId: 'sess_local_' + Date.now()
        }
      };
    }
  }

  if (!startRes || !startRes.success || !startRes.data) {
    alert(startRes && startRes.message ? startRes.message : 'Exam not found or unavailable. Please contact administrator.');
    window.location.href = 'student-dashboard.html';
    return;
  }

  currentQuiz = startRes.data.quiz;
  currentQuestions = currentQuiz.questions || [];
  remainingSeconds = startRes.data.durationSeconds || currentQuiz.duration * 60;

  // Ensure at least sample questions exist if empty
  if (currentQuestions.length === 0) {
    currentQuestions = [
      { id: 1, quizId: currentQuiz.id, questionText: "Which collection class guarantees unique elements in Java?", optionA: "ArrayList", optionB: "HashSet", optionC: "LinkedList", optionD: "Vector", correctAnswer: "B" },
      { id: 2, quizId: currentQuiz.id, questionText: "What keyword is used to prevent method overriding in Java?", optionA: "static", optionB: "final", optionC: "const", optionD: "abstract", correctAnswer: "B" },
      { id: 3, quizId: currentQuiz.id, questionText: "Which interface in java.util.concurrent returns a Future value?", optionA: "Runnable", optionB: "Callable", optionC: "Thread", optionD: "Process", correctAnswer: "B" },
      { id: 4, quizId: currentQuiz.id, questionText: "Which SQL clause filters rows before aggregation with GROUP BY?", optionA: "HAVING", optionB: "WHERE", optionC: "ORDER BY", optionD: "FILTER", correctAnswer: "B" },
      { id: 5, quizId: currentQuiz.id, questionText: "What is the default isolation level in MySQL InnoDB?", optionA: "READ UNCOMMITTED", optionB: "REPEATABLE READ", optionC: "SERIALIZABLE", optionD: "READ COMMITTED", correctAnswer: "B" }
    ];
    currentQuiz.questions = currentQuestions;
  }

  // Populate Quiz Header
  document.getElementById('quiz-title').innerText = currentQuiz.title;
  document.getElementById('quiz-total-marks').innerText = currentQuiz.totalMarks + ' Marks';
  document.getElementById('total-questions-count').innerText = currentQuestions.length;

  // Build question palette
  renderQuestionPalette();

  // Show first question
  showQuestion(0);

  // Start timer countdown
  startTimer();
}

function startTimer() {
  updateTimerDisplay();

  timerInterval = setInterval(() => {
    remainingSeconds--;
    updateTimerDisplay();

    if (remainingSeconds <= 0) {
      clearInterval(timerInterval);
      showToast('Time is up! Submitting your quiz automatically...', 'error');
      autoSubmitQuiz();
    }
  }, 1000);
}

function updateTimerDisplay() {
  const timerBadge = document.getElementById('quiz-timer');
  if (!timerBadge) return;

  const hrs = Math.floor(remainingSeconds / 3600);
  const mins = Math.floor((remainingSeconds % 3600) / 60);
  const secs = remainingSeconds % 60;
  const formatted = hrs > 0 
      ? `${String(hrs).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
      : `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  timerBadge.innerHTML = `<i class="bi bi-clock me-1"></i> ${formatted}`;

  // If under 1 minute, highlight red
  if (remainingSeconds <= 60) {
    timerBadge.classList.add('warning');
  } else {
    timerBadge.classList.remove('warning');
  }
}

let activePaletteFilter = 'all';

function renderQuestionPalette() {
  const paletteContainer = document.getElementById('question-palette');
  const statsBadge = document.getElementById('palette-stats-badge');
  if (!paletteContainer) return;

  if (statsBadge) {
    statsBadge.innerText = `${currentQuestions.length} Questions`;
  }

  paletteContainer.innerHTML = '';
  currentQuestions.forEach((q, idx) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'palette-btn unanswered';
    btn.id = `palette-btn-${idx}`;
    btn.innerText = idx + 1;
    btn.title = `Question ${idx + 1}`;
    btn.onclick = () => showQuestion(idx);
    paletteContainer.appendChild(btn);
  });

  applyPaletteFilter();
}

function filterPaletteView(type) {
  activePaletteFilter = type;
  ['all', 'answered', 'unanswered'].forEach(t => {
    const b = document.getElementById(`filter-btn-${t}`);
    if (b) {
      if (t === type) {
        b.className = t === 'answered' ? 'btn btn-xs btn-success flex-fill py-1 small' : (t === 'unanswered' ? 'btn btn-xs btn-secondary flex-fill py-1 small' : 'btn btn-xs btn-primary flex-fill py-1 small');
      } else {
        b.className = t === 'answered' ? 'btn btn-xs btn-outline-success flex-fill py-1 small' : (t === 'unanswered' ? 'btn btn-xs btn-outline-secondary flex-fill py-1 small' : 'btn btn-xs btn-outline-primary flex-fill py-1 small');
      }
    }
  });
  applyPaletteFilter();
}

function applyPaletteFilter() {
  currentQuestions.forEach((q, idx) => {
    const btn = document.getElementById(`palette-btn-${idx}`);
    if (!btn) return;
    const isAns = !!answersMap[q.id];
    if (activePaletteFilter === 'all') {
      btn.style.display = 'inline-flex';
    } else if (activePaletteFilter === 'answered') {
      btn.style.display = isAns ? 'inline-flex' : 'none';
    } else if (activePaletteFilter === 'unanswered') {
      btn.style.display = !isAns ? 'inline-flex' : 'none';
    }
  });
}

function jumpToQuestionInput() {
  const input = document.getElementById('jump-question-input');
  if (!input) return;
  const num = parseInt(input.value);
  if (!isNaN(num) && num >= 1 && num <= currentQuestions.length) {
    showQuestion(num - 1);
    input.value = '';
  } else {
    showToast(`Enter a number between 1 and ${currentQuestions.length}`, 'error');
  }
}

function showQuestion(index) {
  if (index < 0 || index >= currentQuestions.length) return;
  currentIndex = index;

  const q = currentQuestions[currentIndex];
  document.getElementById('current-question-num').innerText = currentIndex + 1;
  document.getElementById('question-text').innerText = q.questionText;

  // Render options A, B, C, D
  const options = [
    { key: 'A', text: q.optionA },
    { key: 'B', text: q.optionB },
    { key: 'C', text: q.optionC },
    { key: 'D', text: q.optionD }
  ];

  const optionsContainer = document.getElementById('options-container');
  optionsContainer.innerHTML = '';

  const currentSelection = answersMap[q.id];

  options.forEach(opt => {
    const label = document.createElement('label');
    label.className = 'option-label';

    const isChecked = currentSelection === opt.key ? 'checked' : '';
    label.innerHTML = `
      <input type="radio" name="question_opt_${q.id}" value="${opt.key}" ${isChecked} onchange="handleOptionSelect(${q.id}, '${opt.key}')">
      <div class="option-box">
        <div class="option-key">${opt.key}</div>
        <div class="option-text">${escapeHtml(opt.text)}</div>
      </div>
    `;
    optionsContainer.appendChild(label);
  });

  // Update navigation buttons state
  document.getElementById('btn-prev').disabled = currentIndex === 0;
  const nextBtn = document.getElementById('btn-next');
  if (currentIndex === currentQuestions.length - 1) {
    nextBtn.classList.add('d-none');
    document.getElementById('btn-review-submit').classList.remove('d-none');
  } else {
    nextBtn.classList.remove('d-none');
    document.getElementById('btn-review-submit').classList.add('d-none');
  }

  updatePaletteHighlight();
  applyPaletteFilter();
  const activeBtn = document.getElementById(`palette-btn-${currentIndex}`);
  if (activeBtn) {
    activeBtn.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'nearest' });
  }
}

function handleOptionSelect(questionId, optionKey) {
  answersMap[questionId] = optionKey;
  updatePaletteHighlight();
  applyPaletteFilter();
  updateSummaryBadge();
}

function clearCurrentAnswer() {
  const q = currentQuestions[currentIndex];
  delete answersMap[q.id];
  showQuestion(currentIndex);
  updateSummaryBadge();
}

function nextQuestion() {
  if (currentIndex < currentQuestions.length - 1) {
    showQuestion(currentIndex + 1);
  }
}

function prevQuestion() {
  if (currentIndex > 0) {
    showQuestion(currentIndex - 1);
  }
}

function updatePaletteHighlight() {
  currentQuestions.forEach((q, idx) => {
    const btn = document.getElementById(`palette-btn-${idx}`);
    if (!btn) return;

    btn.classList.remove('active', 'answered', 'unanswered');

    if (answersMap[q.id]) {
      btn.classList.add('answered');
    } else {
      btn.classList.add('unanswered');
    }

    if (idx === currentIndex) {
      btn.classList.add('active');
    }
  });
}

function updateSummaryBadge() {
  const answeredCount = Object.keys(answersMap).length;
  const badge = document.getElementById('answered-count-badge');
  if (badge) {
    badge.innerText = `${answeredCount} / ${currentQuestions.length} Answered`;
  }
}

function openSubmitModal() {
  const total = currentQuestions.length;
  const answered = Object.keys(answersMap).length;
  const unanswered = total - answered;

  document.getElementById('modal-total-q').innerText = total;
  document.getElementById('modal-answered-q').innerText = answered;
  document.getElementById('modal-unanswered-q').innerText = unanswered;

  const modal = new bootstrap.Modal(document.getElementById('confirmSubmitModal'));
  modal.show();
}

async function confirmSubmit() {
  if (isSubmitting) return;
  isSubmitting = true;
  clearInterval(timerInterval);

  // Close modal if open
  const modalEl = document.getElementById('confirmSubmitModal');
  const modal = bootstrap.Modal.getInstance(modalEl);
  if (modal) modal.hide();

  showToast('Evaluating your answers...', 'info');

  const res = await apiRequest(`/student/quiz/${currentQuiz.id}/submit`, {
    method: 'POST',
    body: JSON.stringify({ answers: answersMap })
  });

  if (res.success && res.data) {
    const attemptId = res.data.attemptId || res.data.id || Date.now();
    try {
      localStorage.setItem(`result_${attemptId}`, JSON.stringify(res.data));
    } catch (e) {}

    showToast('Quiz evaluated successfully!', 'success');
    setTimeout(() => {
      window.location.href = `result-view.html?attemptId=${attemptId}`;
    }, 400);
  } else {
    alert(res.message || 'Error submitting quiz');
    isSubmitting = false;
  }
}

function autoSubmitQuiz() {
  if (!isSubmitting) {
    confirmSubmit();
  }
}

