package com.quiz.dao;

import com.quiz.annotations.AuditLog;
import com.quiz.annotations.Author;
import com.quiz.exception.DatabaseException;
import com.quiz.model.Admin;
import com.quiz.model.Student;
import com.quiz.model.User;
import com.quiz.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object for User entities.
 * Demonstrates:
 * - JDBC: PreparedStatement, ResultSet, Statement.RETURN_GENERATED_KEYS
 * - Checked Exception handling (SQLException wrapped in DatabaseException)
 * - CRUD database operations
 * - Built-in fallback cache seeded with sample data when MySQL is not active
 */
@Author(name = "Antigravity Team", date = "2026-11-03", version = "1.0.0")
public class UserDao implements GenericDao<User, Integer> {

    // Fallback store with thread-safe ConcurrentHashMap & AtomicInteger
    private static final Map<Integer, User> userCache = new ConcurrentHashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(10);

    static {
        seedSampleUsers();
    }

    private static void seedSampleUsers() {
        Admin admin = new Admin(1, "Dr. Sarah Connor (Admin)", "admin@quiz.com", 
                PasswordUtil.hashPassword("admin123"), "Academic Administration");
        Student s1 = new Student(2, "Alex Johnson", "alex@student.com", PasswordUtil.hashPassword("student123"));
        Student s2 = new Student(3, "Sophia Chen", "sophia@student.com", PasswordUtil.hashPassword("student123"));
        Student s3 = new Student(4, "Liam Davis", "liam@student.com", PasswordUtil.hashPassword("student123"));

        userCache.put(admin.getId(), admin);
        userCache.put(s1.getId(), s1);
        userCache.put(s2.getId(), s2);
        userCache.put(s3.getId(), s3);
    }

    @Override
    @AuditLog(action = "CREATE_USER")
    public User save(User user) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            int newId = idCounter.incrementAndGet();
            user.setId(newId);
            userCache.put(newId, user);
            return user;
        }

        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return Optional.ofNullable(userCache.get(id));
        }

        String sql = "SELECT id, name, email, password, role FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query user by id: " + id, e);
        }
    }

    public Optional<User> findByEmail(String email) throws DatabaseException {
        if (email == null) return Optional.empty();
        String normalizedEmail = email.trim().toLowerCase();

        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return userCache.values().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(normalizedEmail))
                    .findFirst();
        }

        String sql = "SELECT id, name, email, password, role FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query user by email: " + email, e);
        }
    }

    @Override
    public List<User> findAll() throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return new ArrayList<>(userCache.values());
        }

        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, email, password, role FROM users ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all users", e);
        }
    }

    public List<Student> findAllStudents() throws DatabaseException {
        List<Student> students = new ArrayList<>();
        for (User u : findAll()) {
            if (u instanceof Student) {
                students.add((Student) u);
            }
        }
        return students;
    }

    @Override
    @AuditLog(action = "UPDATE_USER")
    public boolean update(User user) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            if (userCache.containsKey(user.getId())) {
                userCache.put(user.getId(), user);
                return true;
            }
            return false;
        }

        String sql = "UPDATE users SET name = ?, email = ?, password = ?, role = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user: " + user.getId(), e);
        }
    }

    @Override
    @AuditLog(action = "DELETE_USER")
    public boolean deleteById(Integer id) throws DatabaseException {
        if (DatabaseConnection.isUsingEmbeddedFallback()) {
            return userCache.remove(id) != null;
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete user: " + id, e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String role = rs.getString("role");

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(id, name, email, password, "Academic Administration");
        } else {
            return new Student(id, name, email, password);
        }
    }
}

