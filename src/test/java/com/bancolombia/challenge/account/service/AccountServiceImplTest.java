package com.bancolombia.challenge.account.service;

import com.bancolombia.challenge.account.dto.AccountRequestDTO;
import com.bancolombia.challenge.account.dto.AccountResponseDTO;
import com.bancolombia.challenge.account.entity.Account;
import com.bancolombia.challenge.account.enums.AccountStatus;
import com.bancolombia.challenge.account.enums.AccountType;
import com.bancolombia.challenge.account.exception.AccountAlreadyExistsException;
import com.bancolombia.challenge.account.exception.AccountNotFoundException;
import com.bancolombia.challenge.account.repository.AccountRepository;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;
    private Account account;
    private AccountRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new AccountRequestDTO("1234567890", AccountType.SAVINGS, new BigDecimal("500000.00"));
        account = Account.builder()
                .id(1L)
                .accountNumber("2646610163")
                .customerId("1098765432")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("500000.00"))
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Must create an account successfully with data is correct")
    void createAccount_success() {
        when(accountRepository.existsByCustomerIdAndAccountType(any(), any())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponseDTO response = accountService.createAccount(requestDTO);

        assertThat(response).isNotNull();
        assertThat(response.accountNumber()).isEqualTo("2646610163");
        assertThat(response.balance()).isEqualTo(new BigDecimal("500000.00"));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should be throw AccountAlreadyExistsException if the client already has that account type")
    void createAccount_ThrowsAccountAlreadyExistsException() {
        when(accountRepository.existsByCustomerIdAndAccountType(requestDTO.customerId(), requestDTO.accountType()))
                .thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(requestDTO))
                .isInstanceOf(AccountAlreadyExistsException.class);

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Must return account when accountNumber exist")
    void getAccountByNumber() {
        when(accountRepository.findByAccountNumber("2646610163")).thenReturn(Optional.of(account));

        AccountResponseDTO response = accountService.getAccountByNumber("2646610163");

        assertThat(response).isNotNull();
        assertThat(response.accountNumber()).isEqualTo("2646610163");
    }

    @Test
    @DisplayName("Should be throw AccountNotFoundException if account not exist")
    void getAccountByNumber_NotFound() {
        when(accountRepository.findByAccountNumber("0000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountByNumber("0000000000"))
                .isInstanceOf(AccountNotFoundException.class);
    }

}
