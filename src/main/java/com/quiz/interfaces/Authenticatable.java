package com.quiz.interfaces;

/**
 * Interface representing entities that can undergo authentication.
 * Demonstrates core interface declaration, contracts, and loose coupling.
 */
public interface Authenticatable {
    boolean authenticate(String rawPassword);
    String getEmail();
    String getRole();
}

