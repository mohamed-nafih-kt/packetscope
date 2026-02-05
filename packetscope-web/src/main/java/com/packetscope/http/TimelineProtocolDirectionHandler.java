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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TimelineProtocolDirectionHandler implements HttpHandler {

    private final PacketQueryDao dao;

    public TimelineProtocolDirectionHandler(PacketQueryDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange ex) {

        try {
            URI uri = ex.getRequestURI();
            Map<String, String> params = parseQuery(uri.getQuery());

            Instant from = Instant.parse(params.get("from"));
            Instant to = Instant.parse(params.get("to"));

            var rows = dao.countPerSecondByProtocolAndDirection(from, to);

            Map<String, Map<String, Long>> buckets = new LinkedHashMap<>();

            for (var row : rows) {

                String bucket = row.get("bucket").toString();
                String proto = PacketSemantics.protocolName((Integer) row.get("protocol"));
                String dir = PacketSemantics.directionName((Integer) row.get("direction"));

                String key = proto + "_" + dir;

                buckets
                        .computeIfAbsent(bucket, k -> new HashMap<>())
                        .merge(key, ((Number) row.get("cnt")).longValue(), Long::sum);
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
