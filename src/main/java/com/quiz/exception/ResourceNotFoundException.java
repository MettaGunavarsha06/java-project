package com.quiz.exception;

/**
 * Thrown when requested quiz, question, or user record cannot be found.
 */
public class ResourceNotFoundException extends QuizException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

