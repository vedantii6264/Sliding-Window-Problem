import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Main {

    static final int PORT = 8080;
    static final Path PUBLIC_DIR = Paths.get("public");

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(
            new InetSocketAddress(PORT), 0
        );

        server.createContext("/", Main::serveFile);
        server.createContext("/analyze", Main::analyze);

        server.setExecutor(null);
        server.start();

        System.out.println("Student Performance Analyzer is running!");
        System.out.println("Open http://localhost:8080");
    }

    static void serveFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        Path file = PUBLIC_DIR.resolve(path.substring(1)).normalize();

        if (!file.startsWith(PUBLIC_DIR.normalize())
                || !Files.isRegularFile(file)) {
            send(exchange, 404, "text/plain", "File not found");
            return;
        }

        String type = "text/plain";
        if (path.endsWith(".html")) type = "text/html; charset=UTF-8";
        if (path.endsWith(".css")) type = "text/css; charset=UTF-8";
        if (path.endsWith(".js")) type = "application/javascript; charset=UTF-8";

        send(exchange, 200, type, Files.readString(file));
    }

    static void analyze(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            send(exchange, 405, "application/json",
                 "{\"error\":\"Use POST request\"}");
            return;
        }

        try {
            String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
            );

            Map<String, String> params = new HashMap<>();
            for (String part : body.split("&")) {
                String[] pair = part.split("=", 2);
                if (pair.length == 2) {
                    params.put(
                        URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                    );
                }
            }

            String[] tokens = params.get("marks").trim().split("[,\\s]+");
            int k = Integer.parseInt(params.get("k"));

            if (tokens.length == 0 || tokens.length > 10000) {
                throw new IllegalArgumentException("Enter between 1 and 10000 marks.");
            }

            int[] marks = new int[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                marks[i] = Integer.parseInt(tokens[i]);
                if (marks[i] < 0 || marks[i] > 100) {
                    throw new IllegalArgumentException(
                        "Every mark must be between 0 and 100."
                    );
                }
            }

            if (k < 1 || k > marks.length) {
                throw new IllegalArgumentException(
                    "Window size K must be between 1 and the number of tests."
                );
            }

            String result = calculate(marks, k);
            send(exchange, 200, "application/json; charset=UTF-8", result);

        } catch (Exception e) {
            String message = e.getMessage() == null
                ? "Invalid input." : e.getMessage();

            send(exchange, 400, "application/json; charset=UTF-8",
                 "{\"error\":\"" + jsonEscape(message) + "\"}");
        }
    }

    static String calculate(int[] marks, int k) {
        int n = marks.length;
        int sum = 0;

        // Step 1: Calculate the first window sum.
        for (int i = 0; i < k; i++) {
            sum += marks[i];
        }

        int bestSum = sum;
        int bestStart = 0;

        StringBuilder windows = new StringBuilder();
        windows.append("[");

        for (int start = 0; start <= n - k; start++) {
            if (start > 0) {
                // Remove outgoing mark and add incoming mark.
                sum = sum - marks[start - 1] + marks[start + k - 1];
            }

            if (sum > bestSum) {
                bestSum = sum;
                bestStart = start;
            }

            if (start > 0) windows.append(",");

            windows.append("{")
                .append("\"start\":").append(start + 1).append(",")
                .append("\"end\":").append(start + k).append(",")
                .append("\"sum\":").append(sum).append(",")
                .append("\"average\":").append((double) sum / k)
                .append("}");
        }

        windows.append("]");

        return "{"
            + "\"windows\":" + windows + ","
            + "\"bestStart\":" + (bestStart + 1) + ","
            + "\"bestEnd\":" + (bestStart + k) + ","
            + "\"bestSum\":" + bestSum + ","
            + "\"bestAverage\":" + ((double) bestSum / k) + ","
            + "\"k\":" + k + ","
            + "\"n\":" + n
            + "}";
    }

    static String jsonEscape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    static void send(HttpExchange exchange, int status,
                     String contentType, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}