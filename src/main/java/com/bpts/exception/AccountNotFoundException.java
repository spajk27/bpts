package com.bpts.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
    
    public AccountNotFoundException(String accountId, String message) {
        super(String.format("Account %s: %s", accountId, message));
    }
}