package com.quiz.server;

import com.quiz.controller.AdminController;
import com.quiz.controller.AuthController;
import com.quiz.controller.QuizController;
import com.quiz.controller.StudentController;
import com.quiz.generics.ApiResponse;
import com.quiz.model.User;
import com.quiz.service.AuthService;
import com.quiz.service.ReportService;
import com.quiz.util.JsonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * High-performance HTTP request router handling both RESTful JSON API endpoints
 * and serving static frontend assets (HTML, CSS, JS) from webapp.
 */
public class HttpRouter implements SimpleHttpServer.RequestHandler {

    private final AuthController authController;
    private final QuizController quizController;
    private final StudentController studentController;
    private final AdminController adminController;
    private final AuthService authService;
    private final ReportService reportService;
    private final String webappPath;

    public HttpRouter(AuthController authController, QuizController quizController,
                      StudentController studentController, AdminController adminController,
                      AuthService authService, ReportService reportService, String webappPath) {
        this.authController = authController;
        this.quizController = quizController;
        this.studentController = studentController;
        this.adminController = adminController;
        this.authService = authService;
        this.reportService = reportService;
        this.webappPath = webappPath;
    }

    @Override
    public void handle(String method, String path, String query, Map<String, String> headers, byte[] bodyBytes, OutputStream out) throws Exception {
        try {
            if (path.startsWith("/api/")) {
                handleApiRoute(method, path, query, headers, bodyBytes, out);
            } else {
                handleStaticAsset(path, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(out, 500, "Internal Server Error", ApiResponse.error("Internal Server Error: " + e.getMessage()));
        }
    }

    private void handleApiRoute(String method, String path, String query, Map<String, String> headers, byte[] bodyBytes, OutputStream out) throws Exception {
        String token = extractToken(headers);
        User currentUser = authService.getUserBySession(token);
        Map<String, String> queryParams = parseQueryParams(query);
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        Map<String, Object> bodyMap = parseSimpleJson(body);

        // ---------------- AUTH & CANDIDATE ROUTES ----------------
        if ("/api/auth/login".equals(path) && "POST".equals(method)) {
            String email = (String) bodyMap.get("email");
            String password = (String) bodyMap.get("password");
            String clientIp = headers.getOrDefault("x-forwarded-for", headers.getOrDefault("host", "127.0.0.1"));
            String userAgent = headers.getOrDefault("user-agent", "Mozilla/5.0 WebBrowser");
            ApiResponse<?> res = authController.login(email, password, clientIp, userAgent);
            sendJsonResponse(out, res.isSuccess() ? 200 : 401, res.isSuccess() ? "OK" : "Unauthorized", res);
            return;
        }

        if ("/api/auth/register".equals(path) && "POST".equals(method)) {
            ApiResponse<?> res = authController.registerCandidate(bodyMap);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if ("/api/auth/me".equals(path) && "GET".equals(method)) {
            ApiResponse<?> res = authController.getCurrentUser(token);
            sendJsonResponse(out, res.isSuccess() ? 200 : 401, res.isSuccess() ? "OK" : "Unauthorized", res);
            return;
        }

        if ("/api/auth/profile".equals(path) && "GET".equals(method)) {
            ApiResponse<?> res = authController.getCandidateProfile(token);
            sendJsonResponse(out, res.isSuccess() ? 200 : 401, res.isSuccess() ? "OK" : "Unauthorized", res);
            return;
        }

        if ("/api/auth/profile".equals(path) && "POST".equals(method)) {
            ApiResponse<?> res = authController.updateProfile(token, bodyMap);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if ("/api/auth/logout".equals(path) && "POST".equals(method)) {
            ApiResponse<?> res = authController.logout(token);
            sendJsonResponse(out, 200, "OK", res);
            return;
        }

        // ---------------- PUBLIC / BROWSE QUIZ ROUTES ----------------
        if ("/api/quizzes".equals(path) && "GET".equals(method)) {
            ApiResponse<?> res = quizController.getAllQuizzes();
            sendJsonResponse(out, 200, "OK", res);
            return;
        }

        if ("/api/quizzes/search".equals(path) && "GET".equals(method)) {
            String q = queryParams.getOrDefault("q", "");
            ApiResponse<?> res = quizController.searchQuizzes(q);
            sendJsonResponse(out, 200, "OK", res);
            return;
        }

        if (path.matches("^/api/quizzes/\\d+$") && "GET".equals(method)) {
            int quizId = Integer.parseInt(path.substring("/api/quizzes/".length()));
            ApiResponse<?> res = quizController.getQuizById(quizId);
            sendJsonResponse(out, res.isSuccess() ? 200 : 404, res.isSuccess() ? "OK" : "Not Found", res);
            return;
        }

        // ---------------- STUDENT ROUTES ----------------
        if (path.matches("^/api/student/quiz/\\d+/start$") && "POST".equals(method)) {
            int quizId = Integer.parseInt(path.split("/")[4]);
            ApiResponse<?> res = studentController.startQuiz(currentUser, quizId, token);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if (path.matches("^/api/student/quiz/\\d+/timer$") && "GET".equals(method)) {
            int durationMinutes = Integer.parseInt(queryParams.getOrDefault("duration", "10"));
            ApiResponse<?> res = studentController.getRemainingTimer(token, durationMinutes);
            sendJsonResponse(out, 200, "OK", res);
            return;
        }

        if (path.matches("^/api/student/quiz/\\d+/submit$") && "POST".equals(method)) {
            int quizId = Integer.parseInt(path.split("/")[4]);
            Map<Integer, String> answers = parseAnswersMap(bodyMap.get("answers"));
            ApiResponse<?> res = studentController.submitQuiz(currentUser, quizId, answers, token);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if ("/api/student/attempts".equals(path) && "GET".equals(method)) {
            ApiResponse<?> res = studentController.getMyAttempts(currentUser);
            sendJsonResponse(out, res.isSuccess() ? 200 : 401, res.isSuccess() ? "OK" : "Unauthorized", res);
            return;
        }

        if (path.matches("^/api/student/results/\\d+$") && "GET".equals(method)) {
            int attemptId = Integer.parseInt(path.substring("/api/student/results/".length()));
            ApiResponse<?> res = studentController.getResultDetails(attemptId);
            sendJsonResponse(out, res.isSuccess() ? 200 : 404, res.isSuccess() ? "OK" : "Not Found", res);
            return;
        }

        if ("/api/leaderboard".equals(path) && "GET".equals(method)) {
            int quizId = Integer.parseInt(queryParams.getOrDefault("quizId", "0"));
            ApiResponse<?> res = quizId > 0 
                    ? studentController.getQuizLeaderboard(quizId, 25) 
                    : studentController.getLeaderboard(25);
            sendJsonResponse(out, 200, "OK", res);
            return;
        }

        // ---------------- ADMIN ROUTES ----------------
        if ("/api/admin/quizzes".equals(path) && "POST".equals(method)) {
            String title = (String) bodyMap.get("title");
            String desc = (String) bodyMap.get("description");
            int duration = parseInt(bodyMap.get("duration"), 10);
            int totalMarks = parseInt(bodyMap.get("totalMarks"), 50);
            int questionCount = parseInt(bodyMap.get("questionCount"), 10);
            ApiResponse<?> res = adminController.createQuiz(currentUser, title, desc, duration, totalMarks, questionCount);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if (path.matches("^/api/admin/quizzes/\\d+$") && "PUT".equals(method)) {
            int id = Integer.parseInt(path.substring("/api/admin/quizzes/".length()));
            String title = (String) bodyMap.get("title");
            String desc = (String) bodyMap.get("description");
            int duration = parseInt(bodyMap.get("duration"), 10);
            int totalMarks = parseInt(bodyMap.get("totalMarks"), 50);
            int questionCount = parseInt(bodyMap.get("questionCount"), 0);
            ApiResponse<?> res = adminController.updateQuiz(currentUser, id, title, desc, duration, totalMarks);
            if (questionCount > 0) {
                adminController.generateBulkQuestions(currentUser, id, questionCount);
            }
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if (path.matches("^/api/admin/quizzes/\\d+$") && "DELETE".equals(method)) {
            int id = Integer.parseInt(path.substring("/api/admin/quizzes/".length()));
            ApiResponse<?> res = adminController.deleteQuiz(currentUser, id);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if (path.matches("^/api/admin/quizzes/\\d+/generate-questions$") && "POST".equals(method)) {
            int quizId = Integer.parseInt(path.split("/")[4]);
            int count = parseInt(bodyMap.get("count"), 50);
            ApiResponse<?> res = adminController.generateBulkQuestions(currentUser, quizId, count);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if ("/api/admin/questions".equals(path) && "POST".equals(method)) {
            int quizId = parseInt(bodyMap.get("quizId"), 0);
            String text = (String) bodyMap.get("questionText");
            String optA = (String) bodyMap.get("optionA");
            String optB = (String) bodyMap.get("optionB");
            String optC = (String) bodyMap.get("optionC");
            String optD = (String) bodyMap.get("optionD");
            String correct = (String) bodyMap.get("correctAnswer");
            ApiResponse<?> res = adminController.addQuestion(currentUser, quizId, text, optA, optB, optC, optD, correct);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if (path.matches("^/api/admin/questions/\\d+$") && "PUT".equals(method)) {
            int questionId = Integer.parseInt(path.substring("/api/admin/questions/".length()));
            String text = (String) bodyMap.get("questionText");
            String optA = (String) bodyMap.get("optionA");
            String optB = (String) bodyMap.get("optionB");
            String optC = (String) bodyMap.get("optionC");
            String optD = (String) bodyMap.get("optionD");
            String correct = (String) bodyMap.get("correctAnswer");
            ApiResponse<?> res = adminController.updateQuestion(currentUser, questionId, text, optA, optB, optC, optD, correct);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if (path.matches("^/api/admin/questions/\\d+$") && "DELETE".equals(method)) {
            int questionId = Integer.parseInt(path.substring("/api/admin/questions/".length()));
            ApiResponse<?> res = adminController.deleteQuestion(currentUser, questionId);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if (("/api/admin/students".equals(path) || "/api/admin/candidates".equals(path)) && "GET".equals(method)) {
            String q = queryParams.get("q");
            ApiResponse<?> res = adminController.getStudents(currentUser, q);
            sendJsonResponse(out, res.isSuccess() ? 200 : 403, res.isSuccess() ? "OK" : "Forbidden", res);
            return;
        }

        if (path.matches("^/api/admin/candidates/\\d+/logins$") && "GET".equals(method)) {
            int candidateId = Integer.parseInt(path.split("/")[4]);
            ApiResponse<?> res = adminController.getCandidateLogins(currentUser, candidateId);
            sendJsonResponse(out, res.isSuccess() ? 200 : 403, res.isSuccess() ? "OK" : "Forbidden", res);
            return;
        }

        if ("/api/admin/attempts".equals(path) && "GET".equals(method)) {
            ApiResponse<?> res = adminController.getAllAttempts(currentUser);
            sendJsonResponse(out, res.isSuccess() ? 200 : 403, res.isSuccess() ? "OK" : "Forbidden", res);
            return;
        }

        if ("/api/admin/stats".equals(path) && "GET".equals(method)) {
            ApiResponse<?> res = adminController.getStatistics(currentUser);
            sendJsonResponse(out, res.isSuccess() ? 200 : 403, res.isSuccess() ? "OK" : "Forbidden", res);
            return;
        }

        if (path.matches("^/api/admin/export/quiz/\\d+$") && "POST".equals(method)) {
            int quizId = Integer.parseInt(path.split("/")[5]);
            ApiResponse<?> res = adminController.exportQuizReport(currentUser, quizId);
            sendJsonResponse(out, res.isSuccess() ? 200 : 400, res.isSuccess() ? "OK" : "Bad Request", res);
            return;
        }

        if ("/api/admin/reports".equals(path) && "GET".equals(method)) {
            ApiResponse<?> res = adminController.listReports(currentUser);
            sendJsonResponse(out, res.isSuccess() ? 200 : 403, res.isSuccess() ? "OK" : "Forbidden", res);
            return;
        }

        // ---------------- SYSTEM & DATABASE DIAGNOSTIC ROUTES ----------------
        if ("/api/system/db-status".equals(path) && "GET".equals(method)) {
            Map<String, Object> dbInfo = new HashMap<>();
            boolean isFallback = com.quiz.dao.DatabaseConnection.isUsingEmbeddedFallback();
            dbInfo.put("connected", !isFallback);
            dbInfo.put("database", "online_quiz_db");
            dbInfo.put("url", com.quiz.dao.DatabaseConnection.getDbUrl());
            dbInfo.put("user", com.quiz.dao.DatabaseConnection.getDbUser());
            dbInfo.put("mode", isFallback ? "EMBEDDED_FALLBACK" : "MYSQL_CONNECTED");
            dbInfo.put("statusMessage", isFallback 
                    ? "In-Memory Embedded Persistence Active (MySQL service not responding on port 3306)" 
                    : "Connected to MySQL Database: online_quiz_db");
            sendJsonResponse(out, 200, "OK", ApiResponse.success(dbInfo));
            return;
        }

        if ("/api/system/reconnect-db".equals(path) && "POST".equals(method)) {
            boolean connected = com.quiz.dao.DatabaseConnection.reconnect();
            Map<String, Object> res = new HashMap<>();
            res.put("connected", connected);
            res.put("message", connected 
                    ? "Successfully connected to MySQL database 'online_quiz_db'!" 
                    : "MySQL server still offline or unreachable on localhost:3306");
            sendJsonResponse(out, 200, "OK", ApiResponse.success(res));
            return;
        }

        // Unmatched API endpoint
        sendJsonResponse(out, 404, "Not Found", ApiResponse.error("API endpoint not found: " + path));
    }

    private void handleStaticAsset(String rawPath, OutputStream out) throws IOException {
        String path = (rawPath == null || rawPath.equals("/")) ? "/index.html" : rawPath;
        File file = new File(webappPath, path.replace("/", File.separator));

        if (!file.exists() || file.isDirectory()) {
            file = new File(webappPath, "index.html");
        }

        if (!file.exists()) {
            String notFound = "<h1>404 Not Found</h1><p>The requested file does not exist.</p>";
            SimpleHttpServer.sendResponse(out, 404, "Not Found", "text/html; charset=UTF-8", notFound.getBytes(StandardCharsets.UTF_8));
            return;
        }

        String mimeType = getMimeType(file.getName());
        byte[] bytes;
        try (InputStream is = new FileInputStream(file)) {
            bytes = is.readAllBytes();
        }

        SimpleHttpServer.sendResponse(out, 200, "OK", mimeType, bytes);
    }

    private String getMimeType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".txt")) return "text/plain; charset=UTF-8";
        return "application/octet-stream";
    }

    private void sendJsonResponse(OutputStream out, int statusCode, String statusText, Object data) throws IOException {
        String json = JsonUtil.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        SimpleHttpServer.sendResponse(out, statusCode, statusText, "application/json; charset=UTF-8", bytes);
    }

    private String extractToken(Map<String, String> headers) {
        String authHeader = headers.get("authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        String cookieHeader = headers.get("cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String c : cookies) {
                String[] pair = c.trim().split("=");
                if (pair.length == 2 && "quiz_token".equalsIgnoreCase(pair[0])) {
                    return pair[1].trim();
                }
            }
        }
        return null;
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                try {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), 
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private Map<String, Object> parseSimpleJson(String json) {
        Map<String, Object> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        String s = json.trim();
        if (s.startsWith("{") && s.endsWith("}")) {
            s = s.substring(1, s.length() - 1).trim();
        }

        int i = 0;
        int len = s.length();
        while (i < len) {
            int keyStart = s.indexOf('"', i);
            if (keyStart == -1) break;
            int keyEnd = s.indexOf('"', keyStart + 1);
            if (keyEnd == -1) break;
            String key = s.substring(keyStart + 1, keyEnd);

            int colon = s.indexOf(':', keyEnd);
            if (colon == -1) break;
            i = colon + 1;
            while (i < len && Character.isWhitespace(s.charAt(i))) i++;
            if (i >= len) break;

            char firstChar = s.charAt(i);
            if (firstChar == '"') {
                int valEnd = findNextUnescapedQuote(s, i + 1);
                String val = s.substring(i + 1, valEnd);
                map.put(key, unescapeJson(val));
                i = valEnd + 1;
            } else if (firstChar == '{') {
                int endObj = findClosingBracket(s, i, '{', '}');
                String sub = s.substring(i, endObj + 1);
                map.put(key, parseSimpleJson(sub));
                i = endObj + 1;
            } else {
                int comma = s.indexOf(',', i);
                int endVal = comma == -1 ? len : comma;
                String valStr = s.substring(i, endVal).trim();
                if ("true".equalsIgnoreCase(valStr)) map.put(key, Boolean.TRUE);
                else if ("false".equalsIgnoreCase(valStr)) map.put(key, Boolean.FALSE);
                else if ("null".equalsIgnoreCase(valStr)) map.put(key, null);
                else {
                    try {
                        if (valStr.contains(".")) {
                            map.put(key, Double.parseDouble(valStr));
                        } else {
                            map.put(key, Integer.parseInt(valStr));
                        }
                    } catch (NumberFormatException e) {
                        map.put(key, valStr);
                    }
                }
                i = endVal;
            }

            int nextComma = s.indexOf(',', i);
            if (nextComma != -1 && nextComma < s.length()) {
                i = nextComma + 1;
            } else {
                break;
            }
        }
        return map;
    }

    private int findNextUnescapedQuote(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '"' && s.charAt(i - 1) != '\\') {
                return i;
            }
        }
        return s.length() - 1;
    }

    private int findClosingBracket(String s, int start, char open, char close) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == open) depth++;
            else if (s.charAt(i) == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return s.length() - 1;
    }

    private String unescapeJson(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private Map<Integer, String> parseAnswersMap(Object obj) {
        Map<Integer, String> map = new HashMap<>();
        if (obj instanceof Map) {
            Map<?, ?> raw = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> entry : raw.entrySet()) {
                try {
                    int qId = Integer.parseInt(entry.getKey().toString());
                    String ans = entry.getValue() != null ? entry.getValue().toString() : null;
                    map.put(qId, ans);
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private int parseInt(Object obj, int defaultValue) {
        if (obj == null) return defaultValue;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(obj.toString().trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
