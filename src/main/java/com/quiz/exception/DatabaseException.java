package com.quiz.exception;

/**
 * Wraps JDBC SQLExceptions into domain-level checked exceptions.
 * Demonstrates checked exception wrapping and maintaining the root cause throwable.
 */
public class DatabaseException extends QuizException {
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }
}

