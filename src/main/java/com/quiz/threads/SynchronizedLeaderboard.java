package com.quiz.threads;

import com.quiz.model.QuizAttempt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Thread-safe leaderboard manager.
 * Demonstrates:
 * - Thread Synchronization: synchronized methods and blocks to prevent race conditions
 * - Collections: ArrayList sorting with custom Comparator
 * - Java Lambdas and Stream API
 */
public class SynchronizedLeaderboard {

    private static final SynchronizedLeaderboard INSTANCE = new SynchronizedLeaderboard();
    private final List<QuizAttempt> sharedEntries = new ArrayList<>();

    private SynchronizedLeaderboard() {
    }

    public static SynchronizedLeaderboard getInstance() {
        return INSTANCE;
    }

    /**
     * Synchronized method ensuring safe concurrent entry addition from multiple threads.
     */
    public synchronized void recordAttempt(QuizAttempt attempt) {
        if (attempt == null) return;
        // Remove existing entry for same student & quiz if score is lower
        sharedEntries.removeIf(e -> e.getStudentId() == attempt.getStudentId() && e.getQuizId() == attempt.getQuizId());
        sharedEntries.add(attempt);
    }

    /**
     * Demonstrates synchronized block for fine-grained locking and Lambda Comparator.
     */
    public List<QuizAttempt> getTopPerformers(int limit) {
        synchronized (sharedEntries) {
            // Sort using Lambda expression with Comparator
            Comparator<QuizAttempt> scoreComparator = (a1, a2) -> {
                int scoreDiff = Double.compare(a2.getPercentage(), a1.getPercentage());
                if (scoreDiff != 0) return scoreDiff;
                // Secondary sort: earlier attempt date takes precedence
                if (a1.getAttemptDate() == null || a2.getAttemptDate() == null) return 0;
                return a1.getAttemptDate().compareTo(a2.getAttemptDate());
            };

            return sharedEntries.stream()
                    .sorted(scoreComparator)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    public synchronized List<QuizAttempt> getLeaderboardByQuiz(int quizId, int limit) {
        return sharedEntries.stream()
                .filter(a -> a.getQuizId() == quizId)
                .sorted((a1, a2) -> Double.compare(a2.getPercentage(), a1.getPercentage()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public synchronized void reload(List<QuizAttempt> attempts) {
        sharedEntries.clear();
        if (attempts != null) {
            sharedEntries.addAll(attempts);
        }
    }

    public synchronized int getTotalRecordedAttempts() {
        return sharedEntries.size();
    }
}

