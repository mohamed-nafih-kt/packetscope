package com.packetscope.packetread.dao;

import com.packetscope.packetread.model.PacketReadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PacketQueryDao {

    private final JdbcTemplate jdbcTemplate;
    private final PacketRowMapper rowMapper = new PacketRowMapper();

    public PacketQueryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PacketReadModel> fetchPacketsAfter(
            Instant from,
            Instant lastCapturedAt,
            Long lastPacketId,
            int limit
    ) {
        String baseSql = """
        SELECT
            packet_id,
            captured_at,
            ip_version,
            source_ip,
            destination_ip,
            protocol,
            source_port,
            destination_port,
            packet_size,
            interface_name,
            direction
        FROM packets
        WHERE captured_at >= ?
    """;

        String cursorClause = """
        AND (captured_at > ? OR (captured_at = ? AND packet_id > ?))
    """;

        String orderLimit = """
        ORDER BY captured_at ASC, packet_id ASC
        LIMIT ?
    """;

        if (lastCapturedAt == null || lastPacketId == null) {
            return jdbcTemplate.query(
                    baseSql + orderLimit,
                    rowMapper,
                    from,
                    limit
            );
        }

        return jdbcTemplate.query(
                baseSql + cursorClause + orderLimit,
                rowMapper,
                from,
                lastCapturedAt,
                lastCapturedAt,
                lastPacketId,
                limit
        );
    }

    public List<PacketReadModel> fetchPackets(Instant from, Instant to, int limit) {
        String sql = """
            SELECT
                captured_at,
                ip_version,
                source_ip,
                destination_ip,
                protocol,
                source_port,
                destination_port,
                packet_size,
                interface_name,
                direction
            FROM packets
            WHERE captured_at BETWEEN ? AND ?
            ORDER BY captured_at ASC
            LIMIT ?
        """;

        return jdbcTemplate.query(
                sql,
                rowMapper,
                from,
                to,
                limit
        );
    }

    public Map<Integer, Long> countByProtocol(Instant from, Instant to) {
        String sql = """
        SELECT protocol, COUNT(*) AS cnt
        FROM packets
        WHERE captured_at BETWEEN ? AND ?
        GROUP BY protocol
    """;

        return jdbcTemplate.query(sql, rs -> {
            Map<Integer, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getInt("protocol"), rs.getLong("cnt"));
            }
            return result;
        }, from, to);
    }

    public List<Map<String, Object>> countPerSecondByProtocol(Instant from, Instant to) {
        String sql = """
        SELECT
            DATE_FORMAT(captured_at, '%Y-%m-%d %H:%i:%s') AS bucket,
            protocol,
            COUNT(*) AS cnt
        FROM packets
        WHERE captured_at BETWEEN ? AND ?
        GROUP BY bucket, protocol
        ORDER BY bucket ASC, protocol ASC
    """;

        return jdbcTemplate.queryForList(sql, from, to);
    }

    public List<Map<String, Object>> countPerSecondByProtocolAndDirection(Instant from, Instant to) {
        String sql = """
        SELECT
            DATE_FORMAT(captured_at, '%Y-%m-%d %H:%i:%s') AS bucket,
            protocol,
            direction,
            COUNT(*) AS cnt
        FROM packets
        WHERE captured_at BETWEEN ? AND ?
        GROUP BY bucket, protocol, direction
        ORDER BY bucket ASC, protocol ASC, direction ASC
    """;

        return jdbcTemplate.queryForList(sql, from, to);
    }
}