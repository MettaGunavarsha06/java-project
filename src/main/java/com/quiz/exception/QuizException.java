package com.quiz.exception;

/**
 * Base custom checked exception for the Quiz Management Application.
 * Demonstrates Exception hierarchy, checked exception design, and custom message wrapping.
 */
public class QuizException extends Exception {
    private static final long serialVersionUID = 1L;

    public QuizException(String message) {
        super(message);
    }

    public QuizException(String message, Throwable cause) {
        super(message, cause);
    }
}

