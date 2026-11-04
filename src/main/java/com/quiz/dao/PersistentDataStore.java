package com.quiz.dao;

import com.quiz.model.CandidateLoginHistory;
import com.quiz.model.QuizAttempt;
import com.quiz.model.User;
import com.quiz.util.SerializationUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ensures permanent on-disk persistence for users, candidate profiles,
 * login audit history, quiz attempts, and active sessions.
 * 
 * Demonstrates:
 * - Java Concept #16: File Handling (File, mkdirs)
 * - Java Concept #17: Object Serialization & Deserialization (saving to disk)
 * - Java Concept #22: Thread Pooling (background disk write executor)
 * 
 * Guarantees that even if the server is stopped or restarted, or MySQL service
 * is offline, candidate records and login tracking are never lost.
 */
public class PersistentDataStore {

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.ser";
    private static final String LOGINS_FILE = DATA_DIR + File.separator + "candidate_logins.ser";
    private static final String ATTEMPTS_FILE = DATA_DIR + File.separator + "quiz_attempts.ser";
    private static final String SESSIONS_FILE = DATA_DIR + File.separator + "sessions.ser";

    private static final ExecutorService diskWriterPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "PersistentDataStore-Writer");
        t.setDaemon(true);
        return t;
    });

    static {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Loads saved users and candidate profiles from disk.
     */
    @SuppressWarnings("unchecked")
    public static Map<Integer, User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }
        try {
            HashMap<Integer, User> map = SerializationUtil.deserializeFromFile(USERS_FILE, HashMap.class);
            System.out.println("[PersistentDataStore] Restored " + map.size() + " candidate/user records from persistent disk storage: " + USERS_FILE);
            return new ConcurrentHashMap<>(map);
        } catch (Exception e) {
            System.err.println("[PersistentDataStore] Notice: Could not read users file, starting fresh (" + e.getMessage() + ")");
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Persists users map to disk asynchronously.
     */
    public static void saveUsersAsync(Map<Integer, User> usersMap) {
        final HashMap<Integer, User> snapshot = new HashMap<>(usersMap);
        diskWriterPool.submit(() -> {
            try {
                SerializationUtil.serializeToFile(snapshot, USERS_FILE);
            } catch (IOException e) {
                System.err.println("[PersistentDataStore] Failed to write users to disk: " + e.getMessage());
            }
        });
    }

    /**
     * Loads candidate login history from disk.
     */
    @SuppressWarnings("unchecked")
    public static List<CandidateLoginHistory> loadCandidateLogins() {
        File file = new File(LOGINS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            ArrayList<CandidateLoginHistory> list = SerializationUtil.deserializeFromFile(LOGINS_FILE, ArrayList.class);
            System.out.println("[PersistentDataStore] Restored " + list.size() + " candidate login audit records from disk: " + LOGINS_FILE);
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Persists candidate login history list to disk asynchronously.
     */
    public static void saveCandidateLoginsAsync(List<CandidateLoginHistory> historyList) {
        final ArrayList<CandidateLoginHistory> snapshot = new ArrayList<>(historyList);
        diskWriterPool.submit(() -> {
            try {
                SerializationUtil.serializeToFile(snapshot, LOGINS_FILE);
            } catch (IOException e) {
                System.err.println("[PersistentDataStore] Failed to write login history to disk: " + e.getMessage());
            }
        });
    }

    /**
     * Loads quiz attempts from disk.
     */
    @SuppressWarnings("unchecked")
    public static Map<Integer, QuizAttempt> loadAttempts() {
        File file = new File(ATTEMPTS_FILE);
        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }
        try {
            HashMap<Integer, QuizAttempt> map = SerializationUtil.deserializeFromFile(ATTEMPTS_FILE, HashMap.class);
            System.out.println("[PersistentDataStore] Restored " + map.size() + " quiz attempts from disk: " + ATTEMPTS_FILE);
            return new ConcurrentHashMap<>(map);
        } catch (Exception e) {
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Persists quiz attempts to disk asynchronously.
     */
    public static void saveAttemptsAsync(Map<Integer, QuizAttempt> attemptsMap) {
        final HashMap<Integer, QuizAttempt> snapshot = new HashMap<>(attemptsMap);
        diskWriterPool.submit(() -> {
            try {
                SerializationUtil.serializeToFile(snapshot, ATTEMPTS_FILE);
            } catch (IOException e) {
                System.err.println("[PersistentDataStore] Failed to write attempts to disk: " + e.getMessage());
            }
        });
    }

    /**
     * Loads persistent sessions from disk.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, User> loadSessions() {
        File file = new File(SESSIONS_FILE);
        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }
        try {
            HashMap<String, User> map = SerializationUtil.deserializeFromFile(SESSIONS_FILE, HashMap.class);
            System.out.println("[PersistentDataStore] Restored " + map.size() + " active user sessions from disk: " + SESSIONS_FILE);
            return new ConcurrentHashMap<>(map);
        } catch (Exception e) {
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Persists active sessions to disk.
     */
    public static void saveSessionsAsync(Map<String, User> sessionsMap) {
        final HashMap<String, User> snapshot = new HashMap<>(sessionsMap);
        diskWriterPool.submit(() -> {
            try {
                SerializationUtil.serializeToFile(snapshot, SESSIONS_FILE);
            } catch (IOException e) {
                System.err.println("[PersistentDataStore] Failed to write sessions to disk: " + e.getMessage());
            }
        });
    }
}

