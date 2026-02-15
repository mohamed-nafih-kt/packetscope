package com.packetscope.http;

import com.packetscope.db.PacketQueryDao;
import com.packetscope.semantic.PacketSemantics;
import com.packetscope.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TimelineProtocolDirectionHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(TimelineProtocolDirectionHandler.class.getName());
    private final PacketQueryDao dao;

    public TimelineProtocolDirectionHandler(PacketQueryDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange ex) {

        try {
            Map<String, String> params = parseQuery(ex.getRequestURI().getQuery());

            // Provide sensible defaults (last 5 minutes) if params are missing
            Instant to = params.containsKey("to") ? Instant.parse(params.get("to")) : Instant.now();
            Instant from = params.containsKey("from") ? Instant.parse(params.get("from")) : to.minus(5, ChronoUnit.MINUTES);

            var rows = dao.countPerSecondByProtocolAndDirection(from, to);

            Map<String, Map<String, Long>> buckets = new LinkedHashMap<>();

            for (var row : rows) {
                String bucket = row.get("bucket").toString();

                Object protoObj = row.get("protocol");
                Object dirObj = row.get("direction");
                Object countObj = row.get("cnt");

                if (protoObj instanceof Integer proto && dirObj instanceof Integer dir) {
                    String protoName = PacketSemantics.protocolName(proto);
                    String dirName = PacketSemantics.directionName(dir);
                    String key = protoName + "_" + dirName;

                    buckets
                            .computeIfAbsent(bucket, k -> new HashMap<>())
                            .merge(key, ((Number) countObj).longValue(), Long::sum);
                }
            }

            List<Map<String, Object>> result =
                    buckets.entrySet()
                            .stream()
                            .map(e -> {
                                Map<String, Object> m = new LinkedHashMap<>();
                                m.put("bucket", e.getKey());
                                m.putAll(e.getValue());
                                return m;
                            })
                            .toList();

            byte[] json = Json.write(result).getBytes(StandardCharsets.UTF_8);

            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, json.length);

            try (OutputStream os = ex.getResponseBody()) {
                os.write(json);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Timeline data generation failed", e);
            sendError(ex);
        }
    }

    private void sendError(HttpExchange ex) {
        try {
            ex.sendResponseHeaders(500, -1);
        } catch (Exception ignored) {}
    }

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> map = new HashMap<>();
        if (q == null) return map;

        for (String part : q.split("&")) {
            String[] kv = part.split("=");
            if (kv.length == 2 && !kv[0].isEmpty()) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
