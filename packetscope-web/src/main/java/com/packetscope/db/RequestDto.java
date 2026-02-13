package com.packetscope.db;

import java.util.Map;

public class RequestDto {
    public String url;
    public String method;
    public String body;
    public Map<String, String> headers;
}