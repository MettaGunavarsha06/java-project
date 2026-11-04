package com.quiz.dao;

import com.quiz.annotations.AuditLog;
import com.quiz.annotations.Author;
import com.quiz.exception.DatabaseException;
import com.quiz.model.Question;

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
import java.util.stream.Collectors;

/**
 * Data Access Object for Multiple-Choice Questions.
 * Demonstrates:
 * - PreparedStatement parameter mapping
 * - Foreign key relationship handling (quiz_id)
 * - Stream API for filtering questions by quiz ID in embedded fallback mode
 */
@Author(name = "Antigravity Team", date = "2026-11-03", version = "1.0.0")
public class QuestionDao implements GenericDao<Question, Integer> {

    private static final Map<Integer, Question> questionCache = new ConcurrentHashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(50);

    static {
        seedSampleQuestions();
    }

    private static void seedSampleQuestions() {
        // Quiz 1 Questions (Java Core & OOP)
        addSeed(1, 1, "Which of the following OOP principles emphasizes hiding internal state and requiring all interaction to be performed through an object's methods?",
                "Inheritance", "Encapsulation", "Polymorphism", "Abstraction", "B");
        addSeed(2, 1, "What is the default value of a boolean variable declared as an instance member in Java?",
                "true", "false", "0", "null", "B");
        addSeed(3, 1, "Which Java keyword is used by a class to inherit properties and behaviors from an abstract class?",
                "implements", "extends", "inherits", "instanceof", "B");
        addSeed(4, 1, "Can an abstract class have constructors in Java?",
                "No, abstract classes cannot have constructors", "Yes, but only private constructors", 
                "Yes, invoked during subclass instantiation via super()", "Only if the class has no abstract methods", "C");
        addSeed(5, 1, "Which of the following is true about method overriding in Java?",
                "Return type must be incompatible", "Method name and argument list must match the superclass method", 
                "Private methods can be overridden", "Static methods can be dynamically overridden at runtime", "B");

        // Quiz 2 Questions (Advanced Java: Collections & Multithreading)
        addSeed(6, 2, "Which Java Collections interface allows unique elements only and does not maintain insertion order by default?",
                "ArrayList", "LinkedList", "HashSet", "LinkedHashSet", "C");
        addSeed(7, 2, "What will happen when you call Thread.start() twice on the same Thread instance in Java?",
                "Thread restarts from beginning", "IllegalThreadStateException is thrown", "A new thread is created automatically", "The call is silently ignored", "B");
        addSeed(8, 2, "Which concurrency interface provides a call() method that can return a value and throw checked exceptions?",
                "Runnable", "Callable", "Thread", "Executor", "B");
        addSeed(9, 2, "What does the Java Stream API intermediate operation \"filter()\" do?",
                "Transforms elements to a new type", "Returns a stream of elements that match the given Predicate", "Terminates the stream processing", "Sorts elements naturally", "B");
        addSeed(10, 2, "Which mechanism in Java provides mutual exclusion to prevent race conditions on shared objects?",
                "volatile keyword", "synchronized block or method", "transient keyword", "finalize method", "B");

        // Quiz 3 Questions (Database Concepts & JDBC Connectivity)
        addSeed(11, 3, "Why is PreparedStatement preferred over Statement in JDBC?",
                "It executes slower but supports more databases", "It prevents SQL Injection and pre-compiles queries for efficiency", "It requires no database connection", "It does not require parameter binding", "B");
        addSeed(12, 3, "Which JDBC method commits the current database transaction when auto-commit is disabled?",
                "connection.setAutoCommit(true)", "connection.save()", "connection.commit()", "statement.executeCommit()", "C");
        addSeed(13, 3, "What type of exception is java.sql.SQLException in Java?",
                "Unchecked Exception (RuntimeException)", "Checked Exception requiring try-catch or throws", "Error", "JVM Fatal Error", "B");
        addSeed(14, 3, "Which interface in java.sql is used to retrieve rows from an executed SQL SELECT query?",
                "RowMapper", "ResultSet", "DataRecord", "CursorSet", "B");
        addSeed(15, 3, "Which clause in SQL is used to filter records after aggregation using GROUP BY?",
                "WHERE", "HAVING", "ORDER BY", "FILTER", "B");
    }

    private static void addSeed(int id, int qId, String text, String a, String b, String c, String d, String correct) {
        Question q = new Question(id, qId, text, a, b, c, d, correct);
        questionCache.put(id, q);
    }

    @Override
    @AuditLog(action = "CREATE_QUESTION")
    public Question save(Question question) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            int newId = idCounter.incrementAndGet();
            question.setId(newId);
            questionCache.put(newId, question);
            return question;
        }

        String sql = "INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, question.getQuizId());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, question.getOptionA());
            ps.setString(4, question.getOptionB());
            ps.setString(5, question.getOptionC());
            ps.setString(6, question.getOptionD());
            ps.setString(7, question.getCorrectAnswer());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        question.setId(rs.getInt(1));
                    }
                }
            }
            return question;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save question: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Question> findById(Integer id) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return Optional.ofNullable(questionCache.get(id));
        }

        String sql = "SELECT id, quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer FROM questions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToQuestion(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query question by id: " + id, e);
        }
    }

    public List<Question> findByQuizId(int quizId) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return questionCache.values().stream()
                    .filter(q -> q.getQuizId() == quizId)
                    .sorted((q1, q2) -> Integer.compare(q1.getId(), q2.getId()))
                    .collect(Collectors.toList());
        }

        List<Question> list = new ArrayList<>();
        String sql = "SELECT id, quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer FROM questions WHERE quiz_id = ? ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToQuestion(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve questions for quiz " + quizId, e);
        }
    }

    @Override
    public List<Question> findAll() throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return new ArrayList<>(questionCache.values());
        }

        List<Question> list = new ArrayList<>();
        String sql = "SELECT id, quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer FROM questions ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToQuestion(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all questions", e);
        }
    }

    @Override
    @AuditLog(action = "UPDATE_QUESTION")
    public boolean update(Question question) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            if (questionCache.containsKey(question.getId())) {
                questionCache.put(question.getId(), question);
                return true;
            }
            return false;
        }

        String sql = "UPDATE questions SET quiz_id = ?, question_text = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_answer = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, question.getQuizId());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, question.getOptionA());
            ps.setString(4, question.getOptionB());
            ps.setString(5, question.getOptionC());
            ps.setString(6, question.getOptionD());
            ps.setString(7, question.getCorrectAnswer());
            ps.setInt(8, question.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update question: " + question.getId(), e);
        }
    }

    @Override
    @AuditLog(action = "DELETE_QUESTION")
    public boolean deleteById(Integer id) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return questionCache.remove(id) != null;
        }

        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete question: " + id, e);
        }
    }

    private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        return new Question(
                rs.getInt("id"),
                rs.getInt("quiz_id"),
                rs.getString("question_text"),
                rs.getString("option_a"),
                rs.getString("option_b"),
                rs.getString("option_c"),
                rs.getString("option_d"),
                rs.getString("correct_answer")
        );
    }
}

