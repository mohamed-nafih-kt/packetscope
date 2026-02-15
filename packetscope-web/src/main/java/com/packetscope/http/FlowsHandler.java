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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FlowsHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(FlowsHandler.class.getName());
    private final PacketQueryDao dao;

    public FlowsHandler(PacketQueryDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            URI uri = exchange.getRequestURI();
            Map<String, String> params = parseQuery(uri.getQuery());

            int seconds =
                    params.containsKey("seconds")
                            ? Integer.parseInt(params.get("seconds"))
                            : 60;

            // Constrain range to prevent database over-utilization
            seconds = Math.max(1, Math.min(seconds, 300));
            Instant since = Instant.now().minusSeconds(seconds);

            List<Map<String, Object>> flows = dao.activeFlows(since);

            for (Map<String, Object> row : flows) {
                // Safely map protocol number to name
                Object protoObj = row.get("protocol");
                if (protoObj instanceof Integer proto) {
                    row.put("protocol", PacketSemantics.protocolName(proto));
                }

                // Defensive IP decoding to prevent one bad record from crashing the response
                try {
                    row.put("ep1", decodeIpPort((String) row.get("ep1")));
                    row.put("ep2", decodeIpPort((String) row.get("ep2")));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to decode flow endpoints", e);
                    row.put("ep1", "unknown");
                    row.put("ep2", "unknown");
                }
            }
            
            byte[] json = Json.write(flows).getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling flows request", e);
            sendErrorResponse(exchange);
        }
    }

    private void sendErrorResponse(HttpExchange ex) {
        try {
            ex.sendResponseHeaders(500, -1);
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

    private static String decodeIpPort(String hexPort) throws Exception {
        if (hexPort == null) return "unknown";

        String[] parts = hexPort.split(":");
        byte[] ipBytes = java.util.HexFormat.of().parseHex(parts[0]);

        var addr = java.net.InetAddress.getByAddress(ipBytes);
        String ip = addr.getHostAddress();

        // Wrap IPv6 in brackets for standard notation
        if (ip.contains(":")) {
            ip = "[" + ip + "]";
        }

        return ip + ":" + parts[1];
    }

}
