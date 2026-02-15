package com.packetscope.packetread;

import com.packetscope.model.PacketReadModel;
import com.packetscope.semantic.PacketSemantics;

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
                PacketSemantics.protocolName(rs.getInt("protocol")),
                rs.getObject("source_port") != null
                        ? ((Number) rs.getObject("source_port")).intValue()
                        : null,
                rs.getObject("destination_port") != null
                        ? ((Number) rs.getObject("destination_port")).intValue()
                        : null,
                rs.getInt("packet_size"),
                rs.getString("interface_name"),
                rs.getInt("direction"),
                PacketSemantics.directionName(rs.getInt("direction")) // NEW
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
