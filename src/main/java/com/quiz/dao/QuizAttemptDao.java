package com.quiz.dao;

import com.quiz.annotations.AuditLog;
import com.quiz.annotations.Author;
import com.quiz.exception.DatabaseException;
import com.quiz.model.QuizAttempt;
import com.quiz.model.StudentAnswer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Data Access Object for Quiz Attempts and Answers.
 * Demonstrates:
 * - JDBC Transactions: setAutoCommit(false), commit(), rollback()
 * - Checked Exception handling (SQLException)
 * - Joining attempts with student and quiz records
 * - Thread-safe fallback store
 */
@Author(name = "Antigravity Team", date = "2026-11-03", version = "1.0.0")
public class QuizAttemptDao implements GenericDao<QuizAttempt, Integer> {

    private static final Map<Integer, QuizAttempt> attemptCache = new ConcurrentHashMap<>();
    private static final Map<Integer, List<StudentAnswer>> answersCache = new ConcurrentHashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(20);

    static {
        seedSampleAttempts();
    }

    private static void seedSampleAttempts() {
        long now = System.currentTimeMillis();
        // 2 days ago
        createSeedAttempt(1, 2, "Alex Johnson", "alex@student.com", 1, "Java Core & OOP Fundamentals", 50, 100.0, new Timestamp(now - 172800000L));
        // 1 day ago
        createSeedAttempt(2, 3, "Sophia Chen", "sophia@student.com", 1, "Java Core & OOP Fundamentals", 40, 80.0, new Timestamp(now - 86400000L));
        // 6 hours ago
        createSeedAttempt(3, 4, "Liam Davis", "liam@student.com", 1, "Java Core & OOP Fundamentals", 30, 60.0, new Timestamp(now - 21600000L));
        // 5 hours ago
        createSeedAttempt(4, 2, "Alex Johnson", "alex@student.com", 2, "Advanced Java: Collections & Multithreading", 40, 80.0, new Timestamp(now - 18000000L));
        // 3 hours ago
        createSeedAttempt(5, 3, "Sophia Chen", "sophia@student.com", 2, "Advanced Java: Collections & Multithreading", 50, 100.0, new Timestamp(now - 10800000L));
        // 1 hour ago
        createSeedAttempt(6, 4, "Liam Davis", "liam@student.com", 3, "Database Concepts & JDBC Connectivity", 40, 80.0, new Timestamp(now - 3600000L));
    }

    private static void createSeedAttempt(int id, int sId, String sName, String sEmail, int qId, String qTitle, int score, double pct, Timestamp date) {
        QuizAttempt qa = new QuizAttempt(id, sId, qId, score, pct, date);
        qa.setStudentName(sName);
        qa.setStudentEmail(sEmail);
        qa.setQuizTitle(qTitle);
        qa.setTotalMarks(50);
        attemptCache.put(id, qa);
    }

    @Override
    @AuditLog(action = "RECORD_QUIZ_ATTEMPT", critical = true)
    public QuizAttempt save(QuizAttempt attempt) throws DatabaseException {
        return saveAttemptWithAnswers(attempt, new ArrayList<>());
    }

    /**
     * Saves a quiz attempt and all corresponding student answers in a single atomic transaction.
     * Demonstrates JDBC Transaction Management (commit / rollback).
     */
    public QuizAttempt saveAttemptWithAnswers(QuizAttempt attempt, List<StudentAnswer> answers) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            int newId = idCounter.incrementAndGet();
            attempt.setId(newId);
            if (attempt.getAttemptDate() == null) {
                attempt.setAttemptDate(new Timestamp(System.currentTimeMillis()));
            }
            attemptCache.put(newId, attempt);
            if (answers != null && !answers.isEmpty()) {
                for (StudentAnswer ans : answers) {
                    ans.setAttemptId(newId);
                }
                answersCache.put(newId, new ArrayList<>(answers));
            }
            return attempt;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Start Transaction
            conn.setAutoCommit(false);

