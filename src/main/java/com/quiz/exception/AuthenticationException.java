package com.quiz.exception;

/**
 * Thrown when user credentials or session authorization fail.
 */
public class AuthenticationException extends QuizException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}

