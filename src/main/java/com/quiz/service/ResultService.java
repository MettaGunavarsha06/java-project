package com.quiz.service;

import com.quiz.concurrency.AsyncResultProcessor;
import com.quiz.concurrency.ScoreCalculationTask;
import com.quiz.dao.QuestionDao;
import com.quiz.dao.QuizAttemptDao;
import com.quiz.dao.QuizDao;
import com.quiz.exception.DatabaseException;
import com.quiz.exception.QuizException;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.QuizAttempt;
import com.quiz.model.Result;
import com.quiz.model.StudentAnswer;
import com.quiz.model.User;
import com.quiz.threads.QuizTimerThread;
import com.quiz.threads.SynchronizedLeaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Service orchestrating score evaluation, multithreaded timers, result calculations,
 * and leaderboard synchronization.
 * Demonstrates:
 * - Multithreading & Thread Management (QuizTimerThread creation, start, sleep, interrupt)
 * - Concurrency Utilities (ExecutorService, Callable, Future)
 * - Synchronization (SynchronizedLeaderboard updates)
 * - Java Collections (ArrayList, HashMap)
 * - Stream API analytics (filtering, mapping, summaryStatistics)
 * - Sorting with custom Comparator and Lambda expressions
 */
public class ResultService {

    private final QuizDao quizDao;
    private final QuestionDao questionDao;
    private final QuizAttemptDao quizAttemptDao;
    private final AsyncResultProcessor asyncProcessor;
    private final SynchronizedLeaderboard leaderboard;

    // Active quiz timers mapped by session token: sessionToken -> QuizTimerThread
    private final Map<String, QuizTimerThread> activeTimers = new ConcurrentHashMap<>();
    // Cached completed results for fast review: attemptId -> Result
    private final Map<Integer, Result> completedResultsCache = new ConcurrentHashMap<>();

    public ResultService(QuizDao quizDao, QuestionDao questionDao, QuizAttemptDao quizAttemptDao) {
        this.quizDao = quizDao;
        this.questionDao = questionDao;
        this.quizAttemptDao = quizAttemptDao;
        this.asyncProcessor = AsyncResultProcessor.getInstance();
        this.leaderboard = SynchronizedLeaderboard.getInstance();

        // Initialize leaderboard with existing attempts
        try {
            this.leaderboard.reload(quizAttemptDao.findAll());
        } catch (DatabaseException e) {
            System.err.println("Could not pre-load leaderboard: " + e.getMessage());
        }
    }

    /**
     * Starts a timed quiz session for a student.
     * Demonstrates creating and starting a QuizTimerThread.
     */
    public QuizTimerThread startQuizSession(String sessionToken, int durationMinutes) {
        // Cancel existing timer if present
        cancelQuizSession(sessionToken);

        QuizTimerThread timer = new QuizTimerThread(sessionToken, durationMinutes, token -> {
            System.out.println("[ResultService] Quiz timer expired for token: " + token);
        });
        activeTimers.put(sessionToken, timer);
        timer.start();
        return timer;
    }

    public int getRemainingSeconds(String sessionToken, int defaultDurationMinutes) {
        QuizTimerThread timer = activeTimers.get(sessionToken);
        if (timer != null) {
            return timer.getRemainingSeconds();
        }
        return defaultDurationMinutes * 60;
    }

    public void cancelQuizSession(String sessionToken) {
        QuizTimerThread timer = activeTimers.remove(sessionToken);
        if (timer != null) {
            timer.cancelTimer();
        }
    }

