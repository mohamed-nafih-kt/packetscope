package com.packetscope.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public final class Json {

    private static final ObjectMapper MAPPER =
            new ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static String write(Object o) throws Exception {
        return MAPPER.writeValueAsString(o);
    }
}
