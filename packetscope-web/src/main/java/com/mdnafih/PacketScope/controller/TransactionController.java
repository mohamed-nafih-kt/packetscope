package com.mdnafih.PacketScope.controller;

import com.mdnafih.PacketScope.model.Transaction;
import com.mdnafih.PacketScope.model.TransactionRequestDTO;
import com.mdnafih.PacketScope.model.TransactionResponseDTO;
import com.mdnafih.PacketScope.model.User;
import com.mdnafih.PacketScope.repository.TransactionRepository;
import com.mdnafih.PacketScope.repository.UserRepository;
import com.mdnafih.PacketScope.service.TransactionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService,
                                 UserRepository userRepository, TransactionRepository transactionRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> executeTransaction(
            @RequestBody TransactionRequestDTO requestDto,
            Principal principal) {

        // 1. Get the logged-in username from Spring Security
        String username = principal.getName();

        // 2. Fetch the User entity from DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 3. Pass request + user to service
        TransactionResponseDTO responseDto = transactionService.executeTransaction(requestDto, user);

        // 4. Return the response DTO as JSON
        return ResponseEntity.ok(responseDto);

    }

    @GetMapping
    public List<Transaction> getUserTransactions(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findByUser(user);
    }
}
