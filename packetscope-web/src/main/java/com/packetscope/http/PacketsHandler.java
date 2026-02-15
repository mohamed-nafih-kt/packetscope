package com.packetscope.http;

import com.packetscope.db.PacketQueryDao;
import com.packetscope.model.PacketReadModel;
import com.packetscope.semantic.PacketSemantics;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PacketsHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(PacketsHandler.class.getName());
    private final PacketQueryDao dao;

    public PacketsHandler(PacketQueryDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange ex) {
        try {
            URI uri = ex.getRequestURI();
            Map<String, String> params = parseQuery(uri.getQuery());

            // Validate mandatory 'from' timestamp
            Instant from;
            try {
                String fromStr = params.get("from");
                if (fromStr == null) throw new IllegalArgumentException("Missing 'from' parameter");
                from = Instant.parse(fromStr);
            } catch (Exception e) {
                sendError(ex, 400);
                return;
            }

            Instant lastCapturedAt =
                    params.containsKey("lastCapturedAt")
                            ? Instant.parse(params.get("lastCapturedAt"))
                            : null;

            Long lastPacketId =
                    params.containsKey("lastPacketId")
                            ? Long.parseLong(params.get("lastPacketId"))
                            : null;

            int limit = 200;
            if (params.containsKey("limit")) {
                try {
                    limit = Integer.parseInt(params.get("limit"));
                } catch (NumberFormatException ignored) {}
            }
            limit = Math.max(1, Math.min(limit, 500));

            // Fetch data from DAO
            List<PacketReadModel> packets =
                    dao.fetchPacketsAfter(from, lastCapturedAt, lastPacketId, limit);

            byte[] json = Json.write(packets).getBytes(StandardCharsets.UTF_8);

            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, json.length);

            try (OutputStream os = ex.getResponseBody()) {
                os.write(json);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching packets for dashboard", e);
            sendError(ex, 500);
        }
    }

    private void sendError(HttpExchange ex, int code) {
        try {
            ex.sendResponseHeaders(code, -1);
        } catch (Exception ignored) {}
    }

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> map = new HashMap<>();
        if (q == null) return map;

        for (String part : q.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

}