            String attemptSql = "INSERT INTO quiz_attempts (student_id, quiz_id, score, percentage, attempt_date) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(attemptSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, attempt.getStudentId());
                ps.setInt(2, attempt.getQuizId());
                ps.setInt(3, attempt.getScore());
                ps.setDouble(4, attempt.getPercentage());
                Timestamp ts = attempt.getAttemptDate() != null ? attempt.getAttemptDate() : new Timestamp(new Date().getTime());
                ps.setTimestamp(5, ts);

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        attempt.setId(rs.getInt(1));
                    }
                }
            }

            // Insert student answers
            if (answers != null && !answers.isEmpty()) {
                String answerSql = "INSERT INTO student_answers (attempt_id, question_id, selected_answer) VALUES (?, ?, ?)";
                try (PreparedStatement psAnswer = conn.prepareStatement(answerSql)) {
                    for (StudentAnswer ans : answers) {
                        psAnswer.setInt(1, attempt.getId());
                        psAnswer.setInt(2, ans.getQuestionId());
                        psAnswer.setString(3, ans.getSelectedAnswer());
                        psAnswer.addBatch();
                    }
                    psAnswer.executeBatch();
                }
            }

            // Commit Transaction
            conn.commit();
            return attempt;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Transaction rollback failed: " + ex.getMessage());
                }
            }
            throw new DatabaseException("Failed to save quiz attempt: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to reset connection auto-commit: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public Optional<QuizAttempt> findById(Integer id) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return Optional.ofNullable(attemptCache.get(id));
        }

        String sql = "SELECT qa.id, qa.student_id, qa.quiz_id, qa.score, qa.percentage, qa.attempt_date, " +
                     "u.name AS student_name, u.email AS student_email, q.title AS quiz_title, q.total_marks " +
                     "FROM quiz_attempts qa " +
                     "JOIN users u ON qa.student_id = u.id " +
                     "JOIN quizzes q ON qa.quiz_id = q.id " +
                     "WHERE qa.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAttempt(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query attempt by id: " + id, e);
        }
    }

    public List<QuizAttempt> findByStudentId(int studentId) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return attemptCache.values().stream()
                    .filter(a -> a.getStudentId() == studentId)
                    .sorted((a1, a2) -> a2.getAttemptDate().compareTo(a1.getAttemptDate()))
                    .collect(Collectors.toList());
        }

        List<QuizAttempt> list = new ArrayList<>();
        String sql = "SELECT qa.id, qa.student_id, qa.quiz_id, qa.score, qa.percentage, qa.attempt_date, " +
                     "u.name AS student_name, u.email AS student_email, q.title AS quiz_title, q.total_marks " +
                     "FROM quiz_attempts qa " +
                     "JOIN users u ON qa.student_id = u.id " +
                     "JOIN quizzes q ON qa.quiz_id = q.id " +
                     "WHERE qa.student_id = ? " +
                     "ORDER BY qa.attempt_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttempt(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find attempts for student: " + studentId, e);
        }
    }

    @Override
    public List<QuizAttempt> findAll() throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return attemptCache.values().stream()
                    .sorted((a1, a2) -> a2.getAttemptDate().compareTo(a1.getAttemptDate()))
                    .collect(Collectors.toList());
        }

        List<QuizAttempt> list = new ArrayList<>();
        String sql = "SELECT qa.id, qa.student_id, qa.quiz_id, qa.score, qa.percentage, qa.attempt_date, " +
                     "u.name AS student_name, u.email AS student_email, q.title AS quiz_title, q.total_marks " +
                     "FROM quiz_attempts qa " +
                     "JOIN users u ON qa.student_id = u.id " +
                     "JOIN quizzes q ON qa.quiz_id = q.id " +
                     "ORDER BY qa.attempt_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAttempt(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query all quiz attempts", e);
        }
    }

    public List<QuizAttempt> findByQuizId(int quizId) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return attemptCache.values().stream()
                    .filter(a -> a.getQuizId() == quizId)
                    .sorted((a1, a2) -> Double.compare(a2.getPercentage(), a1.getPercentage()))
                    .collect(Collectors.toList());
        }

        List<QuizAttempt> list = new ArrayList<>();
        String sql = "SELECT qa.id, qa.student_id, qa.quiz_id, qa.score, qa.percentage, qa.attempt_date, " +
                     "u.name AS student_name, u.email AS student_email, q.title AS quiz_title, q.total_marks " +
                     "FROM quiz_attempts qa " +
                     "JOIN users u ON qa.student_id = u.id " +
                     "JOIN quizzes q ON qa.quiz_id = q.id " +
                     "WHERE qa.quiz_id = ? " +
                     "ORDER BY qa.percentage DESC, qa.attempt_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttempt(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query attempts by quiz: " + quizId, e);
        }
    }

    public List<StudentAnswer> findAnswersByAttemptId(int attemptId) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return answersCache.getOrDefault(attemptId, new ArrayList<>());
        }

        List<StudentAnswer> list = new ArrayList<>();
        String sql = "SELECT id, attempt_id, question_id, selected_answer FROM student_answers WHERE attempt_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentAnswer(
                            rs.getInt("id"),
                            rs.getInt("attempt_id"),
                            rs.getInt("question_id"),
                            rs.getString("selected_answer")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query student answers for attempt: " + attemptId, e);
        }
    }

    @Override
    public boolean update(QuizAttempt entity) throws DatabaseException {
        return false; // Quiz attempts are immutable audit logs
    }

    @Override
    @AuditLog(action = "DELETE_ATTEMPT")
    public boolean deleteById(Integer id) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            answersCache.remove(id);
            return attemptCache.remove(id) != null;
        }

        String sql = "DELETE FROM quiz_attempts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete attempt: " + id, e);
        }
    }

    private QuizAttempt mapResultSetToAttempt(ResultSet rs) throws SQLException {
        QuizAttempt qa = new QuizAttempt(
                rs.getInt("id"),
                rs.getInt("student_id"),
                rs.getInt("quiz_id"),
                rs.getInt("score"),
                rs.getDouble("percentage"),
                rs.getTimestamp("attempt_date")
        );
        qa.setStudentName(rs.getString("student_name"));
        qa.setStudentEmail(rs.getString("student_email"));
        qa.setQuizTitle(rs.getString("quiz_title"));
        qa.setTotalMarks(rs.getInt("total_marks"));
        return qa;
    }
}

