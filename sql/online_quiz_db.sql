-- ==============================================================
-- ONLINE QUIZ MANAGEMENT SYSTEM - DATABASE SCHEMA
-- Database: online_quiz_db
-- Supported RDBMS: MySQL 8.x / MariaDB
-- ==============================================================

CREATE DATABASE IF NOT EXISTS online_quiz_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE online_quiz_db;

-- --------------------------------------------------------------
-- Table 1: users
-- Stores Candidates (Students) and Administrators with full personal,
-- academic, and login tracking information.
-- Passwords stored as SHA-256 hashes:
-- 'admin123'   -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- 'student123' -> b860ab747e9b06828b8686f87498c56e300d8f07096e6ca4bcbf86cbcd3ebfd9
-- --------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
    
    -- Candidate Personal Information
    phone VARCHAR(20) DEFAULT NULL,
    gender VARCHAR(20) DEFAULT NULL,
    dob VARCHAR(30) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    city VARCHAR(100) DEFAULT NULL,
    state VARCHAR(100) DEFAULT NULL,
    pincode VARCHAR(20) DEFAULT NULL,
    
    -- Candidate Academic / Institutional Information
    institution VARCHAR(200) DEFAULT NULL,
    department VARCHAR(100) DEFAULT NULL,
    roll_number VARCHAR(50) DEFAULT NULL,
    degree VARCHAR(100) DEFAULT NULL,
    year_of_study VARCHAR(50) DEFAULT NULL,
    cgpa VARCHAR(20) DEFAULT NULL,
    bio TEXT DEFAULT NULL,
    
    -- Candidate Login Audit & Session Tracking
    last_login_at TIMESTAMP NULL DEFAULT NULL,
    last_login_ip VARCHAR(50) DEFAULT NULL,
    login_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------------
-- Table 2: candidate_login_history
-- Full audit trail of every candidate login event
-- --------------------------------------------------------------
CREATE TABLE IF NOT EXISTS candidate_login_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent TEXT,
    login_status VARCHAR(20) DEFAULT 'SUCCESS',
    CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------
-- Table 3: quizzes
-- Stores Quiz configuration, duration in minutes, total marks
-- --------------------------------------------------------------
CREATE TABLE IF NOT EXISTS quizzes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    duration INT NOT NULL COMMENT 'Duration in minutes',
    total_marks INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------------
-- Table 4: questions
-- Stores Multiple Choice Questions (MCQs) for each quiz
-- --------------------------------------------------------------
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_answer CHAR(1) NOT NULL COMMENT 'A, B, C, or D',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------
-- Table 5: quiz_attempts
-- Stores the summary of student quiz attempts and results
-- --------------------------------------------------------------
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    quiz_id INT NOT NULL,
    score INT NOT NULL,
    percentage DOUBLE(5, 2) NOT NULL,
    attempt_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attempts_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempts_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------
-- Table 6: student_answers
-- Stores individual answers submitted by student in each attempt
-- --------------------------------------------------------------
CREATE TABLE IF NOT EXISTS student_answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_answer CHAR(1) COMMENT 'A, B, C, D or NULL if unanswered',
    CONSTRAINT fk_answers_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ==============================================================
-- SAMPLE DATA INSERTION
-- ==============================================================

-- Insert Admin & Candidates (Students) with complete personal, academic & login tracking information
INSERT INTO users (id, name, email, password, role, phone, gender, dob, address, city, state, pincode, institution, department, roll_number, degree, year_of_study, cgpa, bio, last_login_at, last_login_ip, login_count, status) VALUES
(1, 'Dr. Sarah Connor (Admin)', 'admin@quiz.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', '+1-555-0199', 'Female', '1985-04-12', '100 Campus Drive, Admin Block 4', 'Boston', 'Massachusetts', '02115', 'MIT Department of Computing', 'Computer Science & Engineering', 'FAC-001', 'Ph.D. in Computer Science', 'Faculty', '4.00', 'Lead Exam Coordinator & Senior Academic Professor', NOW(), '127.0.0.1', 42, 'ACTIVE'),

(2, 'Alex Johnson', 'alex@student.com', 'b860ab747e9b06828b8686f87498c56e300d8f07096e6ca4bcbf86cbcd3ebfd9', 'STUDENT', '+1-555-0142', 'Male', '2003-08-24', '742 Evergreen Terrace, Apt 3B', 'San Jose', 'California', '95112', 'Silicon Valley Institute of Technology', 'Computer Science & Engineering', 'CS-2024-001', 'B.Tech in Computer Science', '3rd Year', '9.40', 'Passionate Java backend developer and competitive programmer.', NOW(), '127.0.0.1', 15, 'ACTIVE'),

(3, 'Sophia Chen', 'sophia@student.com', 'b860ab747e9b06828b8686f87498c56e300d8f07096e6ca4bcbf86cbcd3ebfd9', 'STUDENT', '+1-555-0178', 'Female', '2004-02-18', '248 Tech Innovation Blvd', 'Austin', 'Texas', '78701', 'Texas Institute of Advanced Science', 'Information Technology', 'IT-2024-045', 'B.S. in Information Systems', '2nd Year', '9.65', 'Full-stack web application developer, AI/ML explorer, and hackathon winner.', NOW(), '127.0.0.1', 21, 'ACTIVE'),

