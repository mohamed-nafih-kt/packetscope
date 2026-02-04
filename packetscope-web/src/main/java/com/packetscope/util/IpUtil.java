package com.packetscope.util;

public final class IpUtil {

    public static String hexToIp(String hex) {
        if (hex == null || hex.length() % 2 != 0) return "invalid";

        byte[] bytes = new byte[hex.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }

        try {
            return java.net.InetAddress.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            return "invalid";
        }
    }
}
