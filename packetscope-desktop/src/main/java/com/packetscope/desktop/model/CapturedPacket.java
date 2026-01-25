package com.packetscope.desktop.model;

import java.time.Instant;

public class CapturedPacket {

    public Instant timestamp;

    public String ipVersion; // "IPv4" or "IPv6"
    public String srcIp;
    public String dstIp;

    public String protocol; // TCP / UDP / OTHER
    public Integer srcPort;
    public Integer dstPort;

    @Override
    public String toString() {
        return String.format(
            "[%s] %s %s:%s -> %s:%s",
            ipVersion,
            protocol,
            srcIp,
            srcPort,
            dstIp,
            dstPort
        );
    }
}
