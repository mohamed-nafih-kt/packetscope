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

public final class TalkersHandler implements HttpHandler {

    private final PacketQueryDao dao;

    public TalkersHandler(PacketQueryDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange ex) {

        try {
            int seconds = 60;

            URI uri = ex.getRequestURI();
            String q = uri.getQuery();

            if (q != null && q.startsWith("seconds=")) {
                seconds = Integer.parseInt(q.substring(8));
            }

            List<Map<String, Object>> rows =
                    dao.topTalkers(Instant.now().minusSeconds(seconds));

            byte[] json = Json.write(rows).getBytes(StandardCharsets.UTF_8);

            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, json.length);

            try (OutputStream os = ex.getResponseBody()) {
                os.write(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                ex.sendResponseHeaders(500, -1);
            } catch (Exception ignored) {}
        }
    }
}
