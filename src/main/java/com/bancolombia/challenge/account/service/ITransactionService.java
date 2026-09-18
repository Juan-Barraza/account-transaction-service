package com.bancolombia.challenge.account.repository;

import com.bancolombia.challenge.account.dto.TransactionRequestDTO;
import com.bancolombia.challenge.account.dto.TransactionResponseDTO;

public interface ITransactionService {
    TransactionResponseDTO processTransaction(TransactionRequestDTO request);
}
