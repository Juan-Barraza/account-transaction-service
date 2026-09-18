package com.bancolombia.challenge.account.service;

import com.bancolombia.challenge.account.dto.TransactionRequestDTO;
import com.bancolombia.challenge.account.dto.TransactionResponseDTO;
import com.bancolombia.challenge.account.entity.Account;
import com.bancolombia.challenge.account.entity.Transaction;
import com.bancolombia.challenge.account.enums.TransactionStatus;
import com.bancolombia.challenge.account.enums.TransactionType;
import com.bancolombia.challenge.account.exception.AccountNotFoundException;
import com.bancolombia.challenge.account.exception.InsufficientBalanceException;
import com.bancolombia.challenge.account.repository.AccountRepository;
import com.bancolombia.challenge.account.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionServiceImp implements ITransactionService{
    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;

    @Override
    @Transactional
    public TransactionResponseDTO processTransaction(TransactionRequestDTO request) {
        Account account = accountRepo.findByAccountNumber(request.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found" + request.accountNumber()));
        BigDecimal newBalance  = calculateNewBalance(account.getBalance(), request.amount(), request.transactionType());
        account.setBalance(newBalance);
        accountRepo.save(account);

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(request.transactionType())
                .amount(request.amount())
                .balanceAfterTransaction(newBalance)
                .description(request.description())
                .status(TransactionStatus.SUCCESS)
                .build();
        Transaction savedTransaction = transactionRepo.save(transaction);

        return mapToResponse(savedTransaction, account.getAccountNumber());
    }

    @Override
    public Page<TransactionResponseDTO> getTransactionsByAccountNumber(String accountNumber, Pageable pageable) {
        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account Not Found " + accountNumber));
        return  transactionRepo.findByAccountId(account.getId(), pageable)
                .map(tx -> mapToResponse(tx, accountNumber));
    }

    private BigDecimal calculateNewBalance(BigDecimal currentBalance, BigDecimal amount, TransactionType type) {
        if (type == TransactionType.DEPOSIT  || type == TransactionType.TRANSFER_IN) {
            return currentBalance.add(amount);
        }

        if (currentBalance.compareTo(amount) < 0) {
            throw  new InsufficientBalanceException("insufficient balance to complete transaction");
        }

        return currentBalance.subtract(amount);
    }




    private TransactionResponseDTO mapToResponse(Transaction transaction, String accountNumber) {
        return new TransactionResponseDTO(
                transaction.getId(),
                accountNumber,
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalanceAfterTransaction(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}
