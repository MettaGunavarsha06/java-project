package com.quiz.controller;

import com.quiz.annotations.AuditLog;
import com.quiz.annotations.Author;
import com.quiz.exception.AuthenticationException;
import com.quiz.exception.DatabaseException;
import com.quiz.generics.ApiResponse;
import com.quiz.model.CandidateLoginHistory;
import com.quiz.model.User;
import com.quiz.service.AuthService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller managing User and Candidate Authentication, Registration,
 * Candidate Profile Information, and Login History.
 * Demonstrates:
 * - Annotations (@Author, @AuditLog)
 * - Custom Exceptions (AuthenticationException, DatabaseException)
 * - Generic API response handling (ApiResponse<T>)
 */
@Author(name = "Antigravity Team", date = "2026-11-04", version = "2.0.0")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @AuditLog(action = "USER_LOGIN")
    public ApiResponse<?> login(String email, String password) {
        return login(email, password, "127.0.0.1", "Standard Browser");
    }

    @AuditLog(action = "CANDIDATE_LOGIN")
    public ApiResponse<?> login(String email, String password, String ipAddress, String userAgent) {
        try {
            User user = authService.login(email, password, ipAddress, userAgent);
            String token = authService.createSession(user);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return ApiResponse.success("Login successful. Candidate profile and session active.", data);
        } catch (AuthenticationException e) {
            return ApiResponse.error(e.getMessage());
        } catch (DatabaseException e) {
            return ApiResponse.error("Database error during login: " + e.getMessage());
        }
    }

    @AuditLog(action = "USER_REGISTER")
    public ApiResponse<?> register(String name, String email, String password) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("password", password);
        return registerCandidate(data);
    }

    @AuditLog(action = "CANDIDATE_REGISTER")
    public ApiResponse<?> registerCandidate(Map<String, Object> candidateData) {
        try {
            User user = authService.registerCandidate(candidateData);
            String token = authService.createSession(user);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return ApiResponse.success("Registration successful! All candidate information saved to database.", data);
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

    public ApiResponse<?> getCandidateProfile(String token) {
        User user = authService.getUserBySession(token);
        if (user == null) {
            return ApiResponse.error("Session expired or invalid.");
        }

        try {
            List<CandidateLoginHistory> history = authService.getCandidateLoginHistory(user.getId());
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("user", user);
            profileData.put("loginHistory", history);
            return ApiResponse.success("Candidate profile loaded successfully", profileData);
        } catch (DatabaseException e) {
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("user", user);
            return ApiResponse.success("Candidate profile loaded", profileData);
        }
    }

    @AuditLog(action = "UPDATE_CANDIDATE_PROFILE")
    public ApiResponse<?> updateProfile(String token, Map<String, Object> profileData) {
        User user = authService.getUserBySession(token);
        if (user == null) {
            return ApiResponse.error("Unauthorized session");
        }

        try {
            User updated = authService.updateCandidateProfile(user.getId(), profileData);
            return ApiResponse.success("Candidate profile updated successfully!", updated);
        } catch (AuthenticationException | DatabaseException e) {
            return ApiResponse.error("Failed to update profile: " + e.getMessage());
        }
    }

    @AuditLog(action = "USER_LOGOUT")
    public ApiResponse<?> logout(String token) {
        authService.invalidateSession(token);
        return ApiResponse.success("Logged out successfully.", null);
    }
}
