package com.packetscope.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packetscope.db.PacketQueryDao;
import com.packetscope.db.RequestDto;
import com.packetscope.db.TransactionDto;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketHandler  implements HttpHandler {

    private static final Logger LOGGER =  Logger.getLogger(SocketHandler.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Reuse a single HttpClient for connection pooling and resource efficiency
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final PacketQueryDao dao;

    public SocketHandler(PacketQueryDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Handle CORS Pre-flight
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String fullPath = exchange.getRequestURI().getPath();
        byte[] requestBodyBytes = exchange.getRequestBody().readAllBytes();
        String body = new String(requestBodyBytes, StandardCharsets.UTF_8);

        try{
            if (fullPath.endsWith("/execute")) {
                handleExecute(body, exchange);
            } else if (fullPath.endsWith("/history")) {
                handleSavedTransactions(exchange);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Internal error in SocketHandler", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }

    public void handleExecute(String body,  HttpExchange exchange) throws Exception {
        // Deserialize the raw JSON 'body' into RequestDto object
        RequestDto dto  = MAPPER.readValue(body, RequestDto.class);

        // Create  HTTP client to act as the "browser", allowing it to send requests to other servers.
        try {

            // Initialize a Request Builder with the target URL extracted from your DTO;
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(dto.url));

            // Map standard HTTP methods
            switch (dto.method.toUpperCase()) {
                case "GET" -> builder.GET();
                case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(dto.body));
                case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(dto.body));
                case "DELETE" -> builder.DELETE();
                case "PATCH" -> builder.method("PATCH",
                        HttpRequest.BodyPublishers.ofString(dto.body));
                default -> throw new IllegalArgumentException("Unsupported HTTP Method: " + dto.method);
            }

            // Iterate through the headers map and inject them into the request builder.
            if (dto.headers != null) {
                dto.headers.forEach(builder::header);
            }

            // SEND the request and get the response
            HttpResponse<String> upstream = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            dao.saveTransaction(
                    dto.method,
                    dto.url,
                    dto.headers,
                    dto.body,
                    upstream.statusCode(),
                    upstream.headers().map(),
                    upstream.body()
            );

            Map<String, Object> result = Map.of(
                    "status", upstream.statusCode(),
                    "body", upstream.body(),
                    "headers", upstream.headers().map()
            );

            byte[] responseBytes = MAPPER.writeValueAsBytes(result);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);

            // Send the results back to the frontend
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(responseBytes);
            }
        }catch (Exception e) {
            LOGGER.log(Level.WARNING, "Upstream request failed: " + e.getMessage());
            byte[] errorBytes = MAPPER.writeValueAsBytes(Map.of("error", e.getClass().getSimpleName()));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(502, errorBytes.length);
            exchange.getResponseBody().write(errorBytes);
        }
    }

    public void handleSavedTransactions(HttpExchange exchange) throws Exception {
        List<TransactionDto> list = dao.findAllTransactions();
        byte[] bytes = MAPPER.writeValueAsBytes(list);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}