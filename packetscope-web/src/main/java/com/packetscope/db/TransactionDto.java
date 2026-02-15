package com.packetscope.db;

import java.util.Map;

/**
 * Data Transfer Object for HTTP Transaction logs.
 * Used for storing and replaying captured HTTP requests.
 */
public class TransactionDto {

    public long id;
    public String created_at;

    public String method;
    public String url;
    public Map<String, String> request_headers;
    public String request_body;

    public int response_status;
    public Map<String, Object> response_headers;
    public String response_body;

}

