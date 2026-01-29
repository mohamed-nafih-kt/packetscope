package com.packetscope.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Transaction {

    private Long requestId;

    private String method;

    private String url;

    private String requestHeaders;

    private String requestBody;

    private int responseStatus;  // better as int

    private String responseHeaders;

    private String responseBody;

    private LocalDateTime timestamp;

    private Long durationMs;

    private User user;

}
