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
                                 UserRepository userRepository,
                                 TransactionRepository transactionRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/execute")
    public ResponseEntity<TransactionResponseDTO> executeTransaction(
            @RequestBody TransactionRequestDTO requestDto) {
        TransactionResponseDTO responseDto = transactionService.executeTransaction(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveTransaction(
            @RequestBody TransactionRequestDTO requestDto,
            Principal principal) {

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TransactionResponseDTO responseDto = transactionService.executeTransaction(requestDto);
        transactionService.saveTransaction(requestDto, responseDto, user);

        return ResponseEntity.ok("Transaction saved successfully!");
    }

    @GetMapping
    public List<Transaction> getUserTransactions(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findByUser(user);
    }
}
