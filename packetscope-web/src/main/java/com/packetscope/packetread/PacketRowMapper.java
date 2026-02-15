package com.packetscope.packetread;

import com.packetscope.model.PacketReadModel;
import com.packetscope.semantic.PacketSemantics;

import java.sql.SQLException;
import java.time.Instant;
import java.sql.Timestamp;
import java.net.InetAddress;
import java.sql.ResultSet;

/**
 * Maps raw SQL ResultSet rows into the PacketReadModel DTO.
 * Centralizes the transformation logic for the Web API.
 */
public final class PacketRowMapper {

    private PacketRowMapper() {}

    public static PacketReadModel map(ResultSet rs) throws Exception {

        Timestamp ts = rs.getTimestamp("captured_at");
        Instant capturedAt = (ts != null) ? ts.toInstant() : Instant.now();

        int protoCode = rs.getInt("protocol");
        int dirCode = rs.getInt("direction");

        Integer srcPort = getNullableInt(rs, "source_port");
        Integer dstPort = getNullableInt(rs, "destination_port");

        return new PacketReadModel(
                rs.getLong("packet_id"),
                capturedAt,
                rs.getInt("ip_version"),
                decodeIp(rs.getBytes("source_ip")),
                decodeIp(rs.getBytes("destination_ip")),
                protoCode,
                PacketSemantics.protocolName(protoCode),
                srcPort,
                dstPort,
                rs.getInt("packet_size"),
                rs.getString("interface_name"),
                dirCode,
                PacketSemantics.directionName(dirCode)
        );
    }

    private static String decodeIp(byte[] raw) {
        if (raw == null || raw.length == 0) return "0.0.0.0";

        try {
            return InetAddress.getByAddress(raw).getHostAddress();
        } catch (Exception e) {
            return "INVALID_IP";
        }
    }

    /**
     * Handles the JDBC quirk where getInt returns 0 for NULL values.
     */
    private static Integer getNullableInt(ResultSet rs, String col) throws SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : val;
    }
}
