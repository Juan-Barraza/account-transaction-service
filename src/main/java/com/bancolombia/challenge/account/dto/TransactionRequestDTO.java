package com.bancolombia.challenge.account.dto;

import com.bancolombia.challenge.account.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TransactionRequestDTO(
        @NotBlank(message =  "Account number is require")
        String accountNumber,

        @NotNull(message = "Transaction type is require")
        TransactionType transactionType,

        @NotNull(message = "Amount can not be null")
        @PositiveOrZero(message = "Amount must be greater than zero")
        BigDecimal amount,

        String description
) {
}
