package com.quiz.interfaces;

/**
 * Interface defining role-based quiz operations.
 * Demonstrates polymorphism when implemented differently by Student and Admin.
 */
public interface QuizOperations {
    boolean canCreateQuiz();
    boolean canTakeQuiz();
    String getDashboardPath();
    String getRolePermissions();
}

