package com.packetscope.db;

import java.util.Map;

public class TransactionDto {

    public long id;
    public String method;
    public String url;

    public Map request_headers;
    public String request_body;

    public int response_status;
    public Map response_headers;
    public String response_body;

    public String created_at;
}

