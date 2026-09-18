package com.bancolombia.challenge.account.exception;

public class AccountNotFoundException  extends RuntimeException{
    public AccountNotFoundException(String message) {
        super(message);
    }
}
