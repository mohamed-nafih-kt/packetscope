package com.packetscope.repository;

import com.packetscope.model.Transaction;
import com.packetscope.model.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Transaction> findByUser(User user) {
        // Raw SQL Query
        String sql = "SELECT * FROM transactions WHERE user_id = ?";

        // BeanPropertyRowMapper automatically maps columns to your Transaction class fields
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Transaction.class), user.getUserId());
    }

    public Transaction save(Transaction t) {
        String sql = """
            INSERT INTO transaction (
                method, url, request_headers, request_body,
                response_status, response_headers, response_body,
                timestamp, duration_ms, user_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql,
                t.getMethod(),
                t.getUrl(),
                t.getRequestHeaders(),
                t.getRequestBody(),
                t.getResponseStatus(),
                t.getResponseHeaders(),
                t.getResponseBody(),
                t.getTimestamp(),
                t.getDurationMs(),
                t.getUser().getUserId() // Save the ID, not the object
        );
        return t;
    }
}