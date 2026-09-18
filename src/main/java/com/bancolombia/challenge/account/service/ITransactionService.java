package com.bancolombia.challenge.account.service;

import com.bancolombia.challenge.account.dto.TransactionRequestDTO;
import com.bancolombia.challenge.account.dto.TransactionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITransactionService {
    TransactionResponseDTO processTransaction(TransactionRequestDTO request);
    Page<TransactionResponseDTO> getTransactionsByAccountNumber(String accountNumber, Pageable pageable);
}
