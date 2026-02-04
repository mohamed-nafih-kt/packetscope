package com.packetscope.api;

import com.packetscope.packetread.dao.PacketQueryDao;
import com.packetscope.packetread.model.PacketReadModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
public class PacketController {

    private final PacketQueryDao dao;

    public PacketController(PacketQueryDao dao) {
        this.dao = dao;
    }

    @GetMapping("/packets")
    public List<PacketReadModel> getPackets(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        return dao.fetchPacketsAfter(from, to, limit);
    }
}