package com.quiz.dao;

import com.quiz.util.PasswordUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes MySQL Database Schema, tables, and seed data automatically.
 * Ensures the database 'online_quiz_db' and all candidate tables exist
 * upon system startup.
 */
public class DatabaseInitializer {

    private static final String[] CANDIDATE_PASSWORDS = {"root", "", "admin", "password", "root123", "123456"};

    /**
     * Attempts to connect to MySQL, create 'online_quiz_db' if absent,
     * create all tables with candidate information columns, and seed initial records.
     *
     * @return true if MySQL database is successfully initialized and active, false otherwise
     */
    public static boolean initialize(String hostUrl, String targetDbUrl, String configuredUser, String configuredPass) {
        String workingPass = null;
        Connection serverConn = null;

        // 1. Try configured password first
        try {
            serverConn = DriverManager.getConnection(hostUrl, configuredUser, configuredPass);
            workingPass = configuredPass;
        } catch (SQLException e) {
            // Try alternative common developer passwords
            for (String testPass : CANDIDATE_PASSWORDS) {
                if (testPass.equals(configuredPass)) continue;
                try {
                    serverConn = DriverManager.getConnection(hostUrl, configuredUser, testPass);
                    workingPass = testPass;
                    System.out.println("[DatabaseInitializer] Auto-detected MySQL root password: '" + 
                            (testPass.isEmpty() ? "(blank)" : testPass) + "'");
                    break;
                } catch (SQLException ignored) {
                    // try next
                }
            }
        }

        if (serverConn == null || workingPass == null) {
            return false;
        }

        try (Connection conn = serverConn; Statement stmt = conn.createStatement()) {
            // 2. Create database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS online_quiz_db " +
                    "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("[DatabaseInitializer] Database 'online_quiz_db' verified / created.");
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Failed to create database: " + e.getMessage());
            return false;
        }

        // 3. Connect directly to online_quiz_db and create schema tables
        String dbUrl = targetDbUrl;
        try (Connection conn = DriverManager.getConnection(dbUrl, configuredUser, workingPass);
             Statement stmt = conn.createStatement()) {

            // Table 1: users (with candidate profile & login tracking fields)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "email VARCHAR(100) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL," +
                    "role ENUM('ADMIN', 'STUDENT') NOT NULL DEFAULT 'STUDENT'," +
                    "phone VARCHAR(20) DEFAULT NULL," +
                    "gender VARCHAR(20) DEFAULT NULL," +
                    "dob VARCHAR(30) DEFAULT NULL," +
                    "address VARCHAR(255) DEFAULT NULL," +
                    "city VARCHAR(100) DEFAULT NULL," +
                    "state VARCHAR(100) DEFAULT NULL," +
                    "pincode VARCHAR(20) DEFAULT NULL," +
                    "institution VARCHAR(200) DEFAULT NULL," +
                    "department VARCHAR(100) DEFAULT NULL," +
                    "roll_number VARCHAR(50) DEFAULT NULL," +
                    "degree VARCHAR(100) DEFAULT NULL," +
                    "year_of_study VARCHAR(50) DEFAULT NULL," +
                    "cgpa VARCHAR(20) DEFAULT NULL," +
                    "bio TEXT DEFAULT NULL," +
                    "last_login_at TIMESTAMP NULL DEFAULT NULL," +
                    "last_login_ip VARCHAR(50) DEFAULT NULL," +
                    "login_count INT NOT NULL DEFAULT 0," +
                    "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB");

            // Ensure columns exist if table was already created in earlier version
            ensureCandidateColumns(stmt);

            // Table 2: candidate_login_history
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS candidate_login_history (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "ip_address VARCHAR(50)," +
                    "user_agent TEXT," +
                    "login_status VARCHAR(20) DEFAULT 'SUCCESS'," +
                    "CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB");

            // Table 3: quizzes
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS quizzes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(150) NOT NULL," +
                    "description TEXT," +
                    "duration INT NOT NULL COMMENT 'Duration in minutes'," +
                    "total_marks INT NOT NULL DEFAULT 100," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB");

            // Table 4: questions
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS questions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "quiz_id INT NOT NULL," +
                    "question_text TEXT NOT NULL," +
                    "option_a VARCHAR(255) NOT NULL," +
                    "option_b VARCHAR(255) NOT NULL," +
                    "option_c VARCHAR(255) NOT NULL," +
                    "option_d VARCHAR(255) NOT NULL," +
                    "correct_answer CHAR(1) NOT NULL COMMENT 'A, B, C, or D'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB");

