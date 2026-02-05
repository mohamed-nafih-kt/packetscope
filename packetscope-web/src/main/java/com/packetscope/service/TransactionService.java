//package com.packetscope.service;
//
//import com.packetscope.model.Transaction;
//import com.packetscope.model.TransactionRequestDTO;
//import com.packetscope.model.TransactionResponseDTO;
//import com.packetscope.model.User;
//import com.packetscope.repository.TransactionRepository;
//
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@Service
//public class TransactionService {
//
//    private final TransactionRepository transactionRepository;
//    private final RestTemplate restTemplate;
//
//    public TransactionService(TransactionRepository transactionRepository) {
//        this.transactionRepository = transactionRepository;
//        this.restTemplate = new RestTemplate();
//    }
//
//    public TransactionResponseDTO executeTransaction(TransactionRequestDTO requestDto) {
//        long startTime = System.currentTimeMillis();
//
//        HttpHeaders headers = new HttpHeaders();
//        if (requestDto.getHeaders() != null) {
//            for (Map.Entry<String, String> entry : requestDto.getHeaders().entrySet()) {
//                headers.add(entry.getKey(), entry.getValue());
//            }
//        }
//
//        HttpEntity<String> entity = new HttpEntity<>(requestDto.getBody(), headers);
//        HttpMethod httpMethod = HttpMethod.valueOf(requestDto.getMethod().toUpperCase());
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                requestDto.getUrl(),
//                httpMethod,
//                entity,
//                String.class
//        );
//
//        long duration = System.currentTimeMillis() - startTime;
//
//        TransactionResponseDTO responseDto = new TransactionResponseDTO();
//        responseDto.setStatus(response.getStatusCodeValue());
//        responseDto.setHeaders(response.getHeaders().toSingleValueMap());
//        responseDto.setBody(response.getBody());
//        responseDto.setDurationMs(duration);
//        responseDto.setTimestamp(LocalDateTime.now().toString());
//
//        return responseDto;
//    }
//
//    // Step 2: Save transaction explicitly
//    public Transaction saveTransaction(TransactionRequestDTO requestDto,
//                                       TransactionResponseDTO responseDto,
//                                       User user) {
//        Transaction transaction = new Transaction();
//        transaction.setMethod(requestDto.getMethod());
//        transaction.setUrl(requestDto.getUrl());
//        transaction.setRequestHeaders(requestDto.getHeaders().toString());
//        transaction.setRequestBody(requestDto.getBody());
//        transaction.setResponseStatus(responseDto.getStatus());
//        transaction.setResponseHeaders(responseDto.getHeaders().toString());
//        transaction.setResponseBody(responseDto.getBody());
//        transaction.setTimestamp(LocalDateTime.now());
//        transaction.setDurationMs(responseDto.getDurationMs());
//        transaction.setUser(user);
//
//        return transactionRepository.save(transaction);
//    }
//
//
//}
