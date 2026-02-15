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
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public final class FlowsHandler implements HttpHandler {

    private final PacketQueryDao dao;

    public FlowsHandler(PacketQueryDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange ex) {

        try {
            URI uri = ex.getRequestURI();
            Map<String, String> params = parseQuery(uri.getQuery());

            int seconds =
                    params.containsKey("seconds")
                            ? Integer.parseInt(params.get("seconds"))
                            : 60;

            Instant since = Instant.now().minusSeconds(seconds);

            List<Map<String, Object>> flows = dao.activeFlows(since);

            for (Map<String, Object> row : flows) {

                Integer proto = (Integer) row.get("protocol");
                row.put("protocol", PacketSemantics.protocolName(proto));

                row.put("ep1", decodeIpPort((String) row.get("ep1")));
                row.put("ep2", decodeIpPort((String) row.get("ep2")));
            }



            byte[] json = Json.write(flows).getBytes(StandardCharsets.UTF_8);

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
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    private static String decodeIpPort(String hexPort) throws Exception {

        String[] parts = hexPort.split(":");
        byte[] ipBytes = HexFormat.of().parseHex(parts[0]);

        var addr = java.net.InetAddress.getByAddress(ipBytes);
        String ip = addr.getHostAddress();

        if (ip.contains(":")) {
            ip = "[" + ip + "]";
        }

        return ip + ":" + parts[1];
    }

}
