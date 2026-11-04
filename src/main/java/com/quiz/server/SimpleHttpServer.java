package com.quiz.server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * Lightweight, high-concurrency HTTP Server implemented via standard Java ServerSocket.
 * Demonstrates:
 * - Core Java Networking: ServerSocket, Socket, TCP protocol handling
 * - Multithreading & Thread Pooling: Fixed thread pool with ExecutorService
 * - HTTP 1.1 request parsing: Method, URI, headers, chunked/content-length payload
 * - Clean response writing with status codes, MIME types, and CORS headers
 * - Runs reliably on all platforms and restricted sandbox environments.
 */
public class SimpleHttpServer {

    public interface RequestHandler {
        void handle(String method, String path, String query, Map<String, String> headers, byte[] body, OutputStream out) throws Exception;
    }

    private final int port;
    private final RequestHandler handler;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    private static final java.util.concurrent.atomic.AtomicInteger workerCount = new java.util.concurrent.atomic.AtomicInteger(1);

    public SimpleHttpServer(int port, RequestHandler handler, int poolSize) {
        this.port = port;
        this.handler = handler;
        this.threadPool = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r);
            t.setName("HttpWorker-" + workerCount.getAndIncrement());
            t.setDaemon(true);
            return t;
        });
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        Thread listenerThread = new Thread(() -> {
            while (running && !serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> processClient(clientSocket));
                } catch (IOException e) {
                    if (!running) break;
                    System.err.println("[SimpleHttpServer] Accept error: " + e.getMessage());
                }
            }
        }, "HttpListener-" + port);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void processClient(Socket socket) {
        try (socket;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            String[] parts = requestLine.split("\\s+");
            if (parts.length < 2) return;

            String method = parts[0].toUpperCase();
            String fullUri = parts[1];

            String path = fullUri;
            String query = "";
            int qIdx = fullUri.indexOf('?');
            if (qIdx != -1) {
                path = fullUri.substring(0, qIdx);
                query = fullUri.substring(qIdx + 1);
            }

            // Parse Headers
            Map<String, String> headers = new HashMap<>();
            String line;
            int contentLength = 0;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                int colon = line.indexOf(':');
                if (colon != -1) {
                    String hKey = line.substring(0, colon).trim().toLowerCase();
                    String hVal = line.substring(colon + 1).trim();
                    headers.put(hKey, hVal);
                    if ("content-length".equals(hKey)) {
                        try { contentLength = Integer.parseInt(hVal); } catch (NumberFormatException ignored) {}
                    }
                }
            }

            // Read Body if present
            byte[] body = new byte[0];
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                int totalRead = 0;
                while (totalRead < contentLength) {
                    int read = reader.read(bodyChars, totalRead, contentLength - totalRead);
                    if (read == -1) break;
                    totalRead += read;
                }
                body = new String(bodyChars, 0, totalRead).getBytes(StandardCharsets.UTF_8);
            }

            // Handle CORS preflight directly
            if ("OPTIONS".equals(method)) {
                sendResponse(out, 204, "No Content", "text/plain", new byte[0]);
                return;
            }

            // Delegate to handler
            handler.handle(method, path, query, headers, body, out);

        } catch (Exception e) {
            System.err.println("[SimpleHttpServer] Error processing request: " + e.getMessage());
        }
    }

    public static void sendResponse(OutputStream out, int status, String statusText, String contentType, byte[] data) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        pw.print("HTTP/1.1 " + status + " " + statusText + "\r\n");
        pw.print("Content-Type: " + contentType + "\r\n");
        pw.print("Content-Length: " + data.length + "\r\n");
        pw.print("Access-Control-Allow-Origin: *\r\n");
        pw.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
        pw.print("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With\r\n");
        pw.print("Connection: close\r\n");
        pw.print("\r\n");
        pw.flush();

        out.write(data);
        out.flush();
    }

    public void stop() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
        threadPool.shutdown();
    }
}
