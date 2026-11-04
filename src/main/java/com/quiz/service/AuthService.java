package com.quiz.service;

import com.quiz.dao.UserDao;
import com.quiz.exception.AuthenticationException;
import com.quiz.exception.DatabaseException;
import com.quiz.model.Admin;
import com.quiz.model.Student;
import com.quiz.model.User;
import com.quiz.util.PasswordUtil;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing user authentication, registration, and active sessions.
 * Demonstrates:
 * - Session tracking using Collections (ConcurrentHashMap)
 * - Checked Exception handling (AuthenticationException, DatabaseException)
 * - Secure password hashing
 */
public class AuthService {

    private final UserDao userDao;
    private final Map<String, User> activeSessions = new ConcurrentHashMap<>();

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User login(String email, String password) throws AuthenticationException, DatabaseException {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            throw new AuthenticationException("Email and password must not be empty.");
        }

        Optional<User> userOpt = userDao.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("No account registered with email: " + email);
        }

        User user = userOpt.get();
        if (!user.authenticate(password)) {
            throw new AuthenticationException("Invalid password provided.");
        }

        return user;
    }

    public User registerStudent(String name, String email, String password) throws AuthenticationException, DatabaseException {
        if (name == null || name.trim().isEmpty()) {
            throw new AuthenticationException("Name cannot be blank.");
        }
        if (email == null || !email.contains("@")) {
            throw new AuthenticationException("A valid email address is required.");
        }
        if (password == null || password.length() < 6) {
            throw new AuthenticationException("Password must be at least 6 characters long.");
        }

        Optional<User> existing = userDao.findByEmail(email.trim());
        if (existing.isPresent()) {
            throw new AuthenticationException("An account with email " + email + " already exists.");
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        Student student = new Student(name.trim(), email.trim(), hashedPassword);
        return userDao.save(student);
    }

    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, user);
        return token;
    }

    public User getUserBySession(String token) {
        if (token == null) return null;
        return activeSessions.get(token);
    }

    public void invalidateSession(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }
}

