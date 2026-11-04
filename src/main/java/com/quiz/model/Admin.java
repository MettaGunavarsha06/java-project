package com.quiz.model;

import com.quiz.annotations.Author;

/**
 * Concrete class representing an Administrator user.
 * Demonstrates:
 * - Inheritance (extends User)
 * - Constructor chaining using super(...)
 * - Method overriding (Polymorphism)
 * - Custom annotation @Author
 */
@Author(name = "Antigravity Team", date = "2026-11-03", version = "1.0.0")
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private String department;

    public Admin() {
        super();
        setRole("ADMIN");
        this.department = "Academic Administration";
    }

    public Admin(int id, String name, String email, String password, String department) {
        super(id, name, email, password, "ADMIN");
        this.department = department != null ? department : "Academic Administration";
    }

    public Admin(String name, String email, String password) {
        this(0, name, email, password, "Academic Administration");
    }

    @Override
    public String getRoleDescription() {
        return "Administrator: Full control over quiz authoring, question management, student records, and system analytics.";
    }

    @Override
    public boolean canCreateQuiz() {
        return true;  // Administrators can author quizzes
    }

    @Override
    public boolean canTakeQuiz() {
        return false; // Admins oversee rather than take exams
    }

    @Override
    public String getDashboardPath() {
        return "admin-dashboard.html";
    }

    @Override
    public String getRolePermissions() {
        return "ALL_PERMISSIONS (CREATE_QUIZ, EDIT_QUIZ, DELETE_QUIZ, MANAGE_USERS, EXPORT_REPORTS)";
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}

