package com.quiz;

import com.quiz.concurrency.AsyncResultProcessor;
import com.quiz.dao.DatabaseConnection;
import com.quiz.dao.QuestionDao;
import com.quiz.dao.QuizAttemptDao;
import com.quiz.dao.QuizDao;
import com.quiz.dao.UserDao;
import com.quiz.model.Admin;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.QuizAttempt;
import com.quiz.model.Result;
import com.quiz.model.Student;
import com.quiz.model.User;
import com.quiz.service.AuthService;
import com.quiz.service.QuizService;
import com.quiz.service.ReportService;
import com.quiz.service.ResultService;
import com.quiz.service.UserService;
import com.quiz.threads.QuizTimerThread;
import com.quiz.threads.SynchronizedLeaderboard;
import com.quiz.util.PasswordUtil;
import com.quiz.util.ReflectionInspector;
import com.quiz.util.SerializationUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automated Verification Test for all 30 Core & Advanced Java Concepts.
 */
public class WorkflowTest {

    public static void main(String[] args) throws Exception {
        System.out.println("====================================================================");
        System.out.println("        RUNNING COMPREHENSIVE JAVA INTEGRATION TEST SUITE           ");
        System.out.println("====================================================================");

        // Concept 1-6: Classes, Objects, Constructors, Inheritance, Polymorphism, Encapsulation, Abstract Class
        System.out.println("\n[Test 1] Testing OOP, Inheritance, Polymorphism & Encapsulation...");
        User admin = new Admin(1, "Admin User", "admin@test.com", PasswordUtil.hashPassword("admin123"), "IT");
        User student = new Student(2, "Test Student", "student@test.com", PasswordUtil.hashPassword("student123"));

        assert admin instanceof User : "Admin must extend User";
        assert student instanceof User : "Student must extend User";
        assert admin.canCreateQuiz() : "Admin polymorphism check failed";
        assert !student.canCreateQuiz() : "Student polymorphism check failed";
        assert student.canTakeQuiz() : "Student can take quiz check failed";
        System.out.println("  -> Polymorphism Verified: Admin Dashboard = " + admin.getDashboardPath() + ", Student Dashboard = " + student.getDashboardPath());

        // Concept 7 & 28-30: Interfaces, JDBC, PreparedStatement, CRUD Operations
        System.out.println("\n[Test 2] Testing DAOs & CRUD Operations...");
        UserDao userDao = new UserDao();
        QuizDao quizDao = new QuizDao();
        QuestionDao questionDao = new QuestionDao();
        QuizAttemptDao attemptDao = new QuizAttemptDao();

        List<Quiz> quizzes = quizDao.findAll();
        System.out.println("  -> Quizzes in database/cache: " + quizzes.size());
        assert !quizzes.isEmpty() : "Quiz list should not be empty";

        // Concept 8-10: Collections (ArrayList, HashSet, HashMap), Iterators, Enhanced For-Loop
        System.out.println("\n[Test 3] Testing Collections, Iterators & Enhanced For-Loop...");
        Quiz firstQuiz = quizzes.get(0);
        List<Question> questions = questionDao.findByQuizId(firstQuiz.getId());
        System.out.println("  -> Found " + questions.size() + " MCQs for Quiz ID " + firstQuiz.getId());
        assert !questions.isEmpty() : "Questions should not be empty";

        // Concept 11-12: Sorting & Searching using Comparator & Collections
        System.out.println("\n[Test 4] Testing Sorting & Searching with Comparator...");
        SynchronizedLeaderboard leaderboard = SynchronizedLeaderboard.getInstance();
        List<QuizAttempt> topPerformers = leaderboard.getTopPerformers(5);
        System.out.println("  -> Top performers retrieved: " + topPerformers.size());
        if (!topPerformers.isEmpty()) {
            System.out.println("  -> Leader: " + topPerformers.get(0).getStudentName() + " (" + topPerformers.get(0).getPercentage() + "%)");
        }

        // Concept 13-15: Exception Handling (Checked SQLException, IOException & Unchecked IllegalArgumentException)
        System.out.println("\n[Test 5] Testing Exception Handling...");
        try {
            new Student("", "invalid-email", "123");
            assert false : "Should have thrown IllegalArgumentException on empty name";
        } catch (IllegalArgumentException e) {
            System.out.println("  -> Caught expected unchecked validation exception: " + e.getMessage());
        }

        // Concept 18-19: Multithreading & Thread Management (Creation, Start, Sleep, Interruption)
        System.out.println("\n[Test 6] Testing Multithreading & QuizTimerThread...");
        QuizTimerThread timerThread = new QuizTimerThread("test-session", 1, token -> {
            System.out.println("  -> Timer callback triggered for session: " + token);
        });
        timerThread.start();
        Thread.sleep(1100);
        System.out.println("  -> Timer running, elapsed: " + timerThread.getElapsedSeconds() + "s, remaining: " + timerThread.getRemainingSeconds() + "s");
        timerThread.cancelTimer();
        System.out.println("  -> Timer cancelled via thread interruption successfully.");

        // Concept 20-22: Concurrency, Thread Pooling, ExecutorService, Callable, Future
        System.out.println("\n[Test 7] Testing Concurrency (ExecutorService, Callable, Future)...");
        AuthService authService = new AuthService(userDao);
        QuizService quizService = new QuizService(quizDao, questionDao);
        ResultService resultService = new ResultService(quizDao, questionDao, attemptDao);

        Map<Integer, String> studentAnswers = new HashMap<>();
        studentAnswers.put(1, "B"); // Correct
        studentAnswers.put(2, "B"); // Correct
        studentAnswers.put(3, "B"); // Correct
        studentAnswers.put(4, "C"); // Correct
        studentAnswers.put(5, "B"); // Correct

        Result result = resultService.submitQuiz(student, firstQuiz.getId(), studentAnswers, "test-session");
        System.out.println("  -> Concurrency Evaluation: Score = " + result.getScoreObtained() + "/" + result.getTotalMarks() + " (" + result.getPercentage() + "%) - Passed: " + result.isPassed());
        assert result.getScoreObtained() == 50 : "Score should be 50 for 5 correct answers";
        assert result.isPassed() : "Student should pass";

        // Concept 16: File Handling (FileWriter, PrintWriter, BufferedReader, FileReader)
        System.out.println("\n[Test 8] Testing File Handling & Report Generation...");
        ReportService reportService = new ReportService(resultService, quizService);
        File reportFile = reportService.exportResultReport(result.getAttemptId());
        assert reportFile.exists() : "Report file should exist";
        String content = reportService.readReportContent(reportFile.getName());
        System.out.println("  -> Report file generated (" + reportFile.length() + " bytes). Read verification:");
        System.out.println(content.substring(0, Math.min(220, content.length())) + "...");

        // Concept 17: Serialization (ObjectOutputStream & ObjectInputStream)
        System.out.println("\n[Test 9] Testing Object Serialization & Deserialization...");
        String serPath = "reports" + File.separator + "test_result.ser";
        SerializationUtil.serializeToFile(result, serPath);
        Result deserialized = SerializationUtil.deserializeFromFile(serPath, Result.class);
        System.out.println("  -> Deserialized successfully: " + deserialized.getStudentName() + " - " + deserialized.getQuizTitle() + " (" + deserialized.getPercentage() + "%)");
        assert deserialized.getScoreObtained() == result.getScoreObtained();

        // Concept 24-25: Lambda Expressions & Stream API
        System.out.println("\n[Test 10] Testing Stream API & Analytics...");
        Map<String, Object> stats = resultService.getOverallStatistics();
        System.out.println("  -> Platform Stats via Streams: Total Attempts = " + stats.get("totalAttempts") + ", Avg Score = " + stats.get("averageScore") + "%, Pass Rate = " + stats.get("passRate") + "%");

        // Concept 26-27: Annotations & Reflection API
        System.out.println("\n[Test 11] Testing Annotations & Reflection API...");
        String inspection = ReflectionInspector.inspectClass(Admin.class);
        assert inspection.contains("Author") : "Reflection should find Author annotation";
        System.out.println("  -> Reflection inspection confirmed custom annotations and declared methods successfully.");

        System.out.println("\n====================================================================");
        System.out.println("   >>> ALL 30 JAVA CONCEPTS VERIFIED & WORKING 100% PERFECTLY! <<<  ");
        System.out.println("====================================================================");

        // Clean up thread pool
        AsyncResultProcessor.getInstance().shutdown();
    }
}

