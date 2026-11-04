package com.quiz.dao;

import com.quiz.annotations.AuditLog;
import com.quiz.annotations.Author;
import com.quiz.exception.DatabaseException;
import com.quiz.model.Quiz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object for Quiz entities.
 * Demonstrates:
 * - PreparedStatement execution for CRUD operations
 * - Generated key retrieval
 * - Thread-safe embedded caching fallback
 */
@Author(name = "Antigravity Team", date = "2026-11-03", version = "1.0.0")
public class QuizDao implements GenericDao<Quiz, Integer> {

    private static final Map<Integer, Quiz> quizCache = new ConcurrentHashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(10);

    static {
        seedSampleQuizzes();
    }

    private static void seedSampleQuizzes() {
        Quiz q1 = new Quiz(1, "Java Core & OOP Fundamentals", 
                "Test your knowledge on Object-Oriented Programming, Inheritance, Polymorphism, and Encapsulation in Java.", 10, 50);
        Quiz q2 = new Quiz(2, "Advanced Java: Collections & Multithreading", 
                "Evaluate your skills in Java Collections (List, Set, Map), Generics, Thread Lifecycle, and Concurrency utilities.", 15, 50);
        Quiz q3 = new Quiz(3, "Database Concepts & JDBC Connectivity", 
                "Questions covering relational database normalization, SQL queries, PreparedStatement, and JDBC transaction management.", 10, 50);

        quizCache.put(q1.getId(), q1);
        quizCache.put(q2.getId(), q2);
        quizCache.put(q3.getId(), q3);
    }

    @Override
    @AuditLog(action = "CREATE_QUIZ", critical = true)
    public Quiz save(Quiz quiz) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            int newId = idCounter.incrementAndGet();
            quiz.setId(newId);
            quizCache.put(newId, quiz);
            return quiz;
        }

        String sql = "INSERT INTO quizzes (title, description, duration, total_marks) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getDescription());
            ps.setInt(3, quiz.getDuration());
            ps.setInt(4, quiz.getTotalMarks());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        quiz.setId(rs.getInt(1));
                    }
                }
            }
            return quiz;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save quiz: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Quiz> findById(Integer id) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return Optional.ofNullable(quizCache.get(id));
        }

        String sql = "SELECT id, title, description, duration, total_marks FROM quizzes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToQuiz(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query quiz by id: " + id, e);
        }
    }

    @Override
    public List<Quiz> findAll() throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return new ArrayList<>(quizCache.values());
        }

        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT id, title, description, duration, total_marks FROM quizzes ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToQuiz(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve quizzes", e);
        }
    }

    @Override
    @AuditLog(action = "UPDATE_QUIZ")
    public boolean update(Quiz quiz) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            if (quizCache.containsKey(quiz.getId())) {
                quizCache.put(quiz.getId(), quiz);
                return true;
            }
            return false;
        }

        String sql = "UPDATE quizzes SET title = ?, description = ?, duration = ?, total_marks = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getDescription());
            ps.setInt(3, quiz.getDuration());
            ps.setInt(4, quiz.getTotalMarks());
            ps.setInt(5, quiz.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update quiz: " + quiz.getId(), e);
        }
    }

    @Override
    @AuditLog(action = "DELETE_QUIZ", critical = true)
    public boolean deleteById(Integer id) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return quizCache.remove(id) != null;
        }

        String sql = "DELETE FROM quizzes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete quiz: " + id, e);
        }
    }

    private Quiz mapResultSetToQuiz(ResultSet rs) throws SQLException {
        return new Quiz(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("duration"),
                rs.getInt("total_marks")
        );
    }
}

