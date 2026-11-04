package com.quiz.dao;

import com.quiz.exception.DatabaseException;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages JDBC Database Connectivity for the Online Quiz System.
 * Connects to MySQL / MariaDB database 'online_quiz_db'.
 * Automatically verifies schema, tables, and candidate profile/login history.
 * Falls back gracefully to thread-safe memory storage if MySQL is offline.
 */
public class DatabaseConnection {

    private static String dbUrl = "jdbc:mysql://localhost:3306/online_quiz_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static String dbHostUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPassword = "root";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private static boolean useEmbeddedFallback = true;
    private static boolean probed = false;

    static {
        loadConfiguration();
        probeDatabaseAvailability();
    }

    private static void loadConfiguration() {
        Properties props = new Properties();

        // 1. Try classpath resource
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
        }

        // 2. Try file system
        if (props.isEmpty()) {
            Path propPath = Paths.get("src/main/resources/db.properties");
            if (Files.exists(propPath)) {
                try (InputStream in = Files.newInputStream(propPath)) {
                    props.load(in);
                } catch (Exception ignored) {
                }
            }
        }

        // Read properties with fallback to env vars and defaults
        dbUrl = System.getProperty("db.url", 
                props.getProperty("db.url", 
                System.getenv().getOrDefault("DB_URL", dbUrl)));

        dbUser = System.getProperty("db.user", 
                props.getProperty("db.user", 
                System.getenv().getOrDefault("DB_USER", dbUser)));

        dbPassword = System.getProperty("db.password", 
                props.getProperty("db.password", 
                System.getenv().getOrDefault("DB_PASSWORD", dbPassword)));

        // Extract host url (before database name)
        int slashIdx = dbUrl.indexOf("3306/");
        if (slashIdx != -1) {
            dbHostUrl = dbUrl.substring(0, slashIdx + 5) + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }
    }

    public static synchronized void probeDatabaseAvailability() {
        if (probed) return;
        probed = true;

        try {
            Class.forName(DRIVER_CLASS);
            System.out.println("[DatabaseConnection] MySQL Driver " + DRIVER_CLASS + " loaded successfully.");

            // Run database initializer
            boolean initialized = DatabaseInitializer.initialize(dbHostUrl, dbUrl, dbUser, dbPassword);
            if (initialized) {
                useEmbeddedFallback = false;
                System.out.println("[DatabaseConnection] MySQL database online_quiz_db connected and active! (" + dbUrl + ")");
            } else {
                useEmbeddedFallback = true;
                System.out.println("[DatabaseConnection] MySQL service not responding. Switching to built-in embedded fallback mode.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("[DatabaseConnection] MySQL Driver not found on classpath. Switching to built-in embedded fallback mode.");
            useEmbeddedFallback = true;
        } catch (Exception e) {
            System.out.println("[DatabaseConnection] Notice: MySQL unavailable (" + e.getMessage() + "). Active in embedded mode.");
            useEmbeddedFallback = true;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (useEmbeddedFallback) {
            throw new SQLException("Database connection running in embedded fallback mode");
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public static boolean isUsingEmbeddedFallback() {
        return useEmbeddedFallback;
    }

    public static void setUseEmbeddedFallback(boolean fallback) {
        useEmbeddedFallback = fallback;
    }

    public static void setResolvedCredentials(String url, String user, String pass) {
        dbUrl = url;
        dbUser = user;
        dbPassword = pass;
        useEmbeddedFallback = false;
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }
}
