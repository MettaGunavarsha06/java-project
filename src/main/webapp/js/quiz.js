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
    window.location.href = 'login.html';
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
  // 1. Start quiz on backend to trigger server-side timer thread
  const startRes = await apiRequest(`/student/quiz/${quizId}/start`, { method: 'POST' });
  if (!startRes.success) {
    alert(startRes.message || 'Unable to start quiz');
    window.location.href = 'student-dashboard.html';
    return;
  }

  currentQuiz = startRes.data.quiz;
  currentQuestions = currentQuiz.questions || [];
  remainingSeconds = startRes.data.durationSeconds || currentQuiz.duration * 60;

  if (currentQuestions.length === 0) {
    alert('This quiz currently has no questions.');
    window.location.href = 'student-dashboard.html';
    return;
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

  const mins = Math.floor(remainingSeconds / 60);
  const secs = remainingSeconds % 60;
  const formatted = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  timerBadge.innerHTML = `<i class="bi bi-clock me-1"></i> ${formatted}`;

  // If under 1 minute, highlight red
  if (remainingSeconds <= 60) {
    timerBadge.classList.add('warning');
  } else {
    timerBadge.classList.remove('warning');
  }
}

function renderQuestionPalette() {
  const paletteContainer = document.getElementById('question-palette');
  if (!paletteContainer) return;

  paletteContainer.innerHTML = '';
  currentQuestions.forEach((q, idx) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'palette-btn unanswered';
    btn.id = `palette-btn-${idx}`;
    btn.innerText = idx + 1;
    btn.onclick = () => showQuestion(idx);
    paletteContainer.appendChild(btn);
  });
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
}

function handleOptionSelect(questionId, optionKey) {
  answersMap[questionId] = optionKey;
  updatePaletteHighlight();
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
    showToast('Quiz evaluated successfully!', 'success');
    setTimeout(() => {
      window.location.href = `result-view.html?attemptId=${res.data.attemptId}`;
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

