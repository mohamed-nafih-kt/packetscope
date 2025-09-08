package com.mdnafih.PacketScope.service;

import com.mdnafih.PacketScope.model.Transaction;
import com.mdnafih.PacketScope.model.TransactionRequestDTO;
import com.mdnafih.PacketScope.model.TransactionResponseDTO;
import com.mdnafih.PacketScope.model.User;
import com.mdnafih.PacketScope.repository.TransactionRepository;

import jakarta.persistence.Lob;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.restTemplate = new RestTemplate();
    }

    public TransactionResponseDTO executeTransaction(TransactionRequestDTO requestDto, User user) {
        long startTime = System.currentTimeMillis();

        long duration = System.currentTimeMillis() - startTime;

        HttpHeaders headers = new HttpHeaders();

        if(requestDto.getHeaders() != null) {
            for(Map.Entry<String, String> entry: requestDto.getHeaders().entrySet() ) {
                headers.add(entry.getKey(), entry.getValue());
            }
        }

        HttpEntity<String> entity = new HttpEntity<String>(requestDto.getBody(), headers);

        HttpMethod httpMethod = HttpMethod.valueOf(requestDto.getMethod().toUpperCase());

        ResponseEntity<String>  response = restTemplate.exchange(
                requestDto.getUrl(),
                httpMethod,
                entity,
                String.class
        );

        Transaction transaction = new Transaction();
        transaction.setMethod(requestDto.getMethod());
        transaction.setUrl(requestDto.getUrl());
        transaction.setRequestHeaders(requestDto.getHeaders().toString());  // store as JSON/string
        transaction.setRequestBody(requestDto.getBody());
        transaction.setResponseStatus(response.getStatusCodeValue());
        transaction.setResponseHeaders(response.getHeaders().toSingleValueMap().toString());
        transaction.setResponseBody(response.getBody());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDurationMs(duration);
        transaction.setUser(user);

        transactionRepository.save(transaction);

        TransactionResponseDTO responseDto = new TransactionResponseDTO();
        responseDto.setStatus(response.getStatusCodeValue());
        responseDto.setHeaders(response.getHeaders().toSingleValueMap());
        responseDto.setBody(response.getBody());
        responseDto.setDurationMs(duration);
        responseDto.setTimestamp(transaction.getTimestamp().toString());

        return responseDto;

    }



}
