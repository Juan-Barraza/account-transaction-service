package com.bancolombia.challenge.account.controller;

import com.bancolombia.challenge.account.dto.TransactionRequestDTO;
import com.bancolombia.challenge.account.dto.TransactionResponseDTO;
import com.bancolombia.challenge.account.service.ITransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transactions", description = "Transactions of accounts")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final ITransactionService transactionService;

    @Operation(summary = "Process a transaction", description = "Registers a deposit or withdrawal for an account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Process transaction"),
            @ApiResponse(responseCode = "400", description = "Insufficient balance"),
            @ApiResponse(responseCode = "404", description = "Account Not Found")
    })
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> processTransaction(@Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = transactionService.processTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all transactions", description = "List transactions by account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List Paginated of Transactions"),
            @ApiResponse(responseCode = "404", description = "Account Not Found")
    })
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<Page<TransactionResponseDTO>> getTransactions(
            @PathVariable String accountNumber,
            @org.springdoc.core.annotations.ParameterObject
            @org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt") Pageable pageable){
        return ResponseEntity.ok(transactionService.getTransactionsByAccountNumber(accountNumber, pageable));
    }
}
