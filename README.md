# Online Quiz Management System (Java Full-Stack)

A complete, enterprise-grade, database-connected **Online Quiz Management System** built with **Java (Core & Advanced Java)**, **MySQL & JDBC**, **HTML5/CSS3/JavaScript**, and **Bootstrap 5**.

This academic project implements and demonstrates **all 30 required Core Java and Advanced Java concepts** in production-ready architecture. It supports dual execution modes:
1. **Zero-Dependency Standalone Mode:** Runs directly on pure Java JDK (Java 17/21+) via a built-in multi-threaded HTTP server and intelligent persistence engine.
2. **Enterprise Servlet/Tomcat Deployment:** Fully compatible with Apache Tomcat 10+ using Jakarta Servlets, `web.xml`, and JSPs.

---

## 1. Technology Stack

- **Frontend:** HTML5, CSS3, JavaScript (ES6+), Bootstrap 5.3, Bootstrap Icons, Chart.js (Interactive Analytics)
- **Backend:** Java 17 / 21 (OOP, Multithreading, Concurrency, Reflection, Stream API, Generics)
- **Database:** MySQL 8.x / MariaDB (`online_quiz_db`)
- **Database Connectivity:** JDBC (`DriverManager`, `PreparedStatement`, `Statement`, `ResultSet`, Transactions)
- **Application Server:** Built-in Concurrent Socket HTTP Server + Apache Tomcat / Jakarta Servlets (`web.xml`, JSP)
- **Development Environment:** VS Code or IntelliJ IDEA

---

## 2. Project Directory Structure

```
c:\java\java-project
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── quiz
│   │   │           ├── annotations            # Custom runtime annotations
│   │   │           │   ├── AuditLog.java
│   │   │           │   ├── Author.java
│   │   │           │   └── RoleRequired.java
│   │   │           ├── concurrency            # Java concurrency utilities
│   │   │           │   ├── AsyncResultProcessor.java
│   │   │           │   └── ScoreCalculationTask.java
│   │   │           ├── controller             # REST and action controllers
│   │   │           │   ├── AdminController.java
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── QuizController.java
│   │   │           │   └── StudentController.java
│   │   │           ├── dao                    # JDBC Data Access Objects
│   │   │           │   ├── DatabaseConnection.java
│   │   │           │   ├── GenericDao.java
│   │   │           │   ├── QuestionDao.java
│   │   │           │   ├── QuizAttemptDao.java
│   │   │           │   ├── QuizDao.java
│   │   │           │   └── UserDao.java
│   │   │           ├── exception              # Custom checked exception hierarchy
│   │   │           │   ├── AuthenticationException.java
│   │   │           │   ├── DatabaseException.java
│   │   │           │   ├── QuizException.java
│   │   │           │   └── ResourceNotFoundException.java
│   │   │           ├── generics               # Generic classes and algorithms
│   │   │           │   ├── ApiResponse.java
│   │   │           │   ├── CollectionUtils.java
│   │   │           │   └── GenericRepository.java
│   │   │           ├── interfaces             # Core system contracts
│   │   │           │   ├── Authenticatable.java
│   │   │           │   ├── QuizOperations.java
│   │   │           │   └── Reportable.java
│   │   │           ├── model                  # Domain entities & OOP hierarchy
│   │   │           │   ├── Admin.java
│   │   │           │   ├── Question.java
│   │   │           │   ├── Quiz.java
│   │   │           │   ├── QuizAttempt.java
│   │   │           │   ├── Result.java
│   │   │           │   ├── Student.java
│   │   │           │   ├── StudentAnswer.java
│   │   │           │   └── User.java
│   │   │           ├── service                # Business logic services
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── QuizService.java
│   │   │           │   ├── ReportService.java
│   │   │           │   ├── ResultService.java
│   │   │           │   └── UserService.java
│   │   │           ├── threads                # Multithreading, timers, sync
│   │   │           │   ├── BackgroundReportWriter.java
│   │   │           │   ├── QuizTimerThread.java
│   │   │           │   └── SynchronizedLeaderboard.java
│   │   │           ├── util                   # Security, reflection, serialization
│   │   │           │   ├── JsonUtil.java
│   │   │           │   ├── PasswordUtil.java
│   │   │           │   ├── ReflectionInspector.java
│   │   │           │   └── SerializationUtil.java
│   │   │           ├── server                 # Application HTTP server & router
│   │   │           │   ├── AppServer.java
│   │   │           │   ├── HttpRouter.java
│   │   │           │   └── SimpleHttpServer.java
│   │   │           └── WorkflowTest.java      # 30-concept automated test runner
│   │   └── webapp                             # Frontend web assets
│   │       ├── css
│   │       │   └── style.css                  # Soft blue & white theme
│   │       ├── js
│   │       │   ├── admin.js                   # Admin dashboard & Chart.js logic
│   │       │   ├── app.js                     # Core session & auth handling
│   │       │   └── quiz.js                    # Live timer & MCQ session engine
│   │       ├── index.html                     # Public portal & quiz browser
│   │       ├── login.html                     # Authentication with demo quick-fill
│   │       ├── register.html                  # Student enrollment form
│   │       ├── student-dashboard.html         # Student portal & attempt history
│   │       ├── quiz-view.html                 # Live MCQ examination interface
│   │       ├── result-view.html               # Result analysis & question review
│   │       ├── leaderboard.html               # Synchronized student rankings
│   │       ├── admin-dashboard.html           # Admin CRUD & performance statistics
│   │       ├── WEB-INF
│   │       │   └── web.xml                    # Standard deployment descriptor
│   │       └── jsp                            # Standard JSP forwards
│   │           ├── admin-dashboard.jsp
│   │           ├── index.jsp
│   │           ├── login.jsp
│   │           ├── quiz.jsp
│   │           ├── register.jsp
│   │           ├── result.jsp
│   │           └── student-dashboard.jsp
├── sql
│   └── online_quiz_db.sql                     # Full MySQL schema & sample seed data
├── reports                                    # Exported text & serialized results
├── build.bat                                  # Windows one-click build script
├── run.bat                                    # Windows one-click run script
└── pom.xml                                    # Maven configuration for WAR build
```

