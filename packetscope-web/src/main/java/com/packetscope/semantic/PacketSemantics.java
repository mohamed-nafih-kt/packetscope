package com.packetscope.semantic;

public final class PacketSemantics {

    public static String protocolName(int proto) {
        return switch (proto) {
            case 6 -> "TCP";
            case 17 -> "UDP";
            case 1 -> "ICMP";
            case 58 -> "ICMPv6";
            default -> "UNKNOWN";
        };
    }

    public static String directionName(int dir) {
        return switch (dir) {
            case 1 -> "OUTBOUND";
            case 2 -> "INBOUND";
            default -> "UNKNOWN";
        };
    }

    private PacketSemantics() {}
}
