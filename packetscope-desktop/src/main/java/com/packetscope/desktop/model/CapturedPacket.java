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

    public enum TransportProtocol {

        ICMP(1),
        TCP(6),
        UDP(17),
        ICMPV6(58),
        UNKNOWN(0);

        private final int number;

        TransportProtocol(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }

        public static TransportProtocol fromNumber(int number) {
            for (TransportProtocol p : values()) {
                if (p.number == number) {
                    return p;
                }
            }
            return UNKNOWN;
        }
    }
    
    public enum PacketDirection {

        UNKNOWN(0),
        OUTBOUND(1),
        INBOUND(2);

        private final int code;

        PacketDirection(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static PacketDirection fromCode(int code) {
            for (PacketDirection d : values()) {
                if (d.code == code) {
                    return d;
                }
            }
            return UNKNOWN;
        }
    }


}
