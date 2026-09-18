package com.bancolombia.challenge.account.dto;

import com.bancolombia.challenge.account.enums.TransactionStatus;
import com.bancolombia.challenge.account.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
        Long transactionId,
        String accountNumber,
        TransactionType transactionType,
        BigDecimal amount,
        BigDecimal balanceAfterTransaction,
        TransactionStatus status,
        LocalDateTime timestamp
) {
}
