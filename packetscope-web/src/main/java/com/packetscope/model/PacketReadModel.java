package com.packetscope.model;

import java.time.Instant;

public record PacketReadModel(
        long packetId,
        Instant capturedAt,
        int ipVersion,
        String sourceIp,
        String destinationIp,
        int protocol,
        String protocolName,
        Integer sourcePort,
        Integer destinationPort,
        int packetSize,
        String interfaceName,
        int direction,
        String directionName
) {}
