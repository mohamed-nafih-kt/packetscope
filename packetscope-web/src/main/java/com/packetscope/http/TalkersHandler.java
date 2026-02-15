package com.packetscope.http;

import com.packetscope.db.PacketQueryDao;
import com.packetscope.util.Json;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public final class TalkersHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(TalkersHandler.class.getName());
    private final PacketQueryDao dao;

    public TalkersHandler(PacketQueryDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange ex) {
        try {
            Map<String, String> params = parseQuery(ex.getRequestURI().getQuery());

            int seconds = 60;
            if (params.containsKey("seconds")) {
                try {
                    seconds = Integer.parseInt(params.get("seconds"));
                } catch (NumberFormatException ignored) {}
            }

            // Boundary checks to ensure query performance
            seconds = Math.max(1, Math.min(seconds, 300));
            Instant since = Instant.now().minusSeconds(seconds);

            List<Map<String, Object>> rows = dao.topTalkers(since);

            byte[] json = Json.write(rows).getBytes(StandardCharsets.UTF_8);


            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, json.length);

            try (OutputStream os = ex.getResponseBody()) {
                os.write(json);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating top talkers report", e);
            try {
                ex.sendResponseHeaders(500, -1);
            } catch (Exception ignored) {}
        }
    }

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> map = new HashMap<>();
        if (q == null) return map;

        for (String part : q.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && !kv[0].isEmpty()) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
