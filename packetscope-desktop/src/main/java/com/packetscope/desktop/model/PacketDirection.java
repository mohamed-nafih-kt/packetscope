package com.packetscope.desktop.model;

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
            if (d.code == code) return d;
        }
        return UNKNOWN;
    }
}
