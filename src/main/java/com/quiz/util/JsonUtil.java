package com.quiz.util;

import com.quiz.generics.ApiResponse;
import com.quiz.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Lightweight, zero-dependency JSON utility for generating standard JSON responses.
 * Ensures the application runs directly on pure JDK without requiring external JARs.
 */
public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Collection) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Collection<?>) obj) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof ApiResponse) {
            ApiResponse<?> r = (ApiResponse<?>) obj;
            return String.format("{\"success\":%b,\"message\":\"%s\",\"data\":%s,\"timestamp\":%d}",
                    r.isSuccess(), escapeJson(r.getMessage()), toJson(r.getData()), r.getTimestamp());
        }
        if (obj instanceof User) {
            User u = (User) obj;
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"id\":").append(u.getId()).append(",");
            sb.append("\"name\":\"").append(escapeJson(u.getName())).append("\",");
            sb.append("\"email\":\"").append(escapeJson(u.getEmail())).append("\",");
            sb.append("\"role\":\"").append(escapeJson(u.getRole())).append("\",");
            sb.append("\"dashboardPath\":\"").append(escapeJson(u.getDashboardPath())).append("\",");
            sb.append("\"roleDescription\":\"").append(escapeJson(u.getRoleDescription())).append("\"");
            if (u instanceof Student) {
                Student s = (Student) u;
                sb.append(",\"totalQuizzesAttempted\":").append(s.getTotalQuizzesAttempted());
                sb.append(",\"averageScore\":").append(s.getAverageScore());
            } else if (u instanceof Admin) {
                Admin a = (Admin) u;
                sb.append(",\"department\":\"").append(escapeJson(a.getDepartment())).append("\"");
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Quiz) {
            Quiz q = (Quiz) obj;
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"id\":").append(q.getId()).append(",");
            sb.append("\"title\":\"").append(escapeJson(q.getTitle())).append("\",");
            sb.append("\"description\":\"").append(escapeJson(q.getDescription())).append("\",");
            sb.append("\"duration\":").append(q.getDuration()).append(",");
            sb.append("\"totalMarks\":").append(q.getTotalMarks()).append(",");
            sb.append("\"questionCount\":").append(q.getQuestionCount()).append(",");
            sb.append("\"questions\":").append(toJson(q.getQuestions()));
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Question) {
            Question q = (Question) obj;
            return String.format("{\"id\":%d,\"quizId\":%d,\"questionText\":\"%s\",\"optionA\":\"%s\",\"optionB\":\"%s\",\"optionC\":\"%s\",\"optionD\":\"%s\",\"correctAnswer\":\"%s\"}",
                    q.getId(), q.getQuizId(), escapeJson(q.getQuestionText()),
                    escapeJson(q.getOptionA()), escapeJson(q.getOptionB()),
                    escapeJson(q.getOptionC()), escapeJson(q.getOptionD()),
                    escapeJson(q.getCorrectAnswer()));
        }
        if (obj instanceof QuizAttempt) {
            QuizAttempt qa = (QuizAttempt) obj;
            return String.format("{\"id\":%d,\"studentId\":%d,\"studentName\":\"%s\",\"studentEmail\":\"%s\",\"quizId\":%d,\"quizTitle\":\"%s\",\"score\":%d,\"percentage\":%.2f,\"attemptDate\":\"%s\",\"totalMarks\":%d}",
                    qa.getId(), qa.getStudentId(), escapeJson(qa.getStudentName() != null ? qa.getStudentName() : ""),
                    escapeJson(qa.getStudentEmail() != null ? qa.getStudentEmail() : ""),
                    qa.getQuizId(), escapeJson(qa.getQuizTitle() != null ? qa.getQuizTitle() : ""),
                    qa.getScore(), qa.getPercentage(),
                    qa.getAttemptDate() != null ? qa.getAttemptDate().toString() : "",
                    qa.getTotalMarks());
        }
        if (obj instanceof Result) {
            Result r = (Result) obj;
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"attemptId\":").append(r.getAttemptId()).append(",");
            sb.append("\"studentId\":").append(r.getStudentId()).append(",");
            sb.append("\"studentName\":\"").append(escapeJson(r.getStudentName())).append("\",");
            sb.append("\"quizId\":").append(r.getQuizId()).append(",");
            sb.append("\"quizTitle\":\"").append(escapeJson(r.getQuizTitle())).append("\",");
            sb.append("\"totalQuestions\":").append(r.getTotalQuestions()).append(",");
            sb.append("\"correctAnswers\":").append(r.getCorrectAnswers()).append(",");
            sb.append("\"wrongAnswers\":").append(r.getWrongAnswers()).append(",");
            sb.append("\"unansweredQuestions\":").append(r.getUnansweredQuestions()).append(",");
            sb.append("\"totalMarks\":").append(r.getTotalMarks()).append(",");
            sb.append("\"scoreObtained\":").append(r.getScoreObtained()).append(",");
            sb.append("\"percentage\":").append(r.getPercentage()).append(",");
            sb.append("\"passed\":").append(r.isPassed()).append(",");
            sb.append("\"timeTakenSeconds\":").append(r.getTimeTakenSeconds()).append(",");
            sb.append("\"reviews\":[");
            boolean first = true;
            for (Result.QuestionReview qr : r.getReviews()) {
                if (!first) sb.append(",");
                sb.append(String.format("{\"questionId\":%d,\"questionText\":\"%s\",\"optionA\":\"%s\",\"optionB\":\"%s\",\"optionC\":\"%s\",\"optionD\":\"%s\",\"selectedAnswer\":\"%s\",\"correctAnswer\":\"%s\",\"correct\":%b}",
                        qr.getQuestionId(), escapeJson(qr.getQuestionText()),
                        escapeJson(qr.getOptionA()), escapeJson(qr.getOptionB()),
                        escapeJson(qr.getOptionC()), escapeJson(qr.getOptionD()),
                        escapeJson(qr.getSelectedAnswer() != null ? qr.getSelectedAnswer() : ""),
                        escapeJson(qr.getCorrectAnswer()), qr.isCorrect()));
                first = false;
            }
            sb.append("]}");
            return sb.toString();
        }

        return "\"" + escapeJson(obj.toString()) + "\"";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

