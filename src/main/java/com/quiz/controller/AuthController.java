package com.quiz.controller;

import com.quiz.annotations.AuditLog;
import com.quiz.annotations.Author;
import com.quiz.exception.AuthenticationException;
import com.quiz.exception.DatabaseException;
import com.quiz.generics.ApiResponse;
import com.quiz.model.User;
import com.quiz.service.AuthService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller managing User Authentication and Registration.
 * Demonstrates:
 * - Annotations (@Author, @AuditLog)
 * - Custom Exceptions (AuthenticationException, DatabaseException)
 * - Generic API response handling (ApiResponse<T>)
 */
@Author(name = "Antigravity Team", date = "2026-11-03", version = "1.0.0")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @AuditLog(action = "USER_LOGIN")
    public ApiResponse<?> login(String email, String password) {
        try {
            User user = authService.login(email, password);
            String token = authService.createSession(user);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return ApiResponse.success("Login successful", data);
        } catch (AuthenticationException e) {
            return ApiResponse.error(e.getMessage());
        } catch (DatabaseException e) {
            return ApiResponse.error("Database error during login: " + e.getMessage());
        }
    }

    @AuditLog(action = "USER_REGISTER")
    public ApiResponse<?> register(String name, String email, String password) {
        try {
            User user = authService.registerStudent(name, email, password);
            String token = authService.createSession(user);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return ApiResponse.success("Registration successful! Welcome to the Quiz Platform.", data);
        } catch (AuthenticationException e) {
            return ApiResponse.error(e.getMessage());
        } catch (DatabaseException e) {
            return ApiResponse.error("Database error during registration: " + e.getMessage());
        }
    }

    public ApiResponse<?> getCurrentUser(String token) {
        User user = authService.getUserBySession(token);
        if (user == null) {
            return ApiResponse.error("Session expired or invalid.");
        }
        return ApiResponse.success(user);
    }

    @AuditLog(action = "USER_LOGOUT")
    public ApiResponse<?> logout(String token) {
        authService.invalidateSession(token);
        return ApiResponse.success("Logged out successfully.", null);
    }
}

