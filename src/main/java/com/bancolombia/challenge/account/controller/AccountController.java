package com.bancolombia.challenge.account.controller;

import com.bancolombia.challenge.account.dto.AccountRequestDTO;
import com.bancolombia.challenge.account.dto.AccountResponseDTO;
import com.bancolombia.challenge.account.service.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private  final IAccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountRequestDTO request){
        AccountResponseDTO response = accountService.createAccount(request);
        return  ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponseDTO response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(response);
    }
}
