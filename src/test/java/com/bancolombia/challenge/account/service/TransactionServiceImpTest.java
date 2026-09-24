package com.bancolombia.challenge.account.service;

import com.bancolombia.challenge.account.dto.TransactionRequestDTO;
import com.bancolombia.challenge.account.dto.TransactionResponseDTO;
import com.bancolombia.challenge.account.entity.Account;
import com.bancolombia.challenge.account.entity.Transaction;
import com.bancolombia.challenge.account.enums.AccountStatus;
import com.bancolombia.challenge.account.enums.AccountType;
import com.bancolombia.challenge.account.enums.PaymentProvider;
import com.bancolombia.challenge.account.enums.TransactionStatus;
import com.bancolombia.challenge.account.enums.TransactionType;
import com.bancolombia.challenge.account.exception.AccountNotFoundException;
import com.bancolombia.challenge.account.exception.InsufficientBalanceException;
import com.bancolombia.challenge.account.grpc.TelemetryGrpcClientService;
import com.bancolombia.challenge.account.repository.AccountRepository;
import com.bancolombia.challenge.account.repository.TransactionRepository;
import com.bancolombia.challenge.telemetry.grpc.TransactionGrpcResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImpTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransactionRepository transactionRepo;

    @Mock
    private TelemetryGrpcClientService telemetryGrpcClient;

    @InjectMocks
    private TransactionServiceImp transactionService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .accountNumber("2646610163")
                .balance(new BigDecimal("200000.00"))
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should process a deposit updated balance")
    void processTransaction_Deposit_Success() {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "2646610163",
                TransactionType.DEPOSIT,
                new BigDecimal("50000.00"),
                "WEB",
                PaymentProvider.BANCOLOMBIA,
                "payment"
        );

        TransactionGrpcResponse mockGrpcResponse = TransactionGrpcResponse.newBuilder()
                .setCalculatedFee(0.0)
                .setIsHighRisk(false)
                .build();

        Transaction savedTx = Transaction.builder()
                .id(1L)
                .account(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("50000.00"))
                .balanceAfterTransaction(new BigDecimal("250000.00"))
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepo.findByAccountNumber("2646610163")).thenReturn(Optional.of(account));
        when(telemetryGrpcClient.evaluateRiskAndCalculateFee(anyString(), anyString(), anyDouble(), anyString(), anyString()))
                .thenReturn(mockGrpcResponse);
        when(transactionRepo.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponseDTO response = transactionService.processTransaction(request);

        assertThat(response).isNotNull();
        assertThat(response.balanceAfterTransaction()).isEqualTo(new BigDecimal("250000.00"));
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("250000.00"));
    }

    @Test
    @DisplayName("Must process a withdrawal when there is sufficient balance")
    void processTransaction_Withdrawal_Success() {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "2646610163",
                TransactionType.WITHDRAWAL,
                new BigDecimal("50000.00"),
                "ATM",
                PaymentProvider.BANCOLOMBIA,
                "Retiro"
        );

        TransactionGrpcResponse mockGrpcResponse = TransactionGrpcResponse.newBuilder()
                .setCalculatedFee(0.0)
                .setIsHighRisk(false)
                .build();

        Transaction savedTx = Transaction.builder()
                .id(2L)
                .account(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("50000.00"))
                .balanceAfterTransaction(new BigDecimal("150000.00"))
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepo.findByAccountNumber("2646610163")).thenReturn(Optional.of(account));
        when(telemetryGrpcClient.evaluateRiskAndCalculateFee(anyString(), anyString(), anyDouble(), anyString(), anyString()))
                .thenReturn(mockGrpcResponse);
        when(transactionRepo.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponseDTO response = transactionService.processTransaction(request);

        assertThat(response.balanceAfterTransaction()).isEqualTo(new BigDecimal("150000.00"));
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("150000.00"));
    }

    @Test
    @DisplayName("Must launch InsufficientBalanceException when the balance is less than the requested amount")
    void processTransaction_InsufficientBalance_ThrowsException() {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "2646610163",
                TransactionType.WITHDRAWAL,
                new BigDecimal("300000.00"),
                "WEB",
                PaymentProvider.BANCOLOMBIA,
                "Retiro excedido"
        );

        TransactionGrpcResponse mockGrpcResponse = TransactionGrpcResponse.newBuilder()
                .setCalculatedFee(0.0)
                .setIsHighRisk(false)
                .build();

        when(accountRepo.findByAccountNumber("2646610163")).thenReturn(Optional.of(account));
        when(telemetryGrpcClient.evaluateRiskAndCalculateFee(anyString(), anyString(), anyDouble(), anyString(), anyString()))
                .thenReturn(mockGrpcResponse);

        assertThatThrownBy(() -> transactionService.processTransaction(request))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(transactionRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when account does not exist")
    void processTransaction_AccountNotFound_ThrowsException() {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "0000000000",
                TransactionType.DEPOSIT,
                new BigDecimal("10000.00"),
                "WEB",
                PaymentProvider.BANCOLOMBIA,
                "test"
        );

        when(accountRepo.findByAccountNumber("0000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.processTransaction(request))
                .isInstanceOf(AccountNotFoundException.class);

        verify(telemetryGrpcClient, never()).evaluateRiskAndCalculateFee(any(), any(), anyDouble(), any(), any());
        verify(transactionRepo, never()).save(any());
    }
}