(4, 'Liam Davis', 'liam@student.com', 'b860ab747e9b06828b8686f87498c56e300d8f07096e6ca4bcbf86cbcd3ebfd9', 'STUDENT', '+1-555-0123', 'Male', '2002-11-30', '512 Lakeview Avenue', 'Chicago', 'Illinois', '60601', 'Midwest Engineering Academy', 'Software Engineering', 'SE-2024-089', 'B.Tech in Software Engineering', '4th Year', '8.80', 'Cloud computing researcher and distributed database enthusiast.', NOW(), '127.0.0.1', 9, 'ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Candidate Login History Seeds
INSERT INTO candidate_login_history (user_id, login_time, ip_address, user_agent, login_status) VALUES
(2, DATE_SUB(NOW(), INTERVAL 2 DAY), '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0', 'SUCCESS'),
(2, DATE_SUB(NOW(), INTERVAL 1 DAY), '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0', 'SUCCESS'),
(2, NOW(), '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0', 'SUCCESS'),
(3, DATE_SUB(NOW(), INTERVAL 3 DAY), '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Firefox/121.0', 'SUCCESS'),
(3, NOW(), '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Firefox/121.0', 'SUCCESS'),
(4, NOW(), '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', 'SUCCESS');

-- Insert Sample Quizzes
INSERT INTO quizzes (id, title, description, duration, total_marks) VALUES
(1, 'Java Core & OOP Fundamentals', 'Test your knowledge on Object-Oriented Programming, Inheritance, Polymorphism, and Encapsulation in Java.', 10, 50),
(2, 'Advanced Java: Collections & Multithreading', 'Evaluate your skills in Java Collections (List, Set, Map), Generics, Thread Lifecycle, and Concurrency utilities.', 15, 50),
(3, 'Database Concepts & JDBC Connectivity', 'Questions covering relational database normalization, SQL queries, PreparedStatement, and JDBC transaction management.', 10, 50)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- Insert Questions for Quiz 1: Java Core & OOP Fundamentals
INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
(1, 'Which of the following OOP principles emphasizes hiding internal state and requiring all interaction to be performed through an object''s methods?', 'Inheritance', 'Encapsulation', 'Polymorphism', 'Abstraction', 'B'),
(1, 'What is the default value of a boolean variable declared as an instance member in Java?', 'true', 'false', '0', 'null', 'B'),
(1, 'Which Java keyword is used by a class to inherit properties and behaviors from an abstract class?', 'implements', 'extends', 'inherits', 'instanceof', 'B'),
(1, 'Can an abstract class have constructors in Java?', 'No, abstract classes cannot have constructors', 'Yes, but only private constructors', 'Yes, invoked during subclass instantiation via super()', 'Only if the class has no abstract methods', 'C'),
(1, 'Which of the following is true about method overriding in Java?', 'Return type must be incompatible', 'Method name and argument list must match the superclass method', 'Private methods can be overridden', 'Static methods can be dynamically overridden at runtime', 'B');

-- Insert Questions for Quiz 2: Advanced Java: Collections & Multithreading
INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
(2, 'Which Java Collections interface allows unique elements only and does not maintain insertion order by default?', 'ArrayList', 'LinkedList', 'HashSet', 'LinkedHashSet', 'C'),
(2, 'What will happen when you call Thread.start() twice on the same Thread instance in Java?', 'Thread restarts from beginning', 'IllegalThreadStateException is thrown', 'A new thread is created automatically', 'The call is silently ignored', 'B'),
(2, 'Which concurrency interface provides a call() method that can return a value and throw checked exceptions?', 'Runnable', 'Callable', 'Thread', 'Executor', 'B'),
(2, 'What does the Java Stream API intermediate operation "filter()" do?', 'Transforms elements to a new type', 'Returns a stream of elements that match the given Predicate', 'Terminates the stream processing', 'Sorts elements naturally', 'B'),
(2, 'Which mechanism in Java provides mutual exclusion to prevent race conditions on shared objects?', 'volatile keyword', 'synchronized block or method', 'transient keyword', 'finalize method', 'B');

-- Insert Questions for Quiz 3: Database Concepts & JDBC Connectivity
INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
(3, 'Why is PreparedStatement preferred over Statement in JDBC?', 'It executes slower but supports more databases', 'It prevents SQL Injection and pre-compiles queries for efficiency', 'It requires no database connection', 'It does not require parameter binding', 'B'),
(3, 'Which JDBC method commits the current database transaction when auto-commit is disabled?', 'connection.setAutoCommit(true)', 'connection.save()', 'connection.commit()', 'statement.executeCommit()', 'C'),
(3, 'What type of exception is java.sql.SQLException in Java?', 'Unchecked Exception (RuntimeException)', 'Checked Exception requiring try-catch or throws', 'Error', 'JVM Fatal Error', 'B'),
(3, 'Which interface in java.sql is used to retrieve rows from an executed SQL SELECT query?', 'RowMapper', 'ResultSet', 'DataRecord', 'CursorSet', 'B'),
(3, 'Which clause in SQL is used to filter records after aggregation using GROUP BY?', 'WHERE', 'HAVING', 'ORDER BY', 'FILTER', 'B');

-- Insert Sample Quiz Attempts
INSERT INTO quiz_attempts (id, student_id, quiz_id, score, percentage, attempt_date) VALUES
(1, 2, 1, 50, 100.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 3, 1, 40, 80.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 4, 1, 30, 60.00, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(4, 2, 2, 40, 80.00, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(5, 3, 2, 50, 100.00, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(6, 4, 3, 40, 80.00, DATE_SUB(NOW(), INTERVAL 1 HOUR))
ON DUPLICATE KEY UPDATE score=VALUES(score);

-- Insert Student Answers for Attempt 1
INSERT INTO student_answers (attempt_id, question_id, selected_answer) VALUES
(1, 1, 'B'),
(1, 2, 'B'),
(1, 3, 'B'),
(1, 4, 'C'),
(1, 5, 'B')
ON DUPLICATE KEY UPDATE selected_answer=VALUES(selected_answer);
