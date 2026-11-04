package com.quiz.model;

import com.quiz.interfaces.Authenticatable;
import com.quiz.interfaces.QuizOperations;
import com.quiz.util.PasswordUtil;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class representing a system user.
 * Demonstrates:
 * - Abstract classes & inheritance
 * - Encapsulation (private fields, getters & setters with validation)
 * - Constructor overloading
 * - Interface implementation (Authenticatable, QuizOperations, Serializable)
 * - Input validation throwing unchecked IllegalArgumentException
 */
public abstract class User implements Authenticatable, QuizOperations, Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String password; // SHA-256 hashed password
    private String role;     // "ADMIN" or "STUDENT"

    // Default Constructor
    public User() {
    }

    // Parameterized Constructor
    public User(int id, String name, String email, String password, String role) {
        setId(id);
        setName(name);
        setEmail(email);
        setPassword(password);
        setRole(role);
    }

    public User(String name, String email, String password, String role) {
        this(0, name, email, password, role);
    }

    // Abstract methods demonstrating Polymorphism
    public abstract String getRoleDescription();

    @Override
    public boolean authenticate(String rawPassword) {
        if (rawPassword == null || this.password == null) {
            return false;
        }
        return PasswordUtil.verifyPassword(rawPassword, this.password);
    }

    // Getters and Setters with Encapsulation & Validation
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("User ID cannot be negative");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        this.name = name.trim();
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Invalid email address format: " + email);
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.password = password;
    }

    @Override
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("STUDENT"))) {
            throw new IllegalArgumentException("Role must be either ADMIN or STUDENT");
        }
        this.role = role.toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, name='%s', email='%s', role='%s']", id, name, email, role);
    }
}

