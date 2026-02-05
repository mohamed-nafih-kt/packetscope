package com.packetscope.http;

import com.packetscope.db.PacketQueryDao;
import com.packetscope.model.PacketReadModel;
import com.packetscope.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PacketsHandler implements HttpHandler {

    private final PacketQueryDao dao;

    public PacketsHandler(PacketQueryDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange ex) {

        try {
            URI uri = ex.getRequestURI();
            Map<String, String> params = parseQuery(uri.getQuery());

            Instant from = Instant.parse(params.get("from"));

            Instant lastCapturedAt =
                    params.containsKey("lastCapturedAt")
                            ? Instant.parse(params.get("lastCapturedAt"))
                            : null;

            Long lastPacketId =
                    params.containsKey("lastPacketId")
                            ? Long.parseLong(params.get("lastPacketId"))
                            : null;

            int limit =
                    params.containsKey("limit")
                            ? Integer.parseInt(params.get("limit"))
                            : 1000;

            List<PacketReadModel> packets =
                    dao.fetchPacketsAfter(from, lastCapturedAt, lastPacketId, limit);

            byte[] json = Json.write(packets).getBytes(StandardCharsets.UTF_8);

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

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> map = new HashMap<>();
        if (q == null) return map;

        for (String part : q.split("&")) {
            String[] kv = part.split("=");
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
