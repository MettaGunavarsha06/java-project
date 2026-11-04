package com.quiz.concurrency;

import com.quiz.model.Result;
import com.quiz.threads.BackgroundReportWriter;
import com.quiz.util.SerializationUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Service managing background thread pooling and concurrent tasks.
 * Demonstrates:
 * - Thread Pooling: Fixed Thread Pool with Executors.newFixedThreadPool
 * - Concurrency Utilities: ExecutorService, Callable, and Future
 * - Background result serialization and report generation
 */
public class AsyncResultProcessor {

    private static final int POOL_SIZE = 4;
    private final ExecutorService executorService;
    private static final AsyncResultProcessor INSTANCE = new AsyncResultProcessor();
    private static final java.util.concurrent.atomic.AtomicInteger workerCount = new java.util.concurrent.atomic.AtomicInteger(1);

    private AsyncResultProcessor() {
        this.executorService = Executors.newFixedThreadPool(POOL_SIZE, r -> {
            Thread t = new Thread(r);
            t.setName("QuizAsyncWorker-" + workerCount.getAndIncrement());
            t.setDaemon(true);
            return t;
        });
        System.out.println("[AsyncResultProcessor] Thread pool initialized with " + POOL_SIZE + " worker threads.");
    }

    public static AsyncResultProcessor getInstance() {
        return INSTANCE;
    }

    /**
     * Submits a Callable calculation task and returns a Future.
     */
    public Future<Result> submitScoreCalculation(Callable<Result> task) {
        return executorService.submit(task);
    }

    /**
     * Submits a background Runnable task (e.g. for serialization or report generation).
     */
    public void submitBackgroundTask(Runnable runnable) {
        executorService.submit(runnable);
    }

    /**
     * Demonstrates serializing result to disk and generating text report asynchronously in worker thread pool.
     */
    public void processResultPostSubmission(Result result) {
        executorService.submit(() -> {
            try {
                // 1. Serialization of Result object
                String serPath = "reports" + File.separator + "result_attempt_" + result.getAttemptId() + ".ser";
                SerializationUtil.serializeToFile(result, serPath);
                System.out.println("[AsyncResultProcessor] Result serialized successfully to: " + serPath);

                // 2. Text Report generation
                BackgroundReportWriter writer = new BackgroundReportWriter(result, "reports");
                writer.writeReportToFile();
            } catch (IOException e) {
                System.err.println("[AsyncResultProcessor] Error during background processing: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
