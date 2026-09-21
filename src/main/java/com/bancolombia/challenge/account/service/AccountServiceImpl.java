package com.bancolombia.challenge.account.service;

import com.bancolombia.challenge.account.dto.AccountRequestDTO;
import com.bancolombia.challenge.account.dto.AccountResponseDTO;
import com.bancolombia.challenge.account.entity.Account;
import com.bancolombia.challenge.account.enums.AccountStatus;
import com.bancolombia.challenge.account.exception.AccountAlreadyExistsException;
import com.bancolombia.challenge.account.exception.AccountNotFoundException;
import com.bancolombia.challenge.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements IAccountService {
    private final AccountRepository accountRepo;

    @Override
    @Transactional
    public AccountResponseDTO createAccount(AccountRequestDTO request) {
        String generatedAccountNumber = generateAccountNumber();

        if (accountRepo.existsByCustomerIdAndAccountType(request.customerId(), request.accountType())) {
            throw  new AccountAlreadyExistsException("Client already have account of type" + request.accountType());
        }

        Account account = Account.builder()
                .accountNumber(generatedAccountNumber)
                .accountType(request.accountType())
                .balance(request.initialBalance())
                .status(AccountStatus.ACTIVE)
                .customerId(request.customerId())
                .build();
        Account saved = accountRepo.save(account);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "accounts", key = "#accountNumber")
    public AccountResponseDTO getAccountByNumber(String accountNumber) {
        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("account " + accountNumber + "does not exist"));
        return mapToResponse(account);
    }

    private String generateAccountNumber() {
        return String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 10);
    }

    private AccountResponseDTO mapToResponse(Account account) {
        return new AccountResponseDTO(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCustomerId(),
                account.getCreatedAt()
        );
    }
}
