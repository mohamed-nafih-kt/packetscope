package com.packetscope.packetread.dao;

import com.packetscope.network.LocalIpService;
import com.packetscope.packetread.model.PacketReadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PacketQueryDao {

    private final PacketRowMapper rowMapper = new PacketRowMapper();
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedTemplate;
    private final LocalIpService localIpService;

    public PacketQueryDao(JdbcTemplate jdbcTemplate,
                          NamedParameterJdbcTemplate namedTemplate,
                          LocalIpService localIpService) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedTemplate = namedTemplate;
        this.localIpService = localIpService;
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

    public List<Map<String, Object>> timelineProtocolDirection(Instant since) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("localIps", localIpService.getLocalIps());
        params.addValue("since", Timestamp.from(since));

        return namedTemplate.queryForList("""
        SELECT
          DATE_FORMAT(captured_at, '%Y-%m-%d %H:%i:%s') AS bucket,
          protocol,
          CASE
            WHEN source_ip IN (:localIps) THEN 2
            WHEN destination_ip IN (:localIps) THEN 1
            ELSE 0
          END AS inferred_direction,
          COUNT(*) AS cnt
        FROM packets
        WHERE captured_at >= :since
        GROUP BY bucket, protocol, inferred_direction
        ORDER BY bucket
        """, params);
    }

    public List<Map<String, Object>> activeFlows(Instant since) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("since", Timestamp.from(since));

        return namedTemplate.queryForList("""
        SELECT
          protocol,

          LEAST(
            CONCAT(HEX(source_ip), ':', source_port),
            CONCAT(HEX(destination_ip), ':', destination_port)
          ) AS ep1,

          GREATEST(
            CONCAT(HEX(source_ip), ':', source_port),
            CONCAT(HEX(destination_ip), ':', destination_port)
          ) AS ep2,

          COUNT(*)                AS packet_count,
          SUM(packet_size)       AS total_bytes,
          MIN(captured_at)       AS first_seen,
          MAX(captured_at)       AS last_seen

        FROM packets
        WHERE captured_at >= :since
        GROUP BY protocol, ep1, ep2
        ORDER BY total_bytes DESC
        LIMIT 100
        """, params);
    }

    public List<Map<String, Object>> topTalkers(Instant since) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("since", Timestamp.from(since));

        return namedTemplate.queryForList("""
        SELECT
          HEX(source_ip) AS ip,
          SUM(packet_size) AS bytes_sent,
          COUNT(*) AS packets
        FROM packets
        WHERE captured_at >= :since
        GROUP BY source_ip
        ORDER BY bytes_sent DESC
        LIMIT 20
        """, params);
    }
}