package com.packetscope.desktop.model;

public enum TransportProtocol {

    UNKNOWN(0),
    ICMP(1),
    TCP(6),
    UDP(17),
    ICMPV6(58);

    private final int number;

    TransportProtocol(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static TransportProtocol fromNumber(int number) {
        for (TransportProtocol p : values()) {
            if (p.number == number) return p;
        }
        return UNKNOWN;
    }
}
