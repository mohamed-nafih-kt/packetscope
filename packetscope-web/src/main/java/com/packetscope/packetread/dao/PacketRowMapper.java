package com.packetscope.packetread.dao;

import com.packetscope.packetread.model.PacketReadModel;
import org.springframework.jdbc.core.RowMapper;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class PacketRowMapper implements RowMapper<PacketReadModel> {

    @Override
    public PacketReadModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PacketReadModel(
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

    private String decodeIp(byte[] raw) {
        if (raw == null) return null;
        try {
            return InetAddress.getByAddress(raw).getHostAddress();
        } catch (Exception e) {
            return "INVALID_IP";
        }
    }
}