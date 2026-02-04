package com.packetscope.controller;

import com.packetscope.packetread.model.PacketReadModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/monitor")
public class MonitorController {

    public String goHome() {return "monitor";}

    @GetMapping("/monitor")
    public List<PacketReadModel> getPackets(
            @RequestParam Instant from,
            @RequestParam(required = false) Instant lastCapturedAt,
            @RequestParam(required = false) Long lastPacketId,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        return dao.fetchPacketsAfter(from, lastCapturedAt, lastPacketId, limit);
    }

    @GetMapping("/monitor/metrics/by-protocol")
    public Map<Integer, Long> packetsByProtocol(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return dao.countByProtocol(from, to);
    }

    @GetMapping("/packets/metrics/per-second-by-protocol")
    public List<Map<String, Object>> packetsPerSecondByProtocol(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return dao.countPerSecondByProtocol(from, to);
    }

    @GetMapping("/packets/metrics/per-second-by-protocol-direction")
    public List<Map<String, Object>> packetsPerSecondByProtocolAndDirection(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return dao.countPerSecondByProtocolAndDirection(from, to);
    }
}
