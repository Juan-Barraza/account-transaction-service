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
import com.bancolombia.challenge.account.grpc.TelemetryGrpcClientService;
import com.bancolombia.challenge.telemetry.grpc.TransactionGrpcResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImp implements ITransactionService {
    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;
    private final TelemetryGrpcClientService telemetryGrpcClientService;

    @Override
    @Transactional
    @CacheEvict(value = "accounts", key = "#request.accountNumber()")
    public TransactionResponseDTO processTransaction(TransactionRequestDTO request) {
        Account account = accountRepo.findByAccountNumber(request.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found" + request.accountNumber()));

        if (isDebit(request.transactionType()) && account.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for amount requested");
        }

        String transactionId = UUID.randomUUID().toString();
        TransactionGrpcResponse telemetryResponse = telemetryGrpcClientService.evaluateRiskAndCalculateFee(
                transactionId,
                account.getAccountNumber(),
                request.amount().doubleValue(),
                request.channel(),
                request.paymentProvider().name());

        BigDecimal calculatedFee = BigDecimal.valueOf(telemetryResponse.getCalculatedFee());
        BigDecimal totalAmount = request.amount().add(calculatedFee);
        boolean isHighRisk = telemetryResponse.getIsHighRisk();

        if (isDebit(request.transactionType())
                && account.getBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance to cover the transaction + fee ($" + calculatedFee + ")");
        }

        BigDecimal finalBalance = account.getBalance();
        if (!isHighRisk) {
            finalBalance = calculateNewBalance(account.getBalance(), totalAmount, request.transactionType());
            account.setBalance(finalBalance);
            accountRepo.save(account);
        } else {
            log.warn("High risk transaction detected (TxID: {}). Status set to PENDING and balance preserved.", transactionId);
        }


        log.info("gRPC Response -> TxID: {}, Fee: {}, HighRisk: {}",
                transactionId, calculatedFee, telemetryResponse.getIsHighRisk());

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(request.transactionType())
                .amount(request.amount())
                .balanceAfterTransaction(finalBalance)
                .description(request.description() + " [Fee: $" + calculatedFee + "]")
                .status(telemetryResponse.getIsHighRisk() ? TransactionStatus.PENDING : TransactionStatus.SUCCESS)
                .build();
        Transaction savedTransaction = transactionRepo.save(transaction);

        return mapToResponse(savedTransaction, account.getAccountNumber());
    }

    @Override
    public Page<TransactionResponseDTO> getTransactionsByAccountNumber(String accountNumber, Pageable pageable) {
        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account Not Found " + accountNumber));
        return transactionRepo.findByAccountId(account.getId(), pageable)
                .map(tx -> mapToResponse(tx, accountNumber));
    }

    private BigDecimal calculateNewBalance(BigDecimal currentBalance, BigDecimal amount, TransactionType type) {
        if (type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER_IN) {
            return currentBalance.add(amount);
        }
        return currentBalance.subtract(amount);
    }

    private boolean isDebit(TransactionType type) {
        return type == TransactionType.WITHDRAWAL || type == TransactionType.TRANSFER_OUT;
    }

    private TransactionResponseDTO mapToResponse(Transaction transaction, String accountNumber) {
        return new TransactionResponseDTO(
                transaction.getId(),
                accountNumber,
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalanceAfterTransaction(),
                transaction.getStatus(),
                transaction.getCreatedAt());
    }
}
