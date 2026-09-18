package com.bancolombia.challenge.account.dto;

import com.bancolombia.challenge.account.enums.AccountStatus;
import com.bancolombia.challenge.account.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponseDTO(
        String accountNumber,
        AccountType accountType,
        BigDecimal balance,
        AccountStatus status,
        String customerId,
        LocalDateTime createdAt
) {
}
