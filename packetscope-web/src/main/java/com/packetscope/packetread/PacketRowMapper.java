package com.packetscope.packetread;

import com.packetscope.model.PacketReadModel;

import java.net.InetAddress;
import java.sql.ResultSet;

public final class PacketRowMapper {

    private PacketRowMapper() {}

    public static PacketReadModel map(ResultSet rs) throws Exception {

        return new PacketReadModel(
                rs.getLong("packet_id"),
                rs.getTimestamp("captured_at").toInstant(),
                rs.getInt("ip_version"),
                decodeIp(rs.getBytes("source_ip")),
                decodeIp(rs.getBytes("destination_ip")),
                rs.getInt("protocol"),
                (Integer) rs.getObject("source_port"),
                (Integer) rs.getObject("destination_port"),
                rs.getInt("packet_size"),
                rs.getString("interface_name"),
                rs.getInt("direction")
        );
    }

    private static String decodeIp(byte[] raw) {

        if (raw == null) return null;

        try {
            return InetAddress.getByAddress(raw).getHostAddress();
        } catch (Exception e) {
            return "INVALID_IP";
        }
    }
}
