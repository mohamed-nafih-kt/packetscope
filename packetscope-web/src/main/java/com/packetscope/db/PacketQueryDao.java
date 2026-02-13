package com.packetscope.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.packetscope.model.PacketReadModel;
import com.packetscope.packetread.PacketRowMapper;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PacketQueryDao {

    private final Db db;

    public PacketQueryDao(Db db) {
        this.db = db;
    }

    // -------------------------------------------------
    // PACKETS (cursor pagination)
    // -------------------------------------------------

    public List<PacketReadModel> fetchPacketsAfter(
            Instant from,
            Instant lastCapturedAt,
            Long lastPacketId,
            int limit
    ) throws Exception {

        String sql;

        if (lastCapturedAt == null || lastPacketId == null) {
            sql = """
                SELECT *
                FROM packets
                WHERE captured_at >= ?
                ORDER BY captured_at, packet_id
                LIMIT ?
            """;
        } else {
            sql = """
                SELECT *
                FROM packets
                WHERE captured_at >= ?
                AND (captured_at > ? OR (captured_at = ? AND packet_id > ?))
                ORDER BY captured_at, packet_id
                LIMIT ?
            """;
        }

        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            int i = 1;
            ps.setTimestamp(i++, Timestamp.from(from));

            if (lastCapturedAt != null) {
                ps.setTimestamp(i++, Timestamp.from(lastCapturedAt));
                ps.setTimestamp(i++, Timestamp.from(lastCapturedAt));
                ps.setLong(i++, lastPacketId);
            }

            ps.setInt(i, limit);

            ResultSet rs = ps.executeQuery();

            List<PacketReadModel> out = new ArrayList<>();

            while (rs.next()) {
                out.add(PacketRowMapper.map(rs));
            }

            return out;
        }
    }

    // -------------------------------------------------
    // FLOWS
    // -------------------------------------------------

    public List<Map<String, Object>> activeFlows(Instant since) throws Exception {

        String sql = """
            SELECT
              protocol,
              LEAST(CONCAT(HEX(source_ip), ':', source_port),
                    CONCAT(HEX(destination_ip), ':', destination_port)) AS ep1,
              GREATEST(CONCAT(HEX(source_ip), ':', source_port),
                       CONCAT(HEX(destination_ip), ':', destination_port)) AS ep2,
              COUNT(*) AS packet_count,
              SUM(packet_size) AS total_bytes,
              MIN(captured_at) AS first_seen,
              MAX(captured_at) AS last_seen
            FROM packets
            WHERE captured_at >= ?
            GROUP BY protocol, ep1, ep2
            ORDER BY total_bytes DESC
            LIMIT 100
        """;

        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.from(since));

            ResultSet rs = ps.executeQuery();
            return rows(rs);
        }
    }

    // -------------------------------------------------
    // TALKERS
    // -------------------------------------------------

    public List<Map<String, Object>> topTalkers(Instant since) throws Exception {

        String sql = """
            SELECT
              HEX(source_ip) AS ip,
              SUM(packet_size) AS bytes_sent,
              COUNT(*) AS packets
            FROM packets
            WHERE captured_at >= ?
            GROUP BY source_ip
            ORDER BY bytes_sent DESC
            LIMIT 20
        """;

        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.from(since));
            return rows(ps.executeQuery());
        }
    }

    // -------------------------------------------------
    // Helper
    // -------------------------------------------------

    private static List<Map<String, Object>> rows(ResultSet rs) throws Exception {

        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                row.put(md.getColumnLabel(i), rs.getObject(i));
            }
            list.add(row);
        }

        return list;
    }

    public List<Map<String, Object>> countPerSecondByProtocolAndDirection(
            Instant from,
            Instant to
    ) throws Exception {

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

        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.from(from));
            ps.setTimestamp(2, Timestamp.from(to));

            ResultSet rs = ps.executeQuery();
            return rows(rs);
        }
    }

    // -------------------------------------------------
    // Transactions
    // -------------------------------------------------
    public void saveTransaction(
            String method,
            String url,
            Map<String,String> reqHeaders,
            String reqBody,
            int status,
            Map<String,List<String>> resHeaders,
            String resBody
    ) throws SQLException {
        String sql = """
        INSERT INTO transaction_logs
        (method, url, request_headers, request_body,
         response_status, response_headers, response_body)
         VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        ObjectMapper mapper = new ObjectMapper();

        try (Connection conn = db.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, method);
            ps.setString(2, url);
            ps.setString(3, mapper.writeValueAsString(reqHeaders));
            ps.setString(4, reqBody);
            ps.setInt(5, status);
            ps.setString(6, mapper.writeValueAsString(resHeaders));
            ps.setString(7, resBody);

            ps.executeUpdate();
        } catch (JsonProcessingException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    // saved transactions
    public List<RequestDto> findAll() throws SQLException, JsonProcessingException {

        String sql = """
        SELECT id, method, url, request_headers, request_body, created_at
        FROM transaction_logs
        ORDER BY created_at DESC
    """;

        List<RequestDto> list = new ArrayList<>();

        try (Connection conn = db.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ObjectMapper mapper = new ObjectMapper();

            while (rs.next()) {

                RequestDto dto = new RequestDto();

                dto.id = rs.getLong("id");
                dto.method = rs.getString("method");
                dto.url = rs.getString("url");

                String headers = rs.getString("request_headers");
                dto.headers = headers == null ? Map.of() :
                        mapper.readValue(headers, Map.class);

                dto.body = rs.getString("request_body");
                dto.created_at = rs.getTimestamp("created_at").toString();

                list.add(dto);
            }
        }catch (Exception e){
            System.out.println("Failed to find transactions: " + e.getMessage());
        }

        return list;
    }

}
