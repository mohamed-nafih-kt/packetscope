package com.packetscope.semantic;

/**
 * Utility for mapping raw network codes to human-readable semantic names.
 * Follows standard IANA IP protocol numbering.
 */
public final class PacketSemantics {

    public static String protocolName(int proto) {
        return switch (proto) {
            case 1  -> "ICMP";
            case 2  -> "IGMP";
            case 6  -> "TCP";
            case 17 -> "UDP";
            case 41 -> "IPv6-Route";
            case 58 -> "ICMPv6";
            case 89 -> "OSPF";
            default -> "OTHER (" + proto + ")";
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
