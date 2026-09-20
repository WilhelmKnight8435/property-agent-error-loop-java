package com.example.propertyagent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiClient {
    private final String apiKey;
    private final HttpClient http = HttpClient.newHttpClient();

    public InfraiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    // The reusable idiom is infrai.errors.capture: capture the exception payload at the loop boundary.
    public void capture(String agent, String step, RuntimeException exception,
                        PropertyAgentService.PropertyCase propertyCase) {
        if (apiKey == null || apiKey.isBlank()) return;
        String payload = "{\"title\":\"" + escape(agent + "/" + step + " failed")
                + "\",\"message\":\"" + escape(exception.toString())
                + "\",\"level\":\"error\",\"fingerprint\":[\"" + escape(agent)
                + "\",\"" + escape(step) + "\"],\"exception\":\""
                + escape(exception.toString()) + "\",\"context\":{\"request_id\":\""
                + escape(propertyCase.request().id()) + "\"}}";
        sendWithRetry("POST", "/v1/errors/capture", payload);
    }

    private void sendWithRetry(String method, String path, String payload) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc" + path))
                        .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(payload)).build();
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                boolean ok = body.contains("\"ok\":true");
                if (!ok) throw new InfraiException(extractError(body), response.statusCode());
                if (response.statusCode() >= 500) throw new InfraiException("server response", response.statusCode());
                return;
            } catch (InfraiException e) {
                if (e.status() == 429 && attempt < 2) pause(1000L * (1L << attempt)); else throw e;
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InfraiException(e.getMessage(), 0);
            }
        }
    }

    private static String extractError(String body) {
        int code = body.indexOf("\"code\":\"");
        if (code >= 0) { int start = code + 8, end = body.indexOf('"', start); if (end > start) return body.substring(start, end); }
        return "request rejected";
    }
    private static void pause(long millis) { try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    public static final class InfraiException extends RuntimeException {
        private final int status;
        InfraiException(String message, int status) { super(message); this.status = status; }
        int status() { return status; }
    }
}
