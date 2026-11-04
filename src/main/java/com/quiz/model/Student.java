package com.quiz.model;

import com.quiz.annotations.Author;

/**
 * Concrete class representing a Student user.
 * Demonstrates:
 * - Inheritance (extends User)
 * - Constructor chaining using super(...)
 * - Method overriding (Polymorphism)
 * - Custom annotation @Author
 */
@Author(name = "Antigravity Team", date = "2026-11-03", version = "1.0.0")
public class Student extends User {
    private static final long serialVersionUID = 1L;

    private int totalQuizzesAttempted;
    private double averageScore;

    public Student() {
        super();
        setRole("STUDENT");
    }

    public Student(int id, String name, String email, String password) {
        super(id, name, email, password, "STUDENT");
    }

    public Student(String name, String email, String password) {
        super(name, email, password, "STUDENT");
    }

    @Override
    public String getRoleDescription() {
        return "Student: Authorized to enroll, view available quizzes, take exams, and track performance.";
    }

    @Override
    public boolean canCreateQuiz() {
        return false; // Students cannot create quizzes
    }

    @Override
    public boolean canTakeQuiz() {
        return true;  // Students are allowed to take quizzes
    }

    @Override
    public String getDashboardPath() {
        return "student-dashboard.html";
    }

    @Override
    public String getRolePermissions() {
        return "READ_QUIZZES, TAKE_QUIZ, VIEW_OWN_RESULTS, VIEW_LEADERBOARD";
    }

    public int getTotalQuizzesAttempted() {
        return totalQuizzesAttempted;
    }

    public void setTotalQuizzesAttempted(int totalQuizzesAttempted) {
        this.totalQuizzesAttempted = totalQuizzesAttempted;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }
}

