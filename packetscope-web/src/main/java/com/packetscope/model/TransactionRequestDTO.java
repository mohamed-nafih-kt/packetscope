package com.mdnafih.PacketScope.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TransactionRequestDTO {

    private String method;
    private String url;
    private Map<String, String> headers;
    private String body;
}
