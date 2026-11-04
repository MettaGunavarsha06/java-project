package com.quiz.controller;

import com.quiz.annotations.AuditLog;
import com.quiz.annotations.Author;
import com.quiz.annotations.RoleRequired;
import com.quiz.exception.DatabaseException;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.generics.ApiResponse;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.QuizAttempt;
import com.quiz.model.Student;
import com.quiz.model.User;
import com.quiz.service.QuizService;
import com.quiz.service.ReportService;
import com.quiz.service.ResultService;
import com.quiz.service.UserService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller handling Administrator management operations:
 * - Authoring, updating, and deleting Quizzes (supporting up to 100 questions)
 * - Authoring, editing, deleting, and bulk generating Questions up to 100
 * - Reviewing student records and exam attempts
 * - Viewing analytics and exporting text reports
 */
@Author(name = "Antigravity Team", date = "2026-11-04", version = "1.1.0")
@RoleRequired("ADMIN")
public class AdminController {

    private final QuizService quizService;
    private final UserService userService;
    private final ResultService resultService;
    private final ReportService reportService;

    public AdminController(QuizService quizService, UserService userService, 
                           ResultService resultService, ReportService reportService) {
        this.quizService = quizService;
        this.userService = userService;
        this.resultService = resultService;
        this.reportService = reportService;
    }

    private boolean checkAdmin(User user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    @AuditLog(action = "ADMIN_CREATE_QUIZ", critical = true)
    public ApiResponse<?> createQuiz(User user, String title, String description, int duration, int totalMarks) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied: Administrator role required.");
        try {
            Quiz quiz = quizService.createQuiz(title, description, duration, totalMarks);
            return ApiResponse.success("Quiz created successfully", quiz);
        } catch (DatabaseException e) {
            return ApiResponse.error("Failed to create quiz: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Validation error: " + e.getMessage());
        }
    }

    @AuditLog(action = "ADMIN_UPDATE_QUIZ")
    public ApiResponse<?> updateQuiz(User user, int id, String title, String description, int duration, int totalMarks) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied: Administrator role required.");
        try {
            boolean updated = quizService.updateQuiz(id, title, description, duration, totalMarks);
            if (updated) {
                return ApiResponse.success("Quiz updated successfully", null);
            }
            return ApiResponse.error("Failed to update quiz ID: " + id);
        } catch (ResourceNotFoundException | DatabaseException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @AuditLog(action = "ADMIN_DELETE_QUIZ", critical = true)
    public ApiResponse<?> deleteQuiz(User user, int id) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied: Administrator role required.");
        try {
            boolean deleted = quizService.deleteQuiz(id);
            if (deleted) {
                return ApiResponse.success("Quiz deleted successfully", null);
            }
            return ApiResponse.error("Quiz not found or could not be deleted");
        } catch (DatabaseException e) {
            return ApiResponse.error("Failed to delete quiz: " + e.getMessage());
        }
    }

    // Question Management (Supports up to 100 questions)
    @AuditLog(action = "ADMIN_ADD_QUESTION")
    public ApiResponse<?> addQuestion(User user, int quizId, String text, String optA, String optB, String optC, String optD, String correct) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied: Administrator role required.");
        try {
            Question q = quizService.addQuestionToQuiz(quizId, text, optA, optB, optC, optD, correct);
            return ApiResponse.success("Question added successfully", q);
        } catch (ResourceNotFoundException | DatabaseException | IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @AuditLog(action = "ADMIN_BULK_GENERATE_QUESTIONS")
    public ApiResponse<?> generateBulkQuestions(User user, int quizId, int targetTotal) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied: Administrator role required.");
        try {
            int added = quizService.generateQuestionsUpTo(quizId, Math.min(100, Math.max(1, targetTotal)));
            Quiz updatedQuiz = quizService.getQuizWithQuestions(quizId);
            Map<String, Object> data = new HashMap<>();
            data.put("addedCount", added);
            data.put("totalQuestions", updatedQuiz.getQuestions().size());
            data.put("quizId", quizId);
            return ApiResponse.success("Questions bank scaled to " + updatedQuiz.getQuestions().size() + "/100 questions successfully!", data);
        } catch (ResourceNotFoundException | DatabaseException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @AuditLog(action = "ADMIN_UPDATE_QUESTION")
    public ApiResponse<?> updateQuestion(User user, int questionId, String text, String optA, String optB, String optC, String optD, String correct) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied: Administrator role required.");
        try {
            boolean updated = quizService.updateQuestion(questionId, text, optA, optB, optC, optD, correct);
            if (updated) {
                return ApiResponse.success("Question updated successfully", null);
            }
            return ApiResponse.error("Failed to update question");
        } catch (ResourceNotFoundException | DatabaseException | IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @AuditLog(action = "ADMIN_DELETE_QUESTION")
    public ApiResponse<?> deleteQuestion(User user, int questionId) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied: Administrator role required.");
        try {
            boolean deleted = quizService.deleteQuestion(questionId);
            if (deleted) {
                return ApiResponse.success("Question deleted successfully", null);
            }
            return ApiResponse.error("Failed to delete question");
        } catch (DatabaseException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // Students and Attempts
    public ApiResponse<List<Student>> getStudents(User user, String searchKeyword) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied");
        try {
            List<Student> students = (searchKeyword != null && !searchKeyword.trim().isEmpty())
                    ? userService.searchStudents(searchKeyword)
                    : userService.getAllStudents();
            return ApiResponse.success(students);
        } catch (DatabaseException e) {
            return ApiResponse.error("Failed to load students: " + e.getMessage());
        }
    }

    public ApiResponse<List<QuizAttempt>> getAllAttempts(User user) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied");
        try {
            List<QuizAttempt> attempts = resultService.getAllAttempts();
            return ApiResponse.success(attempts);
        } catch (DatabaseException e) {
            return ApiResponse.error("Failed to load attempts: " + e.getMessage());
        }
    }

    public ApiResponse<?> getStatistics(User user) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied");
        try {
            Map<String, Object> stats = resultService.getOverallStatistics();
            stats.put("totalQuizzes", quizService.getAllQuizzes().size());
            stats.put("totalStudents", userService.getAllStudents().size());
            return ApiResponse.success(stats);
        } catch (DatabaseException e) {
            return ApiResponse.error("Failed to load statistics: " + e.getMessage());
        }
    }

    @AuditLog(action = "ADMIN_EXPORT_REPORT")
    public ApiResponse<?> exportQuizReport(User user, int quizId) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied");
        try {
            File reportFile = reportService.exportQuizSpecification(quizId);
            Map<String, String> data = new HashMap<>();
            data.put("filename", reportFile.getName());
            data.put("filepath", reportFile.getAbsolutePath());
            return ApiResponse.success("Report successfully generated on disk", data);
        } catch (ResourceNotFoundException | DatabaseException | IOException e) {
            return ApiResponse.error("Failed to generate report: " + e.getMessage());
        }
    }

    public ApiResponse<?> listReports(User user) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied");
        List<String> reports = reportService.listGeneratedReports();
        return ApiResponse.success(reports);
    }

    public ApiResponse<?> getCandidateLogins(User user, int candidateId) {
        if (!checkAdmin(user)) return ApiResponse.error("Access Denied");
        try {
            return ApiResponse.success(userService.getCandidateLoginHistory(candidateId));
        } catch (DatabaseException e) {
            return ApiResponse.error("Failed to load candidate login history: " + e.getMessage());
        }
    }
}
