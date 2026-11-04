package com.quiz.server;

import com.quiz.controller.AdminController;
import com.quiz.controller.AuthController;
import com.quiz.controller.QuizController;
import com.quiz.controller.StudentController;
import com.quiz.dao.DatabaseConnection;
import com.quiz.dao.QuestionDao;
import com.quiz.dao.QuizAttemptDao;
import com.quiz.dao.QuizDao;
import com.quiz.dao.UserDao;
import com.quiz.model.Admin;
import com.quiz.model.Student;
import com.quiz.service.AuthService;
import com.quiz.service.QuizService;
import com.quiz.service.ReportService;
import com.quiz.service.ResultService;
import com.quiz.service.UserService;
import com.quiz.util.ReflectionInspector;

import java.io.File;
import java.io.IOException;

/**
 * Main Application Launcher for Online Quiz Management System.
 * Demonstrates:
 * - Bootstrapping and Dependency Injection across DAOs, Services, and Controllers
 * - Java Reflection API inspection at startup
 * - Standalone embedded HTTP Server using standard Java ServerSocket and ThreadPool
 * - Zero external dependencies required to compile and run!
 */
public class AppServer {

    private static final int DEFAULT_PORT = 8080;
    private static SimpleHttpServer server;

    public static void main(String[] args) {
        System.out.println("====================================================================");
        System.out.println("       STARTING ONLINE QUIZ MANAGEMENT SYSTEM (JAVA FULL-STACK)      ");
        System.out.println("====================================================================");

        // 1. Demonstrate Java Reflection API at startup
        System.out.println("[Reflection API] Inspecting domain and controller classes via Reflection...");
        System.out.println(ReflectionInspector.inspectClass(Student.class));
        System.out.println(ReflectionInspector.inspectClass(Admin.class));
        System.out.println("[Reflection API] Discovered audited controller methods:");
        ReflectionInspector.getAuditedMethods(AdminController.class).forEach(m -> System.out.println("  * " + m));
        System.out.println("--------------------------------------------------------------------");

        // 2. Initialize Data Access Layer
        UserDao userDao = new UserDao();
        QuizDao quizDao = new QuizDao();
        QuestionDao questionDao = new QuestionDao();
        QuizAttemptDao quizAttemptDao = new QuizAttemptDao();

        // 3. Initialize Business Services
        AuthService authService = new AuthService(userDao);
        UserService userService = new UserService(userDao, quizAttemptDao);
        QuizService quizService = new QuizService(quizDao, questionDao);
        ResultService resultService = new ResultService(quizDao, questionDao, quizAttemptDao);
        ReportService reportService = new ReportService(resultService, quizService);

        // 4. Initialize Controllers
        AuthController authController = new AuthController(authService);
        QuizController quizController = new QuizController(quizService);
        StudentController studentController = new StudentController(quizService, resultService);
        AdminController adminController = new AdminController(quizService, userService, resultService, reportService);

        // 5. Determine Webapp Path
        String webappDir = "src" + File.separator + "main" + File.separator + "webapp";
        File dir = new File(webappDir);
        if (!dir.exists()) {
            webappDir = "webapp"; // fallback for flat distributions
        }

        // 6. Create Router and Start Embedded HTTP Server
        HttpRouter router = new HttpRouter(authController, quizController, studentController, adminController,
                authService, reportService, webappDir);

        int port = getPort();
        try {
            server = new SimpleHttpServer(port, router, 16);
            server.start();

            System.out.println("\n>>> APPLICATION STARTED SUCCESSFULLY! <<<");
            System.out.println("Access the web application at:");
            System.out.println("  Home & Quizzes   : http://localhost:" + port);
            System.out.println("  User Login       : http://localhost:" + port + "/login.html");
            System.out.println("  Student Register : http://localhost:" + port + "/register.html");
            System.out.println("  Leaderboard      : http://localhost:" + port + "/leaderboard.html");
            System.out.println("\nSample Login Credentials:");
            System.out.println("  Admin Account   : admin@quiz.com  / Password: admin123");
            System.out.println("  Student Account : alex@student.com / Password: student123");
            System.out.println("  Student Account : sophia@student.com / Password: student123");
            System.out.println("====================================================================");
            System.out.println("Database status: " + (DatabaseConnection.isUsingEmbeddedFallback() 
                    ? "In-Memory Embedded Persistence Active (MySQL offline/driver optional)" 
                    : "Connected to MySQL database 'online_quiz_db'"));
            System.out.println("Press Ctrl+C in terminal to stop server.");
            System.out.println("====================================================================\n");

        } catch (IOException e) {
            System.err.println("Fatal: Failed to start HTTP server on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[AppServer] Shutting down Online Quiz Server...");
            if (server != null) {
                server.stop();
            }
        }));

        // Keep main thread alive so server remains active
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("[AppServer] Server interrupted.");
        }
    }

    private static int getPort() {
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try { return Integer.parseInt(envPort); } catch (NumberFormatException ignored) {}
        }
        String sysPort = System.getProperty("server.port");
        if (sysPort != null) {
            try { return Integer.parseInt(sysPort); } catch (NumberFormatException ignored) {}
        }
        return DEFAULT_PORT;
    }
}