    /**
     * Submits a quiz and calculates marks asynchronously using Callable and Future.
     */
    public Result submitQuiz(User student, int quizId, Map<Integer, String> studentAnswers, String sessionToken) 
            throws QuizException {
        // 1. Fetch Quiz and Questions
        Quiz quiz = quizDao.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));
        List<Question> questions = questionDao.findByQuizId(quizId);

        if (questions.isEmpty()) {
            throw new QuizException("Quiz contains no questions to evaluate.");
        }

        // 2. Measure elapsed time and stop timer thread
        int elapsedSeconds = 60; // default 1 min if untracked
        QuizTimerThread timer = activeTimers.remove(sessionToken);
        if (timer != null) {
            elapsedSeconds = timer.getElapsedSeconds();
            timer.cancelTimer();
        }

        // 3. Concurrency: Submit ScoreCalculationTask to Thread Pool via Callable
        ScoreCalculationTask calcTask = new ScoreCalculationTask(student, quiz, questions, studentAnswers, elapsedSeconds);
        Future<Result> futureResult = asyncProcessor.submitScoreCalculation(calcTask);

        Result result;
        try {
            result = futureResult.get(); // Await parallel calculation
        } catch (Exception e) {
            throw new QuizException("Failed to calculate quiz score: " + e.getMessage(), e);
        }

        // 4. Save attempt and individual answers to database transactionally
        QuizAttempt attempt = new QuizAttempt(
                student.getId(),
                quiz.getId(),
                result.getScoreObtained(),
                result.getPercentage()
        );
        attempt.setStudentName(student.getName());
        attempt.setStudentEmail(student.getEmail());
        attempt.setQuizTitle(quiz.getTitle());
        attempt.setTotalMarks(quiz.getTotalMarks());

        // Prepare student answers list
        List<StudentAnswer> answersList = new ArrayList<>();
        if (studentAnswers != null) {
            for (Map.Entry<Integer, String> entry : studentAnswers.entrySet()) {
                answersList.add(new StudentAnswer(0, entry.getKey(), entry.getValue()));
            }
        }

        attempt = quizAttemptDao.saveAttemptWithAnswers(attempt, answersList);
        result.setAttemptId(attempt.getId());

        // 5. Update Synchronized Leaderboard
        leaderboard.recordAttempt(attempt);

        // 6. Cache result for instant UI review
        completedResultsCache.put(attempt.getId(), result);

        // 7. Background asynchronous processing: Object Serialization and File Report writing
        asyncProcessor.processResultPostSubmission(result);

        return result;
    }

    public Result getResultDetails(int attemptId) throws DatabaseException, ResourceNotFoundException {
        Result cached = completedResultsCache.get(attemptId);
        if (cached != null) {
            return cached;
        }

        // Reconstruct from database
        QuizAttempt attempt = quizAttemptDao.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt record not found: " + attemptId));

        Quiz quiz = quizDao.findById(attempt.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz record not found: " + attempt.getQuizId()));
        List<Question> questions = questionDao.findByQuizId(quiz.getId());
        List<StudentAnswer> answers = quizAttemptDao.findAnswersByAttemptId(attemptId);

        Map<Integer, String> ansMap = new HashMap<>();
        for (StudentAnswer sa : answers) {
            ansMap.put(sa.getQuestionId(), sa.getSelectedAnswer());
        }

        int correctCount = 0;
        int wrongCount = 0;
        int unansweredCount = 0;

        Result result = new Result();
        result.setAttemptId(attempt.getId());
        result.setStudentId(attempt.getStudentId());
        result.setStudentName(attempt.getStudentName());
        result.setStudentEmail(attempt.getStudentEmail());
        result.setQuizId(attempt.getQuizId());
        result.setQuizTitle(attempt.getQuizTitle());
        result.setTotalQuestions(questions.size());
        result.setTotalMarks(attempt.getTotalMarks());
        result.setScoreObtained(attempt.getScore());
        result.setPercentage(attempt.getPercentage());
        result.setPassed(attempt.getPercentage() >= 50.0);
        result.setAttemptDate(attempt.getAttemptDate());

        for (Question q : questions) {
            String selected = ansMap.get(q.getId());
            boolean isCorrect = false;
            if (selected == null || selected.trim().isEmpty()) {
                unansweredCount++;
            } else if (q.isCorrect(selected)) {
                correctCount++;
                isCorrect = true;
            } else {
                wrongCount++;
            }

            result.addReview(new Result.QuestionReview(
                    q.getId(),
                    q.getQuestionText(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD(),
                    selected,
                    q.getCorrectAnswer(),
                    isCorrect
            ));
        }

        result.setCorrectAnswers(correctCount);
        result.setWrongAnswers(wrongCount);
        result.setUnansweredQuestions(unansweredCount);

        completedResultsCache.put(attemptId, result);
        return result;
    }

    public List<QuizAttempt> getStudentAttempts(int studentId) throws DatabaseException {
        return quizAttemptDao.findByStudentId(studentId);
    }

    public List<QuizAttempt> getAllAttempts() throws DatabaseException {
        return quizAttemptDao.findAll();
    }

    public List<QuizAttempt> getLeaderboard(int limit) {
        return leaderboard.getTopPerformers(limit);
    }

    public List<QuizAttempt> getQuizLeaderboard(int quizId, int limit) {
        return leaderboard.getLeaderboardByQuiz(quizId, limit);
    }

    /**
     * Calculates comprehensive platform performance statistics using Java Stream API.
     * Demonstrates:
     * - Stream API: mapToDouble, summaryStatistics, count, filter, average
     */
    public Map<String, Object> getOverallStatistics() throws DatabaseException {
        List<QuizAttempt> attempts = quizAttemptDao.findAll();
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalAttempts", attempts.size());

        if (attempts.isEmpty()) {
            stats.put("averageScore", 0.0);
            stats.put("highestScore", 0.0);
            stats.put("passRate", 0.0);
            stats.put("passedCount", 0);
            stats.put("failedCount", 0);
            return stats;
        }

        DoubleSummaryStatistics scoreStats = attempts.stream()
                .mapToDouble(QuizAttempt::getPercentage)
                .summaryStatistics();

        long passedCount = attempts.stream()
                .filter(a -> a.getPercentage() >= 50.0)
                .count();

        double passRate = ((double) passedCount / attempts.size()) * 100.0;

        stats.put("averageScore", Math.round(scoreStats.getAverage() * 100.0) / 100.0);
        stats.put("highestScore", Math.round(scoreStats.getMax() * 100.0) / 100.0);
        stats.put("lowestScore", Math.round(scoreStats.getMin() * 100.0) / 100.0);
        stats.put("passedCount", (int) passedCount);
        stats.put("failedCount", (int) (attempts.size() - passedCount));
        stats.put("passRate", Math.round(passRate * 100.0) / 100.0);

        return stats;
    }
}

