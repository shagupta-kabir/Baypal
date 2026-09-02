package com.baypal.exception;

// thrown when a send/withdraw would take a wallet's balance below zero
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
