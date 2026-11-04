package com.quiz.dao;

import com.quiz.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC Database Connectivity for the Online Quiz System.
 * Demonstrates:
 * - JDBC: DriverManager, Connection, SQLException handling
 * - Checked Exception handling
 * - Environment and Property-based configuration
 * - Graceful fallback to embedded in-memory persistence when MySQL service is offline,
 *   guaranteeing the application runs seamlessly out of the box in any environment.
 */
public class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/online_quiz_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private static boolean useEmbeddedFallback = false;
    private static boolean probed = false;

    static {
        probeDatabaseAvailability();
    }

    private static synchronized void probeDatabaseAvailability() {
        if (probed) return;
        probed = true;
        try {
            Class.forName(DRIVER_CLASS);
            String url = System.getProperty("db.url", System.getenv().getOrDefault("DB_URL", DEFAULT_URL));
            String user = System.getProperty("db.user", System.getenv().getOrDefault("DB_USER", DEFAULT_USER));
            String pass = System.getProperty("db.password", System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASS));

            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                System.out.println("[DatabaseConnection] Successfully connected to MySQL database: " + url);
                useEmbeddedFallback = false;
            }
        } catch (ClassNotFoundException e) {
            System.out.println("[DatabaseConnection] MySQL Driver not found on classpath. Switching to built-in embedded persistence mode.");
            useEmbeddedFallback = true;
        } catch (SQLException e) {
            System.out.println("[DatabaseConnection] MySQL service not reachable (" + e.getMessage() + "). Switching to built-in embedded persistence mode.");
            useEmbeddedFallback = true;
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getProperty("db.url", System.getenv().getOrDefault("DB_URL", DEFAULT_URL));
        String user = System.getProperty("db.user", System.getenv().getOrDefault("DB_USER", DEFAULT_USER));
        String pass = System.getProperty("db.password", System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASS));
        return DriverManager.getConnection(url, user, pass);
    }

    public static boolean isUsingEmbeddedFallback() {
        return useEmbeddedFallback;
    }

    public static void setUseEmbeddedFallback(boolean fallback) {
        useEmbeddedFallback = fallback;
    }
}

