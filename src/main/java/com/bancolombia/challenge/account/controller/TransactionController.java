package com.bancolombia.challenge.account.controller;

import com.bancolombia.challenge.account.dto.TransactionRequestDTO;
import com.bancolombia.challenge.account.dto.TransactionResponseDTO;
import com.bancolombia.challenge.account.service.ITransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final ITransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> processTransaction(@Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = transactionService.processTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<Page<TransactionResponseDTO>> getTransactions(
            @PathVariable String accountNumber,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt") Pageable pageable){
        return ResponseEntity.ok(transactionService.getTransactionsByAccountNumber(accountNumber, pageable));
    }
}
