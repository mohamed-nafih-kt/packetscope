package com.packetscope.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packetscope.db.PacketQueryDao;
import com.packetscope.db.RequestDto;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class SocketHandler  implements HttpHandler {
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
        String body = new String(exchange.getRequestBody().readAllBytes());

        try{
            switch(fullPath){
                case "/api/transactions/execute" -> handleExecute(body, exchange);
                case "/api/transactions/save" -> handleSave(body, exchange, dao);
                default -> {
                    exchange.sendResponseHeaders(404, -1);
                }
            }
            }catch (Exception e) {
                System.err.println("Error handling request: " + e.getMessage());
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            }
    }

    public static void handleExecute(String body,  HttpExchange exchange) throws Exception {

        // Initialize the Jackson JSON engine to handle data transformation.
        ObjectMapper mapper = new ObjectMapper();

        // Deserialize the raw JSON 'body' into RequestDto object
        RequestDto dto  = mapper.readValue(body, RequestDto.class);

        // Create  HTTP client to act as the "browser", allowing it to send requests to other servers.
        try (HttpClient client = HttpClient.newHttpClient()){

            // Initialize a Request Builder with the target URL extracted from your DTO;
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(dto.url));

            switch (dto.method) {
                case "GET" -> builder.GET();
                case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(dto.body));
                case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(dto.body));
                case "DELETE" -> builder.DELETE();
                case "PATCH" -> builder.method("PATCH",
                        HttpRequest.BodyPublishers.ofString(dto.body));
            }

            // Iterate through the headers map and inject them into the request builder.
            dto.headers.forEach(builder::header);

            // SEND the request and get the response
            HttpResponse<String> upstream =
                    client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            var result = new java.util.HashMap<String, Object>();

            result.put("status", upstream.statusCode());
            result.put("body", upstream.body());
            result.put("headers", upstream.headers().map());

            String payload = mapper.writeValueAsString(result);

            byte[] bytes = payload.getBytes();

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(upstream.statusCode(), bytes.length);

            // Send the results back to the frontend
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }

        }
    }

    // Inside com.packetscope.http.SocketHandler
    public static void handleSave(String body,  HttpExchange exchange, PacketQueryDao dao) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        RequestDto dto = mapper.readValue(body, RequestDto.class);

        // Persist to MySQL
        dao.saveTransaction(dto);

        // Send a "Success" response back to the frontend
        String payload = "{\"status\": \"saved\", \"url\": \"" + dto.url + "\"}";

        exchange.sendResponseHeaders(200, payload.length());
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(payload.getBytes());
        }
    }
}

/* TEST
https://httpbin.org : This service echoes your request back to you
https://httpbin.org/get : query parms returned in JSON:
| Key  | Value       |
| ---- | ----------- |
| name | packetscope |
| mode | test        |
https://httpbin.org/headers : The response will show your custom header.
X-Debug: PacketScope
https://httpbin.org/post : Response will echo the JSON inside
Content-Type: application/json
{
  "tool": "PacketScope",
  "type": "probe",
  "version": 1
}

 */