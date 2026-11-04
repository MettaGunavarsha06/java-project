package com.quiz.controller;

import com.quiz.annotations.AuditLog;
import com.quiz.annotations.Author;
import com.quiz.annotations.RoleRequired;
import com.quiz.exception.DatabaseException;
import com.quiz.exception.QuizException;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.generics.ApiResponse;
import com.quiz.model.Quiz;
import com.quiz.model.QuizAttempt;
import com.quiz.model.Result;
import com.quiz.model.User;
import com.quiz.service.QuizService;
import com.quiz.service.ResultService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller handling Student operations:
 * - Starting quiz & initiating countdown timer thread
 * - Querying live timer state
 * - Submitting quiz answers & async evaluation
 * - Viewing attempt histories and result analysis
 */
@Author(name = "Antigravity Team", date = "2026-11-03", version = "1.0.0")
@RoleRequired("STUDENT")
public class StudentController {

    private final QuizService quizService;
    private final ResultService resultService;

    public StudentController(QuizService quizService, ResultService resultService) {
        this.quizService = quizService;
        this.resultService = resultService;
    }

    @AuditLog(action = "START_QUIZ")
    public ApiResponse<?> startQuiz(User student, int quizId, String sessionToken) {
        if (student == null) {
            return ApiResponse.error("Authentication required.");
        }
        try {
            Quiz quiz = quizService.getQuizWithQuestions(quizId);
            resultService.startQuizSession(sessionToken, quiz.getDuration());

            Map<String, Object> data = new HashMap<>();
            data.put("quiz", quiz);
            data.put("durationSeconds", quiz.getDuration() * 60);

            return ApiResponse.success("Quiz started. Timer is running.", data);
        } catch (ResourceNotFoundException e) {
            return ApiResponse.error(e.getMessage());
        } catch (DatabaseException e) {
            return ApiResponse.error("Failed to start quiz: " + e.getMessage());
        }
    }

    public ApiResponse<?> getRemainingTimer(String sessionToken, int defaultDuration) {
        int remaining = resultService.getRemainingSeconds(sessionToken, defaultDuration);
        Map<String, Object> data = new HashMap<>();
        data.put("remainingSeconds", remaining);
        data.put("isExpired", remaining <= 0);
        return ApiResponse.success(data);
    }

    @AuditLog(action = "SUBMIT_QUIZ", critical = true)
    public ApiResponse<?> submitQuiz(User student, int quizId, Map<Integer, String> answers, String sessionToken) {
        if (student == null) {
            return ApiResponse.error("Authentication required.");
        }
        try {
            Result result = resultService.submitQuiz(student, quizId, answers, sessionToken);
            return ApiResponse.success("Quiz submitted successfully! Here are your results.", result);
        } catch (QuizException e) {
            return ApiResponse.error("Quiz submission error: " + e.getMessage());
        }
    }

    public ApiResponse<?> getMyAttempts(User student) {
        if (student == null) {
            return ApiResponse.error("Authentication required.");
        }
        try {
            List<QuizAttempt> attempts = resultService.getStudentAttempts(student.getId());
            return ApiResponse.success(attempts);
        } catch (DatabaseException e) {
            return ApiResponse.error("Failed to fetch student attempts: " + e.getMessage());
        }
    }

    public ApiResponse<?> getResultDetails(int attemptId) {
        try {
            Result result = resultService.getResultDetails(attemptId);
            return ApiResponse.success(result);
        } catch (ResourceNotFoundException e) {
            return ApiResponse.error(e.getMessage());
        } catch (DatabaseException e) {
            return ApiResponse.error("Failed to load result: " + e.getMessage());
        }
    }

    public ApiResponse<?> getLeaderboard(int limit) {
        List<QuizAttempt> list = resultService.getLeaderboard(limit > 0 ? limit : 20);
        return ApiResponse.success(list);
    }

    public ApiResponse<?> getQuizLeaderboard(int quizId, int limit) {
        List<QuizAttempt> list = resultService.getQuizLeaderboard(quizId, limit > 0 ? limit : 20);
        return ApiResponse.success(list);
    }
}

