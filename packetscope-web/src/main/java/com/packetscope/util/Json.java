package com.packetscope.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String write(Object o) throws Exception {
        return MAPPER.writeValueAsString(o);
    }
}