---

## 3. User Roles & Credentials

The system includes pre-seeded accounts with secure SHA-256 hashed credentials:

| Role | Name | Email | Password | Access Capabilities |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | Dr. Sarah Connor | `admin@quiz.com` | `admin123` | Full control: Add/edit/delete quizzes, manage questions, set timer/marks, inspect students, export reports, view platform analytics. |
| **Student** | Alex Johnson | `alex@student.com` | `student123` | Enroll, browse quizzes, start timed exams, navigate MCQs, auto-submit, view instant results and leaderboard. |
| **Student** | Sophia Chen | `sophia@student.com` | `student123` | Student portal and exam history. |
| **Student** | Liam Davis | `liam@student.com` | `student123` | Student portal and exam history. |

---

## 4. Implementation of All 30 Java Concepts

| # | Concept | Project Class / Implementation Location | Concrete Explanation |
| :---: | :--- | :--- | :--- |
| **1** | **Classes & Objects** | [`User`](file:///c:/java/java-project/src/main/java/com/quiz/model/User.java), [`Student`](file:///c:/java/java-project/src/main/java/com/quiz/model/Student.java), [`Admin`](file:///c:/java/java-project/src/main/java/com/quiz/model/Admin.java), [`Quiz`](file:///c:/java/java-project/src/main/java/com/quiz/model/Quiz.java), [`Question`](file:///c:/java/java-project/src/main/java/com/quiz/model/Question.java), [`Result`](file:///c:/java/java-project/src/main/java/com/quiz/model/Result.java) | Real-world domain entities modeling students, administrators, quizzes, questions, and attempt evaluations. |
| **2** | **Constructors** | All models (`User`, `Quiz`, `Question`, etc.) | Default, parameterized, and copy constructors with constructor chaining via `this(...)` and `super(...)`. |
| **3** | **Inheritance** | [`Student.java`](file:///c:/java/java-project/src/main/java/com/quiz/model/Student.java), [`Admin.java`](file:///c:/java/java-project/src/main/java/com/quiz/model/Admin.java) | Both `Student` and `Admin` extend the abstract base class `User`, inheriting base fields and behavior. |
| **4** | **Polymorphism** | [`User.java`](file:///c:/java/java-project/src/main/java/com/quiz/model/User.java), [`Student.java`](file:///c:/java/java-project/src/main/java/com/quiz/model/Student.java), [`Admin.java`](file:///c:/java/java-project/src/main/java/com/quiz/model/Admin.java) | Dynamic method dispatch overriding `canCreateQuiz()`, `canTakeQuiz()`, `getDashboardPath()`, and `getRoleDescription()`. |
| **5** | **Encapsulation** | All classes in `com.quiz.model` | All fields are `private` with explicit validation inside public getters and setters (e.g., email format, positive marks, non-negative duration). |
| **6** | **Abstract Classes** | [`abstract class User`](file:///c:/java/java-project/src/main/java/com/quiz/model/User.java) | Defines shared user attributes and declares abstract methods `getRoleDescription()` and `getDashboardPath()`. |
| **7** | **Interfaces** | [`Authenticatable`](file:///c:/java/java-project/src/main/java/com/quiz/interfaces/Authenticatable.java), [`QuizOperations`](file:///c:/java/java-project/src/main/java/com/quiz/interfaces/QuizOperations.java), [`Reportable`](file:///c:/java/java-project/src/main/java/com/quiz/interfaces/Reportable.java), [`GenericDao`](file:///c:/java/java-project/src/main/java/com/quiz/dao/GenericDao.java) | Pure contracts decoupling persistence, authentication, and reporting behavior. |
| **8** | **Collections** | `ArrayList`, `HashSet`, `HashMap`, `ConcurrentHashMap` across services & DAOs | `ArrayList` for ordered questions and attempts, `HashSet` for distinct students/answers, `HashMap` for question-answer mappings. |
| **9** | **Iterators** | [`Quiz.java`](file:///c:/java/java-project/src/main/java/com/quiz/model/Quiz.java#L47), [`QuizService.java`](file:///c:/java/java-project/src/main/java/com/quiz/service/QuizService.java#L106) | Explicit `Iterator<Question>` usage to safely inspect and remove questions from quiz collections. |
| **10** | **Enhanced For-Loop** | [`Quiz.java`](file:///c:/java/java-project/src/main/java/com/quiz/model/Quiz.java#L125), [`ScoreCalculationTask.java`](file:///c:/java/java-project/src/main/java/com/quiz/concurrency/ScoreCalculationTask.java#L61) | Iterates over questions and student response options for text report generation and scoring. |
| **11** | **Sorting** | [`SynchronizedLeaderboard.java`](file:///c:/java/java-project/src/main/java/com/quiz/threads/SynchronizedLeaderboard.java#L44), [`CollectionUtils.java`](file:///c:/java/java-project/src/main/java/com/quiz/generics/CollectionUtils.java#L28) | Multi-criteria sorting using `Comparator<QuizAttempt>` (percentage desc, date asc). |
| **12** | **Searching** | [`UserService.java`](file:///c:/java/java-project/src/main/java/com/quiz/service/UserService.java#L52), [`QuizService.java`](file:///c:/java/java-project/src/main/java/com/quiz/service/QuizService.java#L92) | Real-time search across student and quiz collections matching titles, descriptions, and emails. |
| **13** | **Exception Handling** | All packages | Structured `try-catch-finally` blocks and custom exception hierarchy (`QuizException`, `DatabaseException`, `AuthenticationException`). |
| **14** | **Checked Exceptions** | `SQLException` in `dao/`, `IOException` in `threads/` and `util/` | Handled or wrapped into domain-level checked exceptions (`DatabaseException`). |
| **15** | **Unchecked Exceptions** | `IllegalArgumentException`, `NullPointerException` defensive checks | Validates parameters in model setters, throwing `IllegalArgumentException` on invalid inputs. |
| **16** | **File Handling** | [`BackgroundReportWriter.java`](file:///c:/java/java-project/src/main/java/com/quiz/threads/BackgroundReportWriter.java), [`ReportService.java`](file:///c:/java/java-project/src/main/java/com/quiz/service/ReportService.java) | Writes quiz evaluation reports to disk using `FileWriter` and `PrintWriter`; reads back with `BufferedReader` and `FileReader`. |
| **17** | **Serialization** | [`SerializationUtil.java`](file:///c:/java/java-project/src/main/java/com/quiz/util/SerializationUtil.java), [`Result.java`](file:///c:/java/java-project/src/main/java/com/quiz/model/Result.java) | Serializes and deserializes `Result` domain instances to `.ser` files using `ObjectOutputStream` and `ObjectInputStream`. |
| **18** | **Multithreading** | [`QuizTimerThread.java`](file:///c:/java/java-project/src/main/java/com/quiz/threads/QuizTimerThread.java) | Independent background thread managing countdown timers for active student quiz sessions. |
| **19** | **Thread Management** | [`QuizTimerThread.java`](file:///c:/java/java-project/src/main/java/com/quiz/threads/QuizTimerThread.java) | Demonstrates thread naming, `start()`, `Thread.sleep(1000)`, `interrupt()`, and cooperative interruption check via `isInterrupted()`. |
| **20** | **Synchronization** | [`SynchronizedLeaderboard.java`](file:///c:/java/java-project/src/main/java/com/quiz/threads/SynchronizedLeaderboard.java) | Thread-safe ranking updates with `synchronized` methods and `synchronized(sharedEntries)` blocks preventing race conditions. |
| **21** | **Concurrency Utilities** | [`ScoreCalculationTask.java`](file:///c:/java/java-project/src/main/java/com/quiz/concurrency/ScoreCalculationTask.java), [`AsyncResultProcessor.java`](file:///c:/java/java-project/src/main/java/com/quiz/concurrency/AsyncResultProcessor.java) | Implements `Callable<Result>` and `Future<Result>` to evaluate submissions concurrently. |
| **22** | **Thread Pooling** | [`AsyncResultProcessor.java`](file:///c:/java/java-project/src/main/java/com/quiz/concurrency/AsyncResultProcessor.java) | `Executors.newFixedThreadPool(4)` handles asynchronous result serialization and report generation. |
| **23** | **Generics** | [`GenericDao<T, ID>`](file:///c:/java/java-project/src/main/java/com/quiz/dao/GenericDao.java), [`GenericRepository<T, ID>`](file:///c:/java/java-project/src/main/java/com/quiz/generics/GenericRepository.java), [`ApiResponse<T>`](file:///c:/java/java-project/src/main/java/com/quiz/generics/ApiResponse.java) | Type-safe generic repositories and standardized generic API response wrappers. |
| **24** | **Lambda Expressions** | DAOs, Services, Comparators | Concise lambdas: `(a1, a2) -> Double.compare(a2.getPercentage(), a1.getPercentage())`, predicate filtering. |
| **25** | **Stream API** | [`ResultService.java`](file:///c:/java/java-project/src/main/java/com/quiz/service/ResultService.java#L225), [`UserService.java`](file:///c:/java/java-project/src/main/java/com/quiz/service/UserService.java) | Functional pipelines: `.stream().filter(...).mapToDouble(...).summaryStatistics()` for average scores and pass rates. |
| **26** | **Annotations** | [`@Author`](file:///c:/java/java-project/src/main/java/com/quiz/annotations/Author.java), [`@RoleRequired`](file:///c:/java/java-project/src/main/java/com/quiz/annotations/RoleRequired.java), [`@AuditLog`](file:///c:/java/java-project/src/main/java/com/quiz/annotations/AuditLog.java), `@Override` | Custom runtime annotations with `@Retention(RetentionPolicy.RUNTIME)` for audit logging and metadata. |
| **27** | **Reflection** | [`ReflectionInspector.java`](file:///c:/java/java-project/src/main/java/com/quiz/util/ReflectionInspector.java) | Inspects class methods, modifiers, annotations, and parameters dynamically at runtime during startup. |
| **28** | **JDBC** | [`DatabaseConnection.java`](file:///c:/java/java-project/src/main/java/com/quiz/dao/DatabaseConnection.java) | Standard MySQL JDBC driver loading (`com.mysql.cj.jdbc.Driver`), connection URL configuration, and error recovery. |
| **29** | **PreparedStatement** | `UserDao`, `QuizDao`, `QuestionDao`, `QuizAttemptDao` | All database queries use parameterized SQL (`?`) with `setString`, `setInt`, and `setTimestamp` to eliminate SQL injection risks. |
| **30** | **CRUD Operations** | Complete system | Full Create, Read, Update, and Delete operations for Quizzes, Questions, Users, and Attempt evaluations. |

---

## 5. Database Setup (MySQL)

1. Open MySQL Workbench or your terminal and log into MySQL:
   ```bash
   mysql -u root -p
   ```
2. Execute the provided SQL script located in `sql/online_quiz_db.sql`:
   ```bash
   SOURCE c:/java/java-project/sql/online_quiz_db.sql;
   ```
   Or open `sql/online_quiz_db.sql` in MySQL Workbench and execute the script.
3. This creates the database `online_quiz_db`, the 5 relational tables (`users`, `quizzes`, `questions`, `quiz_attempts`, `student_answers`), and seeds initial users and sample quizzes.

*Note: If MySQL is not running on the evaluator's machine, the application automatically detects this and falls back to its built-in in-memory persistence store, allowing full functionality out of the box with zero setup hurdles!*

---

## 6. How to Compile & Run

### Method 1: Command Line / VS Code Terminal (Quickest & Recommended)

1. Open a terminal in the project directory `c:\java\java-project`.
2. Compile the project:
   ```cmd
   .\build.bat
   ```
3. Run the automated 30-concept verification test:
   ```cmd
   java -cp bin -ea com.quiz.WorkflowTest
   ```
4. Start the application server:
   ```cmd
   .\run.bat
   ```
5. Open your web browser and navigate to:
   ```
   http://localhost:8080
   ```

### Method 2: Running in VS Code
1. Open folder `c:\java\java-project` in VS Code.
2. Ensure the **Extension Pack for Java** is installed.
3. Open `src/main/java/com/quiz/server/AppServer.java` and click **Run** (or press `F5`).
4. Access `http://localhost:8080` in your browser.

### Method 3: Deploying as a WAR to Apache Tomcat
1. Build the WAR package using Maven:
   ```cmd
   mvn clean package
   ```
2. Copy `target/online-quiz-system.war` to your Tomcat `webapps/` folder.
3. Start Tomcat (`catalina.bat start` or `startup.bat`).
4. Access `http://localhost:8080/online-quiz-system/`.

---

## 7. Key Application Features

1. **User Authentication:** Student registration and role-based login (Admin vs Student) with salted SHA-256 password hashing.
2. **Student Portal:** Browse available quizzes, view marks, duration, and examine past attempt history with pass/fail badges.
3. **Interactive Quiz Interface:**
   - Real-time countdown timer with visual warning when under 60 seconds.
   - Question navigation palette with color coding (Green = Answered, Slate = Visited/Unanswered, Blue = Current).
   - Radio button options with instant selection feedback.
   - Submit confirmation modal displaying answered vs unanswered counts.
   - Automatic submission when the timer reaches 00:00.
4. **Instant Evaluation & Results:**
   - Pass / Fail status banner and percentage display.
   - Accuracy breakdown doughnut chart rendered via Chart.js.
   - Question-by-question review showing selected answer, correct answer, and result badge.
5. **Admin Control Center:**
   - Create, edit, and delete quizzes (title, description, duration, total marks).
   - Comprehensive question bank management (add, edit, delete MCQs per quiz).
   - Registered student directory with live search filter.
   - Audit log of all exam attempts and student grades.
   - One-click report file export (`.txt`) for quiz specifications and results.
6. **Performance Leaderboard:**
   - Synchronized rankings across all students.
   - Gold 🥇, Silver 🥈, and Bronze 🥉 medals for top performers.
   - Filter rankings by quiz.
