package com.bancolombia.challenge.account.dto;

import com.bancolombia.challenge.account.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AccountRequestDTO(
        @NotBlank(message = "Client Id is require")
        String customerId,

        @NotNull(message = "Account type is require")
        AccountType accountType,

        @NotNull(message = "The initial balance can not be Null")
        @PositiveOrZero(message = "The initial balance can not be negative")
        BigDecimal initialBalance
) {
}
