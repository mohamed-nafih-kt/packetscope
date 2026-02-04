package com.packetscope.api;

import com.packetscope.packetread.dao.PacketQueryDao;
import com.packetscope.packetread.model.PacketReadModel;
import com.packetscope.semantic.PacketSemantics;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PacketController {

    private final PacketQueryDao dao;

    public PacketController(PacketQueryDao dao) {
        this.dao = dao;
    }

    @GetMapping("/packets")
    public List<PacketReadModel> fetchPackets(
            @RequestParam Instant from,
            @RequestParam(required = false) Instant lastCapturedAt,
            @RequestParam(required = false) Long lastPacketId,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        return dao.fetchPacketsAfter(from, lastCapturedAt, lastPacketId, limit);
    }


    // ------------------------------------------------------------
    // METRICS (AGGREGATIONS)
    // ------------------------------------------------------------

    @GetMapping("/metrics/by-protocol")
    public Map<Integer, Long> countByProtocol(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return dao.countByProtocol(from, to);
    }

    @GetMapping("/metrics/per-second/protocol-direction")
    public List<Map<String, Object>> perSecondByProtocolDirection(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return dao.countPerSecondByProtocolAndDirection(from, to)
                .stream()
                .map(row -> Map.of(
                        "bucket", row.get("bucket"),
                        "protocol", PacketSemantics.protocolName((Integer) row.get("protocol")),
                        "direction", PacketSemantics.directionName((Integer) row.get("direction")),
                        "count", row.get("cnt")
                ))
                .toList();
    }

    // ------------------------------------------------------------
    // TIMELINE (FRONTEND READY)
    // ------------------------------------------------------------

    @GetMapping("/timeline/protocol-direction")
    public List<Map<String, Object>> protocolDirectionTimeline(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {

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

        return buckets.entrySet()
                .stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("bucket", e.getKey());
                    m.putAll(e.getValue());
                    return m;
                })
                .toList();
    }

}