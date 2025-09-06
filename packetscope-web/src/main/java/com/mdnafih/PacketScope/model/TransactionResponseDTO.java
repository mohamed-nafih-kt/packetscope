package com.mdnafih.PacketScope.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TransactionResponseDTO {
    private int status;
    private Map<String, String> headers;
    private String body;
    private long durationMs;
    private String timestamp;
}
