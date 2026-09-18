package com.bancolombia.challenge.account.service;

import com.bancolombia.challenge.account.dto.AccountRequestDTO;
import com.bancolombia.challenge.account.dto.AccountResponseDTO;

public interface IAccountService {
    AccountResponseDTO createAccount(AccountRequestDTO request);
    AccountResponseDTO getAccountByNumber(String accountNumber);
}