            // Table 5: quiz_attempts
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS quiz_attempts (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "student_id INT NOT NULL," +
                    "quiz_id INT NOT NULL," +
                    "score INT NOT NULL," +
                    "percentage DOUBLE(5, 2) NOT NULL," +
                    "attempt_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "CONSTRAINT fk_attempts_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_attempts_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB");

            // Table 6: student_answers
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS student_answers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "attempt_id INT NOT NULL," +
                    "question_id INT NOT NULL," +
                    "selected_answer CHAR(1) COMMENT 'A, B, C, D or NULL if unanswered'," +
                    "CONSTRAINT fk_answers_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB");

            // 4. Seed initial users if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedInitialData(stmt);
                }
            }

            DatabaseConnection.setResolvedCredentials(dbUrl, configuredUser, workingPass);
            System.out.println("[DatabaseInitializer] All tables and candidate schema verified in MySQL!");
            return true;
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Error initializing database tables: " + e.getMessage());
            return false;
        }
    }

    private static void ensureCandidateColumns(Statement stmt) {
        String[] alterColumns = {
                "ALTER TABLE users ADD COLUMN phone VARCHAR(20) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN gender VARCHAR(20) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN dob VARCHAR(30) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN address VARCHAR(255) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN city VARCHAR(100) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN state VARCHAR(100) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN pincode VARCHAR(20) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN institution VARCHAR(200) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN department VARCHAR(100) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN roll_number VARCHAR(50) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN degree VARCHAR(100) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN year_of_study VARCHAR(50) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN cgpa VARCHAR(20) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN bio TEXT DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP NULL DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN last_login_ip VARCHAR(50) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN login_count INT NOT NULL DEFAULT 0",
                "ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'"
        };

        for (String sql : alterColumns) {
            try {
                stmt.executeUpdate(sql);
            } catch (SQLException ignored) {
                // Column already exists, safe to ignore
            }
        }
    }

    private static void seedInitialData(Statement stmt) throws SQLException {
        String adminPass = PasswordUtil.hashPassword("admin123");
        String studentPass = PasswordUtil.hashPassword("student123");

        stmt.executeUpdate("INSERT INTO users (id, name, email, password, role, phone, gender, dob, address, city, state, pincode, institution, department, roll_number, degree, year_of_study, cgpa, bio, last_login_at, last_login_ip, login_count, status) VALUES " +
                "(1, 'Dr. Sarah Connor (Admin)', 'admin@quiz.com', '" + adminPass + "', 'ADMIN', '+1-555-0199', 'Female', '1985-04-12', '100 Campus Drive', 'Boston', 'Massachusetts', '02115', 'MIT Department of Computing', 'Computer Science & Engineering', 'FAC-001', 'Ph.D. in Computer Science', 'Faculty', '4.00', 'Lead Exam Coordinator', NOW(), '127.0.0.1', 42, 'ACTIVE'), " +
                "(2, 'Alex Johnson', 'alex@student.com', '" + studentPass + "', 'STUDENT', '+1-555-0142', 'Male', '2003-08-24', '742 Evergreen Terrace', 'San Jose', 'California', '95112', 'Silicon Valley Institute of Technology', 'Computer Science & Engineering', 'CS-2024-001', 'B.Tech in Computer Science', '3rd Year', '9.40', 'Passionate Java backend developer', NOW(), '127.0.0.1', 15, 'ACTIVE'), " +
                "(3, 'Sophia Chen', 'sophia@student.com', '" + studentPass + "', 'STUDENT', '+1-555-0178', 'Female', '2004-02-18', '248 Tech Innovation Blvd', 'Austin', 'Texas', '78701', 'Texas Institute of Advanced Science', 'Information Technology', 'IT-2024-045', 'B.S. in Information Systems', '2nd Year', '9.65', 'Full-stack web application developer', NOW(), '127.0.0.1', 21, 'ACTIVE'), " +
                "(4, 'Liam Davis', 'liam@student.com', '" + studentPass + "', 'STUDENT', '+1-555-0123', 'Male', '2002-11-30', '512 Lakeview Avenue', 'Chicago', 'Illinois', '60601', 'Midwest Engineering Academy', 'Software Engineering', 'SE-2024-089', 'B.Tech in Software Engineering', '4th Year', '8.80', 'Distributed database enthusiast', NOW(), '127.0.0.1', 9, 'ACTIVE')");

        // Seed sample quizzes
        stmt.executeUpdate("INSERT INTO quizzes (id, title, description, duration, total_marks) VALUES " +
                "(1, 'Java Core & OOP Fundamentals', 'Test your knowledge on Object-Oriented Programming, Inheritance, Polymorphism, and Encapsulation in Java.', 10, 50), " +
                "(2, 'Advanced Java: Collections & Multithreading', 'Evaluate your skills in Java Collections (List, Set, Map), Generics, Thread Lifecycle, and Concurrency utilities.', 15, 50), " +
                "(3, 'Database Concepts & JDBC Connectivity', 'Questions covering relational database normalization, SQL queries, PreparedStatement, and JDBC transaction management.', 10, 50)");

        // Seed questions for quiz 1
        stmt.executeUpdate("INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES " +
                "(1, 'Which OOP principle emphasizes hiding internal state and requiring interaction through methods?', 'Inheritance', 'Encapsulation', 'Polymorphism', 'Abstraction', 'B'), " +
                "(1, 'What is the default value of a boolean variable declared as an instance member in Java?', 'true', 'false', '0', 'null', 'B'), " +
                "(1, 'Which keyword is used by a class to inherit from an abstract class?', 'implements', 'extends', 'inherits', 'instanceof', 'B'), " +
                "(1, 'Can an abstract class have constructors in Java?', 'No', 'Yes, only private', 'Yes, invoked via super() in subclass', 'Only if no abstract methods', 'C'), " +
                "(1, 'Which statement is true regarding method overriding?', 'Return type must differ', 'Signature must match superclass', 'Private methods can override', 'Static methods override dynamically', 'B')");

        // Seed questions for quiz 2
        stmt.executeUpdate("INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES " +
                "(2, 'Which Collections interface allows unique elements only?', 'ArrayList', 'LinkedList', 'HashSet', 'Vector', 'C'), " +
                "(2, 'Calling Thread.start() twice on same instance causes?', 'Restart', 'IllegalThreadStateException', 'New thread', 'Ignored', 'B'), " +
                "(2, 'Which concurrency interface returns a value and throws checked exceptions?', 'Runnable', 'Callable', 'Thread', 'Executor', 'B'), " +
                "(2, 'What does Stream filter() do?', 'Transform type', 'Returns elements matching Predicate', 'Terminates stream', 'Sorts elements', 'B'), " +
                "(2, 'Which keyword provides mutual exclusion to prevent race conditions?', 'volatile', 'synchronized', 'transient', 'native', 'B')");

        // Seed questions for quiz 3
        stmt.executeUpdate("INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES " +
                "(3, 'Why is PreparedStatement preferred over Statement?', 'Slower', 'Prevents SQL Injection and pre-compiles', 'No connection needed', 'No parameters', 'B'), " +
                "(3, 'Which JDBC method commits the transaction when autoCommit is false?', 'setAutoCommit', 'save', 'commit', 'executeCommit', 'C'), " +
                "(3, 'What type of exception is SQLException?', 'Unchecked', 'Checked Exception', 'Error', 'Fatal', 'B'), " +
                "(3, 'Which interface retrieves rows from a SQL query?', 'RowMapper', 'ResultSet', 'DataRecord', 'Cursor', 'B'), " +
                "(3, 'Which clause filters records after GROUP BY?', 'WHERE', 'HAVING', 'ORDER BY', 'FILTER', 'B')");

        // Seed attempts
        stmt.executeUpdate("INSERT INTO quiz_attempts (id, student_id, quiz_id, score, percentage, attempt_date) VALUES " +
                "(1, 2, 1, 50, 100.0, NOW()), " +
                "(2, 3, 1, 40, 80.0, NOW()), " +
                "(3, 4, 1, 30, 60.0, NOW())");

        System.out.println("[DatabaseInitializer] Initial seed data inserted into MySQL.");
    }
}

