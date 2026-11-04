package com.quiz.threads;

import java.util.function.Consumer;

/**
 * Background Thread for tracking and enforcing quiz countdown timer.
 * Demonstrates:
 * - Multithreading: Custom Thread subclass
 * - Thread Management: Creation, start(), Thread.sleep(), and cooperative interruption
 * - Handling InterruptedException cleanly
 */
public class QuizTimerThread extends Thread {

    private final String sessionToken;
    private final int totalSeconds;
    private int remainingSeconds;
    private volatile boolean running = true;
    private final Consumer<String> onTimeoutCallback;

    public QuizTimerThread(String sessionToken, int durationMinutes, Consumer<String> onTimeoutCallback) {
        super("QuizTimer-" + sessionToken);
        this.sessionToken = sessionToken;
        this.totalSeconds = durationMinutes * 60;
        this.remainingSeconds = this.totalSeconds;
        this.onTimeoutCallback = onTimeoutCallback;
        // Set daemon so JVM does not hang on exit
        setDaemon(true);
    }

    @Override
    public void run() {
        System.out.println("[QuizTimerThread] Started countdown for session " + sessionToken + " (" + totalSeconds + "s)");
        try {
            while (running && remainingSeconds > 0) {
                // Sleep for 1 second intervals
                Thread.sleep(1000);
                remainingSeconds--;

                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("[QuizTimerThread] Interrupted explicitly for session: " + sessionToken);
                    return;
                }
            }

            if (remainingSeconds <= 0 && running) {
                System.out.println("[QuizTimerThread] TIME EXPIRED for session: " + sessionToken + ". Triggering auto-submission callback!");
                if (onTimeoutCallback != null) {
                    onTimeoutCallback.accept(sessionToken);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("[QuizTimerThread] Timer safely cancelled via thread interruption for session: " + sessionToken);
            // Restore interrupted status
            Thread.currentThread().interrupt();
        } finally {
            this.running = false;
        }
    }

    public void cancelTimer() {
        this.running = false;
        this.interrupt();
    }

    public int getRemainingSeconds() {
        return Math.max(0, remainingSeconds);
    }

    public int getElapsedSeconds() {
        return totalSeconds - remainingSeconds;
    }

    public boolean isExpired() {
        return remainingSeconds <= 0;
    }
}

