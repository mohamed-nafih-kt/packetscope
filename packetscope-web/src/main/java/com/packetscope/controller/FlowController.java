package com.packetscope.controller;

import com.packetscope.packetread.dao.PacketQueryDao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.time.Instant;

@RestController
@RequestMapping("/flows")
public class FlowController {

    private final PacketQueryDao dao;

    public FlowController(PacketQueryDao dao) {
        this.dao = dao;
    }

    @GetMapping("/active")
    public List<Map<String, Object>> activeFlows(
            @RequestParam(defaultValue = "60") int seconds) {

        Instant since = Instant.now().minusSeconds(seconds);
        return dao.activeFlows(since);
    }
}
