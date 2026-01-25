package com.mdnafih.PacketScope.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private String method;

    private String url;

    @Lob
    private String requestHeaders;

    @Lob
    private String requestBody;

    private int responseStatus;  // better as int

    @Lob
    private String responseHeaders;

    @Lob
    private String responseBody;

    private LocalDateTime timestamp;

    private Long durationMs;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